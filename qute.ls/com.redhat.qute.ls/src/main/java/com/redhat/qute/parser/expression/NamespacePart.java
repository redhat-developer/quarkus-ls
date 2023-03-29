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
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;

/**
 * Namespace part.
 * 
 * <p>
 * {data:item}
 * </p>
 * 
 * @author Angelo ZERR
 *
 */
public class NamespacePart extends Part {

	public static final String DATA_NAMESPACE = "data";

	private int startName = -1;
	
	public NamespacePart(int start, int end) {
		super(start, end);
	}

	@Override
	public PartKind getPartKind() {
		return PartKind.Namespace;
	}
	
	@Override
	public int getStartName() {
		if (startName != -1) {
			return startName;
		}
		startName = super.getStartName();
		Parameter parameter = getOwnerParameter();
		if (parameter != null) {
			Section section = parameter.getOwnerSection();
			if (section != null && section.getSectionKind() == SectionKind.IF) {
				String text = getOwnerTemplate().getText();
				if (text.charAt(startName) == '!') {
					// ex : !inject:
					// !inject --> inject
					startName++;
				}
			}
		}
		return startName;
	}

	@Override
	protected void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
}
