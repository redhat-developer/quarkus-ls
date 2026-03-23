package com.redhat.qute.jdt.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.lsp4mp.jdt.core.project.IConfigSource;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProject;

import com.redhat.qute.commons.config.PropertyConfig;

public class JDTMicroProfileProjectUtils {

	public static String getProperty(PropertyConfig config, JDTMicroProfileProject mpProject) {
		return mpProject.getProperty(config.getName(), config.getDefaultValue());
	}

	/**
	 * Returns the list of segments matching the wildcard segment of the given
	 * pattern.
	 *
	 * <p>
	 * The pattern must contain exactly one {@code *} wildcard character. For
	 * example, given the pattern {@code "quarkus.web-bundler.bundle.*.qute-tags"}
	 * and the properties:
	 * <ul>
	 * <li>{@code quarkus.web-bundler.bundle.components.qute-tags=true}</li>
	 * <li>{@code quarkus.web-bundler.bundle.bar.qute-tags=true}</li>
	 * </ul>
	 * this method returns {@code ["components", "bar"]}.
	 *
	 * @param pattern the property key pattern containing a single {@code *}
	 *                wildcard
	 * @return the list of segments captured by the wildcard, or an empty list if
	 *         the pattern contains no wildcard or no matching keys are found
	 */
	public static Set<String> getMatchingSegments(String pattern, JDTMicroProfileProject mpProject) {
		int starIndex = pattern.indexOf('*');
		if (starIndex == -1) {
			return Set.of();
		}
		Set<String> matchingSegments = new HashSet<>();
		String prefix = pattern.substring(0, starIndex);
		String suffix = pattern.substring(starIndex + 1);
		for (IConfigSource configSource : mpProject.getConfigSources()) {
			Set<String> keys = configSource.getAllKeys();
			for (String key : keys) {
				if (key.startsWith(prefix) && key.endsWith(suffix)) {
					String segment = key.substring(prefix.length(), key.length() - suffix.length());
					matchingSegments.add(segment);
				}
			}
		}
		return matchingSegments;
	}

}
