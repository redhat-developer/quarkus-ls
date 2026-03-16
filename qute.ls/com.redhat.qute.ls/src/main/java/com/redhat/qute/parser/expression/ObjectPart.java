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

import com.redhat.qute.parser.NodeBase;
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
import com.redhat.qute.project.usages.IncludeUsages;

/**
 * Represents an object part in a Qute expression.
 *
 * <p>
 * An object part is the leftmost part of an expression, for example:
 *
 * <pre>
 * {item}
 * {item.name}
 * {data:item}
 * </pre>
 *
 * It is responsible for resolving the Java type of the object it refers to by
 * walking up the section hierarchy and consulting various sources: loop
 * aliases, let/set bindings, the initial data model, global variables, and
 * user tag or include call parameters.
 * </p>
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
					// Skip the leading '!' in negated expressions, e.g.:
					// {#if !active} → startName points to 'a' in 'active'
					startName++;
				}
			}
		}
		return startName;
	}

	/**
	 * Resolves the Java type of this object part.
	 *
	 * <p>
	 * Resolution is attempted in the following order:
	 * <ol>
	 * <li>Namespace expressions (e.g. {@code {data:item}})</li>
	 * <li>Loop aliases from enclosing {@code #for} / {@code #each} sections</li>
	 * <li>Variable bindings from enclosing {@code #let} / {@code #set} sections</li>
	 * <li>Optional parameters from enclosing {@code #if} sections</li>
	 * <li>Section metadata (e.g. {@code count} in {@code #each})</li>
	 * <li>Initial data model from parameter declarations or {@code @CheckedTemplate}</li>
	 * <li>Global variables</li>
	 * <li>User tag call parameters collected from call sites (if user tag template)</li>
	 * <li>Include call parameters collected from call sites (otherwise)</li>
	 * </ol>
	 * </p>
	 *
	 * @return the resolved {@link JavaTypeInfoProvider}, or {@code null} if none found
	 */
	@Override
	public JavaTypeInfoProvider resolveJavaType() {
		Template template = super.getOwnerTemplate();
		String partName = getPartName();
		if (getNamespace() != null) {
			// Namespace expression, e.g.: {data:item}
			return template.findWithNamespace(this);
		}

		// Walk up the section hierarchy to resolve the type from enclosing sections.
		// Also tracks any optional parameter matched inside an #if block.
		//
		// Examples:
		// - {item} inside {#for item in items} → resolved as the iterable element type
		// - {name} inside {#let name="foo"} → resolved as the let binding
		// - {foo} inside {#if foo??} → resolved as an optional parameter
		Parameter matchedOptionalParameter = null;

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
						// {foo} is inside an #if block that matches {#if foo??}
						matchedOptionalParameter = parameter;
					}
				}
				break;
			}
			default:
			}
			// Check for section metadata, e.g.: {count} inside {#each}
			JavaTypeInfoProvider metadata = section.getMetadata(partName);
			if (metadata != null) {
				return metadata;
			}
			section = section.getParentSection();
		}

		// Try to resolve from the initial data model:
		// - parameter declarations
		// - @CheckedTemplate
		JavaTypeInfoProvider initialDataModel = template.findInInitialDataModel(this);
		if (initialDataModel != null) {
			return initialDataModel;
		}

		// Try to resolve from global variables
		JavaTypeInfoProvider globalVariable = template.findGlobalVariables(this);
		if (globalVariable != null) {
			return globalVariable;
		}

		// Try to resolve from call parameters collected at call sites.
		// A template is either a user tag or an included template — never both.
		if (template.isUserTag()) {
			// e.g.: {#myTag name="foo" /} → {name} inside myTag.html resolves to String
			UserTagUsages userTagUsages = getUserTagUsages(template);
			if (userTagUsages != null) {
				JavaTypeInfoProvider provider = userTagUsages.findTypeProvider(partName);
				if (provider != null) {
					return provider;
				}
			}
		} else {
			// e.g.: {#include myTemplate title="foo" /} → {title} inside myTemplate resolves to String
			IncludeUsages includeUsages = getIncludeUsages(template);
			if (includeUsages != null) {
				JavaTypeInfoProvider provider = includeUsages.findTypeProvider(partName);
				if (provider != null) {
					return provider;
				}
			}
		}

		// No match found anywhere — return the optional parameter matched in an #if
		// block if any, so that {foo} inside {#if foo??} is not reported as an error.
		return matchedOptionalParameter;
	}

	/**
	 * Returns the parameters passed at all call sites of the include section that
	 * references the template owning this part.
	 *
	 * <p>
	 * For example, given:
	 *
	 * <pre>
	 * {#include myTemplate title="foo" /}
	 * </pre>
	 *
	 * calling this method on an object part inside {@code myTemplate} returns the
	 * {@code title} parameter.
	 * </p>
	 *
	 * @return the include call parameters for this part name, or an empty list
	 */
	public List<? extends NodeBase<?>> getIncludeCallParameters() {
		IncludeUsages usages = getIncludeUsages(getOwnerTemplate());
		if (usages != null) {
			return usages.findParameters(getPartName());
		}
		return Collections.emptyList();
	}

	/**
	 * Returns the {@link IncludeUsages} for the template that owns this part.
	 *
	 * <p>
	 * Include parameters (e.g. {@code title} in
	 * {@code {#include myTemplate title="foo" /}}) are resolved from the call sites
	 * collected by {@link com.redhat.qute.project.usages.UsagesCollector}. This
	 * method retrieves those usages so that type inference can determine the Java
	 * type of an object part inside an included template.
	 * </p>
	 *
	 * <p>
	 * Returns {@code null} if no project is available.
	 * </p>
	 *
	 * @param template the template owning this object part
	 * @return the usages for the included template, or {@code null}
	 */
	private static IncludeUsages getIncludeUsages(Template template) {
		QuteProject project = template.getProject();
		if (project != null) {
			return project.getIncludeUsagesRegistry().getUsages(template.getTemplateId());
		}
		return null;
	}

	/**
	 * Returns the parameters passed at all call sites of the user tag section that
	 * references the template owning this part.
	 *
	 * <p>
	 * For example, given:
	 *
	 * <pre>
	 * {#myTag name="foo" count=10 /}
	 * </pre>
	 *
	 * calling this method on an object part inside {@code myTag.html} returns the
	 * {@code name} and {@code count} parameters.
	 * </p>
	 *
	 * @return the user tag call parameters for this part name, or an empty list
	 */
	public List<? extends NodeBase<?>> getUserTagCallParameters() {
		UserTagUsages usages = getUserTagUsages(getOwnerTemplate());
		if (usages != null) {
			return usages.findParameters(getPartName());
		}
		return Collections.emptyList();
	}

	/**
	 * Returns the {@link UserTagUsages} for the user tag template that owns this
	 * part.
	 *
	 * <p>
	 * User tag parameters (e.g. {@code name}, {@code count} in
	 * {@code {#myTag name="foo" count=10 /}}) are resolved from the call sites
	 * collected by {@link com.redhat.qute.project.usages.UsagesCollector}. This
	 * method retrieves those usages so that type inference can determine the Java
	 * type of an object part inside a user tag template.
	 * </p>
	 *
	 * <p>
	 * Returns {@code null} if the template is not a user tag, or if no project is
	 * available.
	 * </p>
	 *
	 * @param template the template owning this object part
	 * @return the usages for the user tag, or {@code null}
	 */
	private static UserTagUsages getUserTagUsages(Template template) {
		if (template.isUserTag()) {
			QuteProject project = template.getProject();
			if (project != null) {
				String userTagName = template.getUserTagName();
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