package com.networknt.schema;

import java.text.MessageFormat;

public interface ErrorMessageType {
    /**
     * Your error code. Please ensure global uniqueness. Builtin error codes are sequential numbers.
     * 
     * Customer error codes could have a prefix to denote the namespace of your custom keywords and errors.
     * @return error code
     */
    String getErrorCode();

    /**
     * optional message format
     * @return the message format or null if no message text shall be rendered. 
     */
    MessageFormat getMessageFormat();
    
}
