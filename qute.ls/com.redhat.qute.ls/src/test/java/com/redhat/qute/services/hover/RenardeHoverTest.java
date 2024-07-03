/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.hover;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.r;

import org.junit.jupiter.api.Test;

/**
 * Renarde uri hover tests.
 */
public class RenardeHoverTest {
	
	@Test
	public void timeoutGame() throws Exception {
		// timeoutGame used a method part
		String template = "{uri:Login.timeo|utGame()}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"void rest.Login.timeoutGame()" + //
				System.lineSeparator() + //
				"```", // ,
				r(0, 11, 0, 22));

		// timeoutGame used a property part
		template = "{uri:Login.timeo|utGame}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"void rest.Login.timeoutGame()" + //
				System.lineSeparator() + //
				"```", // ,
				r(0, 11, 0, 22));

	}

}
