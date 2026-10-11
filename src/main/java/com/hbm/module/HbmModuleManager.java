package com.hbm.module;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.hbm.util.LoggingUtil;

import api.hbm.module.IHbmModule;
import api.hbm.module.ModulePhase;
import api.hbm.module.InternalModule;
import net.minecraftforge.common.config.Configuration;

/**
 * Module discovery, toggles and phase driving.
 *
 * Three ways in:
 * · implementations declared under `META-INF/services/api.hbm.module.IHbmModule` in an external jar (`ServiceLoader`);
 * · built-in classes tagged {@link InternalModule} (reflection scan over `com.hbm.addon`, so the core never imports a module class);
 * · when both collide on one ID the first one wins and the loser is **explicitly disabled and logged** —
 *   silently dropping one makes content vanish into thin air, far harder to debug than an error.
 *
 * Missing deps, toggled off, dependency cycles — all three are "disabled + a log line saying why", never silent degradation.
 */
public final class HbmModuleManager {

	/** Modules sorted by dependency and confirmed to be enabled */
	private static final List<IHbmModule> active = new ArrayList();

	private HbmModuleManager() { }

	public static List<IHbmModule> getActive() {
		return active;
	}

	/** Call once in preInit, before content gets built */
	public static void load(File configDir) {

		Map<String, IHbmModule> found = discover();

		Configuration config = new Configuration(new File(configDir, "modules.cfg"));
		config.load();

		// First pass only reads the toggles: knowing which ones were switched off by hand is what lets dependents get an accurate reason
		Map<String, IHbmModule> wanted = new LinkedHashMap();
		List<String> turnedOff = new ArrayList();

		for(Map.Entry<String, IHbmModule> e : found.entrySet()) {
			IHbmModule mod = e.getValue();
			boolean enabled = config.get(Configuration.CATEGORY_GENERAL, "modules." + e.getKey() + ".enabled",
					mod.enabledByDefault(), "Enable or disable the whole module").getBoolean();

			if(enabled)
				wanted.put(e.getKey(), mod);
			else
				turnedOff.add(e.getKey());
		}

		active.addAll(resolve(wanted, turnedOff));

		config.save();

		LoggingUtil.info("[hbm] {} modules ready: {}", active.size(), ids(active));

		if(!turnedOff.isEmpty())
			LoggingUtil.info("[hbm] modules disabled by config: {}", String.join(", ", turnedOff));
	}

	/** Picks out "missing dep / dep toggled off / cycle" one by one with reasons; the rest come back in dependency order */
	private static List<IHbmModule> resolve(Map<String, IHbmModule> wanted, List<String> turnedOff) {

		List<IHbmModule> ordered = new ArrayList();
		Map<String, IHbmModule> pending = new LinkedHashMap(wanted);

		while(!pending.isEmpty()) {

			boolean progressed = false;

			for(Iterator<Map.Entry<String, IHbmModule>> it = pending.entrySet().iterator(); it.hasNext(); ) {

				Map.Entry<String, IHbmModule> entry = it.next();
				IHbmModule mod = entry.getValue();
				String blockedBy = null;

				for(String dep : mod.getDependencies()) {

					if(!wanted.containsKey(dep)) {
						// The dependency never showed up at all — different reason from being toggled off, report separately; usually it just wasn't installed
						blockedBy = turnedOff.contains(dep) ? "Depended-on module disabled: " + dep : "Depended-on module not found: " + dep;
						break;
					}

					if(containsId(ordered, dep) == false) {
						blockedBy = "dependency not in place yet " + dep; // can't call this a failure, just skipped for this round
						break;
					}
				}

				if(blockedBy == null) {
					ordered.add(mod);
					it.remove();
					progressed = true;
				} else if(!blockedBy.startsWith("dependency not in place yet")) {
					LoggingUtil.warn("[hbm] module {} disabled — {}", entry.getKey(), blockedBy);
					turnedOff.add(entry.getKey());
					it.remove();
					progressed = true;
				}
			}

			if(!progressed) {
				// A whole round where nothing becomes ready means the remainder are waiting on each other: a cycle — disable all and name names
				for(String id : pending.keySet())
					LoggingUtil.warn("[hbm] module {} disabled — dependency cycle, involving: {}", id, String.join(", ", pending.keySet()));

				turnedOff.addAll(pending.keySet());
				break;
			}
		}

		return ordered;
	}

	private static Map<String, IHbmModule> discover() {

		Map<String, IHbmModule> found = new LinkedHashMap();

		for(IHbmModule mod : ServiceLoader.load(IHbmModule.class))
			register(found, mod, "service declaration");

		for(Class<?> clazz : scanInternalClasses()) {

			if(!clazz.isAnnotationPresent(InternalModule.class))
				continue;

			if(!IHbmModule.class.isAssignableFrom(clazz)) {
				LoggingUtil.warn("[hbm] {} is marked @InternalModule but doesn't implement IHbmModule; ignored", clazz.getName());
				continue;
			}

			try {
				register(found, (IHbmModule) clazz.getDeclaredConstructor().newInstance(), "built-in marker");
			} catch(Exception ex) {
				LoggingUtil.warn("[hbm] module {} failed to instantiate; ignored: {}", clazz.getName(), ex);
			}
		}

		return found;
	}

	private static void register(Map<String, IHbmModule> found, IHbmModule mod, String how) {

		String id = mod.getId() == null ? "" : mod.getId().toLowerCase(java.util.Locale.ROOT);

		if(id.isEmpty()) {
			LoggingUtil.warn("[hbm] {} has an empty module ID ({}); ignored", how, mod.getClass().getName());
			return;
		}

		if(found.containsKey(id)) {
			LoggingUtil.warn("[hbm] module ID {} collision: {} vs {}; keeping the former",
					id, found.get(id).getClass().getName(), mod.getClass().getName());
			return;
		}

		found.put(id, mod);
	}

	/**
	 * Scans every class under `com.hbm.addon`. Both the directory form (dev) and the jar form (release) have to work;
	 * the latter can't be assumed to follow file paths, or no built-in module will ever be found once packaged.
	 */
	private static List<Class<?>> scanInternalClasses() {

		List<Class<?>> out = new ArrayList();
		Set<String> seen = new HashSet();
		String path = "com/hbm/addon";
		ClassLoader loader = HbmModuleManager.class.getClassLoader();

		try {
			Enumeration<URL> roots = loader.getResources(path);

			while(roots.hasMoreElements()) {
				URL url = roots.nextElement();

				if("file".equals(url.getProtocol())) {
					walkDirectory(new File(url.toURI()), "com.hbm.addon", loader, out, seen);
				} else if("jar".equals(url.getProtocol())) {
					JarURLConnection conn = (JarURLConnection) url.openConnection();
					conn.setUseCaches(false);

					try (JarFile jar = conn.getJarFile()) {
						Enumeration<JarEntry> entries = jar.entries();

						while(entries.hasMoreElements()) {
							String name = entries.nextElement().getName();

							if(name.startsWith(path) && name.endsWith(".class") && !name.contains("$"))
								loadClass(out, seen, loader, name.substring(0, name.length() - 6).replace('/', '.'));
						}
					}
				}
			}
		} catch(IOException | java.net.URISyntaxException ex) {
			LoggingUtil.warn("[hbm] built-in module scan failed; only service-declaration discovery remains: {}", ex);
		}

		return out;
	}

	private static void walkDirectory(File dir, String packageName, ClassLoader loader, List<Class<?>> out, Set<String> seen) {

		File[] children = dir.listFiles();
		if(children == null)
			return;

		for(File child : children) {
			if(child.isDirectory()) {
				walkDirectory(child, packageName + "." + child.getName(), loader, out, seen);
			} else if(child.getName().endsWith(".class") && !child.getName().contains("$")) {
				loadClass(out, seen, loader, packageName + "." + child.getName().substring(0, child.getName().length() - 6));
			}
		}
	}

	private static void loadClass(List<Class<?>> out, Set<String> seen, ClassLoader loader, String name) {
		if(!seen.add(name)) // the same class can be swept up by two classpath roots; without dedup it gets mistaken for a name-colliding module
			return;
		try {
			out.add(Class.forName(name, false, loader)); // false: don't trigger static init yet; being found isn't the same as being used
		} catch(Throwable ex) {
			// a class that won't load isn't an anomaly here — might be something another mod parked on the same path
		}
	}

	private static boolean containsId(List<IHbmModule> list, String id) {
		for(IHbmModule mod : list) {
			if(id.equalsIgnoreCase(mod.getId()))
				return true;
		}
		return false;
	}

	private static String ids(List<IHbmModule> list) {
		List<String> out = new ArrayList();
		for(IHbmModule mod : list) out.add(mod.getId());
		return out.isEmpty() ? "(none)" : String.join(", ", out);
	}

	/* * Phase driving: order is decided by MainRegistry and the registrars together; this only does the roll call + failure isolation */

	public static void preInit() {
		for(IHbmModule mod : active) run(mod, ModulePhase.PRE_INIT, mod::preInit);
	}

	public static void init() {
		for(IHbmModule mod : active) run(mod, ModulePhase.INIT, mod::init);
	}

	public static void postInit() {
		for(IHbmModule mod : active) run(mod, ModulePhase.POST_INIT, mod::postInit);
	}

	/** Only invoked from the client proxy */
	public static void registerModels() {
		for(IHbmModule mod : active) run(mod, ModulePhase.REGISTER_MODELS, mod::registerModels);
	}

	/**
	 * One module blowing up doesn't halt the whole assembly here: report the module name and keep going.
	 * An optional content pack doesn't get to take other mods' content down with it.
	 */
	private static void run(IHbmModule mod, ModulePhase phase, Runnable body) {

		try {
			body.run();
		} catch(Throwable ex) {
			LoggingUtil.error("[hbm] module {} failed at the {} stage; its content may be incomplete: {}", mod.getId(), phase.label, ex);
		}
	}
}
