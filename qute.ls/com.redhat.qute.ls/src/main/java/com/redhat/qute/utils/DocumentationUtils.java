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
import static com.redhat.qute.ls.commons.snippets.SnippetRegistry.addLink;
import static com.redhat.qute.ls.commons.snippets.SnippetRegistry.addLinks;

import java.net.URI;
import java.util.List;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

import com.redhat.qute.commons.JavaElementKind;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.parser.template.CaseOperator;
import com.redhat.qute.parser.template.SectionMetadata;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.project.tags.UserTag;

/**
 * Utility for documentation.
 *
 */
public class DocumentationUtils {

	private DocumentationUtils() {

	}

	public static MarkupContent getDocumentation(JavaTypeInfo javaType, boolean markdown) {
		return getDocumentation(javaType, null, markdown);
	}

	public static MarkupContent getDocumentation(JavaTypeInfo javaType, String description, boolean markdown) {
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
		documentation.append(javaType.getJavaElementSimpleType());
		if (markdown) {
			documentation.append(System.lineSeparator());
			documentation.append("```");
		}

		return createMarkupContent(documentation, markdown);
	}

	private static MarkupContent createMarkupContent(StringBuilder documentation, boolean markdown) {
		return new MarkupContent(markdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, documentation.toString());
	}

	public static MarkupContent getDocumentation(MethodValueResolver resolver,
			ResolvedJavaTypeInfo iterableOfResolvedType, boolean markdown) {
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
		String url = resolver.getUrl();
		addUrl(url, documentation, markdown);
		return createMarkupContent(documentation, markdown);
	}

	public static MarkupContent getDocumentation(JavaMemberInfo member, ResolvedJavaTypeInfo iterableOfResolvedType,
			boolean markdown) {
		if (member instanceof MethodValueResolver) {
			return getDocumentation((MethodValueResolver) member, iterableOfResolvedType, markdown);
		}
		StringBuilder documentation = createDocumentation(member, iterableOfResolvedType, markdown);
		return createMarkupContent(documentation, markdown);
	}

	private static StringBuilder createDocumentation(JavaMemberInfo member, ResolvedJavaTypeInfo iterableOfResolvedType,
			boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("```java");
			documentation.append(System.lineSeparator());
		}
		documentation.append(getSimpleType(member.resolveJavaElementType(iterableOfResolvedType)));
		documentation.append(" ");
		String sourceType = member.getSourceType();
		if (sourceType != null) {
			documentation.append(sourceType);
			documentation.append(".");
		}
		documentation.append(member.getName());

		if (member.getJavaElementKind() == JavaElementKind.METHOD) {
			documentation.append('(');
			JavaMethodInfo methodInfo = (JavaMethodInfo) member;
			boolean virtualMethod = methodInfo.isVirtual();
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

		if (!StringUtils.isEmpty(member.getDocumentation())) {
			documentation.append(System.lineSeparator());
			if (markdown) {
				documentation.append("---");
				documentation.append(System.lineSeparator());
			}
			documentation.append(member.getDocumentation());
		}
		return documentation;
	}

	public static MarkupContent getDocumentation(UserTag userTag, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		if (markdown) {
			documentation.append("**");
		}
		documentation.append("#" + userTag.getName());
		if (markdown) {
			documentation.append("**");
		}
		documentation.append(" user tag ");

		documentation.append(System.lineSeparator());
		documentation.append(System.lineSeparator());

		URI uri = URI.create(userTag.getUri());
		documentation.append("Defined in ");
		if (markdown) {
			documentation.append("[");
			documentation.append(userTag.getFileName());
			documentation.append("](");
			documentation.append(userTag.getUri());
			documentation.append(")");
		} else {
			documentation.append(uri.getPath());
		}

		return createMarkupContent(documentation, markdown);
	}

	/**
	 * Returns the markup content for operators in the #case section.
	 *
	 * @param operator the case operator.
	 * @param markdown true if has markdown.
	 *
	 * @return the markup content for operators in the #case section.
	 */
	public static MarkupContent getDocumentation(CaseOperator operator, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		if (markdown) {
			documentation.append("**");
		}
		documentation.append("Operator");
		if (markdown) {
			documentation.append("**");
		}
		documentation.append(" for #case/#is section.");
		documentation.append(System.lineSeparator());
		documentation.append(System.lineSeparator());

		if (operator.getDescription() != null) {
			documentation.append(operator.getDescription());
		}
		if (operator.getSample() != null) {
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
			for (int i = 0; i < operator.getSample().size(); i++) {
				String line = operator.getSample().get(i);
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
		String url = operator.getUrl();
		addUrl(url, documentation, markdown);

		return createMarkupContent(documentation, markdown);
	}

	public static MarkupContent getDocumentation(Snippet snippet, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		if (markdown) {
			documentation.append("**");
		}
		documentation.append("#" + snippet.getLabel());
		if (markdown) {
			documentation.append("**");
		}
		documentation.append(" section tag ");

		if (snippet.getDescription() != null) {
			documentation.append(System.lineSeparator());
			documentation.append(System.lineSeparator());
			documentation.append(snippet.getDescription());
		}

		addLinks(snippet.getLinks(), documentation, markdown);

		return createMarkupContent(documentation, markdown);
	}

	public static MarkupContent getDocumentation(SectionMetadata metadata, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("```java");
			documentation.append(System.lineSeparator());
		}
		documentation.append(metadata.getSimpleType());
		if (markdown) {
			documentation.append(System.lineSeparator());
			documentation.append("```");
		}

		String description = metadata.getDescription();
		if (description != null) {
			documentation.append(System.lineSeparator());
			documentation.append(description);
		}

		return createMarkupContent(documentation, markdown);
	}

	public static MarkupContent getDocumentation(String namespace, NamespaceResolverInfo namespaceInfo,
			boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Title
		documentation.append("Namespace: ");
		if (markdown) {
			documentation.append("`");
		}
		documentation.append(namespace);
		if (markdown) {
			documentation.append("`");
			documentation.append(System.lineSeparator());
		}

		String description = namespaceInfo.getDescription();
		if (description != null) {
			documentation.append(System.lineSeparator());
			documentation.append(description);
			documentation.append(System.lineSeparator());
		}

		String url = namespaceInfo.getUrl();
		addUrl(url, documentation, markdown);

		return createMarkupContent(documentation, markdown);
	}

	private static void addUrl(String url, StringBuilder documentation, boolean markdown) {
		if (!StringUtils.isEmpty(url)) {
			documentation.append(System.lineSeparator());
			documentation.append("See ");
			addLink(url, "here", documentation, markdown);
			documentation.append(" for more informations.");
		}
	}

}