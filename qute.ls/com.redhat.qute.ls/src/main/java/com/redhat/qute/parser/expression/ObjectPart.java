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

import java.util.List;

import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.LoopSection;

public class ObjectPart extends Part {

	public ObjectPart(int start, int end) {
		super(start, end);
	}

	@Override
	public PartKind getPartKind() {
		return PartKind.Object;
	}

	public JavaTypeInfoProvider resolveJavaType() {
		Template template = super.getOwnerTemplate();
		String partName = getPartName();
		if (!NamespacePart.DATA_NAMESPACE.equals(getNamespace())) {
			// Loop for parent section to discover the class name
			Node parent = super.getParentSection();
			while (parent != null) {
				if (parent.getKind() == NodeKind.Section) {
					Section section = (Section) parent;
					switch (section.getSectionKind()) {
					case EACH:
					case FOR:
						LoopSection iterableSection = (LoopSection) section;
						String alias = iterableSection.getAlias();
						if (partName.equals(alias)) {
							return iterableSection.getIterableParameter();
						}
						break;
					case LET:
					case SET:
						List<Parameter> parameters = section.getParameters();
						for (Parameter parameter : parameters) {
							if (partName.equals(parameter.getName())) {
								return parameter;
							}
						}
						break;
					default:
					}
					// ex : count for #each
					JavaTypeInfoProvider metadata = section.getMetadata(partName);
					if (metadata != null) {
						return metadata;
					}
				}
				parent = parent.getParent();
			}
		}
		// Try to find the class name
		// - from parameter declaration
		// - from @CheckedTemplate
		return template.findInInitialDataModel(this);
	}

}
