package com.redhat.qute.project.extensions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.qute.utils.StringUtils;

public class LanguageInjectionServiceRegistry {

	private static final Logger LOGGER = Logger.getLogger(LanguageInjectionServiceRegistry.class.getName());

	private static final LanguageInjectionServiceRegistry INSTANCE = new LanguageInjectionServiceRegistry();

	public static LanguageInjectionServiceRegistry getInstance() {
		return INSTANCE;
	}

	private final Map<String, LanguageInjectionService> registry;

	private LanguageInjectionServiceRegistry() {
		this.registry = new HashMap<>();
		Iterator<LanguageInjectionService> extensions = ServiceLoader.load(LanguageInjectionService.class).iterator();
		while (extensions.hasNext()) {
			try {
				registerExtension(extensions.next());
			} catch (ServiceConfigurationError e) {
				LOGGER.log(Level.SEVERE, "Error while instantiating extension", e);
			}
		}

	}

	private void registerExtension(LanguageInjectionService extension) {
		registry.put(extension.getLanguageId(), extension);
	}

	public LanguageInjectionService getLanguageService(String languageId) {
		if (StringUtils.isEmpty(languageId)) {
			return null;
		}
		return registry.get(languageId);
	}
}
