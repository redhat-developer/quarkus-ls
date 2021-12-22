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

import static com.redhat.qute.commons.JavaElementInfo.getSimpleType;

import java.util.List;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

import com.redhat.qute.commons.JavaElementKind;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.ValueResolver;

/**
 * Utility for documentation.
 *
 */
public class DocumentationUtils {

	private DocumentationUtils() {

	}

	public static MarkupContent getDocumentation(ResolvedJavaTypeInfo resolvedType, boolean markdown) {
		return getDocumentation(resolvedType, null, markdown);
	}

	public static MarkupContent getDocumentation(ResolvedJavaTypeInfo resolvedType, String description,
			boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		if (description != null) {
			documentation.append(description);
			documentation.append(System.lineSeparator());
		}

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

	public static MarkupContent getDocumentation(ValueResolver resolver, ResolvedJavaTypeInfo iterableOfResolvedType,
			boolean markdown) {
		StringBuilder documentation = createDocumentation(resolver, iterableOfResolvedType, markdown);
		if (resolver.getDescription() != null) {
			documentation.append(System.lineSeparator());
			documentation.append(resolver.getDescription());
		}
		if (resolver.getSample() != null) {
			documentation.append(System.lineSeparator());
			documentation.append(System.lineSeparator());
			if (markdown) {
				documentation.append("`");
			}
			documentation.append("Sample");
			if (markdown) {
				documentation.append("`");
			}
			documentation.append(":");
			documentation.append(System.lineSeparator());
			if (markdown) {
				documentation.append("```qute-html");
				documentation.append(System.lineSeparator());
			}
			for (int i = 0; i < resolver.getSample().size(); i++) {
				String line = resolver.getSample().get(i);
				if (i > 0) {
					documentation.append(System.lineSeparator());
				}
				documentation.append(line);
			}
			documentation.append(System.lineSeparator());
			if (markdown) {
				documentation.append("```");
			}

		}
		if (resolver.getUrl() != null) {
			documentation.append(System.lineSeparator());
			documentation.append("See ");
			if (markdown) {
				documentation.append("[here](");
				documentation.append(resolver.getUrl());
				documentation.append(")");
			} else {
				documentation.append(resolver.getUrl());
			}
			documentation.append(" for more informations.");
		}
		return createMarkupContent(documentation, markdown);
	}

	public static MarkupContent getDocumentation(JavaMemberInfo member, ResolvedJavaTypeInfo iterableOfResolvedType,
			boolean markdown) {
		if (member instanceof ValueResolver) {
			return getDocumentation((ValueResolver) member, iterableOfResolvedType, markdown);
		}
		StringBuilder documentation = createDocumentation(member, iterableOfResolvedType, markdown);
		return createMarkupContent(documentation, markdown);
	}

	public static StringBuilder createDocumentation(JavaMemberInfo member, ResolvedJavaTypeInfo iterableOfResolvedType,
			boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("```java");
			documentation.append(System.lineSeparator());
		}
		documentation.append(getSimpleType(member.resolveJavaElementType(iterableOfResolvedType)));
		documentation.append(" ");
		if (member.getResolvedType() != null) {
			documentation.append(member.getResolvedType().getName());
			documentation.append(".");
		}
		documentation.append(member.getName());

		if (member.getJavaElementKind() == JavaElementKind.METHOD) {
			documentation.append('(');
			JavaMethodInfo methodInfo = (JavaMethodInfo) member;
			boolean virtualMethod = methodInfo instanceof ValueResolver;
			List<JavaParameterInfo> parameters = methodInfo.getParameters();
			int start = virtualMethod ? 1 : 0;
			for (int i = start; i < parameters.size(); i++) {
				JavaParameterInfo parameter = parameters.get(i);
				String type = parameter.getJavaElementSimpleType();
				String name = parameter.getName();
				if (i > start) {
					documentation.append(", ");
				}
				documentation.append(type);
				documentation.append(' ');
				documentation.append(name);
			}
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
		return documentation;
	}
}
