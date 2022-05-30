/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link JavaParameterInfo}.
 *
 * @author Angelo ZERR
 *
 */
public class JavaParameterInfoTest {

	@Test
	public void stringVarargs() {
		String signature = "java.lang.String...";
		JavaParameterInfo type = new JavaParameterInfo("str", "java.lang.String...");
		type.setSignature(signature);
		assertEquals("str", type.getName());

		assertEquals("java.lang.String...", type.getType());
		assertEquals("java.lang.String", type.getVarArgType());
		assertEquals("String...", type.getJavaElementSimpleType());
		assertFalse(type.getJavaType().isArray());
		assertTrue(type.isVarargs());
		assertFalse(type.getJavaType().isGenericType());
	}
}
