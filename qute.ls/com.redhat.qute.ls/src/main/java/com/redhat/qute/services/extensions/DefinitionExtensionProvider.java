package com.redhat.qute.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.LocationLink;

import com.redhat.qute.parser.expression.Part;

public interface DefinitionExtensionProvider {

	List<? extends LocationLink> getLocations(Part part);

}
