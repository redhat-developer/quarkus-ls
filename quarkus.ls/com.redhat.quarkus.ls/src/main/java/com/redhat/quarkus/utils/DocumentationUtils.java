/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package com.redhat.quarkus.utils;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;

/**
 * Utility for documentation.
 *
 */
public class DocumentationUtils {

	public static MarkupContent getDocumentation(ExtendedConfigDescriptionBuildItem item, boolean markdown) {
		StringBuilder documentation = new StringBuilder();
		String javaDoc = item.getDocs();
		if (javaDoc != null) {
			documentation.append(javaDoc);
		}
		String type = item.getType();
		if (type != null) {
			if (documentation.length() > 0) {
				documentation.append(System.lineSeparator());
			}
			documentation.append("Type:");
			if (markdown) {
				documentation.append("`");
			}
			documentation.append(type);
			if (markdown) {
				documentation.append("`");
			}
		}
		String defaultValue = item.getDefaultValue();
		if (defaultValue != null) {
			if (documentation.length() > 0) {
				documentation.append(System.lineSeparator());
			}
			documentation.append("Default:");
			if (markdown) {
				documentation.append("`");
			}
			documentation.append(defaultValue);
			if (markdown) {
				documentation.append("`");
			}
		}
		String source = item.getSource();
		if (source != null) {
			if (documentation.length() > 0) {
				documentation.append(System.lineSeparator());
			}
			documentation.append("Source:");
			if (markdown) {
				documentation.append("`");
			}
			documentation.append(source);
			if (markdown) {
				documentation.append("`");
			}
		}
		return new MarkupContent(markdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, documentation.toString());
	}

}
