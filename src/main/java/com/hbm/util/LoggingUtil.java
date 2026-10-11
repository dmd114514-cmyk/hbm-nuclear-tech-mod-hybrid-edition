package com.hbm.util;

import org.apache.logging.log4j.Level;

import com.hbm.main.MainRegistry;

/** The unified log sink, so MainRegistry.logger isn't written everywhere */
public final class LoggingUtil {

	private LoggingUtil() { }

	public static void log(Level level, String msg, Object... args) {
		MainRegistry.logger.log(level, msg, args);
	}

	public static void info(String msg, Object... args) {
		MainRegistry.logger.info(msg, args);
	}

	public static void warn(String msg, Object... args) {
		MainRegistry.logger.warn(msg, args);
	}

	public static void error(String msg, Object... args) {
		MainRegistry.logger.error(msg, args);
	}
}
