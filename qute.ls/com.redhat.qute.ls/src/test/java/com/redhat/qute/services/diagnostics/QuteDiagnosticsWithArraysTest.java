/*******************************************************************************
* Copyright (c) 2025 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.junit.jupiter.api.Test;

/**
 * Test with arrays.
 *
 * @author Angelo ZERR
 * @see https://quarkus.io/guides/qute-reference#arrays
 */
public class QuteDiagnosticsWithArraysTest {

	@Test
	public void arrays() throws Exception {
		String template = "{@java.lang.String[] myArray}\r\n"
				+ "<h1>Array of length: {myArray.length}</h1> \r\n"
				+ "<ul>\r\n"
				+ "  <li>First: {myArray.0}</li> \r\n"
				+ "  <li>Second: {myArray[1]}</li> \r\n"
				+ "  <li>Third: {myArray.get(2)}</li> \r\n"
				+ "</ul>\r\n"
				+ "<ol>\r\n"
				+ " {#for element in myArray}\r\n"
				+ " <li>{element}</li>\r\n"
				+ " {/for}\r\n"
				+ "</ol>\r\n"
				+ "First two elements: {#each myArray.take(2)}{it}{/each}";
		testDiagnosticsFor(template);
	}

}
