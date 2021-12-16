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
package com.redhat.qute.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link JavaFieldInfo}.
 *
 * @author Angelo ZERR
 *
 */
public class JavaFieldInfoTest {

	@Test
	public void bigInteger() {
		String signature = "price : java.math.BigInteger";
		JavaFieldInfo field = new JavaFieldInfo();
		field.setSignature(signature);
		assertEquals("price", field.getName());
		assertEquals("java.math.BigInteger", field.getType());
		assertEquals("price : BigInteger", field.getSimpleSignature());
	}
}
