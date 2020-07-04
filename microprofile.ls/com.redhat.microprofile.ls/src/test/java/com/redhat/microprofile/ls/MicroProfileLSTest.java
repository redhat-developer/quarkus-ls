/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.microprofile.ls;

import com.redhat.microprofile.commons.MicroProfileJavaProjectLabelsParams;
import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfileProjectInfoParams;
import com.redhat.microprofile.commons.ProjectLabelInfoEntry;
import com.redhat.microprofile.ls.api.MicroProfileLanguageClientAPI;
import com.redhat.microprofile.ls.api.MicroProfileLanguageServerAPI;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class MicroProfileLSTest {
    private static class MyLanguageClient implements MicroProfileLanguageClientAPI {

        private final CountDownLatch latch;

        private MyLanguageClient(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public CompletableFuture<MicroProfileProjectInfo> getProjectInfo(MicroProfileProjectInfoParams params) {
            return null;
        }

        @Override
        public void telemetryEvent(Object object) {
            System.out.println("telemetryEvent called");
        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
        }

        @Override
        public void showMessage(MessageParams messageParams) {
            System.out.println("showMessage called");
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
            return null;
        }

        @Override
        public void logMessage(MessageParams message) {
            System.out.println("MessageParams called");
        }

        @Override
        public CompletableFuture<ProjectLabelInfoEntry> getJavaProjectlabels(MicroProfileJavaProjectLabelsParams javaParams) {
            latch.countDown();
            return CompletableFuture.completedFuture(null);
        }
    }

    private static Process p;

    @BeforeClass
    public static void init() throws IOException {
        String javaHome = System.getProperty("java.home");
        String classPath = System.getProperty("java.class.path");
        ProcessBuilder builder = new ProcessBuilder();
        p = builder.command(javaHome + File.separatorChar + "bin" + File.separatorChar + "java", "-classpath", classPath, MicroProfileServerLauncher.class.getName()).start();
    }

    @AfterClass
    public static void shutdown() {
        p.destroy();
    }

    private Launcher<MicroProfileLanguageServerAPI> getLauncher(CountDownLatch latch) {
        MyLanguageClient client = new MyLanguageClient(latch);
        Launcher<MicroProfileLanguageServerAPI> launcher = Launcher.createLauncher(client, MicroProfileLanguageServerAPI.class, p.getInputStream(), p.getOutputStream(), Executors.newCachedThreadPool(), it-> it);
        launcher.startListening();
        return launcher;
    }

    @Test
    public void burstDidOpen() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(10);
        Launcher<MicroProfileLanguageServerAPI> launcher = getLauncher(latch);
        CompletableFuture.runAsync(() -> {
            for (int i=1; i < 11;i++) {
                try {
                    URL url = MicroProfileLSTest.class.getResource("Class" + i + ".java");
                    byte[] content = Files.readAllBytes(Paths.get(url.toURI()));
                    TextDocumentItem item = new TextDocumentItem(url.toURI().toString(), "java", 0, new String(content, 0, content.length, StandardCharsets.UTF_8));
                    launcher.getRemoteProxy().getTextDocumentService().didOpen(new DidOpenTextDocumentParams(item));
                } catch (IOException e) {
                } catch (URISyntaxException e) {
                }
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }
}
