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

import java.util.Collections;
import java.util.List;

import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.LoopSection;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.tags.UserTagUsages;
import com.redhat.qute.utils.UserTagUtils;

/**
 * Object part.
 * 
 * <p>
 * {item}
 * </p>
 * 
 * @author Angelo ZERR
 *
 */

public class ObjectPart extends Part {

	private int startName = -1;

	public ObjectPart(int start, int end) {
		super(start, end);
	}

	@Override
	public PartKind getPartKind() {
		return PartKind.Object;
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
					// ex : !true
					// !true --> true
					startName++;
				}
			}
		}
		return startName;
	}

	@Override
	public JavaTypeInfoProvider resolveJavaType() {
		Template template = super.getOwnerTemplate();
		String partName = getPartName();
		boolean hasNamespace = getNamespace() != null;
		if (hasNamespace) {
			// ex : {data:item}
			return template.findWithNamespace(this);
		}

		// ex :
		// - {item}
		// - {item??}
		Parameter matchedOptionalParameter = null;

		// Loop for parent section to discover the class name
		Section section = super.getParentSection();
		while (section != null) {
			switch (section.getSectionKind()) {
			case EACH:
			case FOR:
				LoopSection iterableSection = (LoopSection) section;
				if (!iterableSection.isInElseBlock(getStart())) {
					String alias = iterableSection.getAlias();
					if (partName.equals(alias)) {
						return iterableSection.getIterableParameter();
					}
				}
				break;
			case LET:
			case SET: {
				Parameter parameter = section.findParameter(partName);
				if (parameter != null) {
					return parameter;
				}
				break;
			}
			case IF: {
				if (matchedOptionalParameter == null) {
					Parameter parameter = section.findParameter(partName);
					if (parameter != null && parameter.isOptional()) {
						// here {foo} is inside an #if block which matches {#if foo?? }
						matchedOptionalParameter = parameter;
					}
				}
				break;
			}
			default:
			}
			// ex : count for #each
			JavaTypeInfoProvider metadata = section.getMetadata(partName);
			if (metadata != null) {
				return metadata;
			}
			section = section.getParentSection();
		}
		// Try to find the class name
		// - from parameter declaration
		// - from @CheckedTemplate
		JavaTypeInfoProvider initialDataModel = template.findInInitialDataModel(this);
		if (initialDataModel != null) {
			return initialDataModel;
		}

		// Try to find global variables
		JavaTypeInfoProvider globalVariable = template.findGlobalVariables(this);
		if (globalVariable != null) {
			return globalVariable;
		}

		UserTagUsages call = getUsages(template);
		if (call != null) {
			JavaTypeInfoProvider provider = call.getTypeProvider(partName);
			if (provider != null) {
				return provider;
			}
		}

		// There are no parameter which matches the object part name from #set, #let,
		// #for
		// and there are no initial data model which matches the object part name.
		// We return the matched optional parameter inside the #if section {#if foo?? }
		// if it is not null.
		return matchedOptionalParameter;
	}

	public List<Parameter> getCallParams() {
		UserTagUsages usages = getUsages(getOwnerTemplate());
		if (usages != null) {
			return usages.getParameters(getPartName());
		}
		return Collections.emptyList();
	}
	
	private static UserTagUsages getUsages(Template template) {
		if (UserTagUtils.isUserTag(template)) {
			QuteProject project = template.getProject();
			if (project != null) {
				int index = template.getTemplateId().lastIndexOf('/');
				String userTagName = template.getTemplateId().substring(index + 1, template.getTemplateId().length());
				index = userTagName.lastIndexOf('.');
				if (index != -1) {
					userTagName = userTagName.substring(0, index);
				}
				return project.getTagRegistry().getUsages(userTagName);
			}
		}
		return null;
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
