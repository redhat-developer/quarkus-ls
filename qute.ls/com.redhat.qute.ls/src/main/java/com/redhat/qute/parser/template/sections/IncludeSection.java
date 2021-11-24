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

import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;

/**
 * Include section.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#include_helpers
 */
public class IncludeSection extends Section {

	public static final String TAG = "include";

	private static String[] suffixes = { "", ".html", ".qute.html", ".json", ".qute.json", ".txt", ".qute.txt", ".yaml",
			".qute.yaml" };

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

		Path templateBaseDir = getOwnerTemplate().getTemplateBaseDir();
		if (templateBaseDir == null) {
			return null;
		}
		for (String suffix : suffixes) {
			Path referencedTemplateFile = templateBaseDir.resolve(referencedTemplateId + suffix);
			if (Files.exists(referencedTemplateFile)) {
				// The template file exists
				return referencedTemplateFile;
			}
		}
		// The template file doesn't exists, we return a file to create it if user wants
		// to do that (only available on vscode when Ctrl+Click is processed).
		return templateBaseDir.resolve(referencedTemplateId + ".qute.html");
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

}
