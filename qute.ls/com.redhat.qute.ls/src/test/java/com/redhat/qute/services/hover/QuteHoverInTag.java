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
package com.redhat.qute.services.hover;

import static com.redhat.qute.QuteAssert.assertHover;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute hover in tag section.
 *
 * @author Angelo ZERR
 *
 */
public class QuteHoverInTag {

	@Test
	public void noHover() throws Exception {
		String template = "{#|}";
		assertHover(template);
	}
}
