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

		// Now we have 3 children: YAML front matter, newline content, and LET section
		assertEquals(3, template.getChildCount());

		// Verify YAML front matter
		// --- (0-3) + \r\nlayout: page\r\npaginate: true\r\ntagging: posts\r\n (3-51) + --- (51-54)
		Node yamlNode = template.getChild(0);
		assertEquals(NodeKind.LanguageInjection, yamlNode.getKind());
		LanguageInjectionNode yaml = (LanguageInjectionNode) yamlNode;
		assertEquals("yaml-frontmatter", yaml.getLanguageId());
		assertEquals(0, yaml.getStart());
		assertEquals(54, yaml.getEnd());

		// Verify newline content after YAML: \r\n (54-56)
		Node contentNode = template.getChild(1);
		assertEquals(NodeKind.Text, contentNode.getKind());
		assertEquals(54, contentNode.getStart());
		assertEquals(56, contentNode.getEnd());

		// Verify LET section: {#let name=value} starts at 56
		Node letNode = template.getChild(2);
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
