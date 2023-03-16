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
package com.redhat.qute.services.references;

import static com.redhat.qute.QuteAssert.l;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testReferencesFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.BadLocationException;

/**
 * Qute reference with object part.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteReferenceWithObjectPartTest {

	@Test
	public void parameterDeclarationAlias() throws BadLocationException {
		String template = "{@org.acme.Item it|em}\r\n" + //
				"{item.name}}\r\n" + //
				"{item}";
		testReferencesFor(template, //
				l("test.qute", r(1, 1, 1, 5)), //
				l("test.qute", r(2, 1, 2, 5)));
	}
}
