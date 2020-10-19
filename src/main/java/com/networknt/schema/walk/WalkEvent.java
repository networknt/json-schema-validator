package com.networknt.schema.walk;

import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;

/**
 * 
 * Encapsulation of Walk data that is passed into the {@link JsonSchemaWalkListener}.
 *
 */
public class WalkEvent {

	private String schemaPath;
	private JsonNode schemaNode;
	private JsonSchema parentSchema;
	private String keyWordName;
	private JsonNode node;
	private JsonNode rootNode;
	private String at;
	private JsonSchemaFactory currentJsonSchemaFactory;

	public String getSchemaPath() {
		return schemaPath;
	}

	public JsonNode getSchemaNode() {
		return schemaNode;
	}

	public JsonSchema getParentSchema() {
		return parentSchema;
	}

	public String getKeyWordName() {
		return keyWordName;
	}

	public JsonNode getNode() {
		return node;
	}

	public JsonNode getRootNode() {
		return rootNode;
	}

	public String getAt() {
		return at;
	}

	public JsonSchema getRefSchema(URI schemaUri) {
		return currentJsonSchemaFactory.getSchema(schemaUri);
	}

	static class WalkEventBuilder {
		private WalkEvent keywordWalkEvent = null;

		WalkEventBuilder() {
			keywordWalkEvent = new WalkEvent();
		}

		public WalkEventBuilder schemaPath(String schemaPath) {
			keywordWalkEvent.schemaPath = schemaPath;
			return this;
		}

		public WalkEventBuilder schemaNode(JsonNode schemaNode) {
			keywordWalkEvent.schemaNode = schemaNode;
			return this;
		}

		public WalkEventBuilder parentSchema(JsonSchema parentSchema) {
			keywordWalkEvent.parentSchema = parentSchema;
			return this;
		}

		public WalkEventBuilder keyWordName(String keyWordName) {
			keywordWalkEvent.keyWordName = keyWordName;
			return this;
		}

		public WalkEventBuilder node(JsonNode node) {
			keywordWalkEvent.node = node;
			return this;
		}

		public WalkEventBuilder rootNode(JsonNode rootNode) {
			keywordWalkEvent.rootNode = rootNode;
			return this;
		}

		public WalkEventBuilder at(String at) {
			keywordWalkEvent.at = at;
			return this;
		}

		public WalkEventBuilder currentJsonSchemaFactory(JsonSchemaFactory currentJsonSchemaFactory) {
			keywordWalkEvent.currentJsonSchemaFactory = currentJsonSchemaFactory;
			return this;
		}

		public WalkEvent build() {
			return keywordWalkEvent;
		}

	}

	public static WalkEventBuilder builder() {
		return new WalkEventBuilder();
	}

}
