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
package org.eclipse.lsp4mp.jdt.internal.restclient.java;

import org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.InsertAnnotationMissingQuickFix;
import org.eclipse.lsp4mp.jdt.internal.restclient.MicroProfileRestClientConstants;
import org.eclipse.lsp4mp.jdt.internal.restclient.MicroProfileRestClientErrorCode;

/**
 * QuickFix for fixing
 * {@link MicroProfileRestClientErrorCode#InjectAndRestClientAnnotationMissing}
 * error by providing several code actions:
 * 
 * <ul>
 * <li>Insert @Inject and @RestClient annotation and the proper import.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class InjectAndRestClientAnnotationMissingQuickFix extends InsertAnnotationMissingQuickFix {

	public InjectAndRestClientAnnotationMissingQuickFix() {
		super(true, MicroProfileConfigConstants.INJECT_ANNOTATION,
				MicroProfileRestClientConstants.REST_CLIENT_ANNOTATION);
	}

}
