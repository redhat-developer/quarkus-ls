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
package com.redhat.qute.parser.template.sections;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.project.QuteProject;

/**
 * Include section.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#include_helpers
 */
public class IncludeSection extends Section {

	public static final String TAG = "include";

	public IncludeSection(int start, int end) {
		super(TAG, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.INCLUDE;
	}

	/**
	 * Returns the referenced template file defined in the first parameter of the
	 * section and null otherwise.
	 * 
	 * @return the referenced template file defined in the first parameter of the
	 *         section and null otherwise.
	 */
	public Path getReferencedTemplateFile() {
		String referencedTemplateId = getReferencedTemplateId();
		if (referencedTemplateId == null) {
			return null;
		}

		QuteProject project = getOwnerTemplate().getProject();
		if (project == null) {
			return null;
		}

		Path templateBaseDir = project.getTemplateBaseDir();
		if (templateBaseDir == null) {
			return null;
		}

		for (String suffix : project.getTemplateVariants()) {
			Path referencedTemplateFile = templateBaseDir.resolve(referencedTemplateId + suffix);
			if (Files.exists(referencedTemplateFile)) {
				// The template file exists
				return referencedTemplateFile;
			}
		}
		// The template file doesn't exists, we return a file to create it if user wants
		// to do that (available when Ctrl+Click is processed).
		return templateBaseDir.resolve(referencedTemplateId + ".html");
	}

	/**
	 * Returns the template id defined in parameter template of the include section
	 * and null otherwise.
	 * 
	 * @return the template id defined in parameter template of the include section
	 *         and null otherwise.
	 */
	public String getReferencedTemplateId() {
		Parameter templateParameter = getTemplateParameter();
		if (templateParameter == null) {
			return null;
		}
		return templateParameter.getValue();
	}

	/**
	 * Returns the template parameter of the include section and null otherwise.
	 * 
	 * @return the template parameter of the include section and null otherwise.
	 */
	public Parameter getTemplateParameter() {
		return super.getParameterAtIndex(0);
	}

	@Override
	protected void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			List<Parameter> parameters = getParameters();
			for (Parameter parameter : parameters) {
				acceptChild(visitor, parameter);
			}
			acceptChildren(visitor, getChildren());
		}
		visitor.endVisit(this);
	}

}
