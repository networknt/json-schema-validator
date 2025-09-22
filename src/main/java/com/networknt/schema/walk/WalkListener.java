package com.networknt.schema.walk;

import com.networknt.schema.Error;

import java.util.List;

/**
 * 
 * Listener class that captures walkStart and walkEnd events.
 *
 */
public interface WalkListener {

	WalkFlow onWalkStart(WalkEvent walkEvent);

	void onWalkEnd(WalkEvent walkEvent, List<Error> errors);
}
