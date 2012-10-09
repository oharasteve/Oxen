package com.bazaarvoice.oxen.commands;

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxSymbolTable;

import java.util.Vector;

/**
 * Created by steve.ohara
 * Date: 9/21/12 8:34 AM
 */

public abstract class OxCommand implements Comparable<OxCommand> {
    private final String _fixName;
    private final String _name;
    @SuppressWarnings ({"FieldCanBeLocal", "UnusedDeclaration"})
    private final String _tip;
    private int _lineNumber;

    public OxCommand(String fixName, String name, String tip) {
        _fixName = fixName;
        _name = name;
        _tip = tip;
    }

    public abstract void parse(String line, OxSymbolTable symb);

    public abstract void exec(OxProgram pgm);

    public String toString() {
        return _name;
    }

    public String getName() {
        return _name;
    }

    public String getFixName() {
        return _fixName;
    }

    public int getLineNumber() {
        return _lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        _lineNumber = lineNumber;
    }

    // Return new depth count
    int connectCommands(Vector<OxCommand> commands, int i, int depth, OxConstants cons) {
        // Default is to do nothing. Only if/endif etc care about finding the other parts of their block
        return depth;
    }

    public int compareTo(OxCommand other) {
        String name2 = other._name;
        return _name.compareTo(name2);
    }
}
