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
package com.redhat.qute.parser.injection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;

/**
 * Test with template parser which builds a Template AST.
 * 
 * @author Angelo ZERR
 *
 */
public class YamlFrontMatterParserTest {

	@Test
	public void letWithYamlFrontMatter() {
		String content = "---\r\n" + //
				"layout: page\r\n" + //
				"paginate: true\r\n" + //
				"tagging: posts\r\n" + //
				"---\r\n" + //
				"{#let name=value}\r\n" + //
				"    \r\n" + //
				"{/let}";

		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		Template template = TemplateParser.parse(content, "test.qute", injectors);

		assertEquals(2, template.getChildCount());

		// Verify YAML front matter
		Node yamlNode = template.getChild(0);
		assertEquals(NodeKind.LanguageInjection, yamlNode.getKind());
		LanguageInjectionNode yaml = (LanguageInjectionNode) yamlNode;
		assertEquals("yaml-frontmatter", yaml.getLanguageId());
		assertEquals(0, yaml.getStart());
		assertEquals(56, yaml.getEnd()); // Total front matter length

		// Verify LET section
		Node letNode = template.getChild(1);
		assertEquals(NodeKind.Section, letNode.getKind());
		Section section = (Section) letNode;
		assertEquals(SectionKind.LET, section.getSectionKind());
		assertTrue(section.isClosed());

		assertEquals(56, section.getStartTagOpenOffset());
		assertEquals(72, section.getStartTagCloseOffset());
		assertEquals(81, section.getEndTagOpenOffset());
		assertEquals(86, section.getEndTagCloseOffset());
	}
}
