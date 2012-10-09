package com.bazaarvoice.oxen;

/**
 * Created by steve.ohara
 * Date: 9/19/12 12:28 PM
 */

import java.util.HashMap;

public class OxSymbolTable {
    private final HashMap<String, OxSymbol> _symbols = new HashMap<String, OxSymbol>();

    public void addSymbol(OxSymbol symbol) {
        String lowerName = symbol.getName().toLowerCase();
        if (_symbols.containsKey(lowerName)) {
            throw new OxException("Duplicate symbol: " + symbol.getName());
        }
        _symbols.put(lowerName, symbol);
    }

    public OxSymbol findSymbol(String name) {
        String lowerName = name.toLowerCase();
        if (_symbols.containsKey(lowerName)) {
            return _symbols.get(lowerName);
        }
        return null;    // Not found
    }

    @SuppressWarnings ({"SimplifiableIfStatement"})
    public boolean isNotIdentifierChar(char ch)
    {
        if (Character.isLetter(ch)) return false;
        if (Character.isDigit(ch)) return false;
        return ch != '_';
    }

    public OxSymbolTable clone() {
        OxSymbolTable newTable = new OxSymbolTable();
        for (OxSymbol symbol : _symbols.values()) {
            newTable.addSymbol(symbol.clone());
        }
        return newTable;
    }
}
