package com.networknt.schema.walk;

import com.networknt.schema.ValidationMessage;

import java.util.Set;

/**
 * 
 * Listener class that captures walkStart and walkEnd events.
 *
 */
public interface JsonSchemaWalkListener {

	WalkFlow onWalkStart(WalkEvent walkEvent);

	void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages);
}
