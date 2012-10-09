package com.bazaarvoice.oxen.values;

/**
 * Created by steve.ohara
 * Date: 10/1/12 3:10 PM
 */

import com.bazaarvoice.oxen.expressions.OxValue;

public class OxLongValue extends OxValue {
    private final long _value;

    public OxLongValue(long value) {
        _value = value;
    }

    public long getLong() {
        return _value;
    }

    public String toString() {
        return Long.toString(_value);
    }
}
