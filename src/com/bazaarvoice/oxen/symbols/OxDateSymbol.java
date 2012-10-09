package com.bazaarvoice.oxen.symbols;

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.expressions.OxStack;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by steve.ohara
 * Date: 10/1/12 9:27 AM
 */

public class OxDateSymbol extends OxSymbol {
    private Date _value;

    private static SimpleDateFormat _simpleDateFormat = null;

    public OxDateSymbol(String name) {
        super(name);
    }

    public OxDateSymbol(String name, Date value) {
        super(name);
        _value = value;
    }

    public static void setDateFormat(String dateFormat) {
        _simpleDateFormat = new SimpleDateFormat(dateFormat);
    }

    public void setDate(Date value) {
        _value = value;
    }

    public Date getDate() {
        return _value;
    }

    public void setValue(OxStack stack) {
        setDate(stack.getDate());
    }

    public String getValue() {
        return _value.toString();
    }

    public void fromString(String value) {
        if (_simpleDateFormat == null) {
            throw new OxException("Need to specify a date format");
        }
        try {
            _value = _simpleDateFormat.parse(value);
        }
        catch (Exception ex) {
            throw new OxException("Invalid date: " + value);
        }
    }

    public void eval(OxStack stack) {
        stack.pushDate(_value, this);
    }

    public boolean sameValue(OxSymbol otherSymbol) {
        if (otherSymbol instanceof OxDateSymbol) {
            // Equals to the nearest millisecond
            if (((OxDateSymbol) otherSymbol).getDate().getTime() == this.getDate().getTime()) {
                return true;
            }
        }
        return false;
    }

    public OxDateSymbol clone() {
        return new OxDateSymbol(_name, _value);
    }
}
