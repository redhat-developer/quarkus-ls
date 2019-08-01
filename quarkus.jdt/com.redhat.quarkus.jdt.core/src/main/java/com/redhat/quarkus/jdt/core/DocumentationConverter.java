/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.core;

/**
 * Documentation converter API.
 * 
 * @author Angelo ZERR
 *
 */
@FunctionalInterface
public interface DocumentationConverter {

	public static final DocumentationConverter DEFAULT_CONVERTER = (javaDoc) -> javaDoc;

	String convert(String javaDoc);
}
