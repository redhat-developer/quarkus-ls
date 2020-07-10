/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls.commons.snippets;

import static org.eclipse.lsp4mp.services.MicroProfileAssert.assertCompletion;
import static org.eclipse.lsp4mp.services.MicroProfileAssert.c;
import static org.eclipse.lsp4mp.services.MicroProfileAssert.r;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import org.eclipse.lsp4mp.ls.commons.snippets.Snippet;
import org.eclipse.lsp4mp.ls.commons.snippets.SnippetRegistry;
import org.eclipse.lsp4mp.ls.commons.snippets.TextDocumentSnippetRegistry;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for snippet registry.
 * 
 * @author Angelo ZERR
 *
 */
public class SnippetRegistryTest {

	@Test
	public void prefixWithString() throws IOException {
		String content = "{\r\n" + //
				"  \"@Metric\": {\r\n" + //
				"    \"prefix\": \"@Metric\",\r\n" + //
				"    \"body\": [\r\n" + //
				"      \"@Metric(\",\r\n" + //
				"      \"\\tname = \\\"${1:name}\\\",\",\r\n" + //
				"      \"\\tdescription = \\\"${2:description}\\\"\",\r\n" + //
				"      \")\"\r\n" + //
				"    ],\r\n" + //
				"    \"description\": \"An annotation that contains the metadata information when requesting a metric to be injected or produced. This annotation can be used on fields of type Meter, Timer, Counter, and Histogram. For Gauge, the @Metric annotation can only be used on producer methods/fields.\"\r\n"
				+ //
				"  }\r\n" + //
				"  }";
		SnippetRegistry registry = new SnippetRegistry();
		registry.registerSnippets(new StringReader(content));
		Assert.assertEquals(1, registry.getSnippets().size());
	}

	@Test
	public void prefixWithArray() throws IOException {
		String content = "{\r\n" + //
				"  \"@Metric\": {\r\n" + //
				"    \"prefix\": [\r\n" + //
				"      \"@Metric\"\r\n" + //
				"    ],\r\n" + //
				"    \"body\": [\r\n" + //
				"      \"@Metric(\",\r\n" + //
				"      \"\\tname = \\\"${1:name}\\\",\",\r\n" + //
				"      \"\\tdescription = \\\"${2:description}\\\"\",\r\n" + //
				"      \")\"\r\n" + //
				"    ],\r\n" + //
				"    \"description\": \"An annotation that contains the metadata information when requesting a metric to be injected or produced. This annotation can be used on fields of type Meter, Timer, Counter, and Histogram. For Gauge, the @Metric annotation can only be used on producer methods/fields.\"\r\n"
				+ //
				"  }\r\n" + //
				"  }";
		SnippetRegistry registry = new SnippetRegistry();
		registry.registerSnippets(new StringReader(content));
		Assert.assertEquals(1, registry.getSnippets().size());
	}

	@Test
	public void applyCompletion() {
		TextDocumentSnippetRegistry registry = new TextDocumentSnippetRegistry();
		Snippet snippet = new Snippet();
		snippet.setPrefixes(Arrays.asList("mp"));
		snippet.setBody(Arrays.asList("a", "\tb", "c"));
		registry.registerSnippet(snippet);

		String ls = System.lineSeparator();
		String expected = "a" + ls + "\tb" + ls + "c";
		assertCompletion("|", 1, registry, c("mp", expected, r(0, 0, 0)));
		assertCompletion(" |", 1, registry, c("mp", expected, r(0, 1, 1)));
	}

	@Test
	public void applyCompletionWithIndent() throws IOException {
		String content = "{\r\n" + //
				"	\"Quarkus - new native test resource class\": {\r\n" + //
				"		\"prefix\": \"qntrc\",\r\n" + //
				"		\"body\": [\r\n" + //
				"			\"package ${1:packagename};\",\r\n" + //
				"			\"\",\r\n" + //
				"			\"import io.quarkus.test.junit.SubstrateTest;\",\r\n" + //
				"			\"\",\r\n" + //
				"			\"@SubstrateTest\",\r\n" + //
				"			\"public class ${TM_FILENAME_BASE} extends ${2:${TM_FILENAME_BASE/^Native(.*)IT/$1/}Test} {\",\r\n"
				+ //
				"			\"\",\r\n" + //
				"			\"\\t// Execute the same tests, but in native mode.\",\r\n" + //
				"			\"}\"\r\n" + //
				"		],\r\n" + //
				"		\"description\": \"Quarkus native test resource class\"\r\n" + //
				"	}\r\n" + //
				"}";
		TextDocumentSnippetRegistry registry = new TextDocumentSnippetRegistry();
		registry.registerSnippets(new StringReader(content));

		String ls = System.lineSeparator();
		String expected = "package ${1:packagename};" + ls + //
				"" + ls + //
				"import io.quarkus.test.junit.SubstrateTest;" + ls + //
				"" + ls + //
				"@SubstrateTest" + ls + //
				"public class ${TM_FILENAME_BASE} extends ${2:${TM_FILENAME_BASE/^Native(.*)IT/$1/}Test} {" + ls + //
				"" + ls + //
				"\t// Execute the same tests, but in native mode." + ls + //
				"}";

		assertCompletion("|", registry, c("qntrc", expected, r(0, 0, 0)));
		assertCompletion(" |", registry, c("qntrc", expected, r(0, 1, 1)));
	}

	@Test
	public void prefixCompletion() {
		TextDocumentSnippetRegistry registry = new TextDocumentSnippetRegistry();
		Snippet snippet = new Snippet();
		snippet.setPrefixes(Arrays.asList("mp"));
		registry.registerSnippet(snippet);
		snippet = new Snippet();
		snippet.setPrefixes(Arrays.asList("quarkus"));
		registry.registerSnippet(snippet);

		assertCompletion("|", registry, c("mp", "", r(0, 0, 0)), c("quarkus", "", r(0, 0, 0)));
		assertCompletion("| ", registry, c("mp", "", r(0, 0, 0)), c("quarkus", "", r(0, 0, 0)));
		assertCompletion(" | ", registry, c("mp", "", r(0, 1, 1)), c("quarkus", "", r(0, 1, 1)));
		assertCompletion(" |", registry);

		assertCompletion("abcd|", registry, c("mp", "", r(0, 0, 4)), c("quarkus", "", r(0, 0, 4)));
		assertCompletion("abcd |", registry, c("mp", "", r(0, 5, 5)), c("quarkus", "", r(0, 5, 5)));

		assertCompletion("m |", registry, c("mp", "", r(0, 2, 2)), c("quarkus", "", r(0, 2, 2)));
		assertCompletion("m|", registry, c("mp", "", r(0, 0, 1)), c("quarkus", "", r(0, 0, 1)));
	}

}