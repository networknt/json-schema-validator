package com.networknt.schema.walk;

import java.util.Collection;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;

public class KeywordWalkListenerRunner {

	private Collection<KeywordWalkListener> jsonKeywordWalkListeners;

	public KeywordWalkListenerRunner(Collection<KeywordWalkListener> jsonKeywordWalkListeners) {
		this.jsonKeywordWalkListeners = jsonKeywordWalkListeners;
	}

	public void runPreWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at, String schemaPath,
			JsonNode schemaNode, JsonSchema parentSchema) {
		KeywordWalkEvent keywordWalkEvent = constructKeywordWalkEvent(getKeywordName(keyWordPath), node, rootNode, at,
				schemaPath, schemaNode, parentSchema);
		for (KeywordWalkListener jsonKeywordWalkListener : jsonKeywordWalkListeners) {
			jsonKeywordWalkListener.onWalkStart(keywordWalkEvent);
		}
	}

	public void runPostWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at, String schemaPath,
			JsonNode schemaNode, JsonSchema parentSchema, Set<ValidationMessage> validationMessages) {
		KeywordWalkEvent keywordWalkEvent = constructKeywordWalkEvent(getKeywordName(keyWordPath), node, rootNode, at,
				schemaPath, schemaNode, parentSchema);
		for (KeywordWalkListener jsonKeywordWalkListener : jsonKeywordWalkListeners) {
			jsonKeywordWalkListener.onWalkEnd(keywordWalkEvent, validationMessages);
		}
	}

	private String getKeywordName(String keyWordPath) {
		return keyWordPath.substring(keyWordPath.lastIndexOf('/') + 1);
	}

	private KeywordWalkEvent constructKeywordWalkEvent(String keyWordName, JsonNode node, JsonNode rootNode, String at,
			String schemaPath, JsonNode schemaNode, JsonSchema parentSchema) {
		return KeywordWalkEvent.builder().at(at).keyWordName(keyWordName).node(node).parentSchema(parentSchema)
				.rootNode(rootNode).schemaNode(schemaNode).schemaPath(schemaPath).build();
	}
}
