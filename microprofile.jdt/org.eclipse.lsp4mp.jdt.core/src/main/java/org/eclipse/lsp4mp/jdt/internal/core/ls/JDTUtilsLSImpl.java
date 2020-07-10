/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.core.ls;

import java.io.Reader;
import java.util.Optional;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.handlers.DocumentLifeCycleHandler;
import org.eclipse.jdt.ls.core.internal.handlers.JsonRpcHelpers;
import org.eclipse.jdt.ls.core.internal.javadoc.JavadocContentAccess;
import org.eclipse.jdt.ls.core.internal.managers.IBuildSupport;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.core.FakeJavaProject;

/**
 * {@link IJDTUtils} implementation with JDT S {@link JDTUtils}.
 * 
 * @author Angelo ZERR
 *
 */
public class JDTUtilsLSImpl implements IJDTUtils {

	private static final IJDTUtils INSTANCE = new JDTUtilsLSImpl();

	public static IJDTUtils getInstance() {
		return INSTANCE;
	}

	private JDTUtilsLSImpl() {
	}

	@Override
	public IFile findFile(String uriString) {
		return JDTUtils.findFile(uriString);
	}

	@Override
	public ICompilationUnit resolveCompilationUnit(String uriString) {
		return JDTUtils.resolveCompilationUnit(uriString);
	}

	@Override
	public IClassFile resolveClassFile(String uriString) {
		return JDTUtils.resolveClassFile(uriString);
	}

	@Override
	public boolean isHiddenGeneratedElement(IJavaElement element) {
		return JDTUtils.isHiddenGeneratedElement(element);
	}

	@Override
	public Range toRange(IOpenable openable, int offset, int length) throws JavaModelException {
		return JDTUtils.toRange(openable, offset, length);
	}

	@Override
	public String toClientUri(String uri) {
		return ResourceUtils.toClientUri(uri);
	}

	@Override
	public String toUri(ITypeRoot typeRoot) {
		return JDTUtils.toUri(typeRoot);
	}

	@Override
	public void waitForLifecycleJobs(IProgressMonitor monitor) {
		try {
			Job.getJobManager().join(DocumentLifeCycleHandler.DOCUMENT_LIFE_CYCLE_JOBS, monitor);
		} catch (OperationCanceledException ignorable) {
			// No need to pollute logs when query is cancelled
		} catch (Exception e) {
			JavaLanguageServerPlugin.logException(e.getMessage(), e);
		}
	}

	@Override
	public int toOffset(IBuffer buffer, int line, int column) {
		return JsonRpcHelpers.toOffset(buffer, line, column);
	}

	@Override
	public Location toLocation(IJavaElement element) throws JavaModelException {
		return JDTUtils.toLocation(element);
	}

	@Override
	public void discoverSource(IClassFile classFile, IProgressMonitor progress) throws CoreException {
		IJavaProject javaProject = FakeJavaProject.getRealJavaProject(classFile.getJavaProject());
		// Try to download source if required
		Optional<IBuildSupport> bs = JavaLanguageServerPlugin.getProjectsManager()
				.getBuildSupport(javaProject.getProject());
		if (bs.isPresent()) {
			bs.get().discoverSource(classFile, progress);
		}
	}

	@Override
	public String getJavadoc(IMember member, DocumentFormat documentFormat) throws JavaModelException {
		boolean markdown = DocumentFormat.Markdown.equals(documentFormat);
		Reader reader = markdown ? JavadocContentAccess.getMarkdownContentReader(member)
				: JavadocContentAccess.getPlainTextContentReader(member);
		return reader != null ? toString(reader) : null;
	}

	private static String toString(Reader reader) {
		try (Scanner s = new java.util.Scanner(reader)) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}
}