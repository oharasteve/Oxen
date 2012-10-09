package com.bazaarvoice.oxen;

/**
 * Created by steve.ohara
 * Date: 9/19/12 1:27 PM
 */

import com.bazaarvoice.oxen.expressions.OxStack;
import com.bazaarvoice.oxen.expressions.OxTable;

@SuppressWarnings ({"UnusedDeclaration"})
public abstract class OxSymbol implements OxTable.Evaluatable {
    protected final String _name;

    public OxSymbol(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public String toString() {
        return _name;
    }

    public abstract void fromString(String value);

    public abstract void setValue(OxStack stack);

    public abstract String getValue();

    @SuppressWarnings ({"BooleanMethodIsAlwaysInverted"})
    public abstract boolean sameValue(OxSymbol otherSymbol);

    public abstract OxSymbol clone();
}
