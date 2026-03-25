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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.qute.parser.NodeBase;
import com.redhat.qute.parser.template.sections.TemplatePath;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.tags.UserTagUsages;

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
 * passed at every call site across all source documents. This data is then
 * consumed by language features such as type inference, hover, completion, and
 * find references.
 * </p>
 *
 * <p>
 * Usages come from two sources:
 * <ul>
 * <li>the template parsing cycle — managed via {@link #updateUsages}, keyed by
 * template id string</li>
 * <li>extensions — managed via {@link #addUsage} and {@link #removeUsage},
 * keyed by {@link TemplatePath}</li>
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
	 * Reverse index: for each source document, the set of keys it contributes to.
	 *
	 * <p>
	 * Used to efficiently clean up all previous usages of a source document on
	 * re-parse, without iterating over all keys in {@link #usagesByKey}. The
	 * template id is retrieved from the source document via
	 * {@link QuteTextDocument#getTemplateId()}.
	 * </p>
	 */
	private final Map<QuteTextDocument, Set<String>> keysBySource = new IdentityHashMap<>();

	/**
	 * Updates the usages contributed by a given source document during the parsing
	 * cycle.
	 *
	 * <p>
	 * All previous usages contributed by this source are removed first using the
	 * reverse index, then the new usages are registered. This ensures correctness
	 * even when the user types fast and intermediate parses are cancelled — no
	 * stale {@code oldKeys} tracking is needed.
	 * </p>
	 *
	 * <p>
	 * The template id is retrieved from {@code source.getTemplateId()}. Extension
	 * usages registered via {@link #addUsage} are never affected.
	 * </p>
	 *
	 * @param source the document that was just parsed
	 * @param usages parameters collected per key (user tag name or included
	 *               template id) during this parse
	 */
	public synchronized void updateUsages(QuteTextDocument source, Map<String, List<? extends NodeBase<?>>> usages) {
		String templateId = source.getTemplateId();

		// Remove all previous usages contributed by this source using the reverse index
		Set<String> oldKeys = keysBySource.get(source);
		if (oldKeys != null) {
			for (String key : oldKeys) {
				T keyUsages = usagesByKey.get(key);
				if (keyUsages != null) {
					keyUsages.removeParameters(templateId);
					// Remove the entry from usagesByKey if it has no more usages
					if (keyUsages.isEmpty()) {
						usagesByKey.remove(key);
					}
				}
			}
		}

		// Register new usages and rebuild the reverse index for this source
		if (usages.isEmpty()) {
			keysBySource.remove(source);
		} else {
			Set<String> newKeys = new HashSet<>();
			for (Map.Entry<String, List<? extends NodeBase<?>>> entry : usages.entrySet()) {
				String key = entry.getKey();
				T keyUsages = getOrCreateUsages(key);
				keyUsages.putParameters(templateId, entry.getValue());
				newKeys.add(key);
			}
			keysBySource.put(source, newKeys);
		}
	}

	/**
	 * Removes all usages contributed by a given source document.
	 *
	 * <p>
	 * Called when the source document is deleted, to cleanly remove all its
	 * contributions from the registry. Entries in {@link #usagesByKey} that become
	 * empty after removal are also deleted.
	 * </p>
	 *
	 * @param source the document that was deleted
	 */
	public synchronized void removeUsages(QuteTextDocument source) {
		String templateId = source.getTemplateId();
		Set<String> oldKeys = keysBySource.remove(source);
		if (oldKeys != null) {
			for (String key : oldKeys) {
				T keyUsages = usagesByKey.get(key);
				if (keyUsages != null) {
					keyUsages.removeParameters(templateId);
					if (keyUsages.isEmpty()) {
						usagesByKey.remove(key);
					}
				}
			}
		}
	}

	/**
	 * Registers or replaces the single node contributed by an extension for a given
	 * template path and key.
	 *
	 * <p>
	 * Unlike {@link #updateUsages}, which is driven by the template parsing cycle,
	 * this method allows extensions to register individual usages directly — for
	 * example, usages discovered through language injection or other external
	 * sources. Extension usages are stored separately and are never cleared by a
	 * re-parse. If the extension re-registers a node for the same key and path, the
	 * previous instance is simply replaced.
	 * </p>
	 *
	 * @param key          the user tag name or included template id
	 * @param templatePath the path of the calling template
	 * @param usage        the node to register
	 */
	public synchronized void addUsage(String key, TemplatePath templatePath, NodeBase<?> usage) {
		getOrCreateUsages(key).addParameter(templatePath, usage);
	}

	/**
	 * Removes the node previously registered by an extension for a given template
	 * path and key.
	 *
	 * @param key          the user tag name or included template id
	 * @param templatePath the path of the calling template
	 */
	public synchronized void removeUsage(String key, TemplatePath templatePath) {
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