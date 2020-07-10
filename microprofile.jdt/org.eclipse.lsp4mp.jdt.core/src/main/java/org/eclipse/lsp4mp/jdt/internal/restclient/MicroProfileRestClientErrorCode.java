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
package org.eclipse.lsp4mp.jdt.internal.restclient;

import org.eclipse.lsp4mp.jdt.core.java.diagnostics.IJavaErrorCode;

/**
 * MicroProfile RestClient diagnostics error code.
 * 
 * @author Angelo ZERR
 *
 */
public enum MicroProfileRestClientErrorCode implements IJavaErrorCode {

	RegisterRestClientAnnotationMissing, InjectAnnotationMissing, RestClientAnnotationMissing, InjectAndRestClientAnnotationMissing;

	@Override
	public String getCode() {
		return name();
	}

}
