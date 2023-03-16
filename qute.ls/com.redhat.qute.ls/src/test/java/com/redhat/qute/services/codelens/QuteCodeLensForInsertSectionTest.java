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
package com.redhat.qute.services.codelens;

import static com.redhat.qute.QuteAssert.cl;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCodeLensFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.services.commands.QuteClientCommandConstants;

/**
 * Tests for Qute code lens and {#insert name} section tags.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCodeLensForInsertSectionTest {

	@Test
	public void insert() throws Exception {
		String value = "<!DOCTYPE html>\r\n"
				+ "<html lang=\"en\">\r\n"
				+ "<head>  \r\n"
				+ "  <title>{#insert includedTitle}Default Title{/}</title>\r\n"
				+ "</head>\r\n"
				+ "<body>\r\n"
				+ "{#each items}\r\n"
				+ "{/each}\r\n"
				// [2 references]
				+ "{#insert body}No body!{/}\r\n"
				+ "</body>\r\n"
				+ "</html>";
		testCodeLensFor(value, "src/main/resources/templates/tags/base.html",
				cl(r(8, 9, 8, 13), "2 references", QuteClientCommandConstants.COMMAND_SHOW_REFERENCES));
	}

}
