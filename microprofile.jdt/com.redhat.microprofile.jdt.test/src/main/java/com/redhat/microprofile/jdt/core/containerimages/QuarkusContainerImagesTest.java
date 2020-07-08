/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.containerimages;

import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;

import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;

/**
 * Test the availability of the Quarkus Container Images properties
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusContainerImagesTest extends BasePropertiesManagerTest {

	@Test
	public void quarkusContainerImages() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.quarkus_container_images, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,

				p("quarkus-container-image", "quarkus.container-image.group", "java.util.Optional<java.lang.String>",
						"The group the container image will be part of", true,
						"io.quarkus.container.image.deployment.ContainerImageConfig", "group", null, 1,
						"${user.name}"));

		assertPropertiesDuplicate(infoFromClasspath);
	}

}