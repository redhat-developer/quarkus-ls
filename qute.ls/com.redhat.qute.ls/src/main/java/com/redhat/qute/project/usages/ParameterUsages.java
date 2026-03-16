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
package com.redhat.qute.project.usages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.qute.parser.NodeBase;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.sections.TemplatePath;

/**
 * Stores all parameters passed at call sites for a single user tag or included
 * template, grouped by the id of the calling template.
 *
 * <p>
 * For example, given the following call sites across two templates:
 *
 * <pre>
 * // in templateA.html
 * {#myTag name="foo" /}
 *
 * // in templateB.html
 * {#myTag name="bar" count=10 /}
 * </pre>
 *
 * A single {@code ParameterUsages} instance for {@code myTag} would hold:
 * <ul>
 * <li>{@code templateA} → [{@code name}]</li>
 * <li>{@code templateB} → [{@code name}, {@code count}]</li>
 * </ul>
 *
 * <p>
 * Usages are stored in two separate maps:
 * <ul>
 * <li>parse usages — collected during the template parsing cycle, cleared and
 * replaced on every re-parse</li>
 * <li>extension usages — registered directly by extensions, never cleared by a
 * re-parse. At most one node per calling template is stored — on
 * re-registration the previous node is simply replaced.</li>
 * </ul>
 *
 * <p>
 * All search methods ({@link #findParameters}, {@link #findTypeProvider},
 * {@link #getCallingTemplateIds}) consult both maps transparently.
 * </p>
 *
 * <p>
 * This data is consumed by language features such as type inference, hover,
 * completion, and find references.
 * </p>
 */
public class ParameterUsages {

	/**
	 * Parameters collected during the template parsing cycle, grouped by the id of
	 * the calling template.
	 *
	 * <p>
	 * Cleared and replaced on every re-parse via {@link #putParameters}.
	 * </p>
	 */
	private final Map<String, List<? extends NodeBase<?>>> parametersByTemplateId = new HashMap<>();

	/**
	 * Single node registered by an extension per calling template.
	 *
	 * <p>
	 * Never cleared by a re-parse — managed exclusively via {@link #addParameter}
	 * and {@link #removeParameter}. Storing one node per template avoids
	 * instance-identity issues when the AST is re-parsed.
	 * </p>
	 */
	private final Map<TemplatePath, NodeBase<?>> extensionParameterByTemplateId = new HashMap<>();

	/**
	 * Registers or replaces the parameters contributed by a given calling template
	 * during the parsing cycle.
	 *
	 * <p>
	 * Passing an empty list clears the contribution of that template, which happens
	 * when the call site is removed from the template. Extension usages are never
	 * affected by this method.
	 * </p>
	 *
	 * @param templateId the id of the calling template
	 * @param parameters the parameters collected at the call site, may be empty
	 */
	public void putParameters(String templateId, List<? extends NodeBase<?>> parameters) {
		parametersByTemplateId.put(templateId, parameters);
	}

	/**
	 * Registers or replaces the single node contributed by an extension for a given
	 * calling template.
	 *
	 * <p>
	 * Extension parameters are stored separately from parse parameters and are
	 * never cleared by a re-parse. If the extension re-registers a node for the
	 * same template (e.g. after a fast re-parse), the previous instance is simply
	 * replaced — no instance-identity comparison is needed.
	 * </p>
	 *
	 * @param templateId the id of the calling template
	 * @param parameter  the node to register
	 */
	public void addParameter(TemplatePath templatePath, NodeBase<?> parameter) {
		extensionParameterByTemplateId.put(templatePath, parameter);
	}

	/**
	 * Removes the node previously registered by an extension for a given calling
	 * template.
	 *
	 * @param templatePath the template path of the calling template
	 */
	public void removeParameter(TemplatePath templatePath) {
		extensionParameterByTemplateId.remove(templatePath);
	}

	/**
	 * Returns the parameters contributed by a given calling template.
	 *
	 * <p>
	 * Returns parameters from both the parsing cycle and extensions.
	 * </p>
	 *
	 * @param templateId the id of the calling template
	 * @return the parameters for that template, or an empty list if none found
	 */
	public List<? extends NodeBase<?>> getParameters(String templateId) {
		List<? extends NodeBase<?>> parseParams = parametersByTemplateId.get(templateId);
		NodeBase<?> extensionParam = extensionParameterByTemplateId.get(templateId);
		if (parseParams == null && extensionParam == null) {
			return Collections.emptyList();
		}
		if (extensionParam == null) {
			return parseParams;
		}
		if (parseParams == null) {
			return Collections.singletonList(extensionParam);
		}
		// Both maps have entries for this templateId — merge them
		List<NodeBase<?>> merged = new ArrayList<>(parseParams);
		merged.add(extensionParam);
		return merged;
	}

	/**
	 * Returns the ids of all templates that contain a call site for this user tag
	 * or included template.
	 *
	 * <p>
	 * Includes template ids from both the parsing cycle and extensions. Intended
	 * for use by find references and call hierarchy features.
	 * </p>
	 *
	 * @return the set of calling template ids, never {@code null}
	 */
	public Set<String> getCallingTemplateIds() {
		return parametersByTemplateId.keySet();
	}

	/**
	 * Returns the ids of all templates that contain a call site for this user tag
	 * or included template.
	 *
	 * <p>
	 * Includes template ids from both the parsing cycle and extensions. Intended
	 * for use by find references and call hierarchy features.
	 * </p>
	 *
	 * @return the set of calling template ids, never {@code null}
	 */
	public Set<TemplatePath> getCallingTemplatePaths() {
		return extensionParameterByTemplateId.keySet();
	}

	/**
	 * Returns all parameters matching the given name, across all calling templates.
	 *
	 * <p>
	 * Searches both parse usages and extension usages. The returned parameters
	 * implement {@link JavaTypeInfoProvider}, allowing hover, completion and
	 * validation to work directly on them.
	 * </p>
	 *
	 * @param name the parameter name to search for
	 * @return all matching parameters, or an empty list if none found
	 */
	public List<NodeBase<?>> findParameters(String name) {
		List<NodeBase<?>> matches = new ArrayList<>();
		for (List<? extends NodeBase<?>> params : parametersByTemplateId.values()) {
			for (NodeBase<?> p : params) {
				if (isMatchParameter(name, p)) {
					matches.add(p);
				}
			}
		}
		for (NodeBase<?> p : extensionParameterByTemplateId.values()) {
			if (isMatchParameter(name, p)) {
				matches.add(p);
			}
		}
		return matches.isEmpty() ? Collections.emptyList() : matches;
	}

	/**
	 * Returns the first parameter matching the given name, for use as a type
	 * provider.
	 *
	 * <p>
	 * Searches parse usages first, then extension usages. Since all call sites are
	 * expected to pass the same type for a given parameter name, the first match is
	 * sufficient for type inference.
	 * </p>
	 *
	 * @param name the parameter name to search for
	 * @return the first matching {@link JavaTypeInfoProvider}, or {@code null} if
	 *         none found
	 */
	public JavaTypeInfoProvider findTypeProvider(String name) {
		for (List<? extends NodeBase<?>> params : parametersByTemplateId.values()) {
			for (NodeBase<?> p : params) {
				if (isMatchParameter(name, p)) {
					return (JavaTypeInfoProvider) p;
				}
			}
		}
		for (NodeBase<?> p : extensionParameterByTemplateId.values()) {
			if (isMatchParameter(name, p)) {
				return (JavaTypeInfoProvider) p;
			}
		}
		return null;
	}

	/**
	 * Returns the full map of parse parameters grouped by calling template id.
	 *
	 * <p>
	 * Intended for use by find references and call hierarchy features that need to
	 * navigate back to the call sites. Does not include extension parameters.
	 * </p>
	 *
	 * @return the parse parameters grouped by template id
	 */
	public Map<String, List<? extends NodeBase<?>>> getParametersByTemplateId() {
		return parametersByTemplateId;
	}

	/**
	 * Checks whether the given node matches the given parameter name.
	 *
	 * <p>
	 * Delegates to {@link #isMatchParameter(String, Parameter)} if the node is a
	 * {@link Parameter}, otherwise returns {@code false}.
	 * </p>
	 *
	 * @param name the parameter name to match
	 * @param node the node to check
	 * @return {@code true} if the node matches, {@code false} otherwise
	 */
	private boolean isMatchParameter(String name, NodeBase<?> node) {
		if (node instanceof Parameter) {
			return isMatchParameter(name, (Parameter) node);
		}
		return false;
	}

	/**
	 * Checks whether the given parameter matches the given name.
	 *
	 * <p>
	 * Subclasses may override this method to add additional matching rules — for
	 * example, {@link com.redhat.qute.project.tags.UserTagUsages} also matches the
	 * implicit {@code it} argument.
	 * </p>
	 *
	 * @param name the parameter name to match
	 * @param p    the parameter to check
	 * @return {@code true} if the parameter name equals {@code name}
	 */
	protected boolean isMatchParameter(String name, Parameter p) {
		return name.equals(p.getName());
	}
}