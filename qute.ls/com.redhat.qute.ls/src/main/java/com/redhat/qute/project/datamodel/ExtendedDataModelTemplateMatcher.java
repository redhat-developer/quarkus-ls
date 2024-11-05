/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.datamodel;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.commons.datamodel.DataModelTemplateMatcher;
import com.redhat.qute.settings.PathPatternMatcher;

/**
 * Data model template matcher extension.
 */
public class ExtendedDataModelTemplateMatcher extends DataModelTemplateMatcher {

	private Boolean anyMatches;

	private List<PathPatternMatcher> includes;

	public ExtendedDataModelTemplateMatcher(List<String> includes) {
		super(includes);
	}

	public boolean matches(String templateFileUri) {
		if (super.getIncludes() == null) {
			return false;
		}
		if (includes == null && anyMatches == null) {
			// Compute anyMatches flag or path pattern matcher.
			if (super.getIncludes().size() == 1
					&& ("**".equals(super.getIncludes().get(0)) || "**/**".equals(super.getIncludes().get(0)))) {
				// To avoid computing path pattern matcher, if pattern is '**' or '**/**'
				// we consider that it is an any matches.
				anyMatches = true;
			} else {
				includes = super.getIncludes() //
						.stream() //
						.map(p -> new PathPatternMatcher(p)) //
						.collect(Collectors.toList());
			}
		}

		if (anyMatches != null && anyMatches) {
			// Pattern is '**' or '**/**'
			return true;
		}

		// Checks if the template uri matches the includes which defines glob pattern
		URI uri = FileUtils.createUri(templateFileUri);
		for (PathPatternMatcher include : includes) {
			if (include.matches(uri)) {
				return true;
			}
		}
		return false;
	}

}
