package com.redhat.qute.services.commands;

import static com.redhat.qute.QuteAssert.TEMPLATE_BASE_DIR;
import static com.redhat.qute.project.QuteQuickStartProject.PROJECT_URI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.GenerateTemplateInfo;
import com.redhat.qute.project.MockQuteProjectRegistry;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.settings.SharedSettings;

public class QuteGenerateTemplateContentCommandHandlerTest {

	@Test
	public void generateItem() throws InterruptedException, ExecutionException, Exception {

		QuteGenerateTemplateContentCommandHandler command = new QuteGenerateTemplateContentCommandHandler(
				createJavaDataModelCache());
		ExecuteCommandParams params = new ExecuteCommandParams("", Arrays.asList(createItemInfo()));
		String result = (String) command.executeCommand(params, new SharedSettings(), //
				() -> {
				}).get();
		System.err.println(result);
	}

	private JavaDataModelCache createJavaDataModelCache() {
		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		projectRegistry.getProject(new ProjectInfo(PROJECT_URI, TEMPLATE_BASE_DIR));
		return new JavaDataModelCache(projectRegistry);
	}

	@Test
	public void generateListItems() throws InterruptedException, ExecutionException, Exception {
		QuteGenerateTemplateContentCommandHandler command = new QuteGenerateTemplateContentCommandHandler(
				createJavaDataModelCache());
		ExecuteCommandParams params = new ExecuteCommandParams("", Arrays.asList(createListItemsInfo()));
		String result = (String) command.executeCommand(params, new SharedSettings(), //
				() -> {
				}).get();
		System.err.println(result);
	}

	private GenerateTemplateInfo createListItemsInfo() {
		GenerateTemplateInfo info = new GenerateTemplateInfo();
		info.setProjectUri(PROJECT_URI);
		List<DataModelParameter> parameters = new ArrayList<>();
		DataModelParameter parameter = new DataModelParameter();
		parameter.setKey("items");
		parameter.setSourceType("java.util.List<org.acme.Item>");
		parameters.add(parameter);
		info.setParameters(parameters);
		return info;
	}

	private GenerateTemplateInfo createItemInfo() {
		GenerateTemplateInfo info = new GenerateTemplateInfo();
		info.setProjectUri(PROJECT_URI);
		List<DataModelParameter> parameters = new ArrayList<>();
		DataModelParameter parameter = new DataModelParameter();
		parameter.setKey("item");
		parameter.setSourceType("org.acme.Item");
		parameters.add(parameter);
		info.setParameters(parameters);
		return info;
	}
}
