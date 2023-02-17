/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Renarde completion in uri / urabs expression.
 *
 * @author Angelo ZERR
 *
 */
public class RenardeCompletionInExpressionTest {

	@Test
	public void controllerCompletion() throws Exception {
		String template = "{uri:|}";
		testCompletionFor(template, //
				c("Login", "Login", r(0, 5, 0, 5)));
	}

	@Test
	public void methodsOfController() throws Exception {
		String template = "{uri:Login.|}";
		testCompletionFor(template, //
				9, //
				c("login() : TemplateInstance", "login", r(0, 11, 0, 11)), //
				c("manualLogin(userName : String, password : String, webAuthnResponse : WebAuthnLoginResponse, ctx : RoutingContext) : Response",
						"manualLogin()$0", r(0, 11, 0, 11)), //
				c("complete(confirmationCode : String, userName : String, password : String, password2 : String, webAuthnResponse : WebAuthnRegisterResponse, firstName : String, lastName : String, ctx : RoutingContext) : Response",
						"complete()$0", r(0, 11, 0, 11)), //
				c("confirm(confirmationCode : String) : void", "confirm(${1:confirmationCode})$0", r(0, 11, 0, 11)), //
				c("orEmpty(base : T) : List<T>", "orEmpty", r(0, 11, 0, 11)), //
				c("safe(base : Object) : RawString", "safe", r(0, 11, 0, 11)), //
				c("raw(base : Object) : RawString", "raw", r(0, 11, 0, 11)), //
				c("or(base : T, arg : Object) : T", "or(${1:arg})$0", r(0, 11, 0, 11)), //
				c("ifTruthy(base : T, arg : Object) : T", "ifTruthy(${1:arg})$0", r(0, 11, 0, 11)));
	}

}