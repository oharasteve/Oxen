package com.bazaarvoice.oxen.symbols;

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.expressions.OxStack;

/**
 * Created by steve.ohara
 * Date: 10/1/12 9:27 AM
 */

public class OxDoubleSymbol extends OxSymbol {
    private double _value;

    public OxDoubleSymbol(String name) {
        super(name);
    }

    public OxDoubleSymbol(String name, double value) {
        super(name);
        _value = value;
    }

    public void setDouble(double value) {
        _value = value;
    }

    public double getDouble() {
        return _value;
    }

    public void setValue(OxStack stack) {
        setDouble(stack.getDouble());
    }

    public void fromString(String value) {
        try {
            _value = Double.parseDouble(value);
        }
        catch (Exception ex) {
            throw new OxException("Invalid double: " + value);
        }
    }

    public String getValue() {
        return Double.toString(_value);
    }

    public void eval(OxStack stack) {
        stack.pushDouble(_value, this);
    }

    public boolean sameValue(OxSymbol otherSymbol) {
        if (otherSymbol instanceof OxDoubleSymbol) {
            if (((OxDoubleSymbol) otherSymbol).getDouble() == this.getDouble()) {
                return true;
            }
        }
        if (otherSymbol instanceof OxFloatSymbol) {
            if (((OxFloatSymbol) otherSymbol).getFloat() == this.getDouble()) {
                return true;
            }
        }
        return false;
    }

    public OxDoubleSymbol clone() {
        return new OxDoubleSymbol(_name, _value);
    }
}
