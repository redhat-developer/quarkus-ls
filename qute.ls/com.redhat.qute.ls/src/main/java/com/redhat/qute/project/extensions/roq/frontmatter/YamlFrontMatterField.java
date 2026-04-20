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

import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.yaml.YamlPositionUtility;
import com.redhat.qute.parser.yaml.YamlProperty;
import com.redhat.qute.parser.yaml.YamlScalar;
import com.redhat.qute.services.extensions.DefinitionExtensionProvider;
import com.redhat.qute.services.extensions.HoverExtensionProvider;
import com.redhat.qute.services.hover.HoverRequest;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Represents a field from YAML front matter.
 *
 * <p>
 * This class extends JavaFieldInfo to represent properties defined in YAML
 * front matter blocks at the top of Roq templates. It provides navigation and
 * hover support for these properties.
 * </p>
 *
 * <h3>Example:</h3>
 * 
 * <pre>
 * ---
 * layout: 404
 * title: My title
 * ---
 * {page.data.layout}  ← YamlFrontMatterField for "layout"
 * </pre>
 */
public class YamlFrontMatterField extends JavaFieldInfo implements DefinitionExtensionProvider, HoverExtensionProvider {

	private final String fieldName;
	private final YamlProperty yamlProperty;

	/**
	 * Creates a new YAML front matter field.
	 *
	 * @param fieldName    the field name
	 * @param javaType     the Java type signature (may be null if using setResolvedType)
	 * @param yamlProperty the YAML property node (for navigation)
	 */
	public YamlFrontMatterField(String fieldName, String javaType, YamlProperty yamlProperty) {
		this.fieldName = fieldName;
		this.yamlProperty = yamlProperty;
		if (javaType != null) {
			setSignature(fieldName + " : " + javaType);
		}
	}

	@Override
	public String getName() {
		return fieldName;
	}

	@Override
	public boolean shouldLoadDocumentation() {
		return false;
	}

	public YamlProperty getYamlProperty() {
		return yamlProperty;
	}

	@Override
	public List<? extends LocationLink> getLocations(Part part) {
		if (yamlProperty == null || yamlProperty.getKey() == null) {
			return Collections.emptyList();
		}

		LocationLink link = new LocationLink();

		// Origin: The position in the template where the user clicked (e.g.,
		// {page.data.layout})
		link.setOriginSelectionRange(QutePositionUtility.createRange(part));

		// Target: The same template file but at the YAML property location
		String uri = yamlProperty.getOwnerDocument().getUri();
		link.setTargetUri(uri);

		// Target range: The YAML property key (e.g., "layout:" in the front matter)
		YamlScalar keyNode = yamlProperty.getKey();
		Range keyRange = YamlPositionUtility.createRange(keyNode);
		link.setTargetRange(keyRange);
		link.setTargetSelectionRange(keyRange);

		return Collections.singletonList(link);
	}

	@Override
	public Hover getHover(Part part, HoverRequest hoverRequest) {
		if (yamlProperty == null) {
			return null;
		}

		// Create hover documentation showing the field type
		StringBuilder markdown = new StringBuilder();
		markdown.append("```java");
		markdown.append(System.lineSeparator());
		markdown.append(getSimpleSignature());
		markdown.append(System.lineSeparator());
		markdown.append("```");

		// Add YAML front matter source information
		YamlScalar keyNode = yamlProperty.getKey();
		if (keyNode != null) {
			markdown.append(System.lineSeparator());
			markdown.append("---");
			markdown.append(System.lineSeparator());

			// Extract filename from URI
			String uri = yamlProperty.getOwnerDocument().getUri();
			String fileName = uri;
			int lastSlash = uri.lastIndexOf('/');
			if (lastSlash != -1 && lastSlash < uri.length() - 1) {
				fileName = uri.substring(lastSlash + 1);
			}

			// Get line number (1-based)
			Range keyRange = YamlPositionUtility.createRange(keyNode);
			int lineNumber = keyRange.getStart().getLine() + 1;

			// Create markdown link: [filename:line](uri)
			markdown.append("Defined in YAML front matter: ");
			markdown.append("[");
			markdown.append(fileName);
			markdown.append(":");
			markdown.append(lineNumber);
			markdown.append("](");
			markdown.append(uri);
			markdown.append(")");
		}

		MarkupContent contents = new MarkupContent();
		contents.setKind(MarkupKind.MARKDOWN);
		contents.setValue(markdown.toString());

		Hover hover = new Hover();
		hover.setContents(contents);
		hover.setRange(QutePositionUtility.createRange(part));

		return hover;
	}
}
