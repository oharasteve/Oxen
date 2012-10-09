package com.bazaarvoice.oxen.values;

/**
 * Created by steve.ohara
 * Date: 10/1/12 3:10 PM
 */

import com.bazaarvoice.oxen.expressions.OxValue;

public class OxBooleanValue extends OxValue {
    private final boolean _value;

    public OxBooleanValue(boolean value) {
        _value = value;
    }

    public boolean getBoolean() {
        return _value;
    }

    public String toString() {
        return Boolean.toString(_value);
    }
}