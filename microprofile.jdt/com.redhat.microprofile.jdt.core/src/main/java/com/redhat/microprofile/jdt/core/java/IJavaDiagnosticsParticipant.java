/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.java;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;

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
	 * @throws JavaModelException
	 */
	default boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws JavaModelException {
		return true;
	}

	/**
	 * Collect diagnostics according to the context.
	 * 
	 * @param context the java diagnostics context
	 * @param monitor the progress monitor
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	void collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws JavaModelException, CoreException;

}
