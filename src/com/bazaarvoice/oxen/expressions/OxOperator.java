package com.bazaarvoice.oxen.expressions;

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbolTable;

/**
 * Created by steve.ohara
 * Date: 9/20/12 11:27 AM
 */

public abstract class OxOperator implements OxTable.Evaluatable, Comparable {
    public static enum PrecedenceEnum {
        LPAREN_PRECEDENCE,
        RPAREN_PRECEDENCE,
        COMMA_PRECEDENCE,
        OR_PRECEDENCE,
        AND_PRECEDENCE,
        REL_PRECEDENCE,
        PLUS_PRECEDENCE,
        TIMES_PRECEDENCE,
        POWER_PRECEDENCE,
        UNARY_PRECEDENCE
    }

    private final String _fixName;
    private final String _name;
    @SuppressWarnings ({"FieldCanBeLocal", "UnusedDeclaration"})
    private final String _tip;
    private final PrecedenceEnum _precedence;

    // Used for testing
    private int _errors = 0;
    private OxSymbolTable _testSymbolTable;
    private OxConstants _testConstants;
    private OxFunctions _testFunctions;
    private OxOperators _testOperators;

    public OxOperator(String fixName, String name, String tip, PrecedenceEnum precedence) {
        _fixName = fixName;
        _name = name;
        _tip = tip;
        _precedence = precedence;
    }

    public String getName() {
        return _name;
    }

    public String getFixName() {
        return _fixName;
    }

    public PrecedenceEnum getPrecedence() {
        return _precedence;
    }

    public abstract void eval(OxStack top);

    public String toString() {
        // Put a $ on the front of and, or, etc.
        if (_name != null && Character.isLetter(_name.charAt(0))) {
            return '$' + _name;
        }

        // Leave +, - etc alone
        return _name;
    }

    public boolean testOperator(OxSymbolTable symbolTable, OxConstants constants, OxFunctions functions, OxOperators operators) {
        _testSymbolTable = symbolTable;
        _testConstants = constants;
        _testFunctions = functions;
        _testOperators = operators;

        _errors = 0;
        selfTest();
        return _errors == 0;
    }

    protected abstract void selfTest();

    void shouldWork(String expr, String expected) {
        try {
            OxParseExpression parseExpr = new OxParseExpression(_testConstants, _testFunctions, _testOperators);
            OxTable tbl = parseExpr.parse(expr, _testSymbolTable);
            String actual = tbl.evaluateString();

            if (!expected.equals(actual)) {
                System.err.println(toString() + ": Expression \"" + expr + "\" returned " +
                        actual + " instead of " + expected);
                _errors++;
            } else {
                System.out.println(toString() + ": Expression \"" + expr + "\" returned " + actual + " as expected.");
            }
        }
        catch (Exception ex) {
            System.err.println(toString() + ": Expression \"" + expr + "\" threw an exception: " + ex.getMessage());
            ex.printStackTrace(System.err);
            _errors++;
        }
    }

    void shouldFail(String expr) {
        try {
            OxParseExpression parseExpr = new OxParseExpression(_testConstants, _testFunctions, _testOperators);
            OxTable tbl = parseExpr.parse(expr, _testSymbolTable);
            String actual = tbl.evaluateString();
            System.err.println(toString() + ": Expression \"" + expr + "\" returned " + actual +
                    " instead of throwing an exception");
            _errors++;
        } catch (OxException ex) {
            System.out.println("Expression \"" + expr + "\" threw " + ex.getMessage() + " as expected.");
        }
    }

    public int compareTo(Object obj2) {
        String name2 = ((OxOperator) obj2)._name;
        return _name.compareTo(name2);
    }
}
