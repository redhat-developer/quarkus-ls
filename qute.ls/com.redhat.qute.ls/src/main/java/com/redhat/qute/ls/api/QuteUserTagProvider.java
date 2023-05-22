/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.ls.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.qute.commons.usertags.QuteUserTagParams;
import com.redhat.qute.commons.usertags.UserTagInfo;

/**
 * Qute user tags provider.
 *
 * @author Angelo ZERR
 *
 */
public interface QuteUserTagProvider {

	@JsonRequest("qute/template/userTags")
	CompletableFuture<List<UserTagInfo>> getUserTags(QuteUserTagParams params);
}
