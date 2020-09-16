package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;

/**
 * 
 * Encapsulation of Walk data that is passed into the {@link WalkListener}.
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

	static class KeywordWalkEventBuilder {
		private WalkEvent keywordWalkEvent = null;

		KeywordWalkEventBuilder() {
			keywordWalkEvent = new WalkEvent();
		}

		public KeywordWalkEventBuilder schemaPath(String schemaPath) {
			keywordWalkEvent.schemaPath = schemaPath;
			return this;
		}

		public KeywordWalkEventBuilder schemaNode(JsonNode schemaNode) {
			keywordWalkEvent.schemaNode = schemaNode;
			return this;
		}

		public KeywordWalkEventBuilder parentSchema(JsonSchema parentSchema) {
			keywordWalkEvent.parentSchema = parentSchema;
			return this;
		}

		public KeywordWalkEventBuilder keyWordName(String keyWordName) {
			keywordWalkEvent.keyWordName = keyWordName;
			return this;
		}

		public KeywordWalkEventBuilder node(JsonNode node) {
			keywordWalkEvent.node = node;
			return this;
		}

		public KeywordWalkEventBuilder rootNode(JsonNode rootNode) {
			keywordWalkEvent.rootNode = rootNode;
			return this;
		}

		public KeywordWalkEventBuilder at(String at) {
			keywordWalkEvent.at = at;
			return this;
		}

		public WalkEvent build() {
			return keywordWalkEvent;
		}

	}

	public static KeywordWalkEventBuilder builder() {
		return new KeywordWalkEventBuilder();
	}
}
