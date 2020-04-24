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

import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.ClasspathKind;
import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.internal.core.utils.DependencyUtil;

/**
 * Test collection of Quarkus properties from classpath kind
 * 
 * <ul>
 * <li>not in classpath -> 0 quarkus properties</li>
 * <li>in /java/main/src classpath -> N quarkus properties</li>
 * <li>in /java/main/test classpath-> N + M quarkus properties</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class PropertiesManagerClassPathKindTest extends BasePropertiesManagerTest {

	@Test
	public void configQuickstartTest() throws Exception {

		IJavaProject javaProject = loadMavenProject(MavenProjectName.config_quickstart_test);

		// not in classpath -> 0 quarkus properties
		IFile fileFromNone = javaProject.getProject().getFile(new Path("application.properties"));
		MicroProfileProjectInfo infoFromNone = PropertiesManager.getInstance().getMicroProfileProjectInfo(fileFromNone,
				MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES, JDT_UTILS,
				DocumentFormat.Markdown, new NullProgressMonitor());
		Assert.assertEquals(ClasspathKind.NONE, infoFromNone.getClasspathKind());
		Assert.assertEquals(0, infoFromNone.getProperties().size());

		File resteasyJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-resteasy-common-deployment",
				"1.0.0.CR1", null, new NullProgressMonitor());
		Assert.assertNotNull("quarkus-resteasy-common-deployment*.jar is missing", resteasyJARFile);

		// in /java/main/src classpath -> N quarkus properties
		IFile fileFromSrc = javaProject.getProject().getFile(new Path("src/main/resources/application.properties"));
		MicroProfileProjectInfo infoFromSrc = PropertiesManager.getInstance().getMicroProfileProjectInfo(fileFromSrc,
				MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES, JDT_UTILS,
				DocumentFormat.Markdown, new NullProgressMonitor());
		Assert.assertEquals(ClasspathKind.SRC, infoFromSrc.getClasspathKind());
		assertProperties(infoFromSrc, 181 /* properties from JAR */ + //
				3 /* properties from Java sources */ + //
				7 /* static properties from microprofile-context-propagation-api */,

				// quarkus-resteasy JAR
				p("quarkus-resteasy-common", "quarkus.resteasy.gzip.enabled", "boolean", "If gzip is enabled", true,
						"io.quarkus.resteasy.common.deployment.ResteasyCommonProcessor.ResteasyCommonConfigGzip",
						"enabled", null, 1, "false"),

				// GreetingResource
				// @ConfigProperty(name = "greeting.message")
				// String message;
				p(null, "greeting.message", "java.lang.String", null, false, "org.acme.config.GreetingResource",
						"message", null, 0, null),

				// @ConfigProperty(name = "greeting.suffix" , defaultValue="!")
				// String suffix;
				p(null, "greeting.suffix", "java.lang.String", null, false, "org.acme.config.GreetingResource",
						"suffix", null, 0, "!"),

				// @ConfigProperty(name = "greeting.name")
				// Optional<String> name;
				p(null, "greeting.name", "java.util.Optional", null, false, "org.acme.config.GreetingResource", "name",
						null, 0, null));

		assertPropertiesDuplicate(infoFromSrc);

		// in /java/main/test classpath-> N + M quarkus properties
		File undertowJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-undertow-deployment", "1.0.0.CR1",
				null, new NullProgressMonitor());
		Assert.assertNotNull("quarkus-undertow-deployment*.jar is missing", undertowJARFile);

		IFile filefromTest = javaProject.getProject().getFile(new Path("src/test/resources/application.properties"));
		MicroProfileProjectInfo infoFromTest = PropertiesManager.getInstance().getMicroProfileProjectInfo(filefromTest,
				MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES, JDT_UTILS,
				DocumentFormat.Markdown, new NullProgressMonitor());
		Assert.assertEquals(ClasspathKind.TEST, infoFromTest.getClasspathKind());
		assertProperties(infoFromTest, 181 /* properties from JAR */ + //
		 3 /* properties from JAR (test) */ +  // 
		 3 /* properties from (src) Java sources */ + //
		 3 /* properties from (test) Java sources */ + //
		 7 /* static properties from microprofile-context-propagation-api */,

				// quarkus-resteasy JAR
				p("quarkus-resteasy-common", "quarkus.resteasy.gzip.enabled", "boolean", "If gzip is enabled", true,
						"io.quarkus.resteasy.common.deployment.ResteasyCommonProcessor.ResteasyCommonConfigGzip",
						"enabled", null, 1, "false"),

				// quarkus-undertow has maven test scope, add it
				// <dependency>
				// <groupId>io.quarkus</groupId>
				// <artifactId>quarkus-undertow</artifactId>
				// <scope>test</scope>
				// </dependency>

				p("quarkus-undertow", "quarkus.servlet.context-path", "java.util.Optional<java.lang.String>",
						"The context path to serve all Servlet context from. This will also affect any resources\nthat run as a Servlet, e.g. JAX-RS.\n\nNote that this is relative to the HTTP root path set in quarkus.http.root-path, so if the context path\nis /bar and the http root is /foo then the actual Servlet path will be /foo/bar.",
						true, "io.quarkus.undertow.deployment.ServletConfig", "contextPath", null, 1, null),

				// GreetingResource
				// @ConfigProperty(name = "greeting.message")
				// String message;
				p(null, "greeting.message", "java.lang.String", null, false, "org.acme.config.GreetingResource",
						"message", null, 0, null),

				// @ConfigProperty(name = "greeting.suffix" , defaultValue="!")
				// String suffix;
				p(null, "greeting.suffix", "java.lang.String", null, false, "org.acme.config.GreetingResource",
						"suffix", null, 0, "!"),

				// @ConfigProperty(name = "greeting.name")
				// Optional<String> name;
				p(null, "greeting.name", "java.util.Optional", null, false, "org.acme.config.GreetingResource", "name",
						null, 0, null),

				// TestResource
				// @ConfigProperty(name = "greeting.message.test")
				// String message;
				p(null, "greeting.message.test", "java.lang.String", null, false, "org.acme.config.TestResource",
						"message", null, 0, null),

				// @ConfigProperty(name = "greeting.suffix.test" , defaultValue="!")
				// String suffix;
				p(null, "greeting.suffix.test", "java.lang.String", null, false, "org.acme.config.TestResource",
						"suffix", null, 0, "!"),

				// @ConfigProperty(name = "greeting.name.test")
				// Optional<String> name;
				p(null, "greeting.name.test", "java.util.Optional", null, false, "org.acme.config.TestResource", "name",
						null, 0, null)

		);

		assertPropertiesDuplicate(infoFromTest);
	}

}
