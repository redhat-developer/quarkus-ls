/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser;

import java.util.concurrent.CancellationException;

/**
 * Used for processing requests with cancellation support.
 */
public interface CancelChecker {

	/**
	 * Throw a {@link CancellationException} if the currently processed request
	 * has been canceled.
	 */
	void checkCanceled();

	/**
	 * Check for cancellation without throwing an exception.
	 */
	default boolean isCanceled() {
		try {
			checkCanceled();
		} catch (CancellationException ce) {
			return true;
		}
		return false;
	}

}
