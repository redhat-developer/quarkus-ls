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
package com.redhat.qute.services.linkededitingrange;

import static com.redhat.qute.QuteAssert.le;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testLinkedEditingFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.BadLocationException;

/**
 * Tests for Qute linked editing range with object part in #if section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteLinkedEditingRangeInIfSectionTest {

	@Test
	public void noOptionalParameter() throws BadLocationException {
		String template = "{#if fo|o}\r\n" + //
				"	{foo}\r\n" + //
				"{/for}";
		testLinkedEditingFor(template, null);
	}
	
	@Test
	public void optionalParameter() throws BadLocationException {
		String template = "{#if fo|o??}\r\n" + //
				"	{foo}\r\n" + //
				"{/for}";
		testLinkedEditingFor(template, //
				le(r(0, 5, 0, 8), r(1, 2, 1, 5)));
	}
}
