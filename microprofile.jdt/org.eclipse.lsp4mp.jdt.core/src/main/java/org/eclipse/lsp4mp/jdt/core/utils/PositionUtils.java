/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.core.utils;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Range;

/**
 * Position utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class PositionUtils {

	/**
	 * Returns the LSP range for the given field name.
	 * 
	 * @param field teh java field.
	 * @param utils the JDT utilities.
	 * @return the LSP range for the given field name.
	 * @throws JavaModelException
	 */
	public static Range toNameRange(IField field, IJDTUtils utils) throws JavaModelException {
		IOpenable openable = field.getCompilationUnit();
		ISourceRange sourceRange = field.getNameRange();
		return utils.toRange(openable, sourceRange.getOffset(), sourceRange.getLength());
	}

	public static Range toNameRange(IType type, IJDTUtils utils) throws JavaModelException {
		IOpenable openable = type.getCompilationUnit();
		ISourceRange sourceRange = type.getNameRange();
		return utils.toRange(openable, sourceRange.getOffset(), sourceRange.getLength());
	}
}
