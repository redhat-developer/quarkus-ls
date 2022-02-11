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

/**
 * Test definition with #if section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionInIfSectionTest {

	@Test
	public void definedVariable() throws Exception {
		String template = "{#let value=123}\r\n" + //
				"  {#if val|ue}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 7, 1, 12), r(0, 6, 0, 11)));
	}

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{#if val|ue}";
		testDefinitionFor(template);
	}

}
