package com.bazaarvoice.oxen.values;

/**
 * Created by steve.ohara
 * Date: 10/1/12 3:10 PM
 */

import com.bazaarvoice.oxen.expressions.OxValue;

import java.util.Date;

public class OxDateValue extends OxValue {
    private final Date _value;

    public OxDateValue(Date value) {
        _value = value;
    }

    public Date getDate() {
        return _value;
    }

    public String toString() {
        return _value.toString();
    }
}
