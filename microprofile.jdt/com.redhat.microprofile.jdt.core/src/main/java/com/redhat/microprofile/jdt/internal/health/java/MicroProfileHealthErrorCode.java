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
package com.redhat.microprofile.jdt.internal.health.java;

import com.redhat.microprofile.jdt.core.java.diagnostics.IJavaErrorCode;

/**
 * MicroProfile Health diagnostics error code.
 * 
 * @author Angelo ZERR
 *
 */
public enum MicroProfileHealthErrorCode implements IJavaErrorCode {

	ImplementHealthCheck, HealthAnnotationMissing;

	@Override
	public String getCode() {
		return name();
	}

}
