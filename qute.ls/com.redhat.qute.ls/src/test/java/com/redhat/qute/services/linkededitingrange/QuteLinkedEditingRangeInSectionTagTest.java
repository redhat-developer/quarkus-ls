/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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
 * Tests for Qute linked editing range in start tag section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteLinkedEditingRangeInSectionTagTest {

	@Test
	public void inStartTag() throws BadLocationException {
		String template = "{#us|er}\r\n" + //
				"	\r\n" + //
				"{/user}";
		testLinkedEditingFor(template, //
				le(r(0, 2, 0, 6), r(2, 2, 2, 6)));
	}

	@Test
	public void inEmptyStartTag() throws BadLocationException {
		String template = "{#|}\r\n" + //
				"	\r\n" + //
				"{/}";
		testLinkedEditingFor(template, //
				le(r(0, 2, 0, 2), r(2, 2, 2, 2)));
	}

	@Test
	public void inEndTag() throws BadLocationException {
		String template = "{#user}\r\n" + //
				"	\r\n" + //
				"{/us|er}";
		testLinkedEditingFor(template, //
				le(r(0, 2, 0, 6), r(2, 2, 2, 6)));
	}

	@Test
	public void inEmptyEndTag() throws BadLocationException {
		String template = "{#}\r\n" + //
				"	\r\n" + //
				"{/|}";
		testLinkedEditingFor(template, //
				le(r(0, 2, 0, 2), r(2, 2, 2, 2)));
	}
}
