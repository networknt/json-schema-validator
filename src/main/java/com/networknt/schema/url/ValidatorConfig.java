package com.networknt.schema.url;

public class ValidatorConfig {
    /**
     * when validate type, if TYPE_LOOSE = true, will try to convert string to different types to match the type defined in schema.
     */
    private boolean typeLoose;

    public boolean isTypeLoose() {
        return typeLoose;
    }

    public void setTypeLoose(boolean typeLoose) {
        this.typeLoose = typeLoose;
    }

    public ValidatorConfig() {
        loadDefaultConfig();
    }

    private void loadDefaultConfig() {
        this.typeLoose = true;
    }
}
