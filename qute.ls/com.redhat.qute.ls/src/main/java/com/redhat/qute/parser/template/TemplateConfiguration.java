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

import com.redhat.qute.parser.template.sections.LoopSection;

/**
 * Qute template configuration.
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateConfiguration {

	public static final TemplateConfiguration DEFAULT = new TemplateConfiguration();

	public TemplateConfiguration() {
		this.setIterationMetadataPrefix(LoopSection.ITERATION_METADATA_PREFIX_ALIAS_UNDERSCORE);
	}

	private String iterationMetadataPrefix;

	/**
	 * Returns the prefix to use to access the iteration metadata inside a loop
	 * section.
	 * 
	 * @see https://quarkus.io/guides/qute-reference#quarkus-qute_quarkus.qute.iteration-metadata-prefix
	 * 
	 * @return the prefix to use to access the iteration metadata inside a loop
	 *         section.
	 * 
	 */
	public String getIterationMetadataPrefix() {
		return iterationMetadataPrefix;
	}

	/**
	 * Set the prefix to use to access the iteration metadata inside a loop section.
	 * 
	 * @see https://quarkus.io/guides/qute-reference#quarkus-qute_quarkus.qute.iteration-metadata-prefix
	 * 
	 * @param iterationMetadataPrefix the prefix to use to access the iteration
	 *                                metadata inside a loop section.
	 * 
	 */
	public void setIterationMetadataPrefix(String iterationMetadataPrefix) {
		this.iterationMetadataPrefix = iterationMetadataPrefix;
	}

}
