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
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.RESOLVERS_SIZE;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion in user tag section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInUserTagTest {

	@Test
	public void specialKeys() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{|}";

		// In qute template
		testCompletionFor(template, //
				RESOLVERS_SIZE + 1, //
				c("item", "item", r(1, 1, 1, 1)), //
				c("inject:bean", "inject:bean", r(1, 1, 1, 1)), //
				c("inject:plexux", "inject:plexux", r(1, 1, 1, 1)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(1, 1, 1, 1)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(1, 1, 1, 1)), //
				c("GLOBAL", "GLOBAL", r(1, 1, 1, 1)), //
				c("VARCHAR_SIZE", "VARCHAR_SIZE", r(1, 1, 1, 1)), //
				c("uri:Login", "uri:Login", r(1, 1, 1, 1)), //
				c("msg:hello_name(name : String) : String", "msg:hello_name(${1:name})$0", r(1, 1, 1, 1)), //
				c("msg2:hello() : String", "msg2:hello", r(1, 1, 1, 1)), //
				c("bundle", "bundle", r(1, 1, 1, 1)));

		// In user tag
		testCompletionFor(template, //
				"src/main/resources/templates/tags/form.html", //
				"tags/form", //
				RESOLVERS_SIZE /* item, inject:bean, config:getConfigProperty */ + 2 /* it, nested-content */ + 1 /*
																													 * global
																													 * variables
																													 */, //
				c("item", "item", r(1, 1, 1, 1)), //
				c("inject:bean", "inject:bean", r(1, 1, 1, 1)), //
				c("inject:plexux", "inject:plexux", r(1, 1, 1, 1)), //
				c("config:*(propertyName : String) : Object", "config:propertyName", r(1, 1, 1, 1)),
				c("config:property(propertyName : String) : Object", "config:property(propertyName)", r(1, 1, 1, 1)), //
				c("GLOBAL", "GLOBAL", r(1, 1, 1, 1)), //
				c("VARCHAR_SIZE", "VARCHAR_SIZE", r(1, 1, 1, 1)), //
				c("it", "it", r(1, 1, 1, 1)), //
				c("nested-content", "nested-content", r(1, 1, 1, 1)), //
				c("uri:Login", "uri:Login", r(1, 1, 1, 1)), //
				c("msg:hello_name(name : String) : String", "msg:hello_name(name)", r(1, 1, 1, 1)), //
				c("msg2:hello() : String", "msg2:hello", r(1, 1, 1, 1)), //
				c("bundle", "bundle", r(1, 1, 1, 1)));

	}

}