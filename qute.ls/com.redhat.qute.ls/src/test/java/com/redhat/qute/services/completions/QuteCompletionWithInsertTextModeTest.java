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

import static com.redhat.qute.QuteAssert.r;

import java.util.Arrays;

import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionItemInsertTextModeSupportCapabilities;
import org.eclipse.lsp4j.InsertTextMode;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.QuteQuickStartProject;
import com.redhat.qute.settings.QuteCompletionSettings;

/**
 * Tests for Qute completion with {@link InsertTextMode}.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionWithInsertTextModeTest {

	@Test
	public void asIs() throws Exception {
		String template = "|";

		testCompletionFor(template, //
				null, //
				InsertTextMode.AsIs, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 0), //
						null));

		testCompletionFor(template, //
				InsertTextMode.AsIs, //
				InsertTextMode.AsIs, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 0), //
						null));

		testCompletionFor(template, //
				InsertTextMode.AdjustIndentation, //
				InsertTextMode.AsIs, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 0), //
						InsertTextMode.AsIs));

		template = "		  |";
		testCompletionFor(template, //
				null, //
				InsertTextMode.AsIs, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"		  	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"		  {/for}", //
						r(0, 4, 0, 4), //
						null));

		testCompletionFor(template, //
				InsertTextMode.AsIs, //
				InsertTextMode.AsIs, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"		  	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"		  {/for}", //
						r(0, 4, 0, 4), //
						null));

		testCompletionFor(template, //
				InsertTextMode.AdjustIndentation, //
				InsertTextMode.AsIs, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"		  	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"		  {/for}", //
						r(0, 4, 0, 4), //
						InsertTextMode.AsIs));
	}

	@Test
	public void adjustIndentation() throws Exception {
		String template = "|";

		testCompletionFor(template, //
				null, //
				InsertTextMode.AdjustIndentation, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 0), //
						InsertTextMode.AdjustIndentation));

		testCompletionFor(template, //
				InsertTextMode.AdjustIndentation, //
				InsertTextMode.AdjustIndentation, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 0), //
						null));

		testCompletionFor(template, //
				InsertTextMode.AsIs, //
				InsertTextMode.AdjustIndentation, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 0), //
						InsertTextMode.AdjustIndentation));

		template = "	|";
		testCompletionFor(template, //
				null, //
				InsertTextMode.AdjustIndentation, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 1), //
						InsertTextMode.AdjustIndentation));

		testCompletionFor(template, //
				InsertTextMode.AdjustIndentation, //
				InsertTextMode.AdjustIndentation, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 1), //
						null));

		testCompletionFor(template, //
				InsertTextMode.AsIs, //
				InsertTextMode.AdjustIndentation, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 1), //
						InsertTextMode.AdjustIndentation));
	}

	private static void testCompletionFor(String value, InsertTextMode defaultInsertTextMode,
			InsertTextMode insertTextMode, CompletionItem... expectedItems) throws Exception {

		QuteCompletionSettings completionSettings = new QuteCompletionSettings();
		CompletionItemCapabilities completionItemCapabilities = new CompletionItemCapabilities();
		completionItemCapabilities.setSnippetSupport(true);
		CompletionItemInsertTextModeSupportCapabilities insertTextModeSupport = new CompletionItemInsertTextModeSupportCapabilities();
		insertTextModeSupport.setValueSet(Arrays.asList(insertTextMode));
		completionItemCapabilities.setInsertTextModeSupport(insertTextModeSupport);
		CompletionCapabilities completionCapabilities = new CompletionCapabilities(completionItemCapabilities);
		completionCapabilities.setInsertTextMode(defaultInsertTextMode);
		completionSettings.setCapabilities(completionCapabilities);

		QuteAssert.testCompletionFor(value, "test.qute", null, QuteQuickStartProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, null, null, completionSettings, false, expectedItems);
	}

	public static CompletionItem c(String label, String newText, Range range, InsertTextMode insertTextMode) {
		CompletionItem item = QuteAssert.c(label, newText, range, null);
		item.setInsertTextMode(insertTextMode);
		return item;
	}

}