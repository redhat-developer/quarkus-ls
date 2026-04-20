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
package com.redhat.qute.project.extensions.roq.frontmatter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.injection.LanguageInjectionNode;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.yaml.YamlNode;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.extensions.roq.RoqProjectExtension;

/**
 * Support for augmenting page.data and site.data with YAML front matter
 * properties.
 *
 * <p>
 * When a Roq template contains YAML front matter like:
 * </p>
 *
 * <pre>
 * ---
 * layout: 404
 * title: My title
 * ---
 * {page.data.layout}
 * </pre>
 *
 * <p>
 * Or when accessing site.data from content/index.html:
 * </p>
 *
 * <pre>
 * { site.data.title }
 * </pre>
 *
 * <p>
 * This support provides completion, navigation, and hover for the YAML
 * properties.
 * </p>
 */
public class YamlFrontMatterMemberSupport {

	/**
	 * Static type with tagging properties (tag and tagCollection) for page.data.
	 * Created once to avoid recreation on every member resolution.
	 */
	private static final List<ResolvedJavaTypeInfo> PAGE_DATA_TAGGING_TYPE = createPageDataTaggingType();

	private final RoqProjectExtension roq;

	public YamlFrontMatterMemberSupport(RoqProjectExtension roq) {
		this.roq = roq;
	}

	/**
	 * Returns additional types for member resolution based on YAML front matter.
	 *
	 * @param baseType     the base type (e.g., JsonObject for page.data or
	 *                     site.data)
	 * @param previousPart the previous part (e.g., "data" in page.data.layout)
	 * @param part         the part being resolved (may be null during completion)
	 * @param template     the template containing the expression
	 * @return list of additional types with YAML properties, or null
	 */
	public List<ResolvedJavaTypeInfo> getAdditionalTypes(ResolvedJavaTypeInfo baseType, Part previousPart, Part part,
			Template template) {
		YamlFrontMatterDocument yamlDoc = null;
		List<ResolvedJavaTypeInfo> additionalTypes = null;
		// Check if this is page.data pattern
		if (isPageDataPattern(previousPart)) {
			// Add tag, tagCollection for page
			// See https://github.com/quarkiverse/quarkus-roq/blob/a68a0a4982b2853e6e48703e791bdabd3b58497d/roq-plugin/tagging/deployment/src/main/java/io/quarkiverse/roq/plugin/tagging/deployment/RoqPluginTaggingProcessor.java#L107
			additionalTypes = new ArrayList<>(PAGE_DATA_TAGGING_TYPE);
			// Get YAML front matter from current template
			yamlDoc = getPageDataYamlFrontMatter(template);
		}
		// Check if this is site.data pattern
		else if (isSiteDataPattern(previousPart)) {
			// Get YAML front matter from content/index.html
			yamlDoc = getSiteDataYamlFrontMatter(template);
		}

		// Get cached resolved type (with YAML properties)
		ResolvedJavaTypeInfo yamlType = yamlDoc != null ? yamlDoc.getResolvedType() : null;
		if (yamlType != null) {
			if (additionalTypes == null) {
				additionalTypes = new ArrayList<>();
			}
			additionalTypes.add(yamlType);
		}
		return additionalTypes;
	}

	/**
	 * Creates a type with tagging properties for page.data.
	 *
	 * <p>
	 * The Roq tagging plugin adds two properties to page.data:
	 * </p>
	 * <ul>
	 * <li>{@code tag} - String: the current tag when filtering by tag</li>
	 * <li>{@code tagCollection} - String: the tag collection name</li>
	 * </ul>
	 *
	 * @return type with tag and tagCollection fields
	 */
	private static List<ResolvedJavaTypeInfo> createPageDataTaggingType() {
		ResolvedJavaTypeInfo taggingType = new ResolvedJavaTypeInfo();
		taggingType.setResolvedType(taggingType);

		List<JavaFieldInfo> fields = new ArrayList<>();

		// tag : String
		YamlFrontMatterField tagField = new YamlFrontMatterField("tag", "java.lang.String", null);
		fields.add(tagField);

		// tagCollection : String
		YamlFrontMatterField tagCollectionField = new YamlFrontMatterField("tagCollection", "java.lang.String", null);
		fields.add(tagCollectionField);

		taggingType.setFields(fields);
		taggingType.setSignature("");

		List<ResolvedJavaTypeInfo> additionalTypes = new ArrayList<>();
		additionalTypes.add(taggingType);
		return additionalTypes;
	}

	/**
	 * Checks if the previous part is "data" from a page.data expression.
	 *
	 * @param previousPart the previous part (should be "data")
	 * @return true if this is the "data" part from page.data
	 */
	private boolean isPageDataPattern(Part previousPart) {
		return isDataPattern(previousPart, "page");
	}

	/**
	 * Checks if the previous part is "data" from a site.data expression.
	 *
	 * @param previousPart the previous part (should be "data")
	 * @return true if this is the "data" part from site.data
	 */
	private boolean isSiteDataPattern(Part previousPart) {
		return isDataPattern(previousPart, "site");
	}

	/**
	 * Checks if the previous part is "data" from a {prefix}.data expression.
	 *
	 * @param previousPart the previous part (should be "data")
	 * @param prefix       the expected prefix (e.g., "page" or "site")
	 * @return true if this is the "data" part from {prefix}.data
	 */
	private boolean isDataPattern(Part previousPart, String prefix) {
		if (previousPart == null || !"data".equals(previousPart.getPartName())) {
			return false;
		}

		// Check if data has a previous part with the expected prefix
		Parts parts = previousPart.getParent();
		if (parts == null) {
			return false;
		}

		Part prefixPart = parts.getPreviousPart(previousPart);
		return prefixPart != null && prefix.equals(prefixPart.getPartName());
	}

	/**
	 * Gets the YAML front matter from the current template (for page.data).
	 *
	 * @param template the template
	 * @return the YAML front matter document, or null if no front matter
	 */
	private YamlFrontMatterDocument getPageDataYamlFrontMatter(Template template) {
		if (template == null || template.getChildCount() == 0) {
			return null;
		}

		// Check if first node is a language injection for YAML front matter
		Node firstNode = template.getChild(0);
		if (firstNode.getKind() != NodeKind.LanguageInjection) {
			return null;
		}

		LanguageInjectionNode injection = (LanguageInjectionNode) firstNode;
		if (!YamlFrontMatterDetector.YAML_FRONT_MATTER_LANGUAGE_ID.equals(injection.getLanguageId())) {
			return null;
		}

		// Get the parsed YAML document
		YamlNode yamlNode = (YamlNode) injection.getInjectedNode(CancelChecker.NO_CANCELLABLE);
		if (yamlNode == null || !(yamlNode instanceof YamlFrontMatterDocument)) {
			return null;
		}

		return (YamlFrontMatterDocument) yamlNode;
	}

	/**
	 * Gets the YAML front matter from content/index.html (for site.data).
	 *
	 * @param template the current template (used to find the project root)
	 * @return the YAML front matter document from content/index.html, or null if
	 *         not found
	 */
	private YamlFrontMatterDocument getSiteDataYamlFrontMatter(Template template) {
		// Get the project to find the project root
		QuteProject project = template.getProject();
		if (project == null) {
			return null;
		}

		Path contentDir = roq.getContentDir();
		if (contentDir == null) {
			return null;
		}

		Path indexHtml = contentDir.resolve("index.html");
		QuteTextDocument indexDocument = project.findSourceDocument(indexHtml);
		if (indexDocument == null) {
			return null;
		}

		Template indexTemplate = indexDocument.getTemplate();
		if (indexTemplate == null) {
			return null;
		}
		if (indexTemplate.getChildCount() < 1) {
			return null;
		}
		Node firstNode = indexTemplate.getChild(0);
		if (firstNode != null && firstNode.getKind() == NodeKind.LanguageInjection) {
			LanguageInjectionNode injectionNode = (LanguageInjectionNode) firstNode;
			return (YamlFrontMatterDocument) injectionNode.getInjectedNode(CancelChecker.NO_CANCELLABLE);
		}
		return null;
	}

}
