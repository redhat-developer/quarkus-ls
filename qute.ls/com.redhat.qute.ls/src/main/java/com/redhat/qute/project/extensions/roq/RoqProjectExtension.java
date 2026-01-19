/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions.roq;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.datamodel.resolvers.CustomValueResolver;
import com.redhat.qute.project.extensions.ProjectExtension;
import com.redhat.qute.project.extensions.roq.data.DataLoader;
import com.redhat.qute.project.extensions.roq.data.RoqDataFile;
import com.redhat.qute.project.extensions.roq.data.json.JsonDataLoader;
import com.redhat.qute.project.extensions.roq.data.yaml.YamlDataLoader;
import com.redhat.qute.services.ResolvingJavaTypeContext;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.settings.QuteValidationSettings;

public class RoqProjectExtension implements ProjectExtension {

	private static final Map<String /* file extension yaml, yml, json, etc */, DataLoader> dataLoaderRegistrty;

	static {
		dataLoaderRegistrty = new HashMap<>();
		YamlDataLoader yamlDataLoader = new YamlDataLoader();
		dataLoaderRegistrty.put("yml", yamlDataLoader);
		dataLoaderRegistrty.put("yaml", yamlDataLoader);
		dataLoaderRegistrty.put("json", new JsonDataLoader());
	}

	private static class ResolverCache {
		public final CustomValueResolver cdi;
		public final CustomValueResolver inject;

		public ResolverCache(CustomValueResolver cdi, CustomValueResolver inject) {
			this.cdi = cdi;
			this.inject = inject;
		}
	}

	private boolean enabled;

	private Path dataDir;

	private final Map<Path, ResolverCache> dataResolverCache;

	private ExtendedDataModelProject dataModelProject;

	public RoqProjectExtension() {
		this.dataResolverCache = new HashMap<>();
	}

	@Override
	public void init(ExtendedDataModelProject dataModelProject) {
		this.dataModelProject = dataModelProject;
		enabled = dataModelProject.hasProjectFeature(ProjectFeature.Roq);
		if (enabled) {
			if (dataDir == null) {
				// It is a Roq Project
				Path roqDir = dataModelProject.getConfigAsPath(RoqConfig.ROG_DIR);
				if (roqDir != null) {
					dataDir = roqDir.resolve(dataModelProject.getConfig(RoqConfig.ROG_DATA_DIR));
					if (dataDir != null) {
						loadRoqDataFiles(dataModelProject);
					}
				}
			}
		} else {
			dataDir = null;
		}
	}

	private void loadRoqDataFiles(ExtendedDataModelProject dataModelProject) {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir)) {
			for (Path file : stream) {
				if (Files.isRegularFile(file)) {
					regsterRoqDataFile(file, dataModelProject);
				}
			}
		} catch (IOException e) {
			// ignore error
		}
	}

	private void regsterRoqDataFile(Path file, ExtendedDataModelProject dataModelProject) {
		String fileExtension = getFileExtension(file);
		DataLoader dataLoader = dataLoaderRegistrty.get(fileExtension);
		if (dataLoader != null) {
			RoqDataFile cdiResolver = new RoqDataFile(file, "cdi", dataLoader);
			RoqDataFile injectResolver = cdiResolver.create("inject");
			dataResolverCache.put(file, new ResolverCache(cdiResolver, injectResolver));
			dataModelProject.getCustomValueResolvers().add(cdiResolver);
			dataModelProject.getCustomValueResolvers().add(injectResolver);
		}
	}

	@Override
	public void doComplete(CompletionRequest completionRequest, Part part, Parts parts,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			Set<CompletionItem> completionItems, CancelChecker cancelChecker) {
		// TODO Auto-generated method stub

	}

	@Override
	public void definition(Part part, List<LocationLink> locationLinks, CancelChecker cancelChecker) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean validateExpression(Parts parts, QuteValidationSettings validationSettings,
			ResolvingJavaTypeContext resolvingJavaTypeContext, List<Diagnostic> diagnostics) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean didChangeWatchedFile(Path filePath, Set<FileChangeType> changeTypes) {
		if (dataDir != null && filePath.startsWith(dataDir)) {
			if (changeTypes.contains(FileChangeType.Changed)) {
				// Data file changed
				ResolverCache resolver = dataResolverCache.remove(filePath);
				if (resolver != null) {
					dataModelProject.getCustomValueResolvers().remove(resolver.cdi);
					dataModelProject.getCustomValueResolvers().remove(resolver.inject);
				}
				regsterRoqDataFile(filePath, dataModelProject);
				return true;
			} else if (changeTypes.contains(FileChangeType.Created)) {
				// Data file created
				regsterRoqDataFile(filePath, dataModelProject);
				return true;
			} else if (changeTypes.contains(FileChangeType.Deleted)) {
				// Data file deleted
				ResolverCache resolver = dataResolverCache.remove(filePath);
				if (resolver != null) {
					dataModelProject.getCustomValueResolvers().remove(resolver.cdi);
					dataModelProject.getCustomValueResolvers().remove(resolver.inject);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	@Override
	public void doHover(Part part, List<Hover> hovers, CancelChecker cancelChecker) {
		// TODO Auto-generated method stub

	}

	@Override
	public void inlayHint(Expression node, List<InlayHint> inlayHints, CancelChecker cancelChecker) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	static String getFileExtension(Path path) {
		String name = path.getFileName().toString();
		int lastDot = name.lastIndexOf('.');
		return (lastDot == -1) ? "" : name.substring(lastDot + 1);
	}
}
