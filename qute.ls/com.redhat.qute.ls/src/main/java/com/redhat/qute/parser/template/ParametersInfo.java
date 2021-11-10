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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ParametersInfo implements Iterable<List<ParameterInfo>> {

	public static final String MAIN_BLOCK_NAME = "$main";

	public static Builder builder() {
		return new Builder();
	}

	public static final ParametersInfo EMPTY = builder().build();

	private final Map<String, List<ParameterInfo>> parameters;

	private ParametersInfo(Map<String, List<ParameterInfo>> parameters) {
		this.parameters = new HashMap<>(parameters);
	}

	public List<ParameterInfo> get(String sectionPart) {
		return parameters.getOrDefault(sectionPart, Collections.emptyList());
	}

	@Override
	public Iterator<List<ParameterInfo>> iterator() {
		return parameters.values().iterator();
	}

	public int size() {
		return parameters.size();
	}

	public static class Builder {

		private final Map<String, List<ParameterInfo>> parameters;

		Builder() {
			this.parameters = new HashMap<>();
		}

		public Builder addParameter(String name) {
			return addParameter(MAIN_BLOCK_NAME, name, null);
		}

		public Builder addParameter(String name, String defaultValue) {
			return addParameter(MAIN_BLOCK_NAME, name, defaultValue);
		}

		public Builder addParameter(ParameterInfo param) {
			return addParameter(MAIN_BLOCK_NAME, param);
		}

		public Builder addParameter(String blockLabel, String name, String defaultValue) {
			return addParameter(blockLabel, new ParameterInfo(name, defaultValue, false));
		}

		public Builder addParameter(String blockLabel, ParameterInfo parameter) {
			parameters.computeIfAbsent(blockLabel, c -> new ArrayList<>()).add(parameter);
			return this;
		}

		public ParametersInfo build() {
			return new ParametersInfo(parameters);
		}
	}

}
