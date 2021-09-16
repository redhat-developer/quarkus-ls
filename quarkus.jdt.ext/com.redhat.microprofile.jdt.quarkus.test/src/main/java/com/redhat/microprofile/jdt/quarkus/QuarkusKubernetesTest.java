/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertHints;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertHintsDuplicate;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertPropertiesDuplicate;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.h;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.p;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.vh;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

/**
 * Quarkus Kubernetes properties test.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusKubernetesTest extends BasePropertiesManagerTest {

  @Test
  public void kubernetes() throws Exception {
    MicroProfileProjectInfo info = getMicroProfileProjectInfoFromMavenProject(QuarkusMavenProjectName.kubernetes);

    assertProperties(info,

        // io.dekorate.kubernetes.annotation.KubernetesApplication
        p(null, "kubernetes.name", "java.lang.String",
            "The name of the application. This value will be used for naming Kubernetes resources like: - Deployment - Service and so on ... If no value is specified it will attempt to determine the name using the following rules: If its a maven/gradle project use the artifact id. Else if its a bazel project use the name. Else if the system property app.name is present it will be used. Else find the project root folder and use its name (root folder detection is done by moving to the parent folder until .git is found)."
                + System.lineSeparator() + "" + System.lineSeparator() + " *  **Returns:**" + System.lineSeparator()
                + "    " + System.lineSeparator() + "     *  The specified application name.",
            true, "io.dekorate.kubernetes.annotation.KubernetesApplication", null, "name()Ljava/lang/String;", 0, null),

        p(null, "kubernetes.readiness-probe.initial-delay-seconds", "int", "The amount of time to wait in seconds before starting to probe." + //
                  System.lineSeparator() + "" + System.lineSeparator() + //
                  " *  **Returns:**" + System.lineSeparator() + //
                  "    " + System.lineSeparator() + "     *  The initial delay.",
                  true, "io.dekorate.kubernetes.annotation.Probe", null, "initialDelaySeconds()I", 0, "0"),

        p(null, "kubernetes.annotations[*].key", "java.lang.String", null, true,
            "io.dekorate.kubernetes.annotation.Annotation", null, "key()Ljava/lang/String;", 0, null),

        p(null, "kubernetes.init-containers[*].ports[*].protocol", "io.dekorate.kubernetes.annotation.Protocol", null,
            true, "io.dekorate.kubernetes.annotation.Port", null,
            "protocol()Lio/dekorate/kubernetes/annotation/Protocol;", 0, "TCP"),

        p(null, "kubernetes.deployment.target", "java.lang.String",
            "To enable the generation of OpenShift resources, you need to include OpenShift in the target platforms: `kubernetes.deployment.target=openshift`."
                + System.lineSeparator() + ""
                + "If you need to generate resources for both platforms (vanilla Kubernetes and OpenShift), then you need to include both (coma separated)."
                + System.lineSeparator() + "" + "`kubernetes.deployment.target=kubernetes, openshift`.",
            true, null, null, null, 0, "kubernetes"),
        p(null, "kubernetes.registry", "java.lang.String", "Specify the docker registry.", true, null, null, null, 0,
            null));

    assertPropertiesDuplicate(info);

    assertHints(info,

        h("io.dekorate.kubernetes.annotation.Protocol", null, true, "io.dekorate.kubernetes.annotation.Protocol",
            vh("TCP", null, null), //
            vh("UDP", null, null)));

    assertHintsDuplicate(info);

  }

  @Test
  public void openshift() throws Exception {
    MicroProfileProjectInfo info = getMicroProfileProjectInfoFromMavenProject(QuarkusMavenProjectName.kubernetes);

    assertProperties(info,

        // io.dekorate.openshift.annotation.OpenshiftApplication
        p(null, "openshift.name", "java.lang.String",
            "The name of the application. This value will be used for naming Kubernetes resources like: - Deployment - Service and so on ... If no value is specified it will attempt to determine the name using the following rules: If its a maven/gradle project use the artifact id. Else if its a bazel project use the name. Else if the system property app.name is present it will be used. Else find the project root folder and use its name (root folder detection is done by moving to the parent folder until .git is found)."
                + System.lineSeparator() + "" + System.lineSeparator() + " *  **Returns:**" + System.lineSeparator()
                + "    " + System.lineSeparator() + "     *  The specified application name.",
            true, "io.dekorate.openshift.annotation.OpenshiftApplication", null, "name()Ljava/lang/String;", 0, null),

        p(null, "openshift.readiness-probe.initial-delay-seconds", "int", "The amount of time to wait in seconds before starting to probe." + //
                  System.lineSeparator() + "" + System.lineSeparator() + //
                  " *  **Returns:**" + System.lineSeparator() + //
                  "    " + System.lineSeparator() + "     *  The initial delay.", true,
            "io.dekorate.kubernetes.annotation.Probe", null, "initialDelaySeconds()I", 0, "0"),

        p(null, "openshift.annotations[*].key", "java.lang.String", null, true,
            "io.dekorate.kubernetes.annotation.Annotation", null, "key()Ljava/lang/String;", 0, null),

        p(null, "openshift.init-containers[*].ports[*].protocol", "io.dekorate.kubernetes.annotation.Protocol", null,
            true, "io.dekorate.kubernetes.annotation.Port", null,
            "protocol()Lio/dekorate/kubernetes/annotation/Protocol;", 0, "TCP"),

        p(null, "openshift.registry", "java.lang.String", "Specify the docker registry.", true, null, null, null, 0,
            null));

    assertPropertiesDuplicate(info);

    assertHints(info,

        h("io.dekorate.kubernetes.annotation.Protocol", null, true, "io.dekorate.kubernetes.annotation.Protocol",
            vh("TCP", null, null), //
            vh("UDP", null, null)));

    assertHintsDuplicate(info);

  }

  @Test
  public void s2i() throws Exception {
    MicroProfileProjectInfo info = getMicroProfileProjectInfoFromMavenProject(QuarkusMavenProjectName.kubernetes);

    assertProperties(info,

        // io.dekorate.s2i.annotation.S2iBuild
        p(null, "s2i.docker-file", "java.lang.String", "The relative path of the Dockerfile, from the module root." + //
            System.lineSeparator() + "" + System.lineSeparator() + //
            " *  **Returns:**" + //
            System.lineSeparator() + //
            "    " + //
            System.lineSeparator() + //
            "     *  The relative path.", true, "io.dekorate.s2i.annotation.S2iBuild", null,
            "dockerFile()Ljava/lang/String;", 0, "Dockerfile"),

                   p(null, "s2i.group", "java.lang.String",
            "The group of the application. This value will be use as image user."
                  + System.lineSeparator() + "" + System.lineSeparator() + " *  **Returns:**" + System.lineSeparator()
                  + "    " + System.lineSeparator() + "     *  The specified group name.",
            true, "io.dekorate.s2i.annotation.S2iBuild", null, "group()Ljava/lang/String;", 0,
            null),

                   p("quarkus-container-image-s2i", "quarkus.s2i.jar-directory", "java.lang.String",
            "The directory where the jar is added during the assemble phase." + //
            "\n" + //
            "This is dependent on the S2I image and should be supplied if a non default image is used.",
            true, "io.quarkus.container.image.s2i.deployment.S2iConfig", "jarDirectory", null, 1,
            "/deployments/"));

    assertPropertiesDuplicate(info);

    assertHintsDuplicate(info);

  }

  @Test
  public void docker() throws Exception {
    MicroProfileProjectInfo info = getMicroProfileProjectInfoFromMavenProject(QuarkusMavenProjectName.kubernetes);

    assertProperties(info,

        // io.dekorate.docker.annotation.DockerBuild
        p(null, "docker.docker-file", "java.lang.String", "The relative path of the Dockerfile, from the module root." + //
            System.lineSeparator() + "" + System.lineSeparator() + //
            " *  **Returns:**" + //
            System.lineSeparator() + //
            "    " + //
            System.lineSeparator() + //
            "     *  The relative path.", true, "io.dekorate.docker.annotation.DockerBuild", null,
            "dockerFile()Ljava/lang/String;", 0, "Dockerfile"));

    assertPropertiesDuplicate(info);

    assertHintsDuplicate(info);

  }
}