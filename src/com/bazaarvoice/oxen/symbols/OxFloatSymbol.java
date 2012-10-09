package com.bazaarvoice.oxen.symbols;

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.expressions.OxStack;

/**
 * Created by steve.ohara
 * Date: 10/1/12 9:27 AM
 */

public class OxFloatSymbol extends OxSymbol {
    private float _value;

    public OxFloatSymbol(String name) {
        super(name);
    }

    public OxFloatSymbol(String name, float value) {
        super(name);
        _value = value;
    }

    public void setFloat(float value) {
        _value = value;
    }

    public float getFloat() {
        return _value;
    }

    public void setValue(OxStack stack) {
        setFloat((float) stack.getDouble());
    }

    public String getValue() {
        return Float.toString(_value);
    }

    public void fromString(String value) {
        try {
            _value = Float.parseFloat(value);
        }
        catch (Exception ex) {
            throw new OxException("Invalid float: " + value);
        }
    }

    public void eval(OxStack stack) {
        stack.pushDouble(_value, this);
    }

    public boolean sameValue(OxSymbol otherSymbol) {
        if (otherSymbol instanceof OxDoubleSymbol) {
            if (((OxDoubleSymbol) otherSymbol).getDouble() == this.getFloat()) {
                return true;
            }
        }
        if (otherSymbol instanceof OxFloatSymbol) {
            if (((OxFloatSymbol) otherSymbol).getFloat() == this.getFloat()) {
                return true;
            }
        }
        return false;
    }

    public OxFloatSymbol clone() {
        return new OxFloatSymbol(_name, _value);
    }
}
