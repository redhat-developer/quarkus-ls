package com.redhat.qute.project.extensions.config;

public class PropertyConfig {

	private final String name;
	private final String description;
	private final String defaultValue;

	public PropertyConfig(String name, String defaultValue) {
		this(name, null, defaultValue);
	}

	public PropertyConfig(String name, String description, String defaultValue) {
		this.name = name;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
