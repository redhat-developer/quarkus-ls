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

import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_BUILD_TIME;
import static com.redhat.microprofile.jdt.internal.core.JavaUtils.createJavaProject;
import static com.redhat.microprofile.jdt.internal.core.JavaUtils.getJarPath;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.ClasspathKind;
import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfileProjectInfoParams;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;

/**
 * JDT Quarkus manager test.
 * 
 * @author Angelo ZERR
 *
 */
public class PropertiesManagerTest extends BasePropertiesManagerTest {

	private static final String QUARKUS_CORE_JAR = getJarPath("quarkus-core-0.28.1.jar");

	private static final String QUARKUS_CORE_DEPLOYMENT_JAR = getJarPath("quarkus-core-deployment-0.28.1.jar");

	@Test
	public void notBelongToEclipseProject() throws JavaModelException, CoreException {
		MicroProfileProjectInfoParams params = new MicroProfileProjectInfoParams();
		params.setUri("bad-uri");
		MicroProfileProjectInfo info = PropertiesManager.getInstance().getMicroProfileProjectInfo(params,
				JDT_UTILS, new NullProgressMonitor());
		Assert.assertNotNull("MicroProfileProjectInfo for 'bad-uri' should not be null", info);
		Assert.assertTrue("MicroProfileProjectInfo for 'bad-uri' should not belong to an Eclipse project ",
				info.getProjectURI().isEmpty());
	}

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

	private void quarkusCoreTest(String projectName, boolean useDeploymentJarFromMaven, String... classpath)
			throws Exception {
		IJavaProject project = createJavaProject(projectName, classpath);
		MicroProfileProjectInfo info = PropertiesManager.getInstance().getMicroProfileProjectInfo(project,
				MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES, ClasspathKind.SRC, JDT_UTILS,
				DocumentFormat.Markdown, new NullProgressMonitor());

		assertProperties(info,

				// io.quarkus.deployment.ApplicationConfig
				p("quarkus-core", "quarkus.application.name", "java.lang.String",
						"The name of the application.\nIf not set, defaults to the name of the project.", true,
						"io.quarkus.deployment.ApplicationConfig", "name", null, CONFIG_PHASE_BUILD_TIME, null),

				p("quarkus-core", "quarkus.application.version", "java.lang.String",
						"The version of the application.\nIf not set, defaults to the version of the project", true,
						"io.quarkus.deployment.ApplicationConfig", "version", null, CONFIG_PHASE_BUILD_TIME, null),

				// io.quarkus.deployment.JniProcessor.JniConfig
				p("quarkus-core", "quarkus.jni.enable", "boolean", "Enable JNI support.", true,
						"io.quarkus.deployment.JniProcessor.JniConfig", "enable", null, CONFIG_PHASE_BUILD_TIME,
						"false"),

				p("quarkus-core", "quarkus.jni.library-paths", "java.util.List<java.lang.String>",
						"Paths of library to load.", true, "io.quarkus.deployment.JniProcessor.JniConfig",
						"libraryPaths", null, CONFIG_PHASE_BUILD_TIME, null),

				// io.quarkus.deployment.SslProcessor.SslConfig
				p("quarkus-core", "quarkus.ssl.native", "java.util.Optional<java.lang.Boolean>",
						"Enable native SSL support.", true, "io.quarkus.deployment.SslProcessor.SslConfig", "native_",
						null, CONFIG_PHASE_BUILD_TIME, null),

				// io.quarkus.deployment.index.ApplicationArchiveBuildStep.IndexDependencyConfiguration
				// -> Map<String, IndexDependencyConfig>
				p("quarkus-core", "quarkus.index-dependency.{*}.classifier", "java.lang.String",
						"The maven classifier of the artifact to index", true,
						"io.quarkus.deployment.index.IndexDependencyConfig", "classifier", null,
						CONFIG_PHASE_BUILD_TIME, null),
				p("quarkus-core", "quarkus.index-dependency.{*}.artifact-id", "java.lang.String",
						"The maven artifactId of the artifact to index", true,
						"io.quarkus.deployment.index.IndexDependencyConfig", "artifactId", null,
						CONFIG_PHASE_BUILD_TIME, null),
				p("quarkus-core", "quarkus.index-dependency.{*}.group-id", "java.lang.String",
						"The maven groupId of the artifact to index", true,
						"io.quarkus.deployment.index.IndexDependencyConfig", "groupId", null, CONFIG_PHASE_BUILD_TIME,
						null));
	}

}
