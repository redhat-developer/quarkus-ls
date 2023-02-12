/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.tags;

import java.util.ArrayList;
import java.util.List;

import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Parameter;

/**
 * Collect object part names.
 * 
 * @author Angelo ZERR
 *
 */
public class ObjectPartCollector extends ASTVisitor {

	private final String name;

	private final List<ObjectPart> objectParts;

	public ObjectPartCollector(String name) {
		this.name = name;
		this.objectParts = new ArrayList<>();
	}

	@Override
	public boolean visit(MethodPart node) {
		for (Parameter parameter : node.getParameters()) {
			Expression expression = parameter.getJavaTypeExpression();
			if (expression != null) {
				expression.accept(this);
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ObjectPart node) {
		if (name.equals(node.getPartName())) {
			objectParts.add(node);
		}
		return true;
	}

	public List<ObjectPart> getObjectParts() {
		return objectParts;
	}
}
