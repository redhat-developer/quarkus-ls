///usr/bin/env jbang "$0" "$@" ; exit $?
/**
 * Copyright 2022 Fred Bricon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//DEPS commons-io:commons-io:2.11.0
//DEPS org.tukaani:xz:1.9
//JAVA 17+
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.file.PathUtils;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class repoflattener {

  public static void main(String... args) throws IOException {
    Path baseDir = (args == null || args.length == 0) ? Path.of("") : Path.of(args[0]);

    Path originalRepo = baseDir.resolve("target").resolve("repository").toAbsolutePath();
    System.out.println("🛠 flattening " + originalRepo);
    Path flatRepo = originalRepo.resolveSibling("flat-repository");
    if (Files.exists(flatRepo)) {
      PathUtils.deleteDirectory(flatRepo);
    }
    Files.createDirectory(flatRepo);

    var files = Files.walk(originalRepo).filter(path -> {
      if (!Files.isRegularFile(path)) {
        return false;
      }
      var fileName = FilenameUtils.getName(path.toString());
      return !fileName.startsWith("artifacts");
    }).toList();

    for (Path file : files) {
      PathUtils.copyFileToDirectory(file, flatRepo);
    }
    Path artifactsXml = extractAndRewriteArtifactXml(originalRepo.resolve("artifacts.jar"));
    createXZ(artifactsXml, flatRepo);
    createJar(artifactsXml, flatRepo);

    System.out.println("🙌 repository was flattened to " + flatRepo.toAbsolutePath());
  }

  private static Path extractAndRewriteArtifactXml(Path archive) throws IOException {
    var extracted = Files.createTempFile("artifacts", ".xml");
    try (JarInputStream archiveInputStream = new JarInputStream(
        new BufferedInputStream(Files.newInputStream(archive)))) {
      // we assume only 1 entry
      archiveInputStream.getNextJarEntry();
      streamRewrite(archiveInputStream, extracted);
    }
    if (Files.size(extracted) == 0) {
      throw new IOException("💥 Failed to extract/rewrite artifacts.xml");
    }
    return extracted;
  }

  private static void streamRewrite(InputStream src, Path dst) throws IOException {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(src));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(dst)))) {
      String line;
      while ((line = br.readLine()) != null) {
        line = line.replace("/plugins/", "/").replace("/features/", "/");
        bw.write(line);
        bw.newLine();
      }
    }
  }

  private static void createXZ(Path artifactsXml, Path flatRepo) throws IOException {
    Path artifactsXmlXZ = flatRepo.resolve("artifacts.xml.xz");
    try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(artifactsXml));
        XZOutputStream xzOut = new XZOutputStream(
            new BufferedOutputStream(Files.newOutputStream(artifactsXmlXZ)), new LZMA2Options());) {
      byte[] buffer = new byte[4096];
      int n = 0;
      while (-1 != (n = in.read(buffer))) {
        xzOut.write(buffer, 0, n);
      }
    }
  }

  private static void createJar(Path artifactXml, Path flatRepo) throws IOException {
    Path artifactsJar = flatRepo.resolve("artifacts.jar").toAbsolutePath();
    var env = Collections.singletonMap("create", "true");// Create the zip file if it doesn't exist
    URI uri = URI.create("jar:file:" + artifactsJar.toString().replace('\\', '/'));
    try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
      Path pathInZipfile = zipfs.getPath("artifacts.xml");
      Files.copy(artifactXml, pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}