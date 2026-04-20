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
package com.redhat.qute.parser.template;

import java.util.List;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;

/**
 * Java type provider.
 * 
 * @author Angelo ZERR
 *
 */
public interface JavaTypeInfoProvider {

	/**
	 * Returns the Java type as string and null otherwise.
	 * 
	 * For instance parameter declaration implement this API:
	 * 
	 * <code>
	 * {@org.acme.Item item}
	 * </code>
	 * 
	 * In this sample {@link #getJavaType()} will return org.acme.Item.
	 * 
	 * @return the Java type as string and null otherwise.
	 */
	default String getJavaType() {
		return null;
	}

	/**
	 * Returns the Java type as expression to evaluate to get the Java type and null
	 * otherwise.
	 * 
	 * @return the Java type as expression to evaluate to get the Java type and null
	 *         otherwise.
	 */
	default Expression getJavaTypeExpression() {
		return null;
	}

	/**
	 * Returns the owner node where the Java type comes from and null otherwise.
	 * 
	 * For instance parameter declaration implement this API:
	 * 
	 * <code>
	 * {@org.acme.Item item}
	 * </code>
	 * 
	 * In this sample {@link #getJavaTypeOwnerNode()} will return the
	 * {@link ParameterDeclaration} node.
	 * 
	 * @return the owner node where the Java type comes from and null otherwise.
	 */
	Node getJavaTypeOwnerNode();

	default ResolvedJavaTypeInfo getResolvedType() {
		return null;
	}

	/**
	 * Returns alternative types for union type support.
	 *
	 * <p>
	 * This is useful when a parameter can have multiple different types:
	 * </p>
	 * <ul>
	 * <li>User tag parameters inferred from multiple call sites with different
	 * types</li>
	 * <li>Properties defined in multiple locations (e.g., YAML front matter in
	 * different files)</li>
	 * </ul>
	 *
	 * <p>
	 * Example: A user tag parameter used as {#mon-tag name=123} and {#mon-tag
	 * name=true} would return alternatives for Integer and Boolean types.
	 * </p>
	 *
	 * @return the list of alternative type providers, or null if no alternatives
	 */
	default List<JavaTypeInfoProvider> getAlternativeTypes() {
		return null;
	}

	/**
	 * Returns the type label for hover and inlay hints.
	 *
	 * <p>
	 * When alternative types exist, displays them separated by pipe (|), for
	 * example: "Integer|Boolean|String".
	 * </p>
	 *
	 * <p>
	 * When no alternatives exist, returns the simple type name of the primary type.
	 * </p>
	 *
	 * @return the type label for display, or null if type cannot be determined
	 */
	default String getJavaElementTypeLabel() {
		List<JavaTypeInfoProvider> alternatives = getAlternativeTypes();
		if (alternatives != null && !alternatives.isEmpty()) {
			StringBuilder label = new StringBuilder();
			for (int i = 0; i < alternatives.size(); i++) {
				if (i > 0) {
					label.append("|");
				}
				JavaTypeInfoProvider alt = alternatives.get(i);
				String altType = getSimpleTypeName(alt);
				if (altType != null) {
					label.append(altType);
				}
			}
			return label.length() > 0 ? label.toString() : null;
		}
		return getSimpleTypeName(this);
	}

	/**
	 * Returns the simple type name from a JavaTypeInfoProvider.
	 *
	 * @param provider the Java type info provider
	 * @return the simple type name, or null if not available
	 */
	private static String getSimpleTypeName(JavaTypeInfoProvider provider) {
		if (provider == null) {
			return null;
		}
		ResolvedJavaTypeInfo resolvedType = provider.getResolvedType();
		if (resolvedType != null) {
			return resolvedType.getJavaElementSimpleType();
		}
		String javaType = provider.getJavaType();
		if (javaType != null) {
			return getSimpleType(javaType);
		}
		return null;
	}

	/**
	 * Returns the simple type name from a fully qualified type.
	 *
	 * @param type the fully qualified type (e.g., "java.lang.String" or
	 *             "java.util.List&lt;E&gt;")
	 * @return the simple type (e.g., "String" or "List&lt;E&gt;")
	 */
	private static String getSimpleType(String type) {
		if (type == null) {
			return null;
		}
		int index = type.lastIndexOf('.');
		if (index != -1) {
			return type.substring(index + 1);
		}
		return type;
	}
}
