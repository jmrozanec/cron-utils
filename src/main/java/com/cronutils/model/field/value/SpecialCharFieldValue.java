package com.cronutils.model.field.value;

import org.apache.commons.lang3.Validate;

public class SpecialCharFieldValue extends  FieldValue<SpecialChar> {
    private SpecialChar specialChar = SpecialChar.NONE;

    public SpecialCharFieldValue(SpecialChar specialChar){
        Validate.notNull(specialChar, "special char must not be null");
        this.specialChar = specialChar;
    }

    @Override
    public SpecialChar getValue() {
        return specialChar;
    }
}
