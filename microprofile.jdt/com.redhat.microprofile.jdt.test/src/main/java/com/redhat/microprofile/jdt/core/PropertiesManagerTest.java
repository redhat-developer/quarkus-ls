/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_BUILD_TIME;
import static com.redhat.microprofile.jdt.internal.core.JavaUtils.createJavaProject;
import static com.redhat.microprofile.jdt.internal.core.JavaUtils.getJarPath;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.ClasspathKind;
import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfileProjectInfoParams;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;

/**
 * JDT Quarkus manager test.
 * 
 * @author Angelo ZERR
 *
 */
public class PropertiesManagerTest extends BasePropertiesManagerTest {

	private static final String QUARKUS_CORE_JAR = getJarPath("quarkus-core-0.28.1.jar");

	private static final String QUARKUS_CORE_DEPLOYMENT_JAR = getJarPath("quarkus-core-deployment-0.28.1.jar");

	@Test
	public void notBelongToEclipseProject() throws JavaModelException, CoreException {
		MicroProfileProjectInfoParams params = new MicroProfileProjectInfoParams();
		params.setUri("bad-uri");
		MicroProfileProjectInfo info = PropertiesManager.getInstance().getMicroProfileProjectInfo(params,
				JDT_UTILS, new NullProgressMonitor());
		Assert.assertNotNull("MicroProfileProjectInfo for 'bad-uri' should not be null", info);
		Assert.assertTrue("MicroProfileProjectInfo for 'bad-uri' should not belong to an Eclipse project ",
				info.getProjectURI().isEmpty());
	}
}
