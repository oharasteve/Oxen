package com.bazaarvoice.oxen.symbols;

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.expressions.OxStack;

/**
 * Created by steve.ohara
 * Date: 10/1/12 9:26 AM
 */

public class OxIntegerSymbol extends OxSymbol {
    private int _value;

    public OxIntegerSymbol(String name) {
        super(name);
    }

    public OxIntegerSymbol(String name, int value) {
        super(name);
        _value = value;
    }

    public void setInteger(int value) {
        _value = value;
    }

    public int getInteger() {
        return _value;
    }

    public void setValue(OxStack stack) {
        setInteger((int) stack.getLong());
    }

    public String getValue() {
        return Integer.toString(_value);
    }

    public void fromString(String value) {
        try {
            _value = Integer.parseInt(value);
        }
        catch (Exception ex) {
            throw new OxException("Invalid integer: " + value);
        }
    }

    public void eval(OxStack stack) {
        stack.pushLong(_value, this);
    }

    public boolean sameValue(OxSymbol otherSymbol) {
        if (otherSymbol instanceof OxIntegerSymbol) {
            if (((OxIntegerSymbol) otherSymbol).getInteger() == this.getInteger()) {
                return true;
            }
        }
        if (otherSymbol instanceof OxLongSymbol) {
            if (((OxLongSymbol) otherSymbol).getLong() == this.getInteger()) {
                return true;
            }
        }
        return false;
    }

    public OxIntegerSymbol clone() {
        return new OxIntegerSymbol(_name, _value);
    }
}
