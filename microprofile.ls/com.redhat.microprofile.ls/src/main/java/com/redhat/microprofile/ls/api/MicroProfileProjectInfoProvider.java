/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfileProjectInfoParams;

/**
 * MicroProfile project information provider.
 * 
 * @author Angelo ZERR
 *
 */
public interface MicroProfileProjectInfoProvider {

	/**
	 * Returns the MicroProfile project information for the given
	 * <code>params</code>.
	 * 
	 * @param params the MicroProfile project information parameters
	 * @return the MicroProfile project information for the given
	 *         <code>params</code>.
	 */
	@JsonRequest("microprofile/projectInfo")
	CompletableFuture<MicroProfileProjectInfo> getProjectInfo(MicroProfileProjectInfoParams params);

}
