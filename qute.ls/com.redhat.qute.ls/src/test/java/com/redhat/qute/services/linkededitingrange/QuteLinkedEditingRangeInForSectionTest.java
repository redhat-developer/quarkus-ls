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
package com.redhat.qute.services.linkededitingrange;

import static com.redhat.qute.QuteAssert.le;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testLinkedEditingFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.BadLocationException;

/**
 * Tests for Qute linked editing range.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteLinkedEditingRangeInForSectionTest {

	@Test
	public void fromAlias() throws BadLocationException {
		String template = "{#for it|em in items}\r\n" + //
				"	{item.name}\r\n" + //
				"{/for}";
		testLinkedEditingFor(template, //
				le(r(0, 6, 0, 10), r(1, 2, 1, 6)));
	}

	@Test
	public void toAlias() throws BadLocationException {
		String template = "{#for item in items}\r\n" + //
				"	{it|em.name}\r\n" + //
				"{/for}";
		testLinkedEditingFor(template, null);
	}

	@Test
	public void unbalanced() throws BadLocationException {
		String template = "{#for ite|m in items}\r\n" + //
				"{/each}";
		testLinkedEditingFor(template, null);
	}

	@Test
	public void fromAliasInElseBlock() throws BadLocationException {
		String template = "{#for it|em in items}\r\n" + //
				"{#else}" + //
				"	{item.name}\r\n" + //
				"{/for}";
		testLinkedEditingFor(template, null);
	}

}
