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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.qute.parser.NodeBase;
import com.redhat.qute.parser.template.sections.TemplatePath;

/**
 * Abstract registry that stores and manages {@link ParameterUsages} instances,
 * grouped by a key.
 *
 * <p>
 * The key identifies the called entity and varies by subclass:
 * <ul>
 * <li>for user tags — the key is the user tag name (e.g. {@code "myTag"})</li>
 * <li>for include sections — the key is the included template id (e.g.
 * {@code "myTemplate"})</li>
 * </ul>
 *
 * <p>
 * For each key, a {@link ParameterUsages} instance accumulates the parameters
 * passed at every call site across all templates. This data is then consumed by
 * language features such as type inference, hover, completion, and find
 * references.
 * </p>
 *
 * <p>
 * Usages come from two sources:
 * <ul>
 * <li>the template parsing cycle — managed via {@link #updateUsages}</li>
 * <li>extensions — managed via {@link #addUsage} and {@link #removeUsage}</li>
 * </ul>
 * </p>
 *
 * @param <T> the concrete {@link ParameterUsages} subclass managed by this
 *            registry
 */
public abstract class UsagesRegistry<T extends ParameterUsages> {

	/**
	 * Usages grouped by key.
	 *
	 * <p>
	 * The key is the user tag name for {@link UserTagUsages}, or the included
	 * template id for {@link IncludeUsages}.
	 * </p>
	 */
	private final Map<String, T> usagesByKey = new HashMap<>();

	/**
	 * Updates the usages contributed by a given template during the parsing cycle.
	 *
	 * <p>
	 * For each key in {@code usages}, the corresponding {@link ParameterUsages} is
	 * created if absent and updated with the new parameters. Keys present in
	 * {@code oldKeys} but absent from {@code usages} are treated as removed: their
	 * parameters for this template are cleared.
	 * </p>
	 *
	 * <p>
	 * Extension usages registered via {@link #addUsage} are never affected by this
	 * method.
	 * </p>
	 *
	 * @param templateId the id of the template that was just parsed
	 * @param usages     parameters collected per key (user tag name or included
	 *                   template id) during this parse
	 * @param oldKeys    keys that were known from the previous parse of this
	 *                   template, used to detect removals
	 */
	public void updateUsages(String templateId, Map<String, List<? extends NodeBase<?>>> usages, Set<String> oldKeys) {
		// Register or update parameters for each key seen in this parse
		for (Map.Entry<String, List<? extends NodeBase<?>>> entry : usages.entrySet()) {
			String key = entry.getKey();
			T keyUsages = getOrCreateUsages(key);
			keyUsages.putParameters(templateId, entry.getValue());
		}

		// Clear parameters for keys that were removed since the last parse
		for (String key : oldKeys) {
			T keyUsages = usagesByKey.get(key);
			if (keyUsages != null) {
				keyUsages.putParameters(templateId, List.of());
			}
		}
	}

	/**
	 * Registers or replaces the single node contributed by an extension for a
	 * given calling template and key.
	 *
	 * <p>
	 * Unlike {@link #updateUsages}, which is driven by the template parsing cycle,
	 * this method allows extensions to register individual usages directly — for
	 * example, usages discovered through language injection or other external
	 * sources. Extension usages are stored separately and are never cleared by a
	 * re-parse. If the extension re-registers a node for the same key and template,
	 * the previous instance is simply replaced.
	 * </p>
	 *
	 * @param key        the user tag name or included template id
	 * @param templatePath the template path of the calling template
	 * @param usage      the node to register
	 */
	public void addUsage(String key, TemplatePath templatePath, NodeBase<?> usage) {
		getOrCreateUsages(key).addParameter(templatePath, usage);
	}

	/**
	 * Removes the node previously registered by an extension for a given calling
	 * template and key.
	 *
	 * @param key        the user tag name or included template id
	 * @param templateId the id of the calling template
	 */
	public void removeUsage(String key, TemplatePath templatePath) {
		T usages = usagesByKey.get(key);
		if (usages != null) {
			usages.removeParameter(templatePath);
		}
	}

	/**
	 * Returns the existing {@link ParameterUsages} for the given key, or creates
	 * and registers a new one if absent.
	 *
	 * @param key the user tag name or included template id
	 * @return the usages for the given key, never {@code null}
	 */
	private T getOrCreateUsages(String key) {
		return usagesByKey.computeIfAbsent(key, this::createUsages);
	}

	/**
	 * Returns the {@link ParameterUsages} for the given key, or {@code null} if
	 * none exists.
	 *
	 * @param key the user tag name or included template id
	 * @return the usages, or {@code null}
	 */
	public T getUsages(String key) {
		if (key == null) {
			return null;
		}
		return usagesByKey.get(key);
	}

	/**
	 * Removes the {@link ParameterUsages} for the given key.
	 *
	 * @param key the user tag name or included template id
	 */
	protected void removeUsages(String key) {
		usagesByKey.remove(key);
	}

	/**
	 * Creates a new {@link ParameterUsages} instance for the given key.
	 *
	 * <p>
	 * Subclasses return their specific {@link ParameterUsages} type here, for
	 * example {@link UserTagUsages} or {@link IncludeUsages}.
	 * </p>
	 *
	 * @param key the user tag name or included template id
	 * @return a new {@link ParameterUsages} instance, never {@code null}
	 */
	protected abstract T createUsages(String key);

}