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
package com.redhat.qute.parser.expression;

import java.util.List;
import java.util.stream.Collectors;

import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.parameter.ParameterParser;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParametersContainer;

/**
 * Method part.
 * 
 * <p>
 * 	{item.getName()}
 * </p>
 * 
 * @author Angelo ZERR
 *
 */
public class MethodPart extends MemberPart implements ParametersContainer {

	private List<Parameter> parameters;
	private int openBracketOffset = NULL_VALUE;
	private int closeBracketOffset = NULL_VALUE;
	private int endName;

	public MethodPart(int start, int end) {
		super(start, end);
		this.endName = end;
	}

	/**
	 * Returns the offset of the method name end.
	 */
	@Override
	public int getEndName() {
		return endName;
	}

	@Override
	public PartKind getPartKind() {
		return PartKind.Method;
	}

	/**
	 * Set the open bracket offset.
	 *
	 * <p>
	 * {item.compute|(1,'abcd')}
	 * </p>
	 * 
	 * @param openBracketOffset the open bracket offset.
	 */
	void setOpenBracket(int openBracketOffset) {
		this.openBracketOffset = openBracketOffset;
		super.setEnd(openBracketOffset);
	}

	/**
	 * Set the close bracket offset.
	 *
	 * <p>
	 * {item.compute(1,'abcd'|)}
	 * </p>
	 * 
	 * @param closeBracketOffset the close bracket offset.
	 */
	void setCloseBracket(int closeBracketOffset) {
		this.closeBracketOffset = closeBracketOffset;
		super.setEnd(closeBracketOffset);
	}

	// ---------------------------- Parameters methods

	/**
	 * Returns parameters of the section.
	 * 
	 * @return parameters of the section.
	 */
	public List<Parameter> getParameters() {
		if (parameters == null) {
			this.parameters = parseParameters();
		}
		return this.parameters;
	}

	/**
	 * Returns the parameter at the given offset and null otherwise.
	 * 
	 * @param offset the offset.
	 * 
	 * @return the parameter at the given offset and null otherwise.
	 */
	public Parameter getParameterAtOffset(int offset) {
		if (!isInParameters(offset)) {
			return null;
		}
		List<Parameter> parameters = getParameters();
		return (Parameter) Node.findNodeAt(parameters.stream().map(param -> (Node) param).collect(Collectors.toList()),
				offset);
	}

	/**
	 * Returns true if the given offset is inside parameter expression of the
	 * section and false otherwise.
	 * 
	 * @param offset the offset.
	 * 
	 * @return true if the given offset is inside parameter expression of the
	 *         section and false otherwise.
	 */
	public boolean isInParameters(int offset) {
		// cases:
		// - {item.count(|}
		// - {item.count(aaa,|}
		// - {item.count(aaa,b|bb}
		return offset >= getStartParametersOffset() && offset <= getEndParametersOffset();
	}

	private synchronized List<Parameter> parseParameters() {
		if (parameters != null) {
			return parameters;
		}
		List<Parameter> parameters = ParameterParser.parse(this, true, getOwnerTemplate().getCancelChecker());
		parameters.stream().forEach(p -> p.setCanHaveExpression(true));
		return parameters;
	}

	/**
	 * Returns the start offset of the parameters method.
	 * 
	 * <p>
	 * {item.compute(|1,'abcd')}
	 * </p>
	 * 
	 * 
	 * @return the start offset of the parameters method.
	 */
	@Override
	public int getStartParametersOffset() {
		return openBracketOffset + 1;
	}

	/**
	 * Returns the end offset of the parameters method.
	 * 
	 * <p>
	 * {item.compute(1,'abcd'|)}
	 * </p>
	 * 
	 * 
	 * @return the end offset of the parameters method.
	 */
	@Override
	public int getEndParametersOffset() {
		if (isClosed()) {
			return closeBracketOffset;
		}
		return getEnd();
	}

	@Override
	public void setEnd(int end) {
		super.setEnd(end);
	}

	@Override
	public boolean isClosed() {
		return closeBracketOffset != NULL_VALUE;
	}
	
	@Override
	protected void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
}
