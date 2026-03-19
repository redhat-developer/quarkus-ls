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
package com.redhat.qute.services.references;

import static com.redhat.qute.QuteAssert.l;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testReferencesFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.BadLocationException;

/**
 * Qute reference with fragment.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteReferenceWithFragmentTest {

	@Test
	public void fragmentId() throws BadLocationException {
		String template = "{#fragment i|d=menu}\r\n" + // <-- reference on id=menu
				"    {device}\r\n" + //
				"{/fragment}\r\n" + //
				"\r\n" + //
				"{#include $menu device='mobile' /}";

		testReferencesFor(template, //
				"test", //
				l("test.qute", r(4, 10, 4, 15)));
	}
}
