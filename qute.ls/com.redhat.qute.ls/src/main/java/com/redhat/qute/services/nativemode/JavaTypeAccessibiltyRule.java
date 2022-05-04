/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.nativemode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.redhat.qute.commons.annotations.RegisterForReflectionAnnotation;
import com.redhat.qute.commons.annotations.TemplateDataAnnotation;

/**
 * Information about access of Java type, fields and methods in native mode.
 * 
 * <p>
 * An instance of this class is the result of the merge of attributes
 * of @TemplateData and @RegisterForReflection attributes for a given Java type.
 * </p>
 * 
 * @author Angelo ZERR
 *
 */
public class JavaTypeAccessibiltyRule {

	public static final JavaTypeAccessibiltyRule ALLOWED_WITHOUT_RESTRICTION = new JavaTypeAccessibiltyRule();
	
	private Set<String> ignore;
	private boolean ignoreSuperClasses;
	private boolean properties;
	private List<Pattern> ignorePatterns;

	private boolean fields;

	private boolean methods;
	private boolean hasTemplateDataAnnotation;
	private boolean hasRegisterForReflectionAnnotation;

	public JavaTypeAccessibiltyRule() {
		this.fields = true;
		this.methods = true;
	}

	public void merge(TemplateDataAnnotation annotation) {
		this.hasTemplateDataAnnotation = true;
		// @TemplateData/ignore
		List<String> ignore = annotation.getIgnore();
		if (ignore != null && !ignore.isEmpty()) {
			if (this.ignore == null) {
				this.ignore = new HashSet<>();
			}
			this.ignore.addAll(ignore);
		}

		// @TemplateData/ignoreSuperClasses
		boolean ignoreSuperClasses = annotation.isIgnoreSuperclasses();
		if (ignoreSuperClasses) {
			this.ignoreSuperClasses = true;
		}

		// @TemplateData/properties
		boolean properties = annotation.isProperties();
		if (properties) {
			this.properties = true;
		}
	}

	public void merge(RegisterForReflectionAnnotation registerForReflectionAnnotation) {
		this.hasRegisterForReflectionAnnotation = true;
		// @RegisterForReflection/fields
		boolean fields = registerForReflectionAnnotation.isFields();
		if (!fields) {
			this.fields = false;
		}

		// @RegisterForReflection/methods
		boolean methods = registerForReflectionAnnotation.isMethods();
		if (!methods) {
			this.methods = false;
		}
	}

	public String getIgnorePattern(String name) {
		if (ignore == null || ignore.isEmpty()) {
			return null;
		}
		// see
		// https://github.com/quarkusio/quarkus/blob/94e0b35eb7f107846cd9579df2e06bfa2796fa29/extensions/qute/deployment/src/main/java/io/quarkus/qute/deployment/TemplateDataBuildItem.java#L46
		if (ignorePatterns == null) {
			ignorePatterns = TemplateDataAnnotation.createIgnorePatterns(ignore);
		}
		return TemplateDataAnnotation.getMatchedIgnorePattern(name, ignorePatterns);
	}

	public boolean isIgnoreSuperClasses() {
		return ignoreSuperClasses;
	}

	/**
	 * Returns true if include only properties: instance fields and methods without
	 * params and false otherwise (comes from @TemplateData/properties).
	 *
	 * @return true if include only properties: instance fields and methods without
	 *         params and false otherwise.
	 */
	public boolean isProperties() {
		return properties;
	}

	public boolean isFields() {
		return fields;
	}

	public boolean isMethods() {
		return methods;
	}

	public boolean hasTemplateDataAnnotation() {
		return hasTemplateDataAnnotation;
	}

	public boolean hasRegisterForReflectionAnnotation() {
		return hasRegisterForReflectionAnnotation;
	}
}
