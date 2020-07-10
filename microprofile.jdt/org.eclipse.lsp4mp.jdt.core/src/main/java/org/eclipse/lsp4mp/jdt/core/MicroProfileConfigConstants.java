/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.core;

/**
 * MicroProfile Config constants
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileConfigConstants {

	private MicroProfileConfigConstants() {
	}

	public static final String INJECT_ANNOTATION = "javax.inject.Inject";

	// MicroProfile Core annotations

	public static final String CONFIG_PROPERTY_ANNOTATION = "org.eclipse.microprofile.config.inject.ConfigProperty";

	public static final String CONFIG_PROPERTY_ANNOTATION_NAME = "name";

	public static final String CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE = "defaultValue";

}
