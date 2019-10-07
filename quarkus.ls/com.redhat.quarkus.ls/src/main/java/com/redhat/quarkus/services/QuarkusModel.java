/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.redhat.quarkus.commons.EnumItem;

/**
 * Maintains default profiles and boolean types.
 */
public class QuarkusModel {

	private QuarkusModel() {
	}

	public static final List<EnumItem> DEFAULT_PROFILES = Arrays.asList(
		new EnumItem("dev", "Profile activated when in development mode (quarkus:dev)."),
		new EnumItem("prod", "The default profile when not running in development or test mode."),
		new EnumItem("test", "Profile activated when running tests."));

	public static final Collection<EnumItem> BOOLEAN_ENUMS = Arrays.asList(new EnumItem("false", null), new EnumItem("true", null));

	public static List<String> getDefaultProfileNames() {
		return DEFAULT_PROFILES.stream().map(EnumItem::getName).collect(Collectors.toList());
	}
}