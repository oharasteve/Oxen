package com.bazaarvoice.oxen.symbols;

import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.expressions.OxStack;

/**
 * Created by steve.ohara
 * Date: 10/1/12 9:27 AM
 */

public class OxStringSymbol extends OxSymbol {
    private String _value;

    public OxStringSymbol(String name) {
        super(name);
    }

    public OxStringSymbol(String name, String value) {
        super(name);
        _value = value;
    }

    public void setString(String value) {
        _value = value;
    }

    public String getString() {
        return _value;
    }

    public void setValue(OxStack stack) {
        setString(stack.getString());
    }

    public String getValue() {
        return _value;
    }

    public void fromString(String value) {
        _value = value;
    }

    public void eval(OxStack stack) {
        stack.pushString(_value, this);
    }

    public boolean sameValue(OxSymbol otherSymbol) {
        if (otherSymbol instanceof OxStringSymbol) {
            if (((OxStringSymbol) otherSymbol).getString().equals(this.getString())) {
                return true;
            }
        }
        return false;
    }

    public OxStringSymbol clone() {
        return new OxStringSymbol(_name, _value);
    }
}
