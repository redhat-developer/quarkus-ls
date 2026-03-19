/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
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
 * Test definition with fragment section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionInFragmentSectionTest {

	@Test
	public void localFragment() throws Exception {
		String template = "<html>\r\n" + //
				"{#fragment id=menu}\r\n" + //
				"    {device}\r\n" + //
				"{/fragment}\r\n" + //
				"{#include $me|nu device='mobile' /}";

		testDefinitionFor(template, //
				"foo.html", //
				ll("foo.html", r(4, 10, 4, 15), r(1, 14, 1, 18)));
	}

}