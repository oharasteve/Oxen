package com.bazaarvoice.oxen.symbols;

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.expressions.OxStack;

/**
 * Created by steve.ohara
 * Date: 10/1/12 9:26 AM
 */

public class OxLongSymbol extends OxSymbol {
    private long _value;

    public OxLongSymbol(String name) {
        super(name);
    }

    public OxLongSymbol(String name, long value) {
        super(name);
        _value = value;
    }

    public void setLong(long value) {
        _value = value;
    }

    public long getLong() {
        return _value;
    }

    public void setValue(OxStack stack) {
        setLong(stack.getLong());
    }

    public String getValue() {
        return Long.toString(_value);
    }

    public void fromString(String value) {
        try {
            _value = Long.parseLong(value);
        }
        catch (Exception ex) {
            throw new OxException("Invalid long integer: " + value);
        }
    }

    public void eval(OxStack stack) {
        stack.pushLong(_value, this);
    }

    public boolean sameValue(OxSymbol otherSymbol) {
        if (otherSymbol instanceof OxIntegerSymbol) {
            if (((OxIntegerSymbol) otherSymbol).getInteger() == this.getLong()) {
                return true;
            }
        }
        if (otherSymbol instanceof OxLongSymbol) {
            if (((OxLongSymbol) otherSymbol).getLong() == this.getLong()) {
                return true;
            }
        }
        return false;
    }

    public OxLongSymbol clone() {
        return new OxLongSymbol(_name, _value);
    }
}
