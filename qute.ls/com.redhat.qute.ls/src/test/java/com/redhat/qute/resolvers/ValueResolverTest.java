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
package com.redhat.qute.resolvers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.ValueResolver;

/**
 * Tests for value resolver.
 * 
 * @author Angelo ZERR
 *
 */
public class ValueResolverTest {

	@Test
	public void orEmpty() {

		ResolvedJavaTypeInfo list = new ResolvedJavaTypeInfo();
		list.setSignature("java.util.List<org.acme.Item>");

		ValueResolver resolver = new ValueResolver();
		resolver.setSignature("orEmpty(arg : java.util.List<T>) : java.lang.Iterable<T>");

		String returnType = resolver.resolveJavaElementType(list);
		Assertions.assertEquals("java.lang.Iterable<org.acme.Item>", returnType);
	}

	@Test
	public void orEmpty2() {

		ResolvedJavaTypeInfo list = new ResolvedJavaTypeInfo();
		list.setSignature("org.acme.Item");

		ValueResolver resolver = new ValueResolver();
		resolver.setSignature("orEmpty(arg : T) : java.util.List<T>");

		String returnType = resolver.resolveJavaElementType(list);
		Assertions.assertEquals("java.util.List<org.acme.Item>", returnType);
	}

}
