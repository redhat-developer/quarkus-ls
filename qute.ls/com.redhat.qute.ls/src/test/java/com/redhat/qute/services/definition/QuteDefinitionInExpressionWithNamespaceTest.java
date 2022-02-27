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
public class QuteDefinitionInExpressionWithNamespaceTest {

	@Test
	public void dataDefinition() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{it|em}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 1, 1, 5), r(0, 16, 0, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|e}";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Item.java", r(1, 6, 1, 10), MockQuteProjectRegistry.JAVA_FIELD_RANGE));
	}

	@Test
	public void injectDefinition() throws Exception {
		String template = "{inject:be|an}";
		testDefinitionFor(template, "test.qute", //
				ll("org/acme/Bean.java", r(0, 8, 0, 12), MockQuteProjectRegistry.JAVA_FIELD_RANGE));

		template = "{inject:be|anX}";
		testDefinitionFor(template, "test.qute");
	}

	@Test
	public void injectDefinitionInMethodPart() throws Exception {
		String template = "{inject:bean.emp|ty}";
		testDefinitionFor(template, "test.qute", //
				ll("java/lang/String.java", r(0, 13, 0, 18), MockQuteProjectRegistry.JAVA_METHOD_RANGE));

		template = "{inject:bean.|X}";
		testDefinitionFor(template, "test.qute");
	}
}
