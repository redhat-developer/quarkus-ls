/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.services;

import static com.redhat.microprofile.services.MicroProfileAssert.r;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileJavaHoverInfo;
import com.redhat.microprofile.utils.DocumentationUtils;

/**
 * ConfigProperty hover tests for Java source file.
 *
 * In these tests, <code>hoverInfoFromJdt</code> represents the hover
 * information that the Quarkus jdt.ls extension would have provided
 */
public class JavaSourceConfigPropertyHoverTest {

	@Test
	public void hoverTestWithValue() {
		MicroProfileJavaHoverInfo hoverInfoFromJdt = new MicroProfileJavaHoverInfo("greeting.message", "hello",
				r(14, 28, 44));
		Hover expectedHover = getExpectedHover("`greeting.message = hello`", hoverInfoFromJdt.getRange());
		Hover actualHover = DocumentationUtils.doHover(hoverInfoFromJdt, true);
		Assert.assertEquals(expectedHover, actualHover);
	};

	@Test
	public void hoverTestNoValue() {
		MicroProfileJavaHoverInfo hoverInfoFromJdt = new MicroProfileJavaHoverInfo("greeting.message", null,
				r(14, 28, 44));
		Hover expectedHover = getExpectedHover("`greeting.message` is not set.", hoverInfoFromJdt.getRange());
		Hover actualHover = DocumentationUtils.doHover(hoverInfoFromJdt, true);
		Assert.assertEquals(expectedHover, actualHover);
	};

	/**
	 * Returns a <code>Hover</code> object created with information that the
	 * MicroProfile jdt.ls extension would provide
	 * 
	 * @param content the <code>Hover</code> documentation content
	 * @return a
	 */
	private Hover getExpectedHover(String content, Range expectedRange) {
		MarkupContent expectedContent = new MarkupContent(MarkupKind.MARKDOWN, content);
		return new Hover(expectedContent, expectedRange);
	}
}
