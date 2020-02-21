/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.restclient;

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
	
	public static final String DIAGNOSTIC_SOURCE = "microprofile-restclient";
	public static final String DIAGNOSTIC_IMPLEMENT_HEALTH_CHECK_CODE = "ImplementHealthCheck";
	public static final String DIAGNOSTIC_HEALTH_ANNOTATION_MISSING_CODE = "HealthAnnotationMissing";

}
