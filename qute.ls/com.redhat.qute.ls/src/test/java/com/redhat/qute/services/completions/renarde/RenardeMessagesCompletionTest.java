/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions.renarde;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.renarde.RenardeProject;

/**
 * Tests for Qute completion in expression.
 *
 * @author Angelo ZERR
 *
 */
public class RenardeMessagesCompletionTest {

	@Test
	public void completionInExpressionForObjectPart() throws Exception {
		String template = "{|";
		testCompletionFor(template, //
				c("m:main.login = Login/Register", "m:main.login", r(0, 1, 0, 1)), //
				c("m:application.index.title = Welcome to Todos", "m:application.index.title", r(0, 1, 0, 1)), //
				c("m:todos.message.added = Task added: %s", "m:todos.message.added(${1:arg0})$0", r(0, 1, 0, 1)));

		template = "{m|";
		testCompletionFor(template, //
				c("m:main.login = Login/Register", "m:main.login", r(0, 1, 0, 2)), //
				c("m:application.index.title = Welcome to Todos", "m:application.index.title", r(0, 1, 0, 2)), //
				c("m:todos.message.added = Task added: %s", "m:todos.message.added(${1:arg0})$0", r(0, 1, 0, 2)));
		
		template = "{m:|";
		testCompletionFor(template, //
				c("m:main.login = Login/Register", "m:main.login", r(0, 1, 0, 3)), //
				c("m:application.index.title = Welcome to Todos", "m:application.index.title", r(0, 1, 0, 3)), //
				c("m:todos.message.added = Task added: %s", "m:todos.message.added(${1:arg0})$0", r(0, 1, 0, 3)));
		
		template = "{m:m|ain.login";
		testCompletionFor(template, //
				c("m:main.login = Login/Register", "m:main.login", r(0, 1, 0, 13)), //
				c("m:application.index.title = Welcome to Todos", "m:application.index.title", r(0, 1, 0, 13)), //
				c("m:todos.message.added = Task added: %s", "m:todos.message.added(${1:arg0})$0", r(0, 1, 0, 13)));

		template = "{m:main.lo|gin";
		testCompletionFor(template, //
				c("m:main.login = Login/Register", "m:main.login", r(0, 1, 0, 13)), //
				c("m:application.index.title = Welcome to Todos", "m:application.index.title", r(0, 1, 0, 13)), //
				c("m:todos.message.added = Task added: %s", "m:todos.message.added(${1:arg0})$0", r(0, 1, 0, 13)));
	}

	public static void testCompletionFor(String value, CompletionItem... expectedItems) throws Exception {
		QuteAssert.testCompletionFor(value, true, QuteAssert.FILE_URI, RenardeProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, null, expectedItems);
	}
}