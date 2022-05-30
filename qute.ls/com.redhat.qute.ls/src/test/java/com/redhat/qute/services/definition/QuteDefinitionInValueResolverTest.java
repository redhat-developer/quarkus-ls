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
package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.project.MockQuteProjectRegistry;

/**
 * Test definition with virtual method.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionInValueResolverTest {

	@Test
	public void virtualMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{item.discountedPr|ice}";
		testDefinitionFor(template, //
				ll("org/acme/ItemResource.java", r(1, 8, 1, 23), MockQuteProjectRegistry.JAVA_STATIC_METHOD_RANGE));
	}
	
	@Test
	public void varargs() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"		{item.prett|y('a')}";
		testDefinitionFor(template, //
				ll("org/acme/ItemResource.java", r(1, 8, 1, 14), MockQuteProjectRegistry.JAVA_STATIC_METHOD_RANGE));
	}
}
