/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import com.redhat.microprofile.commons.metadata.ItemHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;

public interface IPropertiesCollector {

	ItemMetadata addItemMetadata(String name, String type, String description, String sourceType, String sourceField,
			String sourceMethod, String defaultValue, String extensionName, boolean binary, int phase);

	boolean hasItemHint(String hint);

	ItemHint getItemHint(String hint);
}
