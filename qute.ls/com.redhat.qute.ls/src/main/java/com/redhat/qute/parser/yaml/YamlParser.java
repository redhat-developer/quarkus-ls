package com.redhat.qute.parser.yaml;

import java.util.Stack;

import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.scanner.Scanner;
import com.redhat.qute.parser.yaml.scanner.YamlScanner;
import com.redhat.qute.parser.yaml.scanner.YamlScannerState;
import com.redhat.qute.parser.yaml.scanner.YamlTokenType;

/**
 * YAML parser (fault-tolerant).
 */
public class YamlParser {

	public static YamlDocument parse(String content, String uri) {
		return parse(new TextDocument(content, uri));
	}

	public static YamlDocument parse(TextDocument textDocument) {
		return parse(textDocument, 0, textDocument.getText().length(), CancelChecker.NO_CANCELLABLE);
	}

	public static YamlDocument parse(TextDocument textDocument, int start, int end, CancelChecker cancelChecker) {
		YamlDocument document = new YamlDocument(textDocument);
		document.setCancelChecker(cancelChecker);
		document.setStart(start);

		String content = textDocument.getText();
		Scanner<YamlTokenType, YamlScannerState> scanner = YamlScanner.createScanner(content, start, end);

		YamlNode curr = document;
		Stack<Integer> indentStack = new Stack<>();
		indentStack.push(-1); // Document level

		int currentIndent = 0;
		boolean afterNewline = true;

		YamlProperty currentProperty = null;
		YamlScalar currentScalar = null;

		YamlTokenType token = scanner.scan();
		while (token != YamlTokenType.EOS) {

			switch (token) {

			case Whitespace:
				if (afterNewline && !isInFlowCollection(curr)) {
					currentIndent = scanner.getTokenText().length();
				}
				break;

			case Newline:
				if (!isInFlowCollection(curr)) {
					afterNewline = true;
					currentIndent = 0;

					if (currentProperty != null && currentProperty.getValue() != null) {
						currentProperty.setClosed(true);
						//currentProperty.setEnd(scanner.getTokenOffset());
						currentProperty = null;
					}
				}
				break;

			case StartComment:
				afterNewline = false;
				int commentStart = scanner.getTokenOffset();
				int commentEnd = scanner.getTokenEnd();
				YamlComment comment = new YamlComment(commentStart, commentEnd);

				YamlNode commentParent = curr;
				while (commentParent != null && commentParent.getKind() == YamlNodeKind.YamlProperty) {
					commentParent = commentParent.getParent();
				}
				if (commentParent != null) {
					commentParent.addChild(comment);
				} else {
					curr.addChild(comment);
				}
				curr = comment;
				break;

			case Comment:
				if (curr.getKind() == YamlNodeKind.YamlComment) {
					YamlComment yamlComment = (YamlComment) curr;
					yamlComment.setStartContentOffset(scanner.getTokenOffset());
					yamlComment.setEndContentOffset(scanner.getTokenEnd());
					yamlComment.setEnd(scanner.getTokenEnd());
					yamlComment.setClosed(true);
					curr = curr.getParent();
				}
				break;

			case Key:
				afterNewline = false;

				// Handle indentation - close structures when indentation decreases
				if (!isInFlowCollection(curr)) {
					while (!indentStack.isEmpty() && currentIndent < indentStack.peek()) {
						indentStack.pop();
						if (curr.getParent() != null && curr.getKind() != YamlNodeKind.YamlDocument) {
							curr.setEnd(scanner.getTokenOffset());
							curr.setClosed(true);
							curr = curr.getParent();
						}
					}
				}

				// Check if we need to create a nested mapping as a property value
				// This happens when: person:\n name: John (key at higher indentation after
				// colon)
				if (!isInFlowCollection(curr) && curr.getKind() == YamlNodeKind.YamlMapping) {
					if (curr.getChildCount() > 0) {
						YamlNode lastChild = curr.getChild(curr.getChildCount() - 1);
						if (lastChild.getKind() == YamlNodeKind.YamlProperty) {
							YamlProperty prop = (YamlProperty) lastChild;
							// Property has colon, no value, and we're at higher indentation
							if (prop.getColonOffset() != -1 && prop.getValue() == null
									&& currentIndent > indentStack.peek()) {
								// Create nested mapping as property value
								YamlMapping nestedMapping = new YamlMapping(scanner.getTokenOffset(),
										scanner.getTokenEnd(), false);
								prop.setValue(nestedMapping);
								curr = nestedMapping;
								indentStack.push(currentIndent);
							}
						}
					}
				}

				// If at document level, create root mapping
				if (curr.getKind() == YamlNodeKind.YamlDocument) {
					YamlMapping mapping = new YamlMapping(scanner.getTokenOffset(), scanner.getTokenEnd(), false);
					curr.addChild(mapping);
					curr = mapping;
					indentStack.push(currentIndent);
				}
				// If in a sequence at the expected indentation, create a mapping for the item
				else if (curr.getKind() == YamlNodeKind.YamlSequence) {
					// Check if we need a new mapping or can reuse the last one
					YamlNode lastChild = curr.getChildCount() > 0 ? curr.getChild(curr.getChildCount() - 1) : null;

					if (lastChild == null || lastChild.getKind() != YamlNodeKind.YamlMapping || lastChild.isClosed()) {
						// Create a new mapping for this sequence item
						YamlMapping itemMapping = new YamlMapping(scanner.getTokenOffset(), scanner.getTokenEnd(),
								false);
						curr.addChild(itemMapping);
						curr = itemMapping;
						indentStack.push(currentIndent);
					} else {
						// Continue with the existing mapping
						curr = lastChild;
					}
				}

				// Create the property
				currentProperty = new YamlProperty(scanner.getTokenOffset(), scanner.getTokenEnd());
				YamlScalar keyScalar = new YamlScalar(scanner.getTokenOffset(), scanner.getTokenEnd(),
						YamlTokenType.ScalarString);
				currentProperty.setKey(keyScalar);

				if (curr.getKind() == YamlNodeKind.YamlMapping) {
					curr.addChild(currentProperty);
				}
				break;

			case Colon:
				afterNewline = false;
				if (currentProperty != null) {
					currentProperty.setColonOffset(scanner.getTokenOffset());
				}
				break;

			case Dash:
				afterNewline = false;

				// Handle indentation
				if (!isInFlowCollection(curr)) {
					while (!indentStack.isEmpty() && currentIndent < indentStack.peek()) {
						indentStack.pop();

						YamlNode parent = curr.getParent();
						while (parent != null && parent.getKind() != YamlNodeKind.YamlSequence) {
							parent = parent.getParent();
						}
						if (parent != null) {
							curr = parent;
						}
					}
				}

				// Determine where to create the sequence
				YamlSequence sequence = null;

				if (curr.getKind() == YamlNodeKind.YamlDocument) {
					// Top-level sequence
					sequence = new YamlSequence(scanner.getTokenOffset(), scanner.getTokenEnd(), false);
					curr.addChild(sequence);
					curr = sequence;
					indentStack.push(currentIndent);
				} else if (curr.getKind() == YamlNodeKind.YamlMapping && currentProperty != null
						&& currentProperty.getValue() == null && currentProperty.getColonOffset() != -1) {
					// Sequence as a property value (e.g., items:\n- ...)
					sequence = new YamlSequence(scanner.getTokenOffset(), scanner.getTokenEnd(), false);
					currentProperty.setValue(sequence);
					currentProperty = null;
					curr = sequence;
					indentStack.push(currentIndent);
				} else if (curr.getKind() == YamlNodeKind.YamlSequence) {
					// Already in a sequence - close previous mapping if exists
					if (curr.getChildCount() > 0) {
						YamlNode lastChild = curr.getChild(curr.getChildCount() - 1);
						if (lastChild.getKind() == YamlNodeKind.YamlMapping && !lastChild.isClosed()) {
							lastChild.setClosed(true);
							lastChild.setEnd(scanner.getTokenOffset());
						}
					}
				} else if (curr.getKind() == YamlNodeKind.YamlMapping && curr.getParent() != null
						&& curr.getParent().getKind() == YamlNodeKind.YamlSequence) {
					// We're in a mapping within a sequence, close it and return to sequence
					curr.setClosed(true);
					curr.setEnd(scanner.getTokenOffset());
					curr = curr.getParent();
				}
				break;

			case ScalarString:
			case ScalarNumber:
			case ScalarBoolean:
			case ScalarNull:
			case Value:
				afterNewline = false;
				currentScalar = new YamlScalar(scanner.getTokenOffset(), scanner.getTokenEnd(), token);

				if (currentProperty != null && currentProperty.getValue() == null
						&& currentProperty.getColonOffset() != -1) {
					// Scalar as property value
					currentProperty.setValue(currentScalar);
					currentProperty.setEnd(scanner.getTokenEnd());
				} else if (curr.getKind() == YamlNodeKind.YamlSequence) {
					curr.addChild(currentScalar);
					curr.setEnd(scanner.getTokenEnd());
				} else {
					curr.addChild(currentScalar);
				}
				currentScalar = null;
				break;

			case StartString:
				afterNewline = false;
				int stringStart = scanner.getTokenEnd();
				currentScalar = new YamlScalar(stringStart, stringStart, YamlTokenType.ScalarString);
				currentScalar.setQuoted(true);
				break;

			case String:
				if (currentScalar != null) {
					if (currentScalar.getStart() == currentScalar.getEnd()) {
						currentScalar.setStart(scanner.getTokenOffset());
					}
					currentScalar.setEnd(scanner.getTokenEnd());
				}
				break;

			case EndString:
				if (currentScalar != null) {
					if (currentProperty != null && currentProperty.getValue() == null
							&& currentProperty.getColonOffset() != -1) {
						currentProperty.setValue(currentScalar);
						currentProperty.setEnd(scanner.getTokenEnd());
					} else if (curr.getKind() == YamlNodeKind.YamlSequence) {
						curr.addChild(currentScalar);
						curr.setEnd(scanner.getTokenEnd());
					} else if (curr.getKind() == YamlNodeKind.YamlMapping && currentProperty != null
							&& currentProperty.getKey() == null) {
						currentProperty.setKey(currentScalar);
					} else {
						curr.addChild(currentScalar);
					}
					currentScalar = null;
				}
				break;

			case ArrayOpen:
				afterNewline = false;
				YamlSequence flowSeq = new YamlSequence(scanner.getTokenOffset(), scanner.getTokenEnd(), true);

				if (currentProperty != null && currentProperty.getValue() == null
						&& currentProperty.getColonOffset() != -1) {
					currentProperty.setValue(flowSeq);
				} else if (curr.getKind() == YamlNodeKind.YamlSequence) {
					curr.addChild(flowSeq);
				} else {
					curr.addChild(flowSeq);
				}
				curr = flowSeq;
				break;

			case ArrayClose:
				afterNewline = false;
				if (curr.getKind() == YamlNodeKind.YamlSequence) {
					curr.setEnd(scanner.getTokenEnd());
					curr.setClosed(true);
					if (curr.getParent() != null) {
						curr = curr.getParent();
						if (curr != null && curr.getKind() == YamlNodeKind.YamlProperty) {
							curr = curr.getParent();
						}
					}
				}
				break;

			case ObjectOpen:
				afterNewline = false;
				YamlMapping flowMap = new YamlMapping(scanner.getTokenOffset(), scanner.getTokenEnd(), true);

				if (currentProperty != null && currentProperty.getValue() == null
						&& currentProperty.getColonOffset() != -1) {
					currentProperty.setValue(flowMap);
				} else if (curr.getKind() == YamlNodeKind.YamlSequence) {
					curr.addChild(flowMap);
				} else {
					curr.addChild(flowMap);
				}
				curr = flowMap;
				break;

			case ObjectClose:
				afterNewline = false;
				if (curr.getKind() == YamlNodeKind.YamlMapping) {
					curr.setEnd(scanner.getTokenEnd());
					curr.setClosed(true);
					if (curr.getParent() != null) {
						curr = curr.getParent();
						if (curr != null && curr.getKind() == YamlNodeKind.YamlProperty) {
							curr = curr.getParent();
						}
					}
				}
				break;

			case Comma:
				afterNewline = false;
				if (currentProperty != null) {
					currentProperty.setClosed(true);
					currentProperty.setEnd(scanner.getTokenOffset());
					currentProperty = null;
				}
				break;

			default:
				afterNewline = false;
				break;
			}

			token = scanner.scan();
		}

		// Close any remaining open nodes
		while (curr.getParent() != null) {
			curr.setEnd(end);
			if (!curr.isClosed()) {
				if (shouldBeClosed(curr)) {
					curr.setClosed(true);
				}
			}
			curr = curr.getParent();
		}

		return document;
	}

	private static boolean shouldBeClosed(YamlNode curr) {
		if (curr.getKind() == YamlNodeKind.YamlSequence || curr.getKind() == YamlNodeKind.YamlMapping) {
			return !((YamlCollectionNode) curr).isFlowStyle();
		}
		return true;
	}

	private static boolean isInFlowCollection(YamlNode node) {
		while (node != null) {
			if (node.getKind() == YamlNodeKind.YamlSequence || node.getKind() == YamlNodeKind.YamlMapping) {
				if (((YamlCollectionNode) node).isFlowStyle()) {
					return true;
				}
			}
			node = node.getParent();
		}
		return false;
	}
}