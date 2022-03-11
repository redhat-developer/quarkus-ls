/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.lsp4j.proposed;

import java.util.List;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.json.adapters.JsonElementTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4j.util.Preconditions;
import org.eclipse.xtext.xbase.lib.Pure;

import com.google.gson.annotations.JsonAdapter;

/**
 * Inlay hint information.
 *
 * @since 3.17.0 - proposed state
 */
public class InlayHint {

	/**
	 * The position of this hint.
	 */
	@NonNull
	private Position position;

	/**
	 * The label of this hint. A human readable string or an array of
	 * InlayHintLabelPart label parts.
	 *
	 * *Note* that neither the string nor the label part can be empty.
	 */
	@NonNull
	private Either<String, List<InlayHintLabelPart>> label;

	/**
	 * The kind of this hint. Can be omitted in which case the client should fall
	 * back to a reasonable default.
	 */
	private InlayHintKind kind;

	/**
	 * A data entry field that is preserved on a completion item between a
	 * completion and a completion resolve request.
	 */
	@JsonAdapter(JsonElementTypeAdapter.Factory.class)
	private Object data;

	public InlayHint(@NonNull Position position, Either<String, List<InlayHintLabelPart>> label) {
		this.position = Preconditions.<Position>checkNotNull(position, "position");
		this.label = label;

	}

	public InlayHint() {

	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public Either<String, List<InlayHintLabelPart>> getLabel() {
		return label;
	}

	public void setLabel(Either<String, List<InlayHintLabelPart>> label) {
		this.label = label;
	}

	public InlayHintKind getKind() {
		return kind;
	}

	public void setKind(InlayHintKind kind) {
		this.kind = kind;
	}

	/**
	 * A data entry field that is preserved on a completion item between a
	 * completion and a completion resolve request.
	 */
	@Pure
	public Object getData() {
		return this.data;
	}

	/**
	 * A data entry field that is preserved on a completion item between a
	 * completion and a completion resolve request.
	 */
	public void setData(final Object data) {
		this.data = data;
	}
}
