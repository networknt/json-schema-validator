package com.networknt.schema.walk;

import java.util.Set;

import com.networknt.schema.ValidationMessage;

/**
 * 
 * Listener class that captures walkStart and walkEnd events.
 *
 */
public interface KeywordWalkListener {

	public void onWalkStart(KeywordWalkEvent keywordWalkEvent);

	public void onWalkEnd(KeywordWalkEvent keywordWalkEvent, Set<ValidationMessage> validationMessages);
}
