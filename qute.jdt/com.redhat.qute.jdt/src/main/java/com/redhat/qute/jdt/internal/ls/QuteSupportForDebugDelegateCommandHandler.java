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
package com.redhat.qute.jdt.internal.ls;

import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getFirst;
import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getString;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.jdt.QuteSupportForDebug;
import com.redhat.qute.jdt.debug.JavaSourceLocationArguments;
import com.redhat.qute.jdt.debug.JavaSourceLocationResponse;

/**
 * JDT LS commands used by Qute debugger.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteSupportForDebugDelegateCommandHandler extends AbstractQuteDelegateCommandHandler {

	private static final String QUTE_DEBUG_RESOLVE_JAVA_SOURCE_COMMAND_ID = "qute/debug/resolveJavaSource";

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		switch (commandId) {
		case QUTE_DEBUG_RESOLVE_JAVA_SOURCE_COMMAND_ID:
			return resolveJavaSource(arguments, commandId, monitor);
		default:
			return null;
		}
	}

	/**
	 * Returns the code lenses for the given Java file.
	 *
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return the code lenses for the given Java file.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private static JavaSourceLocationResponse resolveJavaSource(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		JavaSourceLocationArguments args = creatJavaSourceLocationArguments(arguments, commandId);
		return QuteSupportForDebug.getInstance().resolveJavaSource(args, JDTUtilsLSImpl.getInstance(), monitor);
	}

	/**
	 * Create java code lens parameter from the given arguments map.
	 *
	 * @param arguments
	 * @param commandId
	 *
	 * @return java code lens parameter
	 */
	private static JavaSourceLocationArguments creatJavaSourceLocationArguments(List<Object> arguments,
			String commandId) {
		Map<String, Object> argsObj = getFirst(arguments);
		if (argsObj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one JavaSourceLocationArguments argument!", commandId));
		}
		String typeName = getString(argsObj, "typeName");
		if (typeName == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required JavaSourceLocationEventArguments.typeName!", commandId));
		}
		String method = getString(argsObj, "method");
		String annotation = getString(argsObj, "annotation");
		String javaElementUri = getString(argsObj, "javaElementUri");
		JavaSourceLocationArguments args = new JavaSourceLocationArguments();
		args.setJavaElementUri(javaElementUri);
		args.setTypeName(typeName);
		args.setMethod(method);
		args.setAnnotation(annotation);
		return args;
	}

}
