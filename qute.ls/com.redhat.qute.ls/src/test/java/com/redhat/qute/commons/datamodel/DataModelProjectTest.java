/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons.datamodel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Data model project tests.
 * 
 * @author Angelo ZERR
 *
 */
public class DataModelProjectTest {

	@Test
	public void findDataModelTemplate() {
		DataModelProject<DataModelTemplate<DataModelParameter>> project = new DataModelProject<DataModelTemplate<DataModelParameter>>();
		project.setTemplates(Arrays.asList(//
				createNoLocationTemplate(), //
				createLocationTemplate()));

		// Template from @CheckedTemplate (or without @Location)
		DataModelTemplate<DataModelParameter> template = project.findDataModelTemplate(
				"C:/qute-quickstart/src/main/resources/templatesXXXXXXXXXXX/ItemResource/items.qute.html");
		assertNull(template, "Don't find template from @CheckedTemplate");

		template = project
				.findDataModelTemplate("C:/qute-quickstart/src/main/resources/templates/ItemResource/items.qute.html");
		assertNotNull(template, "Find template from @CheckedTemplate");

		// Template from @Location
		template = project.findDataModelTemplate(
				"C:/qute-quickstart/src/main/resources/templates/ItemResource/detail/items2_v1.txt");
		assertNull(template, "Don't find template from @LocationTemplate");

		template = project
				.findDataModelTemplate("C:/qute-quickstart/src/main/resources/templates/detail/items2_v1.html");
		assertNotNull(template, "Find template from @Location");

	}

	private static DataModelTemplate<DataModelParameter> createNoLocationTemplate() {
		// Template items;
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setTemplateUri("src/main/resources/templates/ItemResource/items");
		template.setSourceType("org.acme.qute.ItemResource$Templates");
		template.setSourceMethod("items");
		return template;
	}

	private static DataModelTemplate<DataModelParameter> createLocationTemplate() {
		// @Location("detail/items2_v1.html")
		// Template items;
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setTemplateUri("src/main/resources/templates/detail/items2_v1.html");
		return template;
	}
}
