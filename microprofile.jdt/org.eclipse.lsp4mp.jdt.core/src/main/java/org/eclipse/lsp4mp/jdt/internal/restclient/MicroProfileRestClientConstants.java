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
package org.eclipse.lsp4mp.jdt.internal.restclient;

/**
 * MicroProfile RestClient constants
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileRestClientConstants {

	private MicroProfileRestClientConstants() {
	}

	public static final String REST_CLIENT_ANNOTATION = "org.eclipse.microprofile.rest.client.inject.RestClient";

	public static final String REGISTER_REST_CLIENT_ANNOTATION = "org.eclipse.microprofile.rest.client.inject.RegisterRestClient";

	public static final String REGISTER_REST_CLIENT_ANNOTATION_CONFIG_KEY = "configKey";

	public static final String REGISTER_REST_CLIENT_ANNOTATION_BASE_URI = "baseUri";

	public static final String DIAGNOSTIC_SOURCE = "microprofile-restclient";

}
