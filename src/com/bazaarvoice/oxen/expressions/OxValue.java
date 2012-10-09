package com.bazaarvoice.oxen.expressions;

/**
 * Created by steve.ohara
 * Date: 10/1/12 3:07 PM
 */

import com.bazaarvoice.oxen.OxSymbol;

public abstract class OxValue {
    private OxSymbol _symbol = null;        // If we came from a symbol, remember it

    public void setSymbol(OxSymbol symbol) {
        _symbol = symbol;
    }

    public OxSymbol getSymbol() {
        return _symbol;
    }
}
