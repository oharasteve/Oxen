package com.bazaarvoice.oxen.expressions;

/**
 * Created by steve.ohara
 * Date: 9/19/12 1:26 PM
 */

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.values.OxBooleanValue;
import com.bazaarvoice.oxen.values.OxDateValue;
import com.bazaarvoice.oxen.values.OxDoubleValue;
import com.bazaarvoice.oxen.values.OxLongValue;
import com.bazaarvoice.oxen.values.OxStringValue;

import java.util.ArrayList;
import java.util.Date;

public class OxStack {
    private final ArrayList<OxValue> _stack = new ArrayList<OxValue>();
    private int _stackSize = 0;

    public int size() {
        return _stackSize;
    }

    public void clear() {
        _stack.clear();
        _stackSize = 0;
    }

    //
    // Poppers
    //

    public OxValue popTop() {
        if (_stackSize == 0) {
            throw new OxException("Stack is empty");
        }
        _stackSize--;
        return _stack.get(_stackSize);
    }

    public boolean getBoolean() {
        OxValue value = popTop();
        if (value instanceof OxBooleanValue) {
            return ((OxBooleanValue) value).getBoolean();
        }
        throw new OxException("Expected a boolean, but found " + value + " instead.");
    }

    public long getLong() {
        OxValue value = popTop();
        if (value instanceof OxLongValue) {
            return ((OxLongValue) value).getLong();
        }
        if (value instanceof OxDoubleValue) {
            return Math.round(((OxDoubleValue) value).getDouble());
        }
        throw new OxException("Expected an integer, but found " + value + " instead.");
    }

    public double getDouble() {
        OxValue value = popTop();
        if (value instanceof OxDoubleValue) {
            return ((OxDoubleValue) value).getDouble();
        }
        if (value instanceof OxLongValue) {
            return ((OxLongValue) value).getLong();
        }
        throw new OxException("Expected a double, but found " + value + " instead.");
    }

    public String getString() {
        OxValue value = popTop();
        if (value instanceof OxStringValue) {
            return ((OxStringValue) value).getString();
        }
        return value.toString();
    }

    public Date getDate() {
        OxValue value = popTop();
        if (value instanceof OxDateValue) {
            return ((OxDateValue) value).getDate();
        }
        throw new OxException("Expected a date, but found " + value + " instead.");
    }

    //
    // Type checkers
    //

    public boolean isLong() {
        if (_stackSize > 0) {
            if (_stack.get(_stackSize - 1) instanceof OxLongValue) {
                return true;
            }
        }
        return false;
    }

    public boolean bothBoolean() {
        if (_stackSize > 1) {
            if (_stack.get(_stackSize - 1) instanceof OxBooleanValue &&
                    _stack.get(_stackSize - 2) instanceof OxBooleanValue) {
                return true;
            }
        }
        return false;
    }

    public boolean bothLong() {
        if (_stackSize > 1) {
            if (_stack.get(_stackSize - 1) instanceof OxLongValue &&
                    _stack.get(_stackSize - 2) instanceof OxLongValue) {
                return true;
            }
        }
        return false;
    }

    public boolean bothNumeric() {
        if (_stackSize > 1) {
            OxValue top = _stack.get(_stackSize - 1);
            OxValue next = _stack.get(_stackSize - 2);
            if ((top instanceof OxLongValue || top instanceof OxDoubleValue) &&
                    (next instanceof OxLongValue || next instanceof OxDoubleValue)) {
                return true;
            }
        }
        return false;
    }

    public boolean bothDate() {
        if (_stackSize > 1) {
            if (_stack.get(_stackSize - 1) instanceof OxDateValue &&
                    _stack.get(_stackSize - 2) instanceof OxDateValue) {
                return true;
            }
        }
        return false;
    }

    // But don't allow either to be a boolean ...
    public boolean eitherString() {
        if (_stackSize > 1) {
            OxValue top = _stack.get(_stackSize - 1);
            OxValue next = _stack.get(_stackSize - 2);
            if (top instanceof OxStringValue || next instanceof OxStringValue) {
                if (! (top instanceof OxBooleanValue || next instanceof OxBooleanValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    //
    // Pushers
    //

    private void push(OxValue value) {
        int availableSize = _stack.size();
        if (_stackSize == availableSize) {
            // Have to make room
            _stack.add(value);
        } else {
            // Already room, just smash it
            _stack.set(_stackSize, value);
        }
        _stackSize++;
    }

    private void push(OxValue value, OxSymbol symbol) {
        push(value);
        value.setSymbol(symbol);
    }

    public void pushBoolean(boolean value) {
        push(new OxBooleanValue(value));
    }

    public void pushLong(long value) {
        push(new OxLongValue(value));
    }

    public void pushDouble(double value) {
        push(new OxDoubleValue(value));
    }

    public void pushString(String value) {
        push(new OxStringValue(value));
    }

    public void pushDate(Date value) {
        push(new OxDateValue(value));
    }

    public void pushBoolean(boolean value, OxSymbol symbol) {
        push(new OxBooleanValue(value), symbol);
    }

    public void pushLong(long value, OxSymbol symbol) {
        push(new OxLongValue(value), symbol);
    }

    public void pushDouble(double value, OxSymbol symbol) {
        push(new OxDoubleValue(value), symbol);
    }

    public void pushString(String value, OxSymbol symbol) {
        push(new OxStringValue(value), symbol);
    }

    public void pushDate(Date value, OxSymbol symbol) {
        push(new OxDateValue(value), symbol);
    }
}
