/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.template.roq;

import static com.redhat.qute.jdt.QuteAssert.assertValueResolver;
import static com.redhat.qute.jdt.QuteProjectTest.getJDTUtils;
import static com.redhat.qute.jdt.QuteProjectTest.loadMavenProject;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.jdt.QuteProjectTest.QuteMavenProjectName;
import com.redhat.qute.jdt.QuteSupportForTemplate;

/**
 * Roq @DataMapping annotation resolver support tests.
 *
 * @author Angelo ZERR
 *
 */
public class DataMappingSupportTest {

	@Test
	public void roq() throws Exception {

		loadMavenProject(QuteMavenProjectName.roq_blog);

		QuteDataModelProjectParams params = new QuteDataModelProjectParams(QuteMavenProjectName.roq_blog);
		DataModelProject<DataModelTemplate<DataModelParameter>> project = QuteSupportForTemplate.getInstance()
				.getDataModelProject(params, getJDTUtils(), new NullProgressMonitor());
		Assert.assertNotNull(project);

		List<ValueResolverInfo> resolvers = project.getValueResolvers();
		Assert.assertNotNull(resolvers);
		Assert.assertFalse(resolvers.isEmpty());

		testValueResolversDataMappingAnnotation(resolvers);
	}

	private static void testValueResolversDataMappingAnnotation(List<ValueResolverInfo> resolvers) {
		Assert.assertNotNull(resolvers);
		Assert.assertFalse(resolvers.isEmpty());

		// @DataMapping(value = "events", parentArray = true)
		// public record Events(List<Event> list) {
		assertValueResolver("inject", "Events", "Events", "events", resolvers);

	}

}
