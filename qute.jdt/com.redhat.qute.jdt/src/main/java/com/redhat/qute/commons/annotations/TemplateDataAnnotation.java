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
package com.redhat.qute.commons.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * '@io.quarkus.qute.TemplateData' annotation.
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateDataAnnotation {

	/**
	 * If set to true do not automatically analyze superclasses.
	 */
	private Boolean ignoreSuperclasses;

	/**
	 * The class a value resolver should be generated for. By default, the annotated
	 * type.
	 */
	private String target;

	/**
	 * The regular expressions that are used to match the members that should be
	 * ignored.
	 */
	private List<String> ignore;

	/**
	 * If set to true include only properties: instance fields and methods without
	 * params.
	 */
	private Boolean properties;

	public boolean isIgnoreSuperclasses() {
		return ignoreSuperclasses != null && ignoreSuperclasses.booleanValue();
	}

	public void setIgnoreSuperclasses(Boolean ignoreSuperclasses) {
		this.ignoreSuperclasses = ignoreSuperclasses;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public List<String> getIgnore() {
		return ignore;
	}

	public void setIgnore(List<String> ignore) {
		this.ignore = ignore;
	}

	public static List<Pattern> createIgnorePatterns(Collection<String> ignore) {
		// see
		// https://github.com/quarkusio/quarkus/blob/94e0b35eb7f107846cd9579df2e06bfa2796fa29/extensions/qute/deployment/src/main/java/io/quarkus/qute/deployment/TemplateDataBuildItem.java#L46
		if (ignore == null || ignore.isEmpty()) {
			return Collections.emptyList();
		}
		List<Pattern> ignorePatterns = new ArrayList<>(ignore.size());
		for (String ignoreItem : ignore) {
			try {
				ignorePatterns.add(Pattern.compile(ignoreItem));
			} catch (Exception e) {
				// ignore pattern is not valid
			}
		}
		return ignorePatterns;
	}

	public static String getMatchedIgnorePattern(String name, Collection<Pattern> ignorePatterns) {
		for (Pattern ignorePattern : ignorePatterns) {
			if (ignorePattern.matcher(name).matches()) {
				return ignorePattern.pattern();
			}
		}
		return null;
	}

	public boolean isProperties() {
		return properties != null && properties.booleanValue();
	}

	public void setProperties(Boolean properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("ignoreSuperclasses", this.isIgnoreSuperclasses());
		b.add("target", this.getTarget());
		b.add("ignore", this.getIgnore());
		b.add("properties", this.isProperties());
		return b.toString();
	}
}
