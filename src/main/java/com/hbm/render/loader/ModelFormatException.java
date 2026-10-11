package com.hbm.render.loader;

// 1.12.2's Forge deleted ModelFormatException along with IModelCustom; a same-named stand-in rides along
public class ModelFormatException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ModelFormatException() {
		super();
	}

	public ModelFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModelFormatException(String message) {
		super(message);
	}

	public ModelFormatException(Throwable cause) {
		super(cause);
	}
}
