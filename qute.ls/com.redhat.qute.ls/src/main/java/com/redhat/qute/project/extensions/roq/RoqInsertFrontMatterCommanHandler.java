package com.redhat.qute.project.extensions.roq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.QuteLanguageServer;
import com.redhat.qute.services.commands.ArgumentsUtils;
import com.redhat.qute.services.commands.IDelegateCommandHandler;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;

public class RoqInsertFrontMatterCommanHandler implements IDelegateCommandHandler {

	public static final String COMMAND_ID = "qute.roq.frontmatter.insert";
	private QuteLanguageServer quteLanguageServer;

	public RoqInsertFrontMatterCommanHandler(QuteLanguageServer quteLanguageServer) {
		this.quteLanguageServer = quteLanguageServer;
	}

	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params, SharedSettings sharedSettings,
			CancelChecker cancelChecker) throws Exception {
		String uri = ArgumentsUtils.getArgAt(params, 0, String.class);

		List<TextEdit> edits = new ArrayList<>();
		TextEdit edit = new TextEdit();
		edit.setRange(QutePositionUtility.ZERO_RANGE);
		edit.setNewText("---" + System.lineSeparator() + //
				"layout: default" + System.lineSeparator() + //
				"title: My title" + System.lineSeparator() + //
				"---" + System.lineSeparator());
		edits.add(edit);

		WorkspaceEdit workspaceEdit = new WorkspaceEdit();
		workspaceEdit.setChanges(Collections.singletonMap(uri, edits));

		ApplyWorkspaceEditParams applyParams = new ApplyWorkspaceEditParams(workspaceEdit);

		return quteLanguageServer.getLanguageClient().applyEdit(applyParams).thenApply(result -> {
			return (Object) result;
		});
	}

}
