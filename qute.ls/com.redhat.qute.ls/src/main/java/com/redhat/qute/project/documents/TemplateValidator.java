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
package com.redhat.qute.project.documents;

import java.util.Collection;

import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteTextDocument;

/**
 * Template validator API.
 * 
 * @author Angelo ZERR
 *
 */
public interface TemplateValidator {

	/**
	 * Validate the closed/opened Qute document.
	 * 
	 * @param document the closed/opened Qute document.
	 */
	void triggerValidationFor(QuteTextDocument document);

	/**
	 * Clear diagnostics for the given Qute template file uri.
	 * 
	 * @param fileUri the Qute template file uri.
	 */
	void clearDiagnosticsFor(String fileUri);

	/**
	 * Validate all qute templates for the given Qute projects.
	 * 
	 * @param projects Qute projects.
	 */
	void triggerValidationFor(Collection<QuteProject> projects);

}
