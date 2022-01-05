/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.project.MockQuteProjectRegistry;

/**
 * Test definition with parameter declaration.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionInParameterDeclarationTest {

	@Test
	public void definitionInAlias() throws Exception {
		String template = "{@org.acme.Item it|em}\r\n";
		testDefinitionFor(template, "test.qute");
	}

	@Test
	public void definitionInUnExistingClass() throws Exception {
		String template = "{@org.ac|me.ItemXXXX item}\r\n";
		testDefinitionFor(template, "test.qute");
	}

	@Test
	public void definitionInExistingClass() throws Exception {
		String template = "{@org.ac|me.Item item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 2, 0, 15), MockQuteProjectRegistry.JAVA_CLASS_RANGE));

		template = "{@org.acme.Item| item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 2, 0, 15), MockQuteProjectRegistry.JAVA_CLASS_RANGE));
	}

	@Test
	public void definitionInExistingArrayClass() throws Exception {
		String template = "{@org.ac|me.Item[] item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 2, 0, 15), MockQuteProjectRegistry.JAVA_CLASS_RANGE));

		template = "{@org.acme.Item|[] item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 2, 0, 15), MockQuteProjectRegistry.JAVA_CLASS_RANGE));
	}

	@Test
	public void definitionInExistingClassInsideList() throws Exception {
		String template = "{@java.util.List<org.ac|me.Item> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 17, 0, 30), MockQuteProjectRegistry.JAVA_CLASS_RANGE));

		template = "{@java.util.List<|org.acme.Item> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 17, 0, 30), MockQuteProjectRegistry.JAVA_CLASS_RANGE));

		template = "{@java.util.List<org.acme.Item|> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 17, 0, 30), MockQuteProjectRegistry.JAVA_CLASS_RANGE));

		template = "{@java.util.List<org.acme.Ite|m item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 17, 0, 30), MockQuteProjectRegistry.JAVA_CLASS_RANGE));
	}

	@Test
	public void definitionInExistingClassInsideMap() throws Exception {
		String template = "{@java.util.Map<java.lang.String,org.ac|me.Item> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 33, 0, 46), MockQuteProjectRegistry.JAVA_CLASS_RANGE));

		template = "{@java.util.Map<java.lang.String,|org.acme.Item> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 33, 0, 46), MockQuteProjectRegistry.JAVA_CLASS_RANGE));

		template = "{@java.util.Map<java.lang.String,org.acme.Item|> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 33, 0, 46), MockQuteProjectRegistry.JAVA_CLASS_RANGE));

		template = "{@java.util.Map<java.lang.String,org.acme.Ite|m item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(0, 33, 0, 46), MockQuteProjectRegistry.JAVA_CLASS_RANGE));
	}

	@Test
	public void definitionInClassList() throws Exception {
		String template = "{@java.ut|il.List<org.acme.Item> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("java/util/List.java", r(0, 2, 0, 16), MockQuteProjectRegistry.JAVA_CLASS_RANGE));

		template = "{@|java.util.List<org.acme.Item> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("java/util/List.java", r(0, 2, 0, 16), MockQuteProjectRegistry.JAVA_CLASS_RANGE));

		template = "{@java.util.List|<org.acme.Item> item}\r\n";
		testDefinitionFor(template, "test.qute", //
				ll("java/util/List.java", r(0, 2, 0, 16), MockQuteProjectRegistry.JAVA_CLASS_RANGE));
	}
}
