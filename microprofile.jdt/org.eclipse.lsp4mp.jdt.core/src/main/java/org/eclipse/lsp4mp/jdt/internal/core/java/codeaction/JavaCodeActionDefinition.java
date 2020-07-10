/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.core.java.codeaction;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4mp.jdt.internal.core.java.AbstractJavaFeatureDefinition;

/**
 * Wrapper class around {@link IJavaCodeActionParticipant} participants.
 */
public class JavaCodeActionDefinition extends AbstractJavaFeatureDefinition<IJavaCodeActionParticipant>
		implements IJavaCodeActionParticipant {

	private static final Logger LOGGER = Logger.getLogger(JavaCodeActionDefinition.class.getName());
	private static final String KIND_ATTR = "kind";
	private static final String TARGET_DIAGNOSTIC_ATTR = "targetDiagnostic";

	private final String kind;
	private final String targetDiagnostic;

	public JavaCodeActionDefinition(IConfigurationElement element) {
		super(element);
		this.kind = getKind(element);
		this.targetDiagnostic = element.getAttribute(TARGET_DIAGNOSTIC_ATTR);
	}

	private static String getKind(IConfigurationElement element) throws InvalidRegistryObjectException {
		String kind = element.getAttribute(KIND_ATTR);
		return !StringUtils.isEmpty(kind) ? kind : CodeActionKind.QuickFix;
	}
	
	@Override
	public boolean isAdaptedForCodeAction(JavaCodeActionContext context, IProgressMonitor monitor) {
		try {
			return getParticipant().isAdaptedForCodeAction(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling isAdaptedForCodeAction", e);
			return false;
		}
	}

	@Override
	public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
			IProgressMonitor monitor) {
		try {
			List<? extends CodeAction> codeActions = getParticipant().getCodeActions(context, diagnostic, monitor);
			return codeActions != null ? codeActions : Collections.emptyList();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling getCodeActions", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Returns the code action kind.
	 * 
	 * @return the code action kind.
	 */
	public String getKind() {
		return kind;
	}

	/**
	 * Returns the target diagnostic and null otherwise.
	 * 
	 * @return the target diagnostic and null otherwise.
	 */
	public String getTargetDiagnostic() {
		return targetDiagnostic;
	}

}
