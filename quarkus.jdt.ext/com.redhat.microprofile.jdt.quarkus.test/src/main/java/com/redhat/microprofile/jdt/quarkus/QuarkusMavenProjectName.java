/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus;

import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest.MicroProfileMavenProjectName;

public class QuarkusMavenProjectName extends MicroProfileMavenProjectName {
	public static String kubernetes = "kubernetes";
	public static String quarkus_container_images = "quarkus-container-images";
	public static String scheduler_quickstart="scheduler-quickstart";
}