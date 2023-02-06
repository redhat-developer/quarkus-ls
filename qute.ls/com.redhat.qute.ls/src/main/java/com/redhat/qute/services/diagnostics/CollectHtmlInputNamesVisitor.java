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
package com.redhat.qute.services.diagnostics;

import java.util.ArrayList;
import java.util.List;

import com.redhat.qute.parser.html.scanner.HtmlScanner;
import com.redhat.qute.parser.html.scanner.TokenType;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Text;

/**
 * Collect input section names.
 *
 */
public class CollectHtmlInputNamesVisitor extends ASTVisitor {

	private static final String INPUT_ELEMENT = "input";

	private static final String NAME_ATTR = "name";

	private final List<String> htmlInputNames;

	public CollectHtmlInputNamesVisitor() {
		this.htmlInputNames = new ArrayList<String>();

	}

	public List<String> getHtmlInputNames() {
		return htmlInputNames;
	}

	@Override
	public boolean visit(Text text) {
		// scan with html scanner
		String templateText = text.getOwnerTemplate().getText();
		HtmlScanner scanner = HtmlScanner
				.createScanner(templateText, text.getStart(), text.getEnd());
		TokenType token = scanner.scan();
		String currentAttrName = null;
		String currentElementName = null;
		while (token != TokenType.EOS) {
			switch (token) {
				case ElementName:
					currentElementName = scanner.getTokenText();
					break;
				case AttributeName:
					if (currentElementName != null && INPUT_ELEMENT.equals(currentElementName)) {
						currentAttrName = scanner.getTokenText();
					}
					break;
				case AttributeValue:
					if (currentAttrName != null && NAME_ATTR.equals(currentAttrName)) {
						StringBuilder inputName = new StringBuilder();
						for (int i = scanner.getTokenOffset(); i < scanner.getTokenEnd(); i++) {
							char c = templateText.charAt(i);
							if (c != '\'' && c != '"') {
								inputName.append(c);
							}
						}
						htmlInputNames.add(inputName.toString());
					}
					break;
				default:
			}
			token = scanner.scan();
		}
		return true;
	}
}
