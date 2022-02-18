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
package com.redhat.qute.services.codelens;

import static com.redhat.qute.QuteAssert.cl;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCodeLensFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute code lens and user tags.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCodeLensForUserTagTest {

	@Test
	public void input() throws Exception {
		String value = "<input name=\"{name}\" \r\n" + //
				" type=\"{type ?: 'text'}\"\r\n" + //
				" {#if placeholder??}placeholder=\"{placeholder}\"{/if}\r\n" + //
				" {#if id??}id=\"{id}\"{/if}\r\n" + //
				" class=\"form-control {#ifError name}is-invalid{/ifError}\"\r\n" + //
				" maxlength=\"{VARCHAR_SIZE}\"\r\n" + //
				" value=\"{inject:flash.get(name) ?: value??}\"/>";
		testCodeLensFor(value, "src/main/resources/templates/tags/input.html", //
				"tags/input", //
				cl(r(0, 0, 0, 0), "User tag #input", ""), //
				cl(r(0, 0, 0, 0), "name", ""), //
				cl(r(0, 0, 0, 0), "type", ""), //
				cl(r(0, 0, 0, 0), "placeholder", ""), //
				cl(r(0, 0, 0, 0), "id", ""), //
				cl(r(0, 0, 0, 0), "VARCHAR_SIZE", ""));
	}

	@Test
	public void formElement() throws Exception {
		String value = "<div class=\"mb-3\">\r\n" + //
				"    <label class=\"form-label\" for=\"{name}\">{label}</label>\r\n" + //
				"    {nested-content}\r\n" + //
				"    {#ifError name}\r\n" + //
				"        <span class=\"invalid-feedback\">​{#error name/}</span>​\r\n" + //
				"    {/ifError}\r\n" + //
				"</div>";
		testCodeLensFor(value, "src/main/resources/templates/tags/formElement.html", //
				"tags/formElement", //
				cl(r(0, 0, 0, 0), "User tag #formElement", ""), //
				cl(r(0, 0, 0, 0), "name", ""), //
				cl(r(0, 0, 0, 0), "label", ""), //
				cl(r(0, 0, 0, 0), "nested-content", ""));
	}

}
