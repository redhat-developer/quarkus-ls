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
package com.redhat.qute.parser.yaml.scanner;

/**
 * YAML scanner states.
 * Represents the current scanning context.
 */
public enum YamlScannerState {
	    WithinContent,
	    AfterKey,
	    AfterColon,
	    AfterDash,
	    WithinValue,
	    WithinString,
	    WithinComment,
	    WithinFlowSequence,    // Inside [...]
	    WithinFlowMapping;     // Inside {...}
}
