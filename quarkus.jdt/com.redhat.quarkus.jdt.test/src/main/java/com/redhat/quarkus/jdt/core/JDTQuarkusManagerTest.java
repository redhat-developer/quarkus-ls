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

import static com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem.CONFIG_PHASE_BUILD_TIME;
import static com.redhat.quarkus.jdt.internal.core.JavaUtils.createJavaProject;
import static com.redhat.quarkus.jdt.internal.core.JavaUtils.getJarPath;
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.assertProperties;
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.p;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Test;

import com.redhat.quarkus.commons.QuarkusProjectInfo;

/**
 * JDT Quarkus manager test.
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusManagerTest extends BaseJDTQuarkusManagerTest {

	private static final String QUARKUS_CORE_DEPLOYMENT_JAR = getJarPath("quarkus-core-deployment-0.19.1.jar.test");

	@Test
	public void quarkusCoreDeploymentProperties() throws Exception {
		IJavaProject project = createJavaProject("test-quarkus", new String[] { QUARKUS_CORE_DEPLOYMENT_JAR });
		QuarkusProjectInfo info = JDTQuarkusManager.getInstance().getQuarkusProjectInfo(project.getProject().getName(),
				DocumentationConverter.DEFAULT_CONVERTER, new NullProgressMonitor());
		assertProperties(info,

				// io.quarkus.deployment.ApplicationConfig
				p("quarkus.application.name", "java.lang.String",
						"The name of the application.\n If not set, defaults to the name of the project.",
						QUARKUS_CORE_DEPLOYMENT_JAR, "io.quarkus.deployment.ApplicationConfig#name",
						CONFIG_PHASE_BUILD_TIME),

				p("quarkus.application.version", "java.lang.String",
						"The version of the application.\n If not set, defaults to the version of the project",
						QUARKUS_CORE_DEPLOYMENT_JAR, "io.quarkus.deployment.ApplicationConfig#version",
						CONFIG_PHASE_BUILD_TIME),

				// io.quarkus.deployment.JniProcessor$JniConfig
				p("quarkus.jni.enable", "boolean", "Enable JNI support.", QUARKUS_CORE_DEPLOYMENT_JAR,
						"io.quarkus.deployment.JniProcessor$JniConfig#enable", CONFIG_PHASE_BUILD_TIME),

				p("quarkus.jni.library-paths", "java.util.List<java.lang.String>", "Paths of library to load.",
						QUARKUS_CORE_DEPLOYMENT_JAR, "io.quarkus.deployment.JniProcessor$JniConfig#libraryPaths",
						CONFIG_PHASE_BUILD_TIME),

				// io.quarkus.deployment.SslProcessor$SslConfig
				p("quarkus.ssl.native", "java.util.Optional<java.lang.Boolean>", "Enable native SSL support.",
						QUARKUS_CORE_DEPLOYMENT_JAR, "io.quarkus.deployment.SslProcessor$SslConfig#native_",
						CONFIG_PHASE_BUILD_TIME),

				// io.quarkus.deployment.index.ApplicationArchiveBuildStep$IndexDependencyConfiguration
				// -> Map<String, IndexDependencyConfig>
				p("quarkus.index-dependency.{*}.classifier", "java.lang.String",
						"The maven classifier of the artifact to index", QUARKUS_CORE_DEPLOYMENT_JAR,
						"io.quarkus.deployment.index.IndexDependencyConfig#classifier", CONFIG_PHASE_BUILD_TIME),
				p("quarkus.index-dependency.{*}.artifact-id", "java.lang.String",
						"The maven artifactId of the artifact to index", QUARKUS_CORE_DEPLOYMENT_JAR,
						"io.quarkus.deployment.index.IndexDependencyConfig#artifactId", CONFIG_PHASE_BUILD_TIME),
				p("quarkus.index-dependency.{*}.group-id", "java.lang.String",
						"The maven groupId of the artifact to index", QUARKUS_CORE_DEPLOYMENT_JAR,
						"io.quarkus.deployment.index.IndexDependencyConfig#groupId", CONFIG_PHASE_BUILD_TIME));
	}

}
