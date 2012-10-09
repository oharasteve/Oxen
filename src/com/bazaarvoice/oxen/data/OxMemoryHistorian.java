package com.bazaarvoice.oxen.data;

/**
 * Created by steve.ohara
 * Date: 9/28/12 7:34 AM
 */

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.OxSymbolTable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class OxMemoryHistorian extends OxDataHistorian {
    private final TreeMap<Long, OxSymbolTable> _everything = new TreeMap<Long, OxSymbolTable>();

    public void persist(OxSymbolTable symbolTable, long timeStamp) {
        _everything.put(timeStamp, symbolTable);
    }

    // Count how many records match, in the past 'days'
    public int count(Collection<OxSymbol> symbols, long milliseconds) {
        int matches = 0;

        long latestTimeStamp = _everything.lastKey();
        for (Map.Entry<Long, OxSymbolTable> entries : _everything.entrySet()) {
            OxSymbolTable historical = entries.getValue();

            // Don't go back too far in time ...
            long historicalTimeStamp = entries.getKey();
            if (latestTimeStamp - historicalTimeStamp > milliseconds) continue;

            // All the variables have the same value?
            boolean same = true;
            for (OxSymbol currSymbol : symbols) {
                OxSymbol oldSymbol = historical.findSymbol(currSymbol.getName());
                if (oldSymbol == null) {
                    throw new OxException("Missing symbol: " + currSymbol.getName());
                }
                if (! currSymbol.sameValue(oldSymbol)) {
                    same = false;
                    break;
                }
            }
            if (same) matches++;
        }
        return matches;
    }

    // Count how many unique records match, in the past 'days'
    // The 'symbols' variables must match exactly
    // The 'uniqueSymbol' is counted for # unique values
    public int unique(OxSymbol uniqueSymbol, Collection<OxSymbol> symbols, long milliseconds) {
        HashSet<String> matched = new HashSet<String>();

        long latestTimeStamp = _everything.lastKey();
        for (Map.Entry<Long, OxSymbolTable> entries : _everything.entrySet()) {
            OxSymbolTable historical = entries.getValue();

            // Don't go back too far in time ...
            long historicalTimeStamp = entries.getKey();
            if (latestTimeStamp - historicalTimeStamp > milliseconds) continue;

            // All the variables have the same value?
            boolean same = true;
            for (OxSymbol currSymbol : symbols) {
                OxSymbol oldSymbol = historical.findSymbol(currSymbol.getName());
                if (oldSymbol == null) {
                    throw new OxException("Missing symbol: " + currSymbol.getName());
                }
                if (! currSymbol.sameValue(oldSymbol)) {
                    same = false;
                    break;
                }
            }

            // If matches, see if it is a new value
            if (same) {
                OxSymbol oldSymbol = historical.findSymbol(uniqueSymbol.getName());
                String value = oldSymbol.getValue();
                if (! matched.contains(value)) {
                    // Add it to the set
                    matched.add(value);
                }
            }
        }
        return matched.size();
    }
}
