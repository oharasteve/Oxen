package com.bazaarvoice.oxen.values;

/**
 * Created by steve.ohara
 * Date: 10/1/12 3:11 PM
 */

import com.bazaarvoice.oxen.expressions.OxValue;

public class OxStringValue extends OxValue {
    private final String _value;

    public OxStringValue(String value) {
        _value = value;
    }

    public String getString() {
        return _value;
    }

    public String toString() {
        return _value;
    }
}
