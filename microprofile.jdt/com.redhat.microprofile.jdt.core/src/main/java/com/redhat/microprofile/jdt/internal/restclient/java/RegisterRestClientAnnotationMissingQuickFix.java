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
package com.redhat.microprofile.jdt.internal.restclient.java;

import com.redhat.microprofile.jdt.core.java.codeaction.InsertAnnotationMissingQuickFix;
import com.redhat.microprofile.jdt.internal.restclient.MicroProfileRestClientConstants;
import com.redhat.microprofile.jdt.internal.restclient.MicroProfileRestClientErrorCode;

/**
 * QuickFix for fixing
 * {@link MicroProfileRestClientErrorCode#RegisterRestClientAnnotationMissing}
 * error by providing several code actions:
 * 
 * <ul>
 * <li>Insert @RegisterRestClient annotation and the proper import.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class RegisterRestClientAnnotationMissingQuickFix extends InsertAnnotationMissingQuickFix {

	public RegisterRestClientAnnotationMissingQuickFix() {
		super(true, MicroProfileRestClientConstants.REGISTER_REST_CLIENT_ANNOTATION);
	}

}
