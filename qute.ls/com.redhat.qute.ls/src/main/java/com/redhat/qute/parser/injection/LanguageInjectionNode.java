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
package com.redhat.qute.parser.injection;

import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.NodeBase;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.extensions.LanguageInjectionService;
import com.redhat.qute.project.extensions.LanguageInjectionServiceRegistry;

public class LanguageInjectionNode extends Node {

	private final String languageId;

	private int contentStart;
	private int contentEnd;

	private NodeBase<?> injectedNode;

	public LanguageInjectionNode(int start, int end, String languageId) {
		super(start, end);
		this.languageId = languageId;
	}

	public String getLanguageId() {
		return languageId;
	}

	public int getContentStart() {
		return contentStart;
	}

	public void setContentStart(int contentStart) {
		this.contentStart = contentStart;
	}

	public int getContentEnd() {
		return contentEnd;
	}

	public void setContentEnd(int contentEnd) {
		this.contentEnd = contentEnd;
	}

	@Override
	public String getNodeName() {
		return "injection";
	}

	@Override
	protected void accept0(ASTVisitor visitor) {

	}

	public NodeBase<?> getInjectedNode(CancelChecker cancelChecker) {
		if (injectedNode != null) {
			return injectedNode;
		}
		LanguageInjectionService injectionService = getLanguageService();
		if (injectionService != null) {
			Template template = this.getOwnerTemplate();
			int start = this.getContentStart();
			int end = this.getContentEnd();
			injectedNode = injectionService.parse(template.getTextDocument(), start, end, cancelChecker);
		}
		return injectedNode;
	}

	public LanguageInjectionService getLanguageService() {
		return LanguageInjectionServiceRegistry.getInstance().getLanguageService(languageId);
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.LanguageInjection;
	}

}
