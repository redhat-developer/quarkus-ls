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
import java.util.Map;

import com.redhat.qute.parser.template.Section;

public class DefaultSectionFactory implements SectionFactory {

	private static final Map<String, SectionFactory> factoryByTag;

	static {
		factoryByTag = new HashMap<>();
		factoryByTag.put(EachSection.TAG, (tag, start, end) -> new EachSection(start, end));
		factoryByTag.put(ForSection.TAG, (tag, start, end) -> new ForSection(start, end));
		factoryByTag.put(IfSection.TAG, (tag, start, end) -> new IfSection(start, end));
		factoryByTag.put(IncludeSection.TAG, (tag, start, end) -> new IncludeSection(start, end));
		factoryByTag.put(InsertSection.TAG, (tag, start, end) -> new InsertSection(start, end));
		factoryByTag.put(SetSection.TAG, (tag, start, end) -> new SetSection(start, end));
		factoryByTag.put(LetSection.TAG, (tag, start, end) -> new LetSection(start, end));
		factoryByTag.put(WithSection.TAG, (tag, start, end) -> new WithSection(start, end));
		factoryByTag.put(WhenSection.TAG, (tag, start, end) -> new WhenSection(start, end));
		factoryByTag.put(SwitchSection.TAG, (tag, start, end) -> new SwitchSection(start, end));
	}

	@Override
	public Section createSection(String tag, int start, int end) {
		SectionFactory factory = factoryByTag.get(tag);
		return factory != null ? factory.createSection(tag, start, end) : new Section(tag, start, end);
	}
}
