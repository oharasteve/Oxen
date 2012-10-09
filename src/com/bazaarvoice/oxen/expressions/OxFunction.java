package com.bazaarvoice.oxen.expressions;

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbolTable;

/**
 * Created by steve.ohara
 * Date: 9/20/12 11:26 AM
 */

public abstract class OxFunction implements Comparable<OxFunction> {
    private final String _fixName;
    private final String _name;
    @SuppressWarnings ({"FieldCanBeLocal", "UnusedDeclaration"})
    private final String _tip;
    private final int _minimumArguments;
    private final int _maximumArguments;

    // Used for testing
    private int _errors = 0;
    protected OxSymbolTable _testSymbolTable;
    protected OxConstants _testConstants;
    protected OxFunctions _testFunctions;
    protected OxOperators _testOperators;

    public OxFunction(String fixName, String name, String tip, int minimumArguments, int maximumArguments) {
        _fixName = fixName;
        _name = name;
        _tip = tip;
        _minimumArguments = minimumArguments;
        _maximumArguments = maximumArguments;
    }

    public String getName() {
        return _name;
    }

    public String getFixName() {
        return _fixName;
    }

    public int getMinimumArguments() {
        return _minimumArguments;
    }

    public int getMaximumArguments() {
        return _maximumArguments;
    }

    public String toString() {
        if (Character.isLetter(_name.charAt(0))) {
            return '$' + _name;
        }
        return _name;
    }

    public int compareTo(OxFunction fn2) {
        String nam2 = fn2._name;
        return _name.compareTo(nam2);
    }

    public abstract void evalFn(OxStack top, int nargs);

    public boolean testFunction(OxSymbolTable symbolTable, OxConstants constants, OxFunctions functions, OxOperators operators) {
        _testSymbolTable = symbolTable;
        _testConstants = constants;
        _testFunctions = functions;
        _testOperators = operators;

        _errors = 0;
        selfTest();
        return _errors == 0;
    }

    protected abstract void selfTest();

    public void shouldWork(String expr, String expected) {
        try {
            OxParseExpression parseExpr = new OxParseExpression(_testConstants, _testFunctions, _testOperators);
            OxTable tbl = parseExpr.parse(expr, _testSymbolTable);
            String actual = tbl.evaluateString();

            if (!expected.equals(actual)) {
                System.err.println(toString() + ": Expression \"" + expr + "\" returned " +
                        actual + " instead of " + expected);
                _errors++;
            } else {
                System.out.println(toString() + ": Expression \"" + expr + "\" returned " + actual +
                        " as expected.");
            }
        }
        catch (Exception ex) {
            System.err.println(toString() + ": Expression \"" + expr + "\" threw an exception: " + ex.getMessage());
            ex.printStackTrace(System.err);
            _errors++;
        }
    }

    public void shouldFail(String expr) {
        try {
            OxParseExpression parseExpr = new OxParseExpression(_testConstants, _testFunctions, _testOperators);
            OxTable tbl = parseExpr.parse(expr, _testSymbolTable);
            String actual = tbl.evaluateString();
            System.err.println(toString() + ": Expression \"" + expr + "\" returned " + actual +
                    " instead of throwing an exception");
            _errors++;
        } catch (OxException ex) {
            System.out.println(toString() + ": Expression \"" + expr + "\" threw " + ex.getMessage() + " as expected.");
        }
    }
}