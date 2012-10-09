package com.bazaarvoice.oxen;

/**
 * Created by steve.ohara
 * Date: 9/19/12 12:20 PM
 */

@SuppressWarnings ({"FieldCanBeLocal", "UnusedDeclaration", "UnusedDeclaration", "UnusedDeclaration"})
public class OxException extends RuntimeException {
    private int _sc;
    private int _line;

    public OxException(String msg) {
        super(msg);
    }

    public OxException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public OxException(Throwable ex) {
        super(ex.getMessage());
    }

    public OxException(OxConstants cons, int pos, String msg) {
        super(cons.ERROR_AT_POS + ' ' + (pos + 1) + ", " + msg);
        _sc = pos;
    }

    public void setLine(int i) {
        _line = i;
    }
}
