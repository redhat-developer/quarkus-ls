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
 * Test definition in expression.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionInExpressionTest {

	@Test
	public void definitionInUndefinedVariable() throws Exception {
		String template = "{i|tem}";
		testDefinitionFor(template);
	}

	@Test
	public void definitionInDefinedVariable() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{i|tem}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 1, 1, 5), r(0, 16, 0, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"{item|}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 1, 1, 5), r(0, 16, 0, 20)));
	}

	@Test
	public void definitionInUndefinedProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|eXXX}";
		testDefinitionFor(template);
	}

	@Test
	public void definitionInDefinedProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|e}";
		testDefinitionFor(template, //
				ll("org/acme/Item.java", r(1, 6, 1, 10), MockQuteProjectRegistry.JAVA_FIELD_RANGE));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name|}";
		testDefinitionFor(template, ll("org/acme/Item.java", r(1, 6, 1, 10), MockQuteProjectRegistry.JAVA_FIELD_RANGE));
	}

	@Test
	public void definitionInDefinedPropertyGetter() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.revie|w2}";
		testDefinitionFor(template, //
				ll("org/acme/Item.java", r(1, 6, 1, 13), MockQuteProjectRegistry.JAVA_METHOD_RANGE));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.review2|}";
		testDefinitionFor(template, //
				ll("org/acme/Item.java", r(1, 6, 1, 13), MockQuteProjectRegistry.JAVA_METHOD_RANGE));
	}

	@Test
	public void methodDefinitionForIterable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.siz|e}";
		testDefinitionFor(template, //
				ll("java/util/List.java", r(1, 7, 1, 11), MockQuteProjectRegistry.JAVA_METHOD_RANGE));

	}
}
