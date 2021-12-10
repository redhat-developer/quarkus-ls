/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.utils;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

import com.redhat.qute.commons.JavaElementKind;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;

/**
 * Utility for documentation.
 *
 */
public class DocumentationUtils {

	private DocumentationUtils() {

	}

	public static MarkupContent getDocumentation(ResolvedJavaTypeInfo resolvedType, boolean markdown) {

		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("```java");
			documentation.append(System.lineSeparator());
		}
		documentation.append(resolvedType.getSignature());
		if (markdown) {
			documentation.append(System.lineSeparator());
			documentation.append("```");
		}
		return createMarkupContent(documentation, markdown);
	}

	private static MarkupContent createMarkupContent(StringBuilder documentation, boolean markdown) {
		return new MarkupContent(markdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, documentation.toString());
	}

	public static MarkupContent getDocumentation(JavaMemberInfo member, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("```java");
			documentation.append(System.lineSeparator());
		}
		documentation.append(member.getJavaElementSimpleType());
		documentation.append(" ");
		if (member.getResolvedType() != null) {
			documentation.append(member.getResolvedType().getSignature());
			documentation.append(".");
		}
		documentation.append(member.getName());

		if (member.getJavaElementKind() == JavaElementKind.METHOD) {
			documentation.append('(');
			documentation.append(')');
		}

		if (markdown) {
			documentation.append(System.lineSeparator());
			documentation.append("```");
		}

		if (member.getDocumentation() != null) {
			documentation.append(System.lineSeparator());
			documentation.append(member.getDocumentation());
		}

		return createMarkupContent(documentation, markdown);
	}
}
