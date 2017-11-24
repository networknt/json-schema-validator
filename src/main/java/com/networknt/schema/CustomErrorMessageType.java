package com.networknt.schema;

import java.text.MessageFormat;

public class CustomErrorMessageType implements ErrorMessageType {
    private final String errorCode;
    private final MessageFormat messageFormat;
    
    private CustomErrorMessageType(String errorCode, MessageFormat messageFormat) {
        this.errorCode = errorCode;
        this.messageFormat = messageFormat;
    }
    
    public static ErrorMessageType of(String errorCode) {
        return new CustomErrorMessageType(errorCode, null);
    }
    public static ErrorMessageType of(String errorCode, MessageFormat messageFormat) {
        return new CustomErrorMessageType(errorCode, null);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public MessageFormat getMessageFormat() {
        return messageFormat;
    }

}