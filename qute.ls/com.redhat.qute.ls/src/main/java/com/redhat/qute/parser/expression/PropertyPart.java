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
package com.redhat.qute.parser.expression;

import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;

/**
 * Property part.
 * 
 * <p>
 * {item.name}
 * </p>
 *
 * <p>
 * {item['name']}
 * </p>
 * 
 * @author Angelo ZERR
 *
 */

public class PropertyPart extends MemberPart {

	private Boolean bracketNotationComputed;

	public PropertyPart(int start, int end) {
		super(start, end);
	}

	@Override
	public PartKind getPartKind() {
		return PartKind.Property;
	}

	@Override
	public JavaTypeInfoProvider resolveJavaType() {
		return null;
	}

	@Override
	public int getStartName() {
		computeBracketNotationIfNeeded();
		return super.getStartName();
	}

	@Override
	public int getEndName() {
		computeBracketNotationIfNeeded();
		return super.getEndName();
	}

	private void computeBracketNotationIfNeeded() {
		if (bracketNotationComputed != null) {
			return;
		}
		computeBracketNotation();
	}

	/**
	 * Remove the bracket notation ['name'] --> name.
	 */
	private synchronized void computeBracketNotation() {
		if (bracketNotationComputed != null) {
			return;
		}
		String text = getOwnerTemplate().getText();
		int start = super.getStartName();
		int end = super.getEndName() - 1;
		if (text.charAt(start) == '[') {
			// ex : item['name'], list[0]
			// ['name'] --> name']
			char next = text.length() > start + 1 ? text.charAt(start + 1) : (char) -1;
			if (next == '\'' || next == '"') {
				// ['name']
				super.setStart(start + 2);
			} else {
				// [0]
				super.setStart(start + 1);
			}
		}
		if (text.charAt(end) == ']') {
			// ex : item['name'], list[0]
			// name'] --> name
			char previous = text.charAt(end - 1);
			if (previous == '\'' || previous == '"') {
				// ['name']
				super.setEnd(end - 1);
			} else {
				// [0]
				super.setEnd(end);
			}
		}
		bracketNotationComputed = true;
	}

	@Override
	protected boolean canBeOptional() {
		return true;
	}

	@Override
	protected void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
}
