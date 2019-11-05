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

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.quarkus.commons.ClasspathKind;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusPropertiesScope;
import com.redhat.quarkus.jdt.internal.core.utils.DependencyUtil;

/**
 * JDT Quarkus manager test.
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusManagerTest extends BaseJDTQuarkusManagerTest {

	private static final String QUARKUS_CORE_JAR = getJarPath("quarkus-core-0.28.1.jar");

	private static final String QUARKUS_CORE_DEPLOYMENT_JAR = getJarPath("quarkus-core-deployment-0.28.1.jar");

	@Test
	public void quarkusCorePropertiesWithOnlyCore() throws Exception {
		quarkusCoreTest("test-quarkus-Core", true, QUARKUS_CORE_JAR);
	}

	@Test
	public void quarkusCorePropertiesWithOnlyCoreDeployment() throws Exception {
		quarkusCoreTest("test-quarkus-Deployment", false, QUARKUS_CORE_DEPLOYMENT_JAR);
	}

	@Test
	public void quarkusCorePropertiesWithCoreAndDeploymentBoth() throws Exception {
		quarkusCoreTest("test-quarkus-CoreAndDeployment", false, QUARKUS_CORE_JAR, QUARKUS_CORE_DEPLOYMENT_JAR);
	}

	private void quarkusCoreTest(String projectName, boolean useDeploymentJarFromMaven, String... classpath) throws Exception {
		IJavaProject project = createJavaProject(projectName, classpath);
		QuarkusProjectInfo info = JDTQuarkusManager.getInstance().getQuarkusProjectInfo(project,
				QuarkusPropertiesScope.classpath, DocumentationConverter.DEFAULT_CONVERTER, ClasspathKind.SRC,
				new NullProgressMonitor());

		String expectedDeploymentJar = QUARKUS_CORE_DEPLOYMENT_JAR;
		if (useDeploymentJarFromMaven) {
			File f = DependencyUtil.getArtifact("io.quarkus", "quarkus-core-deployment", "0.28.1", null);
			Assert.assertNotNull("Test existing of quarkus-core-deployment*.jar", f);
			expectedDeploymentJar = f.getAbsolutePath();
		} else {
			expectedDeploymentJar = QUARKUS_CORE_DEPLOYMENT_JAR;
		}

		assertProperties(info,

				// io.quarkus.deployment.ApplicationConfig
				p("quarkus-core", "quarkus.application.name", "java.lang.String",
						"The name of the application.\nIf not set, defaults to the name of the project.",
						expectedDeploymentJar, "io.quarkus.deployment.ApplicationConfig#name", CONFIG_PHASE_BUILD_TIME,
						null),

				p("quarkus-core", "quarkus.application.version", "java.lang.String",
						"The version of the application.\nIf not set, defaults to the version of the project",
						expectedDeploymentJar, "io.quarkus.deployment.ApplicationConfig#version",
						CONFIG_PHASE_BUILD_TIME, null),

				// io.quarkus.deployment.JniProcessor$JniConfig
				p("quarkus-core", "quarkus.jni.enable", "boolean", "Enable JNI support.", expectedDeploymentJar,
						"io.quarkus.deployment.JniProcessor$JniConfig#enable", CONFIG_PHASE_BUILD_TIME, "false"),

				p("quarkus-core", "quarkus.jni.library-paths", "java.util.List<java.lang.String>",
						"Paths of library to load.", expectedDeploymentJar,
						"io.quarkus.deployment.JniProcessor$JniConfig#libraryPaths", CONFIG_PHASE_BUILD_TIME, null),

				// io.quarkus.deployment.SslProcessor$SslConfig
				p("quarkus-core", "quarkus.ssl.native", "java.util.Optional<java.lang.Boolean>",
						"Enable native SSL support.", expectedDeploymentJar,
						"io.quarkus.deployment.SslProcessor$SslConfig#native_", CONFIG_PHASE_BUILD_TIME, null),

				// io.quarkus.deployment.index.ApplicationArchiveBuildStep$IndexDependencyConfiguration
				// -> Map<String, IndexDependencyConfig>
				p("quarkus-core", "quarkus.index-dependency.{*}.classifier", "java.lang.String",
						"The maven classifier of the artifact to index", expectedDeploymentJar,
						"io.quarkus.deployment.index.IndexDependencyConfig#classifier", CONFIG_PHASE_BUILD_TIME, null),
				p("quarkus-core", "quarkus.index-dependency.{*}.artifact-id", "java.lang.String",
						"The maven artifactId of the artifact to index", expectedDeploymentJar,
						"io.quarkus.deployment.index.IndexDependencyConfig#artifactId", CONFIG_PHASE_BUILD_TIME, null),
				p("quarkus-core", "quarkus.index-dependency.{*}.group-id", "java.lang.String",
						"The maven groupId of the artifact to index", expectedDeploymentJar,
						"io.quarkus.deployment.index.IndexDependencyConfig#groupId", CONFIG_PHASE_BUILD_TIME, null));
	}

}
