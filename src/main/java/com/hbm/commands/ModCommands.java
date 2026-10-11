package com.hbm.commands;

import java.util.ArrayList;
import java.util.List;

import com.hbm.registry.IRegistrar;
import com.hbm.util.LoggingUtil;

import net.minecraft.command.ICommand;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/**
 * Command registration unit: the roster of the mod's own commands lives here; everywhere else (including {@code MainRegistry}) just forwards one call,
 * no more stacking {@code registerServerCommand(new X())} lines.
 * <br><br>
 * The roster is built in {@link #init()} — command objects are stateless, building them early surfaces "class not found"-type problems
 * at startup instead of after the first world load. The actual hookup waits for the server, and in 1.12.2
 * {@link FMLServerStartingEvent} is a **mod-bus** state event: this version's {@code @EventBusSubscriber}
 * only puts classes on the game bus ({@code AutomaticEventSubscriber} hard-wires {@code MinecraftForge.EVENT_BUS.register},
 * no mod-bus option exists), so it never receives it ⇒ the {@code @Mod} class forwards it in.
 * <br><br>
 * The event re-fires on every server start and the command instances are stateless, so build the roster once and reuse it.
 */
public class ModCommands implements IRegistrar {

	private static final List<ICommand> commands = new ArrayList<ICommand>();

	@Override
	public void init() {

		if(commands.isEmpty()) {
			commands.add(new CommandDebugChunkLoad());
			commands.add(new CommandTotalTime());
			commands.add(new CommandRadiation());
		}

		// The roster should answer roll call in the logs: "it compiled" isn't the same as "it made it onto the assembly list"
		LoggingUtil.info("[hbm] command roster built: {} entries: {}", commands.size(), names());
	}

	/** Forwarded from the {@code @Mod} class's serverStarting: hangs the roster's commands onto this server */
	public static void register(FMLServerStartingEvent event) {

		for(ICommand command : commands)
			event.registerServerCommand(command);

		if(!commands.isEmpty())
			LoggingUtil.info("[hbm] {} commands mounted: {}", commands.size(), names());
	}

	private static String names() {

		StringBuilder builder = new StringBuilder();

		for(ICommand command : commands) {
			if(builder.length() > 0)
				builder.append(", ");
			builder.append(command.getName());
		}

		return builder.toString();
	}
}
