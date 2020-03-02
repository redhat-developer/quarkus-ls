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
package com.redhat.microprofile.jdt.core.jaxrs;

import org.junit.Assert;
import org.junit.Test;

/**
 * JAX-RS utilities tests.
 * 
 * @author Angelo ZERR
 *
 */
public class JaxRsUtilsTest {

	@Test
	public void buildURL() {

		String actual = JaxRsUtils.buildURL("http://locatlhost:8080/", "/v2", "rest");
		Assert.assertEquals("http://locatlhost:8080/v2/rest", actual);

		actual = JaxRsUtils.buildURL("http://locatlhost:8080", "/v2", "rest");
		Assert.assertEquals("http://locatlhost:8080/v2/rest", actual);

		actual = JaxRsUtils.buildURL("http://locatlhost:8080/", "v2", "rest");
		Assert.assertEquals("http://locatlhost:8080/v2/rest", actual);

		actual = JaxRsUtils.buildURL("http://locatlhost:8080/", "/v2/", "rest");
		Assert.assertEquals("http://locatlhost:8080/v2/rest", actual);

		actual = JaxRsUtils.buildURL("http://locatlhost:8080/", "/v2/", "/rest");
		Assert.assertEquals("http://locatlhost:8080/v2/rest", actual);

	}
}
