/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.internal.core.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link JDTQuarkusUtils}
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusUtilsTest {

	@Test
	public void getStandardExtensionName() {
		String extensionName = JDTQuarkusUtils.getExtensionName(
				"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.21.1/quarkus-core-0.21.1.jar");
		Assert.assertEquals("quarkus-core", extensionName);
	}

	@Test
	public void getDeploymentExtensionName() {
		String extensionName = JDTQuarkusUtils.getExtensionName(
				"C:/Users/azerr/.m2/repository/io/quarkus/quarkus-arc-deployment/0.21.1/quarkus-arc-deployment-0.21.1.jar");
		Assert.assertEquals("quarkus-arc", extensionName);
	}

	@Test
	public void getBadExtensionName() {
		String extensionName = JDTQuarkusUtils
				.getExtensionName("C:/Users/azerr/.m2/repository/io/quarkus/quarkus-arc-deployment/0.21.1/rt.jar");
		Assert.assertNull(extensionName);
	}
}
