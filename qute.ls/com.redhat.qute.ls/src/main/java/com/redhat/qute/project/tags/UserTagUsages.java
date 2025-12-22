/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.utils.UserTagUtils;

/**
 * Stores all usages of a given user tag across templates.
 *
 * <p>
 * Usages are grouped by template URI.
 * </p>
 */
public class UserTagUsages {

	/**
	 * Parameters used per template URI.
	 */
	private final Map<String, List<Parameter>> usagesByUri = new HashMap<>();

	public UserTagUsages(String tagName) {
	}

	/**
	 * Registers or updates usages for a given template.
	 */
	public void updateUsages(String uri, List<Parameter> parameters) {
		usagesByUri.put(uri, parameters);
	}

	/**
	 * Returns all parameters matching the given name.
	 *
	 * <p>
	 * The returned parameters already implement {@link JavaTypeInfoProvider},
	 * allowing hover, completion and validation to work automatically.
	 * </p>
	 */
	public List<Parameter> getParameters(String name) {
		List<Parameter> matches = new ArrayList<>();
		for (List<Parameter> params : usagesByUri.values()) {
			for (Parameter p : params) {
				if (isMatchParameter(name, p)) {
					matches.add(p);
				}
			}
		}
		return matches.isEmpty() ? Collections.emptyList() : matches;
	}

	/**
	 * Returns a representative type provider for the given parameter.
	 */
	public JavaTypeInfoProvider getTypeProvider(String name) {
		for (List<Parameter> params : usagesByUri.values()) {
			for (Parameter p : params) {
				if (isMatchParameter(name, p)) {
					return p;
				}
			}
		}
		return null;
	}

	private boolean isMatchParameter(String name, Parameter p) {
		return name.equals(p.getName()) || (UserTagUtils.IT_OBJECT_PART_NAME.equals(name) && !p.hasValueAssigned());
	}
}
