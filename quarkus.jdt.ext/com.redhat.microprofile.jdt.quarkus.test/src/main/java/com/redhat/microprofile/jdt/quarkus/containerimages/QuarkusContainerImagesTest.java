/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus.containerimages;


import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.p;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;


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