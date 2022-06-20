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
package com.redhat.qute.services.snippets;

import com.redhat.qute.ls.commons.snippets.AbstractSnippetContext;
import com.redhat.qute.services.completions.CompletionRequest;

/**
 * Abstract Qute snippet context.
 *
 * @param <T> the value type waited by the snippet context.
 */
public abstract class AbstractQuteSnippetContext extends AbstractSnippetContext<CompletionRequest>
		implements IQuteSnippetContext {

}
