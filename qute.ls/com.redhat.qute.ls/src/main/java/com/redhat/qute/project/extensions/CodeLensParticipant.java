/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions;

import java.util.List;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.template.Template;
import com.redhat.qute.settings.SharedSettings;

/**
 * Participant for providing codelens in Qute templates.
 * 
 * @author Angelo ZERR
 */

public interface CodeLensParticipant extends BaseParticpant {

	/**
	 * Provides code lenses for the given template.
	 * 
	 * @param template      the template to provide hints for
	 * @param settings      the shared settings.
	 * @param lenses        accumulator for code lenses.
	 * @param cancelChecker used to check if the operation was cancelled
	 */
	void collectCodeLenses(Template template, SharedSettings settings, List<CodeLens> lenses,
			CancelChecker cancelChecker);

}
