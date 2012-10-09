package com.bazaarvoice.oxen.data;

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.OxSymbolTable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by steve.ohara
 * Date: 9/27/12 7:24 AM
 */

public class OxXmlReader extends OxReader {
    private static final boolean VERBOSE = false;

    // Read the whole file
    public void readFile(String fname, OxSymbolTable symbolTable, RowProcessor processor) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(fname);
            if (VERBOSE) System.out.println("**** Processing " + fname);

            // Find all the <row> entries
            NodeList rows = doc.getElementsByTagName(XML_ROW);
            int nrows = rows.getLength();
            for (int i = 0; i < nrows; i++) {
                // Each row is processed independently
                Node row = rows.item(i);
                NodeList pieces = row.getChildNodes();
                int npieces = pieces.getLength();
                for (int j = 0; j < npieces; j++) {
                    Node piece = pieces.item(j);
                    String pieceName = piece.getNodeName();
                    String pieceText = piece.getTextContent();
                    if (! pieceName.startsWith("#")) {
                        if (VERBOSE) System.out.println("   " + pieceName + " = " + pieceText);
                        OxSymbol symbol = symbolTable.findSymbol(pieceName);
                        if (symbol == null) {
                            throw new OxException("Unable to find symbol: " + pieceName);
                        }
                        symbol.fromString(pieceText);   // Set the value here
                    }
                }
                if (VERBOSE) System.out.println();

                // Have a full row, stored in the symbol table. So, run the program
                processor.processRow();
            }
        } catch (Exception ex) {
            throw new OxException("Problem reading " + fname, ex);
        }
    }
}
