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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link JavaMethodInfo}.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaMethodInfoTest {

	@Test
	public void map() {
		String signature = "find(query : java.lang.String, params : java.util.Map<java.lang.String,java.lang.Object>) : io.quarkus.hibernate.orm.panache.PanacheQuery<T>";
		JavaMethodInfo method = new JavaMethodInfo();
		method.setSignature(signature);
		assertEquals("find", method.getName());

		assertTrue(method.hasParameters());
		assertEquals(2, method.getParameters().size());

		JavaMethodParameterInfo parameter = method.getParameters().get(0);
		assertEquals("query", parameter.getName());
		assertEquals("java.lang.String", parameter.getType());

		parameter = method.getParameters().get(1);
		assertEquals("params", parameter.getName());
		assertEquals("java.util.Map<java.lang.String,java.lang.Object>", parameter.getType());

		assertEquals("io.quarkus.hibernate.orm.panache.PanacheQuery<T>", method.getReturnType());
	}
}
