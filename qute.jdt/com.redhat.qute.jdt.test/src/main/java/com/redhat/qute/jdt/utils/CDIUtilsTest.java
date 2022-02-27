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
package com.redhat.qute.jdt.utils;

import org.eclipse.jdt.core.IJavaElement;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link CDIUtils}.
 * 
 * @author Angelo ZERR
 *
 */
public class CDIUtilsTest {

	@Test
	public void namedWithType() {
		String javaType = "MyClass";

		String name = CDIUtils.getSimpleName(javaType, null, IJavaElement.TYPE);
		Assert.assertEquals("myClass", name);

		String named = "foo";
		name = CDIUtils.getSimpleName(javaType, named, IJavaElement.TYPE);
		Assert.assertEquals("foo", name);
	}
	
	@Test
	public void namedWithField() {
		String javaType = "MyField";

		String name = CDIUtils.getSimpleName(javaType, null, IJavaElement.FIELD);
		Assert.assertEquals("MyField", name);

		String named = "foo";
		name = CDIUtils.getSimpleName(javaType, named, IJavaElement.FIELD);
		Assert.assertEquals("foo", name);
	}
	
	@Test
	public void namedWithMethod() {
		String javaType = "MyMethod";

		String name = CDIUtils.getSimpleName(javaType, null, IJavaElement.METHOD);
		Assert.assertEquals("MyMethod", name);

		String named = "foo";
		name = CDIUtils.getSimpleName(javaType, named, IJavaElement.METHOD);
		Assert.assertEquals("foo", name);
	}
	
	@Test
	public void namedWithGetterMethod() {
		String javaType = "getMethod";

		String name = CDIUtils.getSimpleName(javaType, null, IJavaElement.METHOD, () -> true);
		Assert.assertEquals("method", name);

		String named = "foo";
		name = CDIUtils.getSimpleName(javaType, named, IJavaElement.METHOD, () -> true);
		Assert.assertEquals("foo", name);
	}
}
