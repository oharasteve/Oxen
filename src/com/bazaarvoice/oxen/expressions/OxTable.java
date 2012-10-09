package com.bazaarvoice.oxen.expressions;

/**
 * Created by steve.ohara
 * Date: 9/19/12 12:23 PM
 */

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;

import java.util.LinkedList;

public class OxTable {
    private final LinkedList<Evaluatable> _actions;
    private final OxStack _top;
    private final OxConstants _constants;

    public OxTable(OxConstants c) {
        _constants = c;
        _top = new OxStack();
        _actions = new LinkedList<Evaluatable>();
    }

    public void add(Evaluatable op) {
        _actions.add(op);
    }

    public OxStack evaluate() {
        _top.clear(); // Make sure no junk leftover from last eval

        // Evaluate stuff
        for (Object act : _actions) {
            Evaluatable x = (Evaluatable) act;
            x.eval(_top);
        }

        // Make sure stack now contains at most one item, the result
        if (_top.size() > 1) {
            throw new OxException(_constants.ERROR_STACK_TOO_BIG);
        }

        //System.out.println("Value of expression = " + _top.peekS(1, 0).toString());
        return _top;
    }

    public boolean evaluateBoolean() {
        evaluate();
        return _top.getBoolean();
    }

    public long evaluateLong() {
        evaluate();
        return _top.getLong();
    }

    public String evaluateString() {
        evaluate();
        return _top.getString();
    }

    public String toString() {
        // Let another class do all the work ...
        OxPrintExpression prt = new OxPrintExpression(_constants);
        return prt.toString(_actions);
    }

    /**
     * All evaluation routines implement this routine
     * It evaluates the function on the runtime stack
     */
    public interface Evaluatable {
        void eval(OxStack top);
    }
}
