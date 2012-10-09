package com.bazaarvoice.oxen.expressions;

/**
 * Created by steve.ohara
 * Date: 9/19/12 4:47 PM
 */

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;

import java.util.LinkedList;

public class OxPrintExpression {
    private final OxConstants _constants;

    public OxPrintExpression(OxConstants c) {
        _constants = c;
    }

    private String getNext(LinkedList acts, boolean parens) {
        if (acts.isEmpty()) {
            return _constants.STACK_ERROR;
        }

        PrtPiece top = (PrtPiece) acts.removeLast();

        if (!parens) {
            return top.val;
        }

        return top.toString();
    }

    public String toString(LinkedList<OxTable.Evaluatable> actions) {
        LinkedList<OxTable.Evaluatable> acts = new LinkedList<OxTable.Evaluatable>();

        for (Object action : actions) {
            PrtPiece piece = new PrtPiece();

            if (action instanceof OxOperators.Unary_op) {
                OxOperators.Unary_op fnop = (OxOperators.Unary_op) action;
                piece.val = fnop.fn.toString();
                piece.use_parens = false;

                if (fnop.nargs > 0) {
                    String args = "";

                    for (int j = 0; j < fnop.nargs; j++) {
                        if (j > 0) {
                            args = _constants.COMMA_CHAR + " " + args;
                        }

                        String arg = getNext(acts, false);
                        args = arg + args;
                    }

                    piece.val += _constants.LPAREN_CHAR + args + _constants.RPAREN_CHAR;
                }
            } else if (action instanceof OxOperator) {
                String rt = getNext(acts, true);
                String lft = getNext(acts, true);
                String opnam = action.toString();
                piece.val = lft + " " + opnam + " " + rt;
                piece.use_parens = true;
            } else if (action instanceof OxSymbol) {
                OxSymbol symb = (OxSymbol) action;
                piece.val = symb.toString();
                piece.use_parens = false;
            } else {
                piece.val = action.toString();
                piece.use_parens = false;
            }

            acts.add(piece);
        }

        return acts.removeLast().toString();
    }

    private class PrtPiece implements OxTable.Evaluatable {
        public boolean use_parens;
        public String val;

        public void eval(OxStack stack) {
            throw new OxException("Cannot evaluate a PrtPiece");
        }

        public String toString() {
            if (use_parens) {
                return _constants.LPAREN_CHAR + val + _constants.RPAREN_CHAR;
            }

            return val;
        }
    }
}
