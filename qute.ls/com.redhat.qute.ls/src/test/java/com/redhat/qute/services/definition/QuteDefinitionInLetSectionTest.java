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

/**
 * Test definition with #let section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionInLetSectionTest {

	@Test
	public void definedObject() throws Exception {
		String template = "{#set name=item.name age=10 long=10L negDouble=-10D isActive=true simpleQuote='abcd' doubleQuote=\"efgh\"}\r\n"
				+ //
				"  {true}\r\n" + //
				"  {name}\r\n" + //
				"  {neg|Double}\r\n" + //
				"  {isActive}\r\n" + //
				"  {simpleQuote}\r\n" + //
				"  {doubleQuote}\r\n" + //
				"{/set}\r\n" + //
				"";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(3, 3, 3, 12), r(0, 37, 0, 46)));
	}
}
