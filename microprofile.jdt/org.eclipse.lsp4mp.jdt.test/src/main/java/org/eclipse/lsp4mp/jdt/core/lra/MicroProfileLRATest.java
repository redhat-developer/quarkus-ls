/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.core.lra;

import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.p;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

/**
 * Test the availability of the MicroProfile LRA properties
 * 
 * @author David Kwon
 *
 */
public class MicroProfileLRATest extends BasePropertiesManagerTest {

	@Test
	public void microprofileLRAPropertiesTest() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.microprofile_lra, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,

				p("microprofile-lra-api", "mp.lra.propagation.active", "java.lang.String", 
						"When a JAX-RS endpoint, or the containing class, is not "
						+ "annotated with `@LRA`, but it is called on a MicroProfile "
						+ "LRA compliant runtime, the system will propagate the LRA "
						+ "related HTTP headers when this parameter resolves to true.\r\n\r\n"
						+ "The behaviour is similar to the `LRA.Type` `SUPPORTS` "
						+ "(when true) and `NOT_SUPPORTED` (when false) values but "
						+ "only defines the propagation aspect.\r\n\r\n"
						+ "In other words the class does not have to be a participant in "
						+ "order for the LRA context to propagate, i.e. such propagation "
						+ "of the header does not imply that the LRA is in any particular "
						+ "state, and in fact the LRA may not even correspond to a valid LRA.",
						true, null, null, null, 0, null)

		);

		assertPropertiesDuplicate(infoFromClasspath);
	}

}
