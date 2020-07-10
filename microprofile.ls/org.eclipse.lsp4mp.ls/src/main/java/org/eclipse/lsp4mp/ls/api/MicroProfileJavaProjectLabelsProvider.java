/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4mp.commons.MicroProfileJavaProjectLabelsParams;
import org.eclipse.lsp4mp.commons.ProjectLabelInfoEntry;

/**
 * MicroProfile Java project labels provider.
 * 
 * @author Angelo ZERR
 *
 */
public interface MicroProfileJavaProjectLabelsProvider {

	@JsonRequest("microprofile/java/projectLabels")
	default CompletableFuture<ProjectLabelInfoEntry> getJavaProjectlabels(MicroProfileJavaProjectLabelsParams javaParams) {
		return CompletableFuture.completedFuture(null);
	}

}
