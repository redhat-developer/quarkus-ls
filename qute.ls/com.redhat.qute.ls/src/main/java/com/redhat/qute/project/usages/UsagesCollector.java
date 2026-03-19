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

import com.redhat.qute.parser.NodeBase;
import com.redhat.qute.parser.injection.LanguageInjectionNode;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.CustomSection;
import com.redhat.qute.parser.template.sections.IncludeSection;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.extensions.LanguageInjectionService;

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
 * This collector is invoked on every keystroke for opened documents. The
 * {@link UsageTracker} instances are owned by the caller and reused across
 * visits. Cleanup of stale usages is handled by {@link UsagesRegistry} via the
 * source document and its reverse index — no {@code oldKeys} tracking is needed
 * here.
 * </p>
 *
 * <p>
 * For read-only documents, the collector is used only once and the trackers are
 * not stored.
 * </p>
 */
public class UsagesCollector extends ASTVisitor {

	/**
	 * Tracks usages for a single {@link UsagesRegistry} during a template visit.
	 *
	 * <p>
	 * Parameters are accumulated in {@code current} during the visit, then flushed
	 * to the registry via {@link #flush}. The registry handles cleanup of previous
	 * usages for the source document before applying the new ones.
	 * </p>
	 *
	 * <p>
	 * For opened documents, instances are owned by
	 * {@link com.redhat.qute.project.documents.QuteOpenedTextDocument} and reused
	 * across visits. For read-only documents, instances are created inline and
	 * discarded after a single use.
	 * </p>
	 *
	 * <p>
	 * Lifecycle per visit:
	 * <ol>
	 * <li>{@link #beginVisit()} — clear the current map</li>
	 * <li>{@link #collect(String, List)} — accumulate parameters per key</li>
	 * <li>{@link #flush(QuteTextDocument)} — flush collected data to the
	 * registry</li>
	 * </ol>
	 * </p>
	 */
	public static class UsageTracker {

		/**
		 * Parameters collected during the current visit, keyed by tag or template name.
		 * Reused across visits — never reallocated after construction.
		 */
		private final Map<String, List<? extends NodeBase<?>>> current = new HashMap<>();

		/** Registry to notify once the visit is complete. */
		private final UsagesRegistry<?> registry;

		public UsageTracker(UsagesRegistry<?> registry) {
			this.registry = registry;
		}

		/**
		 * Prepares this tracker for a new template visit by clearing the current map.
		 * No new collections are allocated.
		 */
		void beginVisit() {
			current.clear();
		}

		/**
		 * Records parameters for a given tag or template name.
		 *
		 * @param key        the user tag name or included template id
		 * @param parameters the parameters collected at this call site
		 */
		void collect(String key, List<? extends NodeBase<?>> parameters) {
			((List<NodeBase<?>>) (List<?>) current.computeIfAbsent(key, k -> new ArrayList<>()))
					.addAll((List<NodeBase<?>>) (List<?>) parameters);
		}

		/**
		 * Flushes collected usages to the registry.
		 *
		 * <p>
		 * The registry removes all previous usages contributed by {@code source} before
		 * applying the new ones — no stale key tracking needed.
		 * </p>
		 *
		 * @param source the document that was just parsed
		 */
		void flush(QuteTextDocument source) {
			registry.updateUsages(source, current);
		}
	}

	/** The source document being visited. */
	private final QuteTextDocument source;

	/** Tracks usages of user tag sections (e.g. {@code {#myTag /}}). */
	private final UsageTracker userTagTracker;

	/** Tracks usages of include sections (e.g. {@code {#include template /}}). */
	private final UsageTracker includeTracker;

	/**
	 * Creates a new collector for the given source document.
	 *
	 * <p>
	 * The {@link UsageTracker} instances are provided by the caller. For opened
	 * documents they are created once and reused across visits. For read-only
	 * documents they are created inline and discarded after a single use.
	 * </p>
	 *
	 * @param source         the document being visited
	 * @param userTagTracker tracker for user tag usages
	 * @param includeTracker tracker for include section usages
	 */
	public UsagesCollector(QuteTextDocument source, UsageTracker userTagTracker, UsageTracker includeTracker) {
		this.source = source;
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
		userTagTracker.flush(source);
		includeTracker.flush(source);
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
			/// {#include templateId title="foo" /}
			// First parameter is the included template id
			String includedTemplateId = parameters.get(0).getValue();
			if (!includedTemplateId.isEmpty()) {
				if (includedTemplateId.startsWith("$")) {
					includedTemplateId = source.getTemplateId() + includedTemplateId;
				}
				includeTracker.collect(includedTemplateId, parameters);
			}
		}
		return super.visit(section);
	}

	@Override
	public boolean visit(LanguageInjectionNode node) {
		LanguageInjectionService service = node.getLanguageService();
		if (service != null) {
			service.collectUsages(node, source);
		}
		return super.visit(node);
	}
}