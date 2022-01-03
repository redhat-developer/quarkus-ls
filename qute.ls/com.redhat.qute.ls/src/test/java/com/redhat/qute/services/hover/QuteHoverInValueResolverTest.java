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
import static com.redhat.qute.QuteAssert.r;

import org.junit.jupiter.api.Test;

/**
 * Test hover with virtual method.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteHoverInValueResolverTest {

	@Test
	public void virtualMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{item.discountedPr|ice}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"BigDecimal org.acme.ItemResource.discountedPrice()" + //
				System.lineSeparator() + //
				"```", // ,
				r(1, 8, 1, 23));
		
		template = "{@org.acme.Item item}\r\n" + //
				"		{item.discountedPr|ice()}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"BigDecimal org.acme.ItemResource.discountedPrice()" + //
				System.lineSeparator() + //
				"```", // ,
				r(1, 8, 1, 23));

	}
}
