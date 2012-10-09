package com.bazaarvoice.oxen.data;

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.OxSymbolTable;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by steve.ohara
 * Date: 9/27/12 1:05 PM
 */

public class OxDirectReader extends OxReader {
    private static final boolean VERBOSE = false;

    // Read the whole file
    public void readFile(String fname, OxSymbolTable symbolTable, RowProcessor processor) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fname));
            if (VERBOSE) System.out.println("**** Processing " + fname);

            // Find all the <row> entries
            String rec;
            String startRow = "<" + XML_ROW + ">";
            String endRow = "</" + XML_ROW + ">";
            boolean inside = false;
            int seq = 0;
            while ((rec = reader.readLine()) != null) {
                if (rec.trim().startsWith(endRow)) {
                    // Have a full row, stored in the symbol table. So, run the program
                    processor.processRow();

                    inside = false;
                }
                seq++;

                if (inside) {
                    int firstLess = rec.indexOf('<');
                    if (firstLess < 0) throw new OxException("Missing '<' on line " + seq + " of " + fname + "\n" + rec);
                    int firstGreater = rec.indexOf('>', firstLess);
                    if (firstGreater < 0) throw new OxException("Missing '>' on line " + seq + " of " + fname + "\n" + rec);
                    int lastLess = rec.lastIndexOf('<');
                    if (lastLess < 0) throw new OxException("Missing '<' on line " + seq + " of " + fname + "\n" + rec);

                    String pieceName = rec.substring(firstLess+1, firstGreater);
                    String pieceText = rec.substring(firstGreater+1, lastLess);
                    if (VERBOSE) System.out.println("   " + pieceName + " = " + fix(pieceText));
                    OxSymbol symbol = symbolTable.findSymbol(pieceName);
                    if (symbol == null) {
                        throw new OxException("Unable to find symbol: " + pieceName);
                    }
                    symbol.fromString(pieceText);   // Set the value here
                }
                if (VERBOSE) System.out.println();

                if (rec.trim().startsWith(startRow)) {
                    inside = true;
                }
            }
        } catch (Exception ex) {
            throw new OxException("Problem reading " + fname, ex);
        }
    }

    private String fix(String txt) {
        if (txt.indexOf('&') < 0) return txt;

        return txt.
                replaceAll("&nbsp;", " ").
                replaceAll("&lt;", "<").
                replaceAll("&gt;", ">").
                replaceAll("&quot;", "\"").
                replaceAll("&amp;", "&");
    }
}
