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
package com.redhat.qute.utils;

import java.util.concurrent.CompletableFuture;

/**
 * Future utility.
 */
public class FutureUtils {

	private FutureUtils() {
	}

	public static boolean isFutureLoaded(CompletableFuture<?> future) {
		return future != null && !future.isCancelled() && !future.isCompletedExceptionally();
	}
}
