package org.eclipse.lsp4mp.jdt.internal.core.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.TextEdit;

public class ChangeUtil {

	private static final Range ZERO_RANGE = new Range(new Position(), new Position());

	/**
	 * Converts Change to WorkspaceEdit for further consumption.
	 *
	 * @param change {@link Change} to convert
	 * @return {@link WorkspaceEdit} converted from the change
	 * @throws CoreException
	 */
	public static WorkspaceEdit convertToWorkspaceEdit(Change change, String uri, IJDTUtils utils,
			boolean resourceOperationSupported) throws CoreException {
		WorkspaceEdit edit = new WorkspaceEdit();
		if (change instanceof CompositeChange) {
			convertCompositeChange((CompositeChange) change, edit, uri, utils, resourceOperationSupported);
		} else {
			convertSingleChange(change, edit, uri, utils, resourceOperationSupported);
		}
		return edit;
	}

	private static void convertCompositeChange(CompositeChange change, WorkspaceEdit edit, String uri, IJDTUtils utils,
			boolean resourceOperationSupported) throws CoreException {
		Change[] changes = change.getChildren();
		for (Change ch : changes) {
			if (ch instanceof CompositeChange) {
				convertCompositeChange((CompositeChange) ch, edit, uri, utils, resourceOperationSupported);
			} else {
				convertSingleChange(ch, edit, uri, utils, resourceOperationSupported);
			}
		}
	}

	private static void convertSingleChange(Change change, WorkspaceEdit edit, String uri, IJDTUtils utils,
			boolean resourceOperationSupported) throws CoreException {
		if (change instanceof CompositeChange) {
			return;
		}

		if (change instanceof TextChange) {
			convertTextChange((TextChange) change, edit, uri, utils, resourceOperationSupported);
		}
		// else if (change instanceof ResourceChange) {
		// convertResourceChange((ResourceChange) change, edit);
		// }
	}

	private static void convertTextChange(TextChange textChange, WorkspaceEdit rootEdit, String uri, IJDTUtils utils,
			boolean resourceOperationSupported) {
		Object modifiedElement = textChange.getModifiedElement();
		if (!(modifiedElement instanceof IJavaElement)) {
			return;
		}

		TextEdit textEdits = textChange.getEdit();
		if (textEdits == null) {
			return;
		}
		ICompilationUnit compilationUnit = (ICompilationUnit) ((IJavaElement) modifiedElement)
				.getAncestor(IJavaElement.COMPILATION_UNIT);
		convertTextEdit(rootEdit, compilationUnit, textEdits, uri, utils, resourceOperationSupported);
	}

	private static void convertTextEdit(WorkspaceEdit root, ICompilationUnit unit, TextEdit edit, String uri,
			IJDTUtils utils, boolean resourceOperationSupported) {
		if (edit == null) {
			return;
		}

		TextEditConverter converter = new TextEditConverter(unit, edit, uri, utils);
		if (resourceOperationSupported) {
			List<Either<TextDocumentEdit, ResourceOperation>> changes = root.getDocumentChanges();
			if (changes == null) {
				changes = new ArrayList<>();
				root.setDocumentChanges(changes);
			}

			VersionedTextDocumentIdentifier identifier = new VersionedTextDocumentIdentifier(uri, 0);
			TextDocumentEdit documentEdit = new TextDocumentEdit(identifier, converter.convert());
			changes.add(Either.forLeft(documentEdit));
		} else {

			Map<String, List<org.eclipse.lsp4j.TextEdit>> changes = root.getChanges();
			if (changes.containsKey(uri)) {
				changes.get(uri).addAll(converter.convert());
			} else {
				changes.put(uri, converter.convert());
			}
		}
	}

	/**
	 * @return <code>true</code> if a {@link WorkspaceEdit} contains any actual
	 *         changes, <code>false</code> otherwise.
	 */
	public static boolean hasChanges(WorkspaceEdit edit) {
		if (edit == null) {
			return false;
		}
		if (edit.getDocumentChanges() != null && !edit.getDocumentChanges().isEmpty()) {
			return true;
		}
		boolean hasChanges = false;
		// @formatter:off
		if ((edit.getChanges() != null && !edit.getChanges().isEmpty())) {
			hasChanges = edit.getChanges().values().stream()
					.filter(changes -> changes != null && !changes.isEmpty() && hasChanges(changes)).findFirst()
					.isPresent();
		}
		// @formatter:on
		return hasChanges;
	}

	/**
	 * @return <code>true</code> if a list of {@link org.eclipse.lsp4j.TextEdit}
	 *         contains any actual changes, <code>false</code> otherwise.
	 */
	public static boolean hasChanges(List<org.eclipse.lsp4j.TextEdit> edits) {
		if (edits == null) {
			return false;
		}
		// @formatter:off
		return edits.stream().filter(edit -> (!edit.getRange().equals(ZERO_RANGE) || !"".equals(edit.getNewText())))
				.findFirst().isPresent();
		// @formatter:on
	}
}
