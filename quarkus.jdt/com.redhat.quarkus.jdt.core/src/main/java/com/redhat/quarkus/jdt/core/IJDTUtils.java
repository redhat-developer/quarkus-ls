/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.lsp4j.Range;

/**
 * JDT LS utils provides some helpful utilities. To avoid having a strong
 * dependencies to JDT-LS, we use this API. This API gives the capability to
 * consume Quarkus manager without having JDT LS.
 * 
 * @author Angelo ZERR
 *
 */
public interface IJDTUtils {

	/**
	 * Given the uri returns a {@link ICompilationUnit}. May return null if it can
	 * not associate the uri with a Java file.
	 *
	 * @param uriString
	 * @return compilation unit
	 */
	ICompilationUnit resolveCompilationUnit(String uriString);

	/**
	 * Given the uri returns a {@link IClassFile}. May return null if it can not
	 * resolve the uri to a library.
	 *
	 * @param uri with 'jdt' scheme
	 * @return class file
	 */
	IClassFile resolveClassFile(String uri);

	boolean isHiddenGeneratedElement(IJavaElement element);

	/**
	 * Creates a range for the given offset and length for an {@link IOpenable}
	 *
	 * @param openable
	 * @param offset
	 * @param length
	 * @return
	 * @throws JavaModelException
	 */
	Range toRange(IOpenable openable, int offset, int length) throws JavaModelException;

	/**
	 * Format URIs to be consumed by clients. On Windows platforms, UNC (Universal
	 * Naming Convention) URIs are transformed to follow the <code>file://</code>
	 * pattern.
	 *
	 * @param uri the String URI to transform.
	 * @return a String URI compatible with clients.
	 */
	String toClientUri(String uri);

	String toUri(ITypeRoot typeRoot);

	void waitForLifecycleJobs(IProgressMonitor monitor);

	int toOffset(IBuffer buffer, int line, int column);
}