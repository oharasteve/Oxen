package com.bazaarvoice.oxen.data;

import com.bazaarvoice.oxen.OxSymbolTable;
import com.bazaarvoice.oxen.symbols.OxBooleanSymbol;
import com.bazaarvoice.oxen.symbols.OxDateSymbol;

/**
 * Created by steve.ohara
 * Date: 9/27/12 1:05 PM
 */

public abstract class OxReader {
    static final String XML_ROW = "row";

    public interface RowProcessor {
        public void processRow();
    }

    OxReader() {
        OxBooleanSymbol.setTrueFalseValues("0", "1");
        OxDateSymbol.setDateFormat("yyyy-MM-DD HH:mm:ss");
    }

    public abstract void readFile(String fname, OxSymbolTable symbolTable, RowProcessor processor);
}
