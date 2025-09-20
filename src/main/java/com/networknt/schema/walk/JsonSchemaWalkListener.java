package com.networknt.schema.walk;

import com.networknt.schema.ValidationMessage;

import java.util.List;

/**
 * 
 * Listener class that captures walkStart and walkEnd events.
 *
 */
public interface JsonSchemaWalkListener {

	WalkFlow onWalkStart(WalkEvent walkEvent);

	void onWalkEnd(WalkEvent walkEvent, List<ValidationMessage> validationMessages);
}
