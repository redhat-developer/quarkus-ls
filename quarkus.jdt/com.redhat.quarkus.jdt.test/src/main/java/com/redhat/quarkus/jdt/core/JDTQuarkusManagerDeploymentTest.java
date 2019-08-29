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
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.assertProperties;
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.p;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.jdt.internal.core.JobHelpers;
import com.redhat.quarkus.jdt.internal.core.utils.DependencyUtil;

/**
 * Test to download and use in classpath deployment JARs declared in //
 * META-INF/quarkus-extension.properties of quarkus-hibernate-orm.jar as
 * property:
 * <code>deployment-artifact=io.quarkus\:quarkus-hibernate-orm-deployment\:0.21.1</code>
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusManagerDeploymentTest extends BaseJDTQuarkusManagerTest {

	@Test
	public void hibernateOrmResteasy() throws Exception {
		QuarkusProjectInfo info = getQuarkusProjectInfo("hibernate-orm-resteasy");

		File f = DependencyUtil.getArtifact("io.quarkus", "quarkus-hibernate-orm-deployment", "0.19.1", null);
		Assert.assertNotNull("Test existing of quarkus-hibernate-orm-deployment*.jar", f);

		assertProperties(info,

				// io.quarkus.hibernate.orm.deployment.HibernateOrmConfig
				p("quarkus.hibernate-orm.dialect", "java.util.Optional<java.lang.String>",
						"The hibernate ORM dialect class name", f.getAbsolutePath(),
						"io.quarkus.hibernate.orm.deployment.HibernateOrmConfig#dialect", CONFIG_PHASE_BUILD_TIME));
	}

	@Test
	public void allQuarkusExtensions() throws Exception {
		QuarkusProjectInfo info = getQuarkusProjectInfo("all-quarkus-extensions");

		File keycloakJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-keycloak-deployment", "0.21.1", null);
		Assert.assertNotNull("Test existing of quarkus-keycloak-deployment*.jar", keycloakJARFile);
		File hibernateJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-hibernate-orm-deployment", "0.21.1",
				null);
		Assert.assertNotNull("Test existing of quarkus-hibernate-orm-deployment*.jar", hibernateJARFile);

		assertProperties(info,

				// Test with Map<String, String>
				// https://github.com/quarkusio/quarkus/blob/0.21/extensions/keycloak/deployment/src/main/java/io/quarkus/keycloak/KeycloakConfig.java#L308
				p("quarkus.keycloak.credentials.jwt.{*}", "java.lang.String",
						"The settings for client authentication with signed JWT", keycloakJARFile.getAbsolutePath(),
						"io.quarkus.keycloak.KeycloakConfig$KeycloakConfigCredentials#jwt", CONFIG_PHASE_BUILD_TIME),

				// Test with Map<String, Map<String, Map<String, String>>>
				// https://github.com/quarkusio/quarkus/blob/0.21/extensions/keycloak/deployment/src/main/java/io/quarkus/keycloak/KeycloakConfig.java#L469
				p("quarkus.keycloak.policy-enforcer.paths.{*}.claim-information-point.{*}.{*}.{*}", "java.lang.String",
						"", keycloakJARFile.getAbsolutePath(),
						"io.quarkus.keycloak.KeycloakConfig$KeycloakConfigPolicyEnforcer$ClaimInformationPointConfig#complexConfig",
						CONFIG_PHASE_BUILD_TIME),

				// io.quarkus.hibernate.orm.deployment.HibernateOrmConfig
				p("quarkus.hibernate-orm.dialect", "java.util.Optional<java.lang.String>",
						"The hibernate ORM dialect class name", hibernateJARFile.getAbsolutePath(),
						"io.quarkus.hibernate.orm.deployment.HibernateOrmConfig#dialect", CONFIG_PHASE_BUILD_TIME));
	}

	private static QuarkusProjectInfo getQuarkusProjectInfo(String projectName)
			throws CoreException, Exception, JavaModelException {
		// Load existing "hibernate-orm-resteasy" maven project
		IPath path = new Path(new File("projects/maven/" + projectName + "/.project").getAbsolutePath());
		IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(path);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
		project.create(description, null);
		project.open(null);

		// We need to call waitForBackgroundJobs with a Job which does nothing to have a
		// resolved classpath (IJavaProject#getResolvedClasspath) when search is done.
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				monitor.done();

			}
		};
		IProgressMonitor monitor = new NullProgressMonitor();
		JavaCore.run(runnable, null, monitor);
		waitForBackgroundJobs(monitor);

		// Collect Quarkus properties from the "hibernate-orm-resteasy" project. It
		// should collect Quarkus properties from given JAR:

		// 1) quarkus-hibernate-orm.jar which is declared in the dependencies of the
		// pom.xml
		// <dependency>
		// <groupId>io.quarkus</groupId>
		// <artifactId>quarkus-hibernate-orm</artifactId>
		// </dependency>

		// 2) quarkus-hibernate-orm-deployment.jar which is declared in
		// META-INF/quarkus-extension.properties of quarkus-hibernate-orm.jar as
		// property:
		// deployment-artifact=io.quarkus\:quarkus-hibernate-orm-deployment\:0.21.1

		QuarkusProjectInfo info = JDTQuarkusManager.getInstance().getQuarkusProjectInfo(project.getName(),
				DocumentationConverter.DEFAULT_CONVERTER, new NullProgressMonitor());
		return info;
	}

	private static void waitForBackgroundJobs(IProgressMonitor monitor) throws Exception {
		JobHelpers.waitForJobsToComplete(monitor);
	}
}
