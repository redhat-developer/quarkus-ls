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
package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.project.MockQuteProjectRegistry;

/**
 * Test definition in infix notation.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionInInfixNotationTest {

	@Test
	public void charAt() throws Exception {
		// Infix notation
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name ch|arAt 1}";
		testDefinitionFor(template, //
				ll("java/lang/String.java", r(1, 11, 1, 17), MockQuteProjectRegistry.JAVA_METHOD_RANGE));
		
		// No infix notation
		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.ch|arAt(1)}";
		testDefinitionFor(template, //
				ll("java/lang/String.java", r(1, 11, 1, 17), MockQuteProjectRegistry.JAVA_METHOD_RANGE));
	}
}
