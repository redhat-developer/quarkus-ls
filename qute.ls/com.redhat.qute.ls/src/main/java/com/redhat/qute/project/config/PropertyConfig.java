package com.redhat.qute.project.config;

public class PropertyConfig {

	private final String name;
	private final String defaultValue;

	public PropertyConfig(String name, String defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
