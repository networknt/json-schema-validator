package com.networknt.schema.walk;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

public class KeywordWalkListenerRunner {

	private Map<String, List<KeywordWalkListener>> keywordWalkListenersMap;

	public KeywordWalkListenerRunner(Map<String, List<KeywordWalkListener>> keywordWalkListenersMap) {
		this.keywordWalkListenersMap = keywordWalkListenersMap;
	}

	public void runPreWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at, String schemaPath,
			JsonNode schemaNode, JsonSchema parentSchema) {
		String keyword = getKeywordName(keyWordPath);
		KeywordWalkEvent keywordWalkEvent = constructKeywordWalkEvent(keyword, node, rootNode, at, schemaPath,
				schemaNode, parentSchema);
		List<KeywordWalkListener> allKeywordListeners = keywordWalkListenersMap
				.get(JsonSchemaFactory.ALL_KEYWORD_WALK_LISTENER_KEY);
		// Run Listeners that are setup for all keywords.
		if (allKeywordListeners != null) {
			for (KeywordWalkListener jsonKeywordWalkListener : allKeywordListeners) {
				jsonKeywordWalkListener.onWalkStart(keywordWalkEvent);
			}
		}
		// Run Listeners that are setup only for this keyword.
		List<KeywordWalkListener> currentKeywordListeners = keywordWalkListenersMap.get(keyword);
		if (currentKeywordListeners != null) {
			for (KeywordWalkListener jsonKeywordWalkListener : currentKeywordListeners) {
				jsonKeywordWalkListener.onWalkStart(keywordWalkEvent);
			}
		}
	}

	public void runPostWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at, String schemaPath,
			JsonNode schemaNode, JsonSchema parentSchema, Set<ValidationMessage> validationMessages) {
		String keyword = getKeywordName(keyWordPath);
		KeywordWalkEvent keywordWalkEvent = constructKeywordWalkEvent(keyword, node, rootNode, at, schemaPath,
				schemaNode, parentSchema);
		List<KeywordWalkListener> allKeywordListeners = keywordWalkListenersMap
				.get(JsonSchemaFactory.ALL_KEYWORD_WALK_LISTENER_KEY);
		// Run Listeners that are setup for all keywords.
		if (allKeywordListeners != null) {
			for (KeywordWalkListener jsonKeywordWalkListener : allKeywordListeners) {
				jsonKeywordWalkListener.onWalkEnd(keywordWalkEvent, validationMessages);
			}
		}
		// Run Listeners that are setup only for this keyword.
		List<KeywordWalkListener> currentKeywordListeners = keywordWalkListenersMap.get(keyword);
		if (currentKeywordListeners != null) {
			for (KeywordWalkListener jsonKeywordWalkListener : currentKeywordListeners) {
				jsonKeywordWalkListener.onWalkEnd(keywordWalkEvent, validationMessages);
			}
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
