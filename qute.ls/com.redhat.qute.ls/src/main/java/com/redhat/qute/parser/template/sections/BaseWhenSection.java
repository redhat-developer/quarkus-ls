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
package com.redhat.qute.parser.template.sections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.qute.parser.template.CaseOperator;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParametersInfo;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;

/**
 * Base class for #switch and #when section.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#when_section
 *
 */
public abstract class BaseWhenSection extends Section {

	private static final String VALUE = "value";

	private static final Map<String, CaseOperator> caseOperators;

	static {
		caseOperators = new HashMap<>();
		registerOperator("gt", false, ">"); // greater than
		registerOperator("ge", false, ">="); // greater than or equal to
		registerOperator("lt", false, "<"); // less than
		registerOperator("le", false, "<="); // less than or equal to
		registerOperator("not", false, "ne", "!="); // not equals
		registerOperator("in", true); // Is in
		registerOperator("ni", true, "!in"); // Is not in
	}

	private static void registerOperator(String name, boolean isMulti, String... aliases) {
		CaseOperator operator = new CaseOperator(name, isMulti, aliases);
		caseOperators.put(operator.getName(), operator);
		if (aliases != null) {
			for (String alias : aliases) {
				caseOperators.put(alias, operator);
			}
		}
	}

	private static final ParametersInfo PARAMETER_INFOS = ParametersInfo.builder() //
			.addParameter(VALUE) //
			.build();

	public BaseWhenSection(String tag, int start, int end) {
		super(tag, start, end);
	}

	public Parameter getValueParameter() {
		if (getParameters().isEmpty()) {
			return null;
		}
		return getParameterAtIndex(0);
	}

	public ParametersInfo getParametersInfo() {
		return PARAMETER_INFOS;
	}

	@Override
	public List<SectionKind> getBlockLabels() {
		return List.of(SectionKind.IS, SectionKind.CASE, SectionKind.ELSE);
	}

	public static boolean shouldCompleteWhenSection(Section section) {
		int paramCount = section.getParameters().size();
		if (paramCount == 0) {
			return true;
		}
		CaseOperator operator = caseOperators.get(section.getParameterAtIndex(0).getName());
		if (operator == null) {
			if (paramCount == 1) {
				// There is only parameter and it is not a operator
				return false;
			}
			return true;
		}
		if (paramCount == 2 && !operator.isMulti()) {
			// first parameter is operator that only allows single value, second is the
			// existing Enum
			return false;
		}
		return true;
	}
}
