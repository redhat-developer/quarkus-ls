/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus;


import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static org.eclipse.lsp4mp.jdt.internal.core.MicroProfileAssert.p;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;


/**
 * Test the availability of the Quarkus Hibernate ORM properties
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusHibernateORMPropertyTest extends BasePropertiesManagerTest {

	@Test
	public void quarkusContainerImages() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MicroProfileMavenProjectName.hibernate_orm_resteasy, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,

				p("quarkus-hibernate-orm", "quarkus.hibernate-orm.database.generation", "java.lang.String",
						"Select whether the database schema is generated or not."
						+ "\n\n`drop-and-create` is awesome in development mode."
						+ "\n\nAccepted values: `none`, `create`, `drop-and-create`, `drop`, `update`.", true,
						"io.quarkus.hibernate.orm.deployment.HibernateOrmConfig.HibernateOrmConfigDatabase", "generation", null, 1,
						"none"));

		assertPropertiesDuplicate(infoFromClasspath);
	}

}