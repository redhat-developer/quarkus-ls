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
package org.eclipse.lsp4mp.jdt.internal.health.java;

import org.eclipse.lsp4mp.jdt.core.java.codeaction.InsertAnnotationMissingQuickFix;
import org.eclipse.lsp4mp.jdt.internal.health.MicroProfileHealthConstants;

/**
 * QuickFix for fixing
 * {@link MicroProfileHealthErrorCode#HealthAnnotationMissing} error by
 * providing several code actions:
 * 
 * <ul>
 * <li>Insert @Liveness annotation and the proper import.</li>
 * <li>Insert @Readiness annotation and the proper import.</li>
 * <li>Insert @Health annotation and the proper import.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class HealthAnnotationMissingQuickFix extends InsertAnnotationMissingQuickFix {

	public HealthAnnotationMissingQuickFix() {
		super(MicroProfileHealthConstants.LIVENESS_ANNOTATION, MicroProfileHealthConstants.READINESS_ANNOTATION,
				MicroProfileHealthConstants.HEALTH_ANNOTATION);
	}
}
