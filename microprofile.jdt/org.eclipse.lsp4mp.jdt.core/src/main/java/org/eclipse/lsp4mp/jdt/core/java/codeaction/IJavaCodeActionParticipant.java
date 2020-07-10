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
package org.eclipse.lsp4mp.jdt.core.java.codeaction;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

/**
 * Java codeAction participants API.
 * 
 * @author Angelo ZERR
 *
 */
public interface IJavaCodeActionParticipant {
	
	/**
	 * Returns true if the code actions are adaptable for the given context and false
	 * otherwise.
	 * 
	 * <p>
	 * Participants can override this to check if some classes are on the classpath 
	 * before deciding to process the code actions.
	 * </p>
	 * 
	 * @param context java code action context
	 * @param monitor the progress monitor
	 * @return true if adaptable and false
	 *         otherwise.
	 * 
	 */
	default boolean isAdaptedForCodeAction(JavaCodeActionContext context, IProgressMonitor monitor)
			throws CoreException {
		return true;
	}

	/**
	 * Return the code action list for a given compilation unit and null otherwise.
	 * 
	 * @param context    the java code action context.
	 * @param diagnostic the diagnostic which must be fixed and null otherwise.
	 * @param monitor    the progress monitor
	 * @return the code action list for a given compilation unit and null otherwise.
	 * @throws CoreException
	 */
	List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
			IProgressMonitor monitor) throws CoreException;
}
