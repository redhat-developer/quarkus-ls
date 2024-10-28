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
package com.redhat.qute.jdt.internal.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Source file type resolver implementation.
 * 
 * @author Angelo ZERR
 *
 */
public class CompilationUnitTypeResolver extends AbstractTypeResolver {

	private static final Logger LOGGER = Logger.getLogger(CompilationUnitTypeResolver.class.getName());

	private final Map<String, String> packages;

	public CompilationUnitTypeResolver(ICompilationUnit compilationUnit) {
		super(compilationUnit.findPrimaryType());
		this.packages = new HashMap<>();
		try {
			IImportDeclaration[] imports = compilationUnit.getImports();
			if (imports != null) {
				for (int i = 0; i < imports.length; i++) {
					String name = imports[i].getElementName();
					int importedIndex = name.lastIndexOf('.');
					String importedClassName = name.substring(importedIndex + 1, name.length());
					packages.put(importedClassName, name);
				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE,
					"Error while creating packages cache for '" + compilationUnit.getElementName() + "'.", e);

		}
	}

	@Override
	protected String resolveSimpleType(String type) {
		try {
			if (type.indexOf('.') == -1) {
				// ex: Item
				String resolvedType = packages.get(type);
				if (resolvedType != null) {
					// Item is found as import packages. Ex:
					// import org.acme.Item;
					return resolvedType;
				}
			}
			String[][] resolvedNames = primaryType.resolveType(type);
			if (resolvedNames != null && resolvedNames.length > 0) {
				String packageName = resolvedNames[0][0];
				String className = resolvedNames[0][1];
				if (className.indexOf('.') != -1) {
					// Ex : Map.Entry should be updated to Map$Entry
					className = className.replace('.', '$');
				}
				if (packageName.isBlank()) {
					return className;
				}
				return packageName + '.' + className;
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while resolving simple type '" + type + "'.", e);
		}
		return type;
	}
}
