package com.redhat.qute.services.extensions;

import org.eclipse.lsp4j.Hover;

import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.services.hover.HoverRequest;

public interface HoverExtensionProvider {

	Hover getHover(Part part, HoverRequest hoverRequest);

}
