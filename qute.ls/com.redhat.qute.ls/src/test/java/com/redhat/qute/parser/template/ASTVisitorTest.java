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
package com.redhat.qute.parser.template;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.template.sections.LetSection;

/**
 * AST visitor tests
 * 
 * @author Angelo ZERR
 *
 */
public class ASTVisitorTest {

	@Test
	public void visitFromTemplate() {
		String content = "{item.name}\r\n" + //
				"{item}\r\n" + //
				"{foo}\r\n" + //
				"\r\n" + //
				"{#let param1=value param2=root.value }\r\n" + //
				" {bar}\r\n" + //
				"{\\let}  \r\n" + "";
		Template template = TemplateParser.parse(content, "test.qute");

		CollectObjectPartNameASTVisitor visitor = new CollectObjectPartNameASTVisitor();
		template.accept(visitor);

		Assertions.assertArrayEquals(new String[] { "item", "bar", "foo", "root", "value" },
				visitor.getObjectPartNames().toArray());

	}
	
	@Test
	public void visitFromLet() {
		String content = "{item.name}\r\n" + //
				"{item}\r\n" + //
				"{foo}\r\n" + //
				"\r\n" + //
				"{#let param1=value param2=root.value }\r\n" + //
				" {bar}\r\n" + //
				"{\\let}  \r\n" + "";
		Template template = TemplateParser.parse(content, "test.qute");
		LetSection section = (LetSection) template.findNodeAt(33);
		
		
		CollectObjectPartNameASTVisitor visitor = new CollectObjectPartNameASTVisitor();
		section.accept(visitor);

		Assertions.assertArrayEquals(new String[] { "bar", "root", "value" },
				visitor.getObjectPartNames().toArray());

	}

}
