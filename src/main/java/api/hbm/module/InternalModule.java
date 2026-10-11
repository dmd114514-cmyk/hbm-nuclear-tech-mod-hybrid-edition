package api.hbm.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sits on a module class to say this is a built-in module shipped with the mod.
 *
 * External jars get found through `ServiceLoader`; built-ins have no META-INF services file,
 * so the manager reflects over the `com.hbm.addon` package looking for this marker - which is why the core imports no module class at all,
 * and adding a module only touches that module's own directory.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InternalModule {
}
