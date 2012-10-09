package com.bazaarvoice.oxen.values;

/**
 * Created by steve.ohara
 * Date: 10/1/12 3:10 PM
 */

import com.bazaarvoice.oxen.expressions.OxValue;

public class OxDoubleValue extends OxValue {
    private final double _value;

    public OxDoubleValue(double value) {
        _value = value;
    }

    public double getDouble() {
        return _value;
    }

    public String toString() {
        return Double.toString(_value);
    }
}
