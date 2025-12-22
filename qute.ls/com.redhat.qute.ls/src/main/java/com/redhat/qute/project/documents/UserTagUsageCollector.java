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
package com.redhat.qute.project.documents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.CustomSection;
import com.redhat.qute.project.tags.UserTagRegistry;

/**
 * Collects usages of Qute user tags inside a template.
 * <p>
 * A usage corresponds to a call to a user tag section, for example:
 *
 * <pre>
 * {#myTag name="foo" count=10 /}
 * </pre>
 *
 * In this example:
 * <ul>
 * <li>{@code myTag} is the user tag name</li>
 * <li>{@code name} is a parameter called with a String value</li>
 * <li>{@code count} is a parameter called with an Integer value</li>
 * </ul>
 *
 * <p>
 * This collector is executed just after the template parsing phase. It
 * traverses the AST and gathers all user tag calls along with their
 * {@link Parameter} instances.
 * </p>
 *
 * <p>
 * The collected data is then forwarded to the {@link UserTagRegistry}, which is
 * responsible for:
 * <ul>
 * <li>type inference</li>
 * <li>hover support</li>
 * <li>completion</li>
 * <li>find references / call hierarchy</li>
 * </ul>
 * </p>
 */
public class UserTagUsageCollector extends ASTVisitor {

	/** URI of the template currently being visited */
	private final String templateId;

	/** Central registry that stores all user tag usages */
	private final UserTagRegistry userTagRegistry;

	/**
	 * Collected usages for the current template.
	 *
	 * Key : user tag name Value : list of parameters used when calling the tag
	 */
	private final Map<String, List<Parameter>> usages;

	/**
	 * Set of user tag names that were previously used in this template. Used to
	 * detect removed usages when the template is updated.
	 */
	private Set<String> previousTagNames;

	public UserTagUsageCollector(String templateId, UserTagRegistry userTagRegistry) {
		this.templateId = templateId;
		this.userTagRegistry = userTagRegistry;
		this.usages = new HashMap<>();
	}

	@Override
	public boolean visit(Template node) {
		// Keep track of previously known user tag usages for this template
		previousTagNames = new HashSet<>(usages.keySet());

		// Clear current usages before collecting them again
		usages.clear();

		return super.visit(node);
	}

	@Override
	public void endVisit(Template node) {
		// Notify the registry that usages for this template have been updated
		userTagRegistry.updateUsages(templateId, usages, previousTagNames);
		super.endVisit(node);
	}

	@Override
	public boolean visit(CustomSection node) {
		List<Parameter> parameters = node.getParameters();
		if (!parameters.isEmpty()) {
			String userTagName = node.getTag();

			// This tag is still in use
			previousTagNames.remove(userTagName);

			// Register parameters for this user tag
			usages.computeIfAbsent(userTagName, k -> new ArrayList<>()).addAll(parameters);
		}
		return super.visit(node);
	}
}
