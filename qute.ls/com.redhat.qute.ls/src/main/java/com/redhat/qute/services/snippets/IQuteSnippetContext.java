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
package com.redhat.qute.services.snippets;

import com.redhat.qute.ls.commons.snippets.ISnippetContext;
import com.redhat.qute.services.completions.CompletionRequest;

/**
 * Qute snippet context API.
 *
 */
public interface IQuteSnippetContext extends ISnippetContext<CompletionRequest> {

}
