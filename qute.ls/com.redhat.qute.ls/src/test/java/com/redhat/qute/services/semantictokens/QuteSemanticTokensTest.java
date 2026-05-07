/*******************************************************************************
* Copyright (c) 2025 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.semantictokens;

import static com.redhat.qute.QuteAssert.st;
import static com.redhat.qute.QuteAssert.testSemanticTokensFor;

import org.eclipse.lsp4j.SemanticTokenTypes;
import org.junit.jupiter.api.Test;

/**
 * Tests for Qute semantic tokens.
 *
 * @author Angelo ZERR
 *
 */
public class QuteSemanticTokensTest {

	@Test
	public void testCoreTag_For() throws Exception {
		String template = "{#for item in items}\n" +
				"  {item}\n" +
				"{/for}";
		testSemanticTokensFor(template,
				st(0, 0, 5, SemanticTokenTypes.Keyword, 0),  // {#for
				st(0, 19, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(2, 0, 6, SemanticTokenTypes.Keyword, 0)); // {/for}
	}

	@Test
	public void testCoreTag_If() throws Exception {
		String template = "{#if condition}\n" +
				"  content\n" +
				"{/if}";
		testSemanticTokensFor(template,
				st(0, 0, 4, SemanticTokenTypes.Keyword, 0),  // {#if
				st(0, 14, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(2, 0, 5, SemanticTokenTypes.Keyword, 0)); // {/if}
	}

	@Test
	public void testCoreTag_Let_SelfClosed() throws Exception {
		String template = "{#let name=\"value\" /}";
		testSemanticTokensFor(template,
				st(0, 0, 5, SemanticTokenTypes.Keyword, 0),  // {#let
				st(0, 19, 2, SemanticTokenTypes.Keyword, 0)); // /}
	}

	@Test
	public void testCoreTag_Insert() throws Exception {
		String template = "{#insert content /}";
		testSemanticTokensFor(template,
				st(0, 0, 8, SemanticTokenTypes.Keyword, 0),  // {#insert
				st(0, 17, 2, SemanticTokenTypes.Keyword, 0)); // /}
	}

	@Test
	public void testCoreTag_Multiple() throws Exception {
		String template = "{#for item in items}\n" +
				"  {#if item.active}\n" +
				"    {item.name}\n" +
				"  {/if}\n" +
				"{/for}";
		testSemanticTokensFor(template,
				st(0, 0, 5, SemanticTokenTypes.Keyword, 0),  // {#for
				st(0, 19, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(1, 2, 4, SemanticTokenTypes.Keyword, 0),  // {#if
				st(1, 18, 1, SemanticTokenTypes.Keyword, 0),  // } at position 17, delta from 2 = 15
				st(3, 2, 5, SemanticTokenTypes.Keyword, 0),  // {/if}
				st(4, 0, 6, SemanticTokenTypes.Keyword, 0)); // {/for}
	}

	@Test
	public void testCoreTag_Nested() throws Exception {
		String template = "{#if outer}\n" +
				"  {#for item in items}\n" +
				"    {item}\n" +
				"  {/for}\n" +
				"{/if}";
		testSemanticTokensFor(template,
				st(0, 0, 4, SemanticTokenTypes.Keyword, 0),  // {#if
				st(0, 10, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(1, 2, 5, SemanticTokenTypes.Keyword, 0),  // {#for
				st(1, 21, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(3, 2, 6, SemanticTokenTypes.Keyword, 0),  // {/for}
				st(4, 0, 5, SemanticTokenTypes.Keyword, 0)); // {/if}
	}

	@Test
	public void testEmptyTemplate() throws Exception {
		testSemanticTokensFor("");
	}

	@Test
	public void testNoSemanticTags() throws Exception {
		String template = "{item.name}\n" +
				"{@java.lang.String name}\n" +
				"Plain text";
		testSemanticTokensFor(template);
	}

	@Test
	public void testCoreTag_Each() throws Exception {
		String template = "{#each items}\n" +
				"  {it}\n" +
				"{/each}";
		testSemanticTokensFor(template,
				st(0, 0, 6, SemanticTokenTypes.Keyword, 0),  // {#each
				st(0, 12, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(2, 0, 7, SemanticTokenTypes.Keyword, 0)); // {/each}
	}

	@Test
	public void testCoreTag_Else() throws Exception {
		String template = "{#if condition}\n" +
				"  true\n" +
				"{#else}\n" +
				"  false\n" +
				"{/if}";
		testSemanticTokensFor(template,
				st(0, 0, 4, SemanticTokenTypes.Keyword, 0),  // {#if
				st(0, 14, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(2, 0, 6, SemanticTokenTypes.Keyword, 0),  // {#else
				st(2, 6, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(4, 0, 5, SemanticTokenTypes.Keyword, 0)); // {/if}
	}

	@Test
	public void testCoreTag_Set() throws Exception {
		String template = "{#set myVar=\"test\" /}";
		testSemanticTokensFor(template,
				st(0, 0, 5, SemanticTokenTypes.Keyword, 0),  // {#set
				st(0, 19, 2, SemanticTokenTypes.Keyword, 0)); // /}
	}

	@Test
	public void testCoreTag_With() throws Exception {
		String template = "{#with person}\n" +
				"  {name}\n" +
				"{/with}";
		testSemanticTokensFor(template,
				st(0, 0, 6, SemanticTokenTypes.Keyword, 0),  // {#with
				st(0, 13, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(2, 0, 7, SemanticTokenTypes.Keyword, 0)); // {/with}
	}

	@Test
	public void testCoreTag_Switch() throws Exception {
		String template = "{#switch value}\n" +
				"  {#case 1}one{/case}\n" +
				"  {#case 2}two{/case}\n" +
				"{/switch}";
		testSemanticTokensFor(template,
				st(0, 0, 8, SemanticTokenTypes.Keyword, 0),  // {#switch
				st(0, 14, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(1, 2, 6, SemanticTokenTypes.Keyword, 0),  // {#case
				st(1, 10, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(2, 2, 6, SemanticTokenTypes.Keyword, 0),  // {#case
				st(2, 10, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(3, 0, 9, SemanticTokenTypes.Keyword, 0)); // {/switch}
	}

	@Test
	public void testCoreTag_Fragment() throws Exception {
		String template = "{#fragment id=myFragment}\n" +
				"  content\n" +
				"{/fragment}";
		testSemanticTokensFor(template,
				st(0, 0, 10, SemanticTokenTypes.Keyword, 0),  // {#fragment
				st(0, 24, 1, SemanticTokenTypes.Keyword, 0),  // }
				st(2, 0, 11, SemanticTokenTypes.Keyword, 0)); // {/fragment}
	}

	@Test
	public void testCoreTag_Include() throws Exception {
		String template = "{#include base /}";
		testSemanticTokensFor(template,
				st(0, 0, 9, SemanticTokenTypes.Keyword, 0),  // {#include
				st(0, 15, 2, SemanticTokenTypes.Keyword, 0)); // /}
	}
}
