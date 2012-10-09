package com.bazaarvoice.oxen.symbols;

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.expressions.OxStack;

/**
 * Created by steve.ohara
 * Date: 10/1/12 9:25 AM
 */

public class OxBooleanSymbol extends OxSymbol {
    private boolean _value;

    private static String _falseValue = "false";
    private static String _trueValue = "true";

    public OxBooleanSymbol(String name) {
        super(name);
    }

    public OxBooleanSymbol(String name, boolean value) {
        super(name);
        _value = value;
    }

    public static void setTrueFalseValues(String trueValue, String falseValue) {
        _trueValue = trueValue;
        _falseValue = falseValue;
    }

    public void setBoolean(boolean value) {
        _value = value;
    }

    public boolean getBoolean() {
        return _value;
    }

    public void setValue(OxStack stack) {
        setBoolean(stack.getBoolean());
    }

    public String getValue() {
        return Boolean.toString(_value);
    }

    public void fromString(String value) {
        if (value.equalsIgnoreCase(_trueValue)) _value = true;
        else if (value.equalsIgnoreCase(_falseValue)) _value = false;
        else throw new OxException("Invalid boolean value: " + value);
    }

    public void eval(OxStack stack) {
        stack.pushBoolean(_value, this);
    }

    public boolean sameValue(OxSymbol otherSymbol) {
        if (otherSymbol instanceof OxBooleanSymbol) {
            if (((OxBooleanSymbol) otherSymbol).getBoolean() == this.getBoolean()) {
                return true;
            }
        }
        return false;
    }

    public OxBooleanSymbol clone() {
        return new OxBooleanSymbol(_name, _value);
    }
}