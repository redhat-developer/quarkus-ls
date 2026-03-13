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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.CustomSection;
import com.redhat.qute.parser.template.sections.IncludeSection;

/**
 * Collects usages of Qute user tags and include sections inside a template.
 *
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
 * traverses the AST and gathers all user tag calls and include section calls
 * along with their {@link Parameter} instances.
 * </p>
 *
 * <p>
 * The collected data is then forwarded to the relevant {@link UsagesRegistry},
 * which is responsible for:
 * <ul>
 * <li>type inference</li>
 * <li>hover support</li>
 * <li>completion</li>
 * <li>find references / call hierarchy</li>
 * </ul>
 *
 * <p>
 * This collector is invoked on every keystroke for opened documents. To avoid
 * per-visit heap allocations, the {@link UsageTracker} instances are owned by
 * the caller and reused across visits via a map-swap strategy. This collector
 * itself is a stateless visitor that simply delegates to those trackers.
 * </p>
 *
 * <p>
 * For read-only documents, the collector is used only once and the trackers are
 * not stored.
 * </p>
 */
public class UsagesCollector extends ASTVisitor {

	/**
	 * Tracks usages for a single {@link UsagesRegistry} across template visits.
	 *
	 * <p>
	 * On each visit, instead of allocating new collections for previously known
	 * names, the two internal maps are swapped. This eliminates per-visit heap
	 * allocations, which matters since this collector runs on every keystroke.
	 * </p>
	 *
	 * <p>
	 * For opened documents, instances are owned by
	 * {@link com.redhat.qute.project.documents.QuteOpenedTextDocument} so that
	 * their internal maps survive across visits for the same template. For
	 * read-only documents, instances are created inline and discarded after a
	 * single use.
	 * </p>
	 *
	 * <p>
	 * Lifecycle per visit:
	 * <ol>
	 * <li>{@link #beginVisit()} — swap maps, clear the active one</li>
	 * <li>{@link #collect(String, List)} — accumulate parameters per name</li>
	 * <li>{@link #endVisit(String)} — flush collected data to the registry</li>
	 * </ol>
	 * </p>
	 */
	public static class UsageTracker {

		/**
		 * Parameters collected during the current visit, keyed by tag or template name.
		 * Reused across visits via map swap — never reallocated after construction.
		 */
		private Map<String, List<Parameter>> current = new HashMap<>();

		/**
		 * Parameters from the previous visit, used to detect removed usages. Reused
		 * across visits via map swap — never reallocated after construction.
		 */
		private Map<String, List<Parameter>> previous = new HashMap<>();

		/** Registry to notify once the visit is complete. */
		private final UsagesRegistry<?> registry;

		public UsageTracker(UsagesRegistry<?> registry) {
			this.registry = registry;
		}

		/**
		 * Prepares this tracker for a new template visit.
		 *
		 * <p>
		 * Swaps {@code current} and {@code previous} so that the previous data becomes
		 * the removal candidate set, then clears {@code current} for fresh collection.
		 * No new collections are allocated.
		 * </p>
		 */
		void beginVisit() {
			// Swap: previous recycles the old current map, current is cleared for reuse
			Map<String, List<Parameter>> tmp = previous;
			previous = current;
			current = tmp;
			current.clear();
		}

		/**
		 * Records parameters for a given tag or template name.
		 *
		 * <p>
		 * Removes {@code key} from {@code previous} to signal it is still active, then
		 * appends the parameters to the current collection.
		 * </p>
		 *
		 * @param key        the user tag name or included template id
		 * @param parameters the parameters collected at this call site
		 */
		void collect(String key, List<Parameter> parameters) {
			// Mark this name as still active so it won't be treated as removed
			previous.remove(key);
			current.computeIfAbsent(key, k -> new ArrayList<>()).addAll(parameters);
		}

		/**
		 * Flushes collected usages to the registry.
		 *
		 * <p>
		 * Any names remaining in {@code previous} were not encountered during this
		 * visit and are therefore treated as removed by the registry.
		 * </p>
		 *
		 * @param templateId URI of the visited template
		 */
		void endVisit(String templateId) {
			// previous.keySet() holds names that disappeared since the last visit
			registry.updateUsages(templateId, current, previous.keySet());
		}
	}

	/** URI of the template currently being visited. */
	private final String templateId;

	/** Tracks usages of user tag sections (e.g. {@code {#myTag /}}). */
	private final UsageTracker userTagTracker;

	/** Tracks usages of include sections (e.g. {@code {#include template /}}). */
	private final UsageTracker includeTracker;

	/**
	 * Creates a new collector for the given template.
	 *
	 * <p>
	 * The {@link UsageTracker} instances are provided by the caller. For opened
	 * documents they are created once and reused across visits. For read-only
	 * documents they are created inline and discarded after a single use.
	 * </p>
	 *
	 * @param templateId     URI of the template being visited
	 * @param userTagTracker tracker for user tag usages
	 * @param includeTracker tracker for include section usages
	 */
	public UsagesCollector(String templateId, UsageTracker userTagTracker, UsageTracker includeTracker) {
		this.templateId = templateId;
		this.userTagTracker = userTagTracker;
		this.includeTracker = includeTracker;
	}

	@Override
	public boolean visit(Template node) {
		userTagTracker.beginVisit();
		includeTracker.beginVisit();
		return super.visit(node);
	}

	@Override
	public void endVisit(Template node) {
		userTagTracker.endVisit(templateId);
		includeTracker.endVisit(templateId);
		super.endVisit(node);
	}

	@Override
	public boolean visit(CustomSection node) {
		List<Parameter> parameters = node.getParameters();
		if (!parameters.isEmpty()) {
			// user tag is called, e.g.:
			// {#myTag name="foo" count=10 /}
			userTagTracker.collect(node.getTag(), parameters);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(IncludeSection section) {
		List<Parameter> parameters = section.getParameters();
		if (!parameters.isEmpty()) {
			// {#include templateId title="foo" /}
			// First parameter is the included template id
			String includedTemplateId = parameters.get(0).getValue();
			includeTracker.collect(includedTemplateId, parameters);
		}
		return super.visit(section);
	}

}