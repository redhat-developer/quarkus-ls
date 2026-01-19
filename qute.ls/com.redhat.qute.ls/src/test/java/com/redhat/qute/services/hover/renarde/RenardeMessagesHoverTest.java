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
package com.redhat.qute.services.hover.renarde;

import static com.redhat.qute.QuteAssert.r;

import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.renarde.RenardeProject;

/**
 * Tests with Renarde Quarkus extension and resources message.
 *
 * @author Angelo ZERR
 *
 */
public class RenardeMessagesHoverTest {

	@Test
	public void invalidMessageKey() throws Exception {
		String template = "{m:main.lo|ginXXX}";
		assertHover(template, null, null);
	}

	@Test
	public void validMessageKey() throws Exception {
		String template = "{m:main.lo|gin}";
		assertHover(template, //
				" * default: **Login/Register**\n" + //
						" * fr: Connexion/Enregistrement", //
				r(0, 1, 0, 13));

	}

	@Test
	public void validMessageKeyEncoding() throws Exception {
		String template = "{m:todos.message.dele|ted}";
		assertHover(template, //
				" * default: **Task deleted: %s**\n" + //
						" * fr: Tâche supprimée : %s", //
				r(0, 1, 0, 24));

	}

	public static void assertHover(String value, String expectedHoverLabel, Range expectedHoverRange) throws Exception {
		QuteAssert.assertHover(value, QuteAssert.FILE_URI, null, RenardeProject.PROJECT_URI, expectedHoverLabel,
				expectedHoverRange);
	}

}
