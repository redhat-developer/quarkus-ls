package com.redhat.qute.project.extensions.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PropertiesFile {

	private final Path file;

	private final Properties properties;

	public PropertiesFile(Path file) {
		this.file = file;
		properties = new Properties();
		if (Files.exists(file)) {
			try (InputStream input = Files.newInputStream(file)) {
				properties.load(input);
			} catch (IOException e) {
				//throw new RuntimeException("Failed to load properties file: " + file, e);
			}
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}
}
