package com.redhat.qute.project.extensions.roq.data.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.parser.yaml.YamlASTVisitor;
import com.redhat.qute.parser.yaml.YamlDocument;
import com.redhat.qute.parser.yaml.YamlMapping;
import com.redhat.qute.parser.yaml.YamlNode;
import com.redhat.qute.parser.yaml.YamlProperty;
import com.redhat.qute.parser.yaml.YamlScalar;
import com.redhat.qute.parser.yaml.YamlSequence;
import com.redhat.qute.parser.yaml.scanner.YamlTokenType;
import com.redhat.qute.project.extensions.roq.data.ArrayDataMapping;
import com.redhat.qute.project.extensions.roq.data.RoqDataField;
import com.redhat.qute.project.extensions.roq.data.RoqDataFile;

/**
 * Visitor that builds JavaFieldInfo from YAML AST
 */
class YamlFieldBuilder extends YamlASTVisitor {

	private final List<JavaFieldInfo> rootFields = new ArrayList<>();
	private final Stack<Context> contextStack = new Stack<>();
	private final RoqDataFile fileInfo;

	public YamlFieldBuilder(RoqDataFile fileInfo) {
		this.fileInfo = fileInfo;
	}

	public List<JavaFieldInfo> getFields() {
		return rootFields;
	}

	@Override
	public boolean visit(YamlMapping node) {
		// Push mapping context
		contextStack.push(new Context(ContextType.MAPPING, node));
		return true;
	}

	@Override
	public void endVisit(YamlMapping node) {
		// Pop mapping context
		if (!contextStack.isEmpty() && contextStack.peek().node == node) {
			contextStack.pop();
		}
	}

	@Override
	public boolean visit(YamlSequence node) {
		// Push sequence context
		contextStack.push(new Context(ContextType.SEQUENCE, node));
		return true;
	}

	@Override
	public void endVisit(YamlSequence node) {
		// Pop sequence context
		if (!contextStack.isEmpty() && contextStack.peek().node == node) {
			contextStack.pop();
		}
	}

	@Override
	public boolean visit(YamlProperty node) {
		YamlNode keyNode = node.getKey();
		if (!(keyNode instanceof YamlScalar)) {
			return false;
		}

		String fieldName = ((YamlScalar) keyNode).getValue();
		if (fieldName == null) {
			return false;
		}

		String currentPath = buildPath(fieldName);
		YamlNode valueNode = node.getValue();

		if (valueNode == null) {
			// Property without value
			RoqDataField field = new RoqDataField(fieldName, null, fileInfo);
			field.setSignature(fieldName + " : java.lang.String");
			addFieldToCurrentContext(field);
			return false;
		}

		// Create field based on value type
		JavaFieldInfo field = createFieldFromValue(fieldName, valueNode);
		if (field != null) {
			addFieldToCurrentContext(field);
		}

		return false; // Don't visit children, we handle them in createFieldFromValue
	}

	private JavaFieldInfo createFieldFromValue(String fieldName, YamlNode valueNode) {
		if (valueNode instanceof YamlScalar) {
			YamlScalar scalar = (YamlScalar) valueNode;
			String javaType = inferJavaType(scalar);
			RoqDataField field = new RoqDataField(fieldName, null, fileInfo);
			field.setSignature(fieldName + " : " + javaType);
			return field;

		} else if (valueNode instanceof YamlMapping) {
			YamlMapping mapping = (YamlMapping) valueNode;
			ResolvedJavaTypeInfo objectType = new ResolvedJavaTypeInfo();
			objectType.setResolvedType(objectType);

			List<JavaFieldInfo> nestedFields = extractFieldsFromMapping(mapping);
			objectType.setFields(nestedFields);
			objectType.setSignature(fieldName + " : java.lang.Object");

			RoqDataField field = new RoqDataField(fieldName, objectType, fileInfo);
			field.setSignature(fieldName + " : java.lang.Object");
			return field;

		} else if (valueNode instanceof YamlSequence) {
			YamlSequence sequence = (YamlSequence) valueNode;
			ResolvedJavaTypeInfo itemType = inferSequenceItemType(fieldName, sequence);
			ResolvedJavaTypeInfo arrayType = new ArrayDataMapping(itemType);

			RoqDataField field = new RoqDataField(fieldName, arrayType, fileInfo);
			field.setSignature(fieldName + " : java.util.Collection<" + itemType.getJavaElementType() + ">");
			return field;
		}

		return null;
	}

	private List<JavaFieldInfo> extractFieldsFromMapping(YamlMapping mapping) {
		List<JavaFieldInfo> fields = new ArrayList<>();

		for (int i = 0; i < mapping.getChildCount(); i++) {
			YamlNode child = mapping.getChild(i);
			if (child instanceof YamlProperty) {
				YamlProperty property = (YamlProperty) child;
				YamlNode keyNode = property.getKey();

				if (keyNode instanceof YamlScalar) {
					String fieldName = ((YamlScalar) keyNode).getValue();
					if (fieldName != null) {
						YamlNode valueNode = property.getValue();

						if (valueNode != null) {
							JavaFieldInfo field = createFieldFromValue(fieldName, valueNode);
							if (field != null) {
								fields.add(field);
							}
						}
					}
				}
			}
		}

		return fields;
	}

	private ResolvedJavaTypeInfo inferSequenceItemType(String arrayPath, YamlSequence sequence) {
		if (sequence.getChildCount() == 0) {
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setSignature("java.lang.String");
			return type;
		}

		YamlNode firstItem = sequence.getChild(0);

		if (firstItem instanceof YamlScalar) {
			YamlScalar scalar = (YamlScalar) firstItem;
			String javaType = inferJavaType(scalar);
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setSignature(javaType);
			return type;

		} else if (firstItem instanceof YamlMapping) {
			YamlMapping mapping = (YamlMapping) firstItem;
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setResolvedType(type);

			List<JavaFieldInfo> fields = extractFieldsFromMapping(mapping);
			type.setFields(fields);
			type.setSignature("java.lang.Object");
			return type;
		}

		ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
		type.setSignature("java.lang.String");
		return type;
	}

	private String buildPath(String fieldName) {
		StringBuilder path = new StringBuilder();

		for (Context ctx : contextStack) {
			if (ctx.type == ContextType.SEQUENCE) {
				// Get the property that owns this sequence
				if (ctx.node.getParent() instanceof YamlProperty) {
					YamlProperty prop = (YamlProperty) ctx.node.getParent();
					YamlNode keyNode = prop.getKey();
					if (keyNode instanceof YamlScalar) {
						if (path.length() > 0)
							path.append(".");
						path.append(((YamlScalar) keyNode).getValue());
						path.append("[]");
					}
				}
			} else if (ctx.type == ContextType.MAPPING) {
				// Check if this mapping is inside a property
				if (ctx.node.getParent() instanceof YamlProperty) {
					YamlProperty prop = (YamlProperty) ctx.node.getParent();
					// Only add if not root
					if (!(prop.getParent() instanceof YamlMapping
							&& prop.getParent().getParent() instanceof YamlDocument)) {
						YamlNode keyNode = prop.getKey();
						if (keyNode instanceof YamlScalar) {
							if (path.length() > 0)
								path.append(".");
							path.append(((YamlScalar) keyNode).getValue());
						}
					}
				}
			}
		}

		if (path.length() > 0)
			path.append(".");
		path.append(fieldName);

		return path.toString();
	}

	private void addFieldToCurrentContext(JavaFieldInfo field) {
		// Add to root if we're at document level
		rootFields.add(field);
	}

	private String inferJavaType(YamlScalar scalar) {
		YamlTokenType scalarType = scalar.getScalarType();

		switch (scalarType) {
		case ScalarNumber:
			String value = scalar.getValue();
			if (value != null && value.contains(".")) {
				return "java.lang.Double";
			}
			return "java.lang.Integer";
		case ScalarBoolean:
			return "java.lang.Boolean";
		case ScalarNull:
			return "java.lang.Object";
		case ScalarString:
		default:
			return "java.lang.String";
		}
	}

	static enum ContextType {
		MAPPING, SEQUENCE
	}

	private static class Context {
		ContextType type;
		YamlNode node;

		Context(ContextType type, YamlNode node) {
			this.type = type;
			this.node = node;
		}
	}
}
