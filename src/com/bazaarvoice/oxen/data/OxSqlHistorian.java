package com.bazaarvoice.oxen.data;

/**
 * Created by steve.ohara
 * Date: 9/28/12 7:34 AM
 */

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.OxSymbolTable;

import java.util.Collection;

public class OxSqlHistorian extends OxDataHistorian {
    public void persist(OxSymbolTable symbolTable, long timeStamp) {
        throw new OxException("SQL Historian not yet implemented");
    }

    public int count(Collection<OxSymbol> symbols, long milliseconds) {
        throw new OxException("SQL Historian not yet implemented");
    }

    public int unique(OxSymbol uniqueSymbol, Collection<OxSymbol> symbols, long milliseconds) {
        throw new OxException("SQL Historian not yet implemented");
    }
}
