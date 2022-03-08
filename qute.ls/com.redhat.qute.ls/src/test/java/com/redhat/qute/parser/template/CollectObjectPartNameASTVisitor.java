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
package com.redhat.qute.parser.template;

import java.util.HashSet;
import java.util.Set;

import com.redhat.qute.parser.expression.ObjectPart;

/**
 * Collect object part names.
 * 
 * @author Angelo ZERR
 *
 */
public class CollectObjectPartNameASTVisitor extends ASTVisitor {

	private final Set<String> objectPartNames;

	public CollectObjectPartNameASTVisitor() {
		this.objectPartNames = new HashSet<>();
	}

	@Override
	public boolean visit(ObjectPart node) {
		objectPartNames.add(node.getPartName());
		return true;
	}

	public Set<String> getObjectPartNames() {
		return objectPartNames;
	}
}
