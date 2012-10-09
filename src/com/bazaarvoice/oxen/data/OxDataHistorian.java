package com.bazaarvoice.oxen.data;

/**
 * Created by steve.ohara
 * Date: 9/27/12 10:39 AM
 */

import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.OxSymbolTable;
import com.bazaarvoice.oxen.expressions.OxFunctions;

import java.util.Collection;

public abstract class OxDataHistorian {
    public abstract void persist(OxSymbolTable symbolTable, long timeStamp);
    public abstract int count(Collection<OxSymbol> symbols, long milliseconds);
    public abstract int unique(OxSymbol uniqueSymbol, Collection<OxSymbol> symbols, long milliseconds);

    public static void addFunctions(OxFunctions fns) {
        fns.addFunction(new OxCountFn("count", "count", "Count how often this pattern has arrived."));
        fns.addFunction(new OxUniqueFn("unique", "unique", "Count how many different something or anothers."));
    }
}
