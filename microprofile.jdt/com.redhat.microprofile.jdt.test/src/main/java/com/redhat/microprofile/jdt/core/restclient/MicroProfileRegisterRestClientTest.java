/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.restclient;

import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertHints;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertHintsDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.h;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.vh;

import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;

/**
 * Test collection of MicroProfile properties from @RegisterRestClient
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileRegisterRestClientTest extends BasePropertiesManagerTest {

	@Test
	public void restClientQuickstart() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.rest_client_quickstart, MicroProfilePropertiesScope.ONLY_SOURCES);

		// mp-rest Properties
		assertProperties(infoFromClasspath, 7,

				p(null, "${mp.register.rest.client.class}/mp-rest/url", "java.lang.String",
						"The base URL to use for this service, the equivalent of the `baseUrl` method.\r\n"
								+ "This property (or */mp-rest/uri) is considered required, however implementations may have other ways to define these URLs/URIs.",
						false, null, null, null, 0, null),

				p(null, "${mp.register.rest.client.class}/mp-rest/uri", "java.lang.String",
						"The base URI to use for this service, the equivalent of the baseUri method.\r\n"
								+ "This property (or */mp-rest/url) is considered required, however implementations may have other ways to define these URLs/URIs."
								+ "This property will override any `baseUri` value specified in the `@RegisterRestClient` annotation.",
						false, null, null, null, 0, null),

				p(null, "${mp.register.rest.client.class}/mp-rest/scope", "java.lang.String",
						"The fully qualified classname to a CDI scope to use for injection, defaults to "
								+ "`javax.enterprise.context.Dependent`.",
						false, null, null, null, 0, null),

				p(null, "${mp.register.rest.client.class}/mp-rest/providers", "java.lang.String",
						"A comma separated list of fully-qualified provider classnames to include in the client, "
								+ "the equivalent of the `register` method or the `@RegisterProvider` annotation.",
						false, null, null, null, 0, null),

				p(null, "${mp.register.rest.client.class}/mp-rest/connectTimeout", "long",
						"Timeout specified in milliseconds to wait to connect to the remote endpoint.", false, null,
						null, null, 0, null),

				p(null, "${mp.register.rest.client.class}/mp-rest/readTimeout", "long",
						"Timeout specified in milliseconds to wait for a response from the remote endpoint.", false,
						null, null, null, 0, null),

				p(null, "${mp.register.rest.client.class}/mp-rest/providers/{*}/priority", "int",
						"Override the priority of the provider for the given interface.", false, null, null, null, 0,
						null)

		);

		assertPropertiesDuplicate(infoFromClasspath);

		// mp-rest Hints
		assertHints(infoFromClasspath, 1,

				h("${mp.register.rest.client.class}", null, false, null,
						vh("org.acme.restclient.CountriesService", null, "org.acme.restclient.CountriesService"), //
						vh("configKey", null, "org.acme.restclient.CountiesServiceWithConfigKey")));

		assertHintsDuplicate(infoFromClasspath);

	}

}
