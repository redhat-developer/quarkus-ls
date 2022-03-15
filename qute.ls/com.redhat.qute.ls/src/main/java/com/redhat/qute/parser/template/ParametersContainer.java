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
package com.redhat.qute.parser.template;

import com.redhat.qute.parser.CancelChecker;

/**
 * Container for parameters (ex :
 * 
 * <p>
 * Given the following section:
 * </p>
 * 
 * <p>
 * {#let a=b c=d })
 * </p>
 * 
 * <p>
 * the parameters container is
 * </p>
 * 
 * <p>
 * a=b c=d
 * </p>
 * 
 * @author ANgelo ZERR
 *
 */
public interface ParametersContainer {

	/**
	 * Returns the start offset of the parameters container.
	 * 
	 * <p>
	 * {#let |a=b c=d })
	 * </p>
	 * 
	 * @return the start offset of the parameters container.
	 */
	int getStartParametersOffset();

	/**
	 * Returns the end offset of the parameters container.
	 * 
	 * <p>
	 * {#let a=b c=d| })
	 * </p>
	 * 
	 * @return the end offset of the parameters container.
	 */
	int getEndParametersOffset();

	/**
	 * Returns the template content.
	 * 
	 * @return the template content.
	 */
	String getTemplateContent();

	/**
	 * Returns the cancel checker.
	 * 
	 * @return the cancel checker.
	 */
	CancelChecker getCancelChecker();

}
