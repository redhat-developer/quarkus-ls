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
package com.redhat.microprofile.jdt.core.java.diagnostics;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4j.Diagnostic;

/**
 * Java diagnostics participants API.
 * 
 * @author Angelo ZERR
 *
 */
public interface IJavaDiagnosticsParticipant {

	/**
	 * Returns true if diagnostics must be collected for the given context and false
	 * otherwise.
	 * 
	 * <p>
	 * Collection is done by default. Participants can override this to check if
	 * some classes are on the classpath before deciding to process the collection.
	 * </p>
	 * 
	 * @param the     java diagnostics context
	 * @param monitor the progress monitor
	 * @return true if diagnostics must be collected for the given context and false
	 *         otherwise.
	 * 
	 */
	default boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		return true;
	}

	/**
	 * Begin diagnostics collection.
	 * 
	 * @param context the java diagnostics context
	 * @param monitor the progress monitor
	 * 
	 * @throws CoreException
	 */
	default void beginDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {

	}

	/**
	 * Collect diagnostics according to the context.
	 * 
	 * @param context the java diagnostics context
	 * @param monitor the progress monitor
	 * 
	 * @return diagnostics list and null otherwise.
	 * 
	 * @throws CoreException
	 */
	List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException;

	/**
	 * End diagnostics collection.
	 * 
	 * @param context the java diagnostics context
	 * @param monitor the progress monitor
	 * 
	 * @throws CoreException
	 */
	default void endDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {

	}
}
