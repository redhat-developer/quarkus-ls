package com.redhat.qute.parser.injection;

import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;

public class LanguageInjectionNode extends Node {

	private final String languageId;

	private int contentStart;
	private int contentEnd;

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

	@Override
	public NodeKind getKind() {
		return NodeKind.LanguageInjection;
	}

}
