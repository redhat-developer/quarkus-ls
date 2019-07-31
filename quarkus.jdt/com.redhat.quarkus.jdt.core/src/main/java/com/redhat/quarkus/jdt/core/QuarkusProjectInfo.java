/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.core;

import java.util.List;

/**
 * Quarkus Project Information
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusProjectInfo {

	private boolean quarkusProject;

	private List<ExtendedConfigDescriptionBuildItem> properties;

	public boolean isQuarkusProject() {
		return quarkusProject;
	}

	public void setQuarkusProject(boolean quarkusProject) {
		this.quarkusProject = quarkusProject;
	}

	public List<ExtendedConfigDescriptionBuildItem> getProperties() {
		return properties;
	}

	public void setProperties(List<ExtendedConfigDescriptionBuildItem> properties) {
		this.properties = properties;
	}

}
