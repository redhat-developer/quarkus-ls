/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.settings;

import static com.redhat.qute.commons.FileUtils.isFileURI;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Qute validation settings.
 *
 * @author Angelo ZERR
 *
 */
public class QuteValidationSettings {

	public static enum Severity {
		ignore, error, warning;
	}

	public static final QuteValidationSettings DEFAULT;

	private static final QuteValidationTypeSettings DEFAULT_UNDEFINED_OBJECT;

	private static final QuteValidationTypeSettings DEFAULT_UNDEFINED_NAMESPACE;

	private static final QuteValidationTypeSettings DEFAULT_UNDEFINED_SECTION_TAG;

	static {
		DEFAULT_UNDEFINED_OBJECT = new QuteValidationTypeSettings();
		DEFAULT_UNDEFINED_OBJECT.setSeverity(Severity.warning.name());
		DEFAULT_UNDEFINED_NAMESPACE = new QuteValidationTypeSettings();
		DEFAULT_UNDEFINED_NAMESPACE.setSeverity(Severity.warning.name());
		DEFAULT_UNDEFINED_SECTION_TAG = new QuteValidationTypeSettings();
		DEFAULT_UNDEFINED_SECTION_TAG.setSeverity(Severity.warning.name());
		DEFAULT = new QuteValidationSettings();
		DEFAULT.updateDefault();
	}

	private boolean enabled;

	private QuteValidationTypeSettings undefinedObject;

	private QuteValidationTypeSettings undefinedNamespace;

	private QuteValidationTypeSettings undefinedSectionTag;

	private List<String> excluded;

	private transient boolean updated;

	private transient List<PathPatternMatcher> excludedPatterns;

	public QuteValidationSettings() {
		setEnabled(true);
	}

	/**
	 * Returns true if the validation is enabled and false otherwise.
	 *
	 * @return true if the validation is enabled and false otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set true if the validation is enabled and false otherwise.
	 *
	 * @param enabled true if the validation is enabled and false otherwise.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns the array of properties to ignore for this validation type.
	 *
	 * @return the array of properties to ignore for this validation type.
	 */
	public List<String> getExcluded() {
		return excluded;
	}

	/**
	 * Set the array of properties to ignore for this validation type.
	 *
	 * @param excluded the array of properties to ignore for this validation type.
	 */
	public void setExcluded(List<String> excluded) {
		this.excluded = excluded;
		this.excludedPatterns = null;
	}

	/**
	 * Update each kind of validation settings with default value if not defined.
	 */
	private void updateDefault() {
		if (updated) {
			return;
		}
		setUndefinedObject(undefinedObject != null ? undefinedObject : DEFAULT_UNDEFINED_OBJECT);
		setUndefinedNamespace(undefinedNamespace != null ? undefinedNamespace : DEFAULT_UNDEFINED_NAMESPACE);
		setUndefinedSectionTag(undefinedSectionTag != null ? undefinedSectionTag : DEFAULT_UNDEFINED_SECTION_TAG);
		updated = true;
	}

	/**
	 * Update the the validation settings with the given new validation settings.
	 *
	 * @param newValidation the new validation settings.
	 */
	public void update(QuteValidationSettings newValidation) {
		this.setEnabled(newValidation.isEnabled());
		this.setExcluded(newValidation.getExcluded());
		this.setUndefinedObject(newValidation.getUndefinedObject());
		this.setUndefinedNamespace(newValidation.getUndefinedNamespace());
		this.setUndefinedSectionTag(newValidation.getUndefinedSectionTag());
	}

	/**
	 * Returns the settings for Qute undefined object validation.
	 *
	 * @return the settings for Qute undefined object validation
	 */
	public QuteValidationTypeSettings getUndefinedObject() {
		updateDefault();
		return this.undefinedObject;
	}

	/**
	 * Set the settings for Qute undefined object validation.
	 *
	 * @param undefinedObject the settings for Qute undefined object validation.
	 */
	public void setUndefinedObject(QuteValidationTypeSettings undefinedObject) {
		this.undefinedObject = undefinedObject;
		this.updated = false;
	}

	/**
	 * Returns the settings for Qute undefined namespace validation.
	 *
	 * @return the settings for Qute undefined namespace validation
	 */
	public QuteValidationTypeSettings getUndefinedNamespace() {
		updateDefault();
		return this.undefinedNamespace;
	}

	/**
	 * Set the settings for Qute undefined namespace validation.
	 *
	 * @param undefinedNamespace the settings for Qute undefined namespace
	 *                           validation.
	 */
	public void setUndefinedNamespace(QuteValidationTypeSettings undefinedNamespace) {
		this.undefinedNamespace = undefinedNamespace;
		this.updated = false;
	}

	/**
	 * Returns the settings for Qute undefined SectionTag validation.
	 *
	 * @return the settings for Qute undefined SectionTag validation
	 */
	public QuteValidationTypeSettings getUndefinedSectionTag() {
		updateDefault();
		return this.undefinedSectionTag;
	}

	/**
	 * Set the settings for Qute undefined SectionTag validation.
	 *
	 * @param undefinedSectionTag the settings for Qute undefined SectionTag
	 *                            validation.
	 */
	public void setUndefinedSectionTag(QuteValidationTypeSettings undefinedSectionTag) {
		this.undefinedSectionTag = undefinedSectionTag;
		this.updated = false;
	}

	public boolean canValidate(String templateUri) {
		if (!isEnabled()) {
			return false;
		}
		if (isExcluded(templateUri)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if the given property name must be excluded and false otherwise.
	 *
	 * @param propertyName the property name
	 * @return true if the given property name must be excluded and false otherwise.
	 */
	private boolean isExcluded(String templateUri) {
		if (excluded == null) {
			return false;
		}
		URI uri = URI.create(templateUri);
		if (!isFileURI(uri)) {
			// - ignore jdt://jarentry : when we open an HTML file from a JAR, template uri
			// looks like this
			// jdt://jarentry/templates/tags/error.html?%3Drenarde-todo%2FC%3A%5C%2FUsers%5C%2Fazerr%5C%2F.m2%5C%2Frepository%5C%2Fquarkus-renarde-mock%5C%2Fquarkus-renarde-mock%5C%2F0.0.1-SNAPSHOT%5C%2Fquarkus-renarde-mock-0.0.1-SNAPSHOT.jar%3D%2Fmaven.pomderived%3D%2Ftrue%3D%2F%3D%2Fmaven.pomderived%3D%2Ftrue%3D%2F%3D%2Fmaven.groupId%3D%2Fquarkus-renarde-mock%3D%2F%3D%2Fmaven.artifactId%3D%2Fquarkus-renarde-mock%3D%2F%3D%2Fmaven.version%3D%2F0.0.1-SNAPSHOT%3D%2F%3D%2Fmaven.scope%3D%2Fcompile%3D%2F
			// we ignore the matches
			// - ignore untitled:// URI kind
			return false;
		}
		for (PathPatternMatcher matcher : getExcludedPatterns()) {
			if (matcher.matches(uri)) {
				return true;
			}
		}
		return false;
	}

	public List<PathPatternMatcher> getExcludedPatterns() {
		if (excludedPatterns == null) {
			excludedPatterns = createExcluded(excluded);
		}
		return excludedPatterns;
	}

	public List<String> getMatchingExcluded(String templateUri) {
		if (excluded == null) {
			return Collections.emptyList();
		}
		URI uri = URI.create(templateUri);
		if (!isFileURI(uri)) {
			return Collections.emptyList();
		}
		List<String> matching = new ArrayList<>();
		for (PathPatternMatcher matcher : getExcludedPatterns()) {
			if (matcher.matches(uri)) {
				matching.add(matcher.getPattern());
			}
		}
		return matching;
	}

	private List<PathPatternMatcher> createExcluded(List<String> excluded) {
		if (excluded == null || excluded.isEmpty()) {
			return Collections.emptyList();
		}
		return excluded //
				.stream() //
				.map(pattern -> {
					return new PathPatternMatcher(pattern);
				}) //
				.collect(Collectors.toList());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((excluded == null) ? 0 : excluded.hashCode());
		result = prime * result + ((undefinedObject == null) ? 0 : undefinedObject.hashCode());
		result = prime * result + ((undefinedNamespace == null) ? 0 : undefinedNamespace.hashCode());
		result = prime * result + ((undefinedSectionTag == null) ? 0 : undefinedSectionTag.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuteValidationSettings other = (QuteValidationSettings) obj;
		if (enabled != other.enabled)
			return false;
		if (excluded == null) {
			if (other.excluded != null)
				return false;
		} else if (!excluded.equals(other.excluded))
			return false;
		if (undefinedObject == null) {
			if (!getUndefinedObject().equals(other.getUndefinedObject())) {
				return false;
			}
		} else if (!undefinedObject.equals(other.undefinedObject))
			return false;
		if (undefinedNamespace == null) {
			if (!getUndefinedNamespace().equals(other.getUndefinedNamespace())) {
				return false;
			}
		} else if (!undefinedNamespace.equals(other.undefinedNamespace))
			return false;
		if (undefinedSectionTag == null) {
			if (!getUndefinedSectionTag().equals(other.getUndefinedSectionTag())) {
				return false;
			}
		} else if (!undefinedSectionTag.equals(other.undefinedSectionTag))
			return false;
		return true;
	}

}