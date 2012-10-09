package com.bazaarvoice.oxen.expressions;

/**
 * Created by steve.ohara
 * Date: 9/19/12 12:22 PM
 */

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.OxSymbolTable;

import java.util.Stack;

public class OxParseExpression {
    private String _expression = null;
    private int _startChar = 0;
    private int _numberChars = 0;
    private final OxFunctions _functions;
    private final OxOperators _operators;
    private final OxConstants _constants;
    private final OxParseNumber _parseNumber;

    public OxParseExpression(OxConstants c, OxFunctions f, OxOperators o) {
        _constants = c;
        _functions = f;
        _operators = o;
        _parseNumber = new OxParseNumber();
    }

    //
    // Main parse routine. Takes a string and a symbol table and produces an (opaque)
    // OxTable that can be evaluated one or more times
    //
    @SuppressWarnings ({"ConstantConditions"})
    public OxTable parse(String expression, OxSymbolTable symbolTable) {
        if (expression == null || expression.trim().length() == 0) {
            throw new OxException(_constants.ERROR_NO_STRING_TO_PARSE);
        }

        _expression = expression;
        _startChar = 0;
        _numberChars = _expression.length();

        boolean expecting_binop = false;
        int paren_depth = 0;
        Stack<OxOperator> operators = new Stack<OxOperator>();
        OxTable mt = new OxTable(_constants);
        int last_binop_pos = 0;

        // Look for a token, any token
        while (_startChar < _numberChars) {
            char ch = _expression.charAt(_startChar);

            // Toss leading spaces
            if (Character.isWhitespace(ch)) {
                _startChar++;
                continue;
            }

            // Done if find comment char
            if (ch == _constants.COMMENT_CHAR) {
                break;
            }

            // Big choice here if were are expecting a binary operator or not
            if (expecting_binop) {
                if (ch == _constants.RPAREN_CHAR) {
                    got_rparen(mt, operators);
                    paren_depth--;
                } else if (ch == _constants.COMMA_CHAR) {
                    if (paren_depth < 1) {
                        throw new OxException(_constants, _startChar, _constants.ERROR_COMMA_OUT_OF_PLACE);
                    }

                    got_comma(mt, operators);
                    expecting_binop = false;
                } else if (got_binop(mt, operators, symbolTable)) {
                    last_binop_pos = _startChar;
                    expecting_binop = false;
                    continue;
                } else // No idea what the durn thing is
                {
                    throw new OxException(_constants, _startChar, _constants.ERROR_EXPECTED_BINOP);
                }

                _startChar++;
            } else // not expecting a binop
            {
                if (ch == _constants.LPAREN_CHAR) {
                    got_lparen(operators);
                    paren_depth++;
                    _startChar++;
                } else if (ch == _constants.SINGLE_QUOTE_CHAR || ch == _constants.DOUBLE_QUOTE_CHAR) {
                    got_quotes(mt);
                    expecting_binop = true;
                } else if (ch == _constants.USERVAR_CHAR) {
                    int ec = _expression.indexOf(_constants.USERVAR_CHAR, _startChar + 1);
                    if (ec < 0) {
                        throw new OxException(_constants, _startChar,
                                _constants.ERROR_MISSING_USERVAR_CHAR + ' ' +
                                        _constants.USERVAR_CHAR);
                    }

                    String var = _expression.substring(_startChar + 1, ec);
                    OxSymbol s = symbolTable.findSymbol(var);
                    got_var(s, mt, var);
                    _startChar = ec + 1;
                    expecting_binop = true;
                } else if (ch == _constants.NEG_SIGN.charAt(0)) {
                    OxFunction fn = _functions.findFunction(_constants.NEG_SIGN);
                    expecting_binop = got_func(mt, operators, fn);
                    _startChar++;
                } else {
                    OxParseNumber.OxParseNumberResult result = _parseNumber.parseNumber(_constants, _expression, _startChar, _numberChars);
                    if (result != null) {
                        if (result instanceof OxParseNumber.OxParsedDouble) {
                            mt.add(new LoadDoubleConst(((OxParseNumber.OxParsedDouble) result).getValue()));
                            // System.out.println("  parse: found double " + pn.dblValue);
                        } else if (result instanceof OxParseNumber.OxParsedLong) {
                            mt.add(new LoadLongConst(((OxParseNumber.OxParsedLong) result).getValue()));
                            // System.out.println("  parse: found long " + pn.intValue);
                        }
                        _startChar = result._nextSC; // get past the number
                        expecting_binop = true;
                    } else {
                        // Must be a function or a variable
                        // Find the end of the darn _name
                        int ec = _startChar + 1;
                        while (ec < _numberChars) {
                            char ch2 = _expression.charAt(ec);
                            if (symbolTable.isNotIdentifierChar(ch2)) {
                                break;
                            }

                            ec++;
                        }

                        if (ch == _constants.SYSVAR_CHAR) {
                            String fnname = _expression.substring(_startChar + 1, ec);
                            OxFunction fn = _functions.findFunction(fnname);

                            if (fn == null) {
                                throw new OxException(_constants, _startChar,
                                        _constants.ERROR_UNDEFINED_FUNCTION + ' ' + fnname);
                            }

                            expecting_binop = got_func(mt, operators, fn);
                        } else // Have to guess if function or variable
                        {
                            String id = _expression.substring(_startChar, ec);
                            OxSymbol s = symbolTable.findSymbol(id);

                            if (s != null) {
                                got_var(s, mt, id);
                                _startChar = ec + 1;
                                expecting_binop = true;
                            } else {
                                // Better be a function!
                                OxFunction fn = _functions.findFunction(id);

                                if (fn != null) {
                                    expecting_binop = got_func(mt, operators, fn);
                                } else {
                                    throw new OxException(_constants.ERROR_NO_SUCH_VARIABLE +
                                            ' ' + id);
                                }
                            }
                        }

                        _startChar = ec;
                    }
                }
            }
        }

        // Pop everything else off
        pop_opers(mt, operators, OxOperator.PrecedenceEnum.LPAREN_PRECEDENCE);

        // Make sure no junk left over (never happens)
        if (!operators.isEmpty()) {
            throw new OxException(_constants.ERROR_PARSE_INCOMPLETE);
        }

        if (paren_depth > 0) {
            throw new OxException(_constants.MISSING_RIGHT_PAREN);
        }

        // Make sure ended correctly, not with a * for example
        if (!expecting_binop) {
            throw new OxException(_constants, last_binop_pos, _constants.ERROR_PARSE_ENDED_EARLY);
        }

        return mt;
    }

    private void got_lparen(Stack<OxOperator> operators) {
        // Just push it on the stack like a regular binop
        operators.push(_operators.new LParen_op());
    }

    private void got_rparen(OxTable mt, Stack<OxOperator> operators) {
        // Pop operators, until we find a matching left paren
        pop_opers(mt, operators, OxOperator.PrecedenceEnum.RPAREN_PRECEDENCE);

        // Must be a left paren on top of stack, or else!!
        if (operators.isEmpty()) {
            throw new OxException(_constants, _startChar, _constants.ERROR_PARENS_DONT_MATCH);
        }

        OxOperator op = operators.pop();
        if (op.getPrecedence() != OxOperator.PrecedenceEnum.LPAREN_PRECEDENCE) {
            throw new OxException(_constants, _startChar, _constants.ERROR_PARENS_DONT_MATCH);
        }
    }

    private void got_comma(OxTable mt, Stack<OxOperator> operators) {
        // Just push it on the stack like a regular binop
        // Pop off all higher precedence operators
        pop_opers(mt, operators, OxOperator.PrecedenceEnum.COMMA_PRECEDENCE);
        operators.push(_operators.new Comma_op());
    }

    private void got_quotes(OxTable mt) {
        final char qch = _expression.charAt(_startChar); // Either ' or "
        StringBuilder buf = new StringBuilder();
        char ch;
        int savesc = _startChar;

        _startChar++; // Get past the first quote

        while (_startChar < _numberChars) {
            ch = _expression.charAt(_startChar);

            // Look for \n
            if (ch == _constants.BACKSLASH_CHAR) {
                if (_startChar + 1 < _numberChars) // Peek ahead
                {
                    ch = _expression.charAt(_startChar + 1);

                    if (ch == _constants.BACKSLASH_CHAR) {
                        _startChar++;
                    } else if (ch == _constants.NEWLINE_CHAR) {
                        _startChar++;
                        ch = '\n';
                    }
                }
            }

            if (ch == qch) // Might be end-of-literal
            {
                if (_startChar + 1 >= _numberChars) // Peek ahead
                {
                    break;
                }

                ch = _expression.charAt(_startChar + 1);

                if (ch != qch) {
                    break;
                }

                // Oops -- was two quotes
                _startChar++;
            }

            buf.append(ch);
            _startChar++;
        }

        if (_startChar >= _numberChars) {
            throw new OxException(_constants, savesc, _constants.ERROR_MISSING_QUOTE);
        }

        // Ok, put the string into the table
        mt.add(new LoadStringConst(buf.toString()));
        _startChar++; // Get past the ending quote
    }

    private boolean got_binop(OxTable mt, Stack<OxOperator> operators, OxSymbolTable symb) {
        boolean alpha = false;

        // Look it up in the list
        OxOperator op = null;
        char ch1 = _expression.charAt(_startChar);
        char ch2 = ' ';
        int savesc = _startChar;

        if (_startChar + 1 < _numberChars) {
            ch2 = _expression.charAt(_startChar + 1);
        }

        String nam;
        int ec;

        // Case I : $mod
        if (ch1 == _constants.SYSVAR_CHAR) {
            _startChar++;
            alpha = true;
            ch1 = ch2;
        }

        // Case I and II : $mod or just mod
        if (Character.isLetter(ch1)) {
            alpha = true;
            ec = _startChar + 1;

            // Find end of the 'and' operator
            while (ec < _numberChars) {
                ch2 = _expression.charAt(ec);

                if (Character.isWhitespace(ch2)) {
                    break;
                }

                if (symb.isNotIdentifierChar(ch2)) {
                    break; // ec is now one too far
                }

                ec++;
            }

            // Now look it up!
            nam = _expression.substring(_startChar, ec);
            op = _operators.findBinOp(nam);

            if (op != null) {
                _startChar = ec;
            }
        }

        // Case III : <= etc
        if (op == null && !alpha) {
            nam = String.valueOf(ch1);
            op = _operators.findBinOp(nam + ch2);
            if (op != null) {
                _startChar += 2;
            }

            // Case IV : > etc
            if (op == null) {
                op = _operators.findBinOp(nam);
                if (op != null) {
                    _startChar++;
                }
            }
        }

        if (op == null) {
            _startChar = savesc;
            return false;
        }

        // Yep, it is a binary operator
        // Pop off all higher precedence operators
        pop_opers(mt, operators, op.getPrecedence());

        // System.out.println("  parse: found oper " + op._name);
        // Now put it on the operator stack
        operators.push(op);

        return true;
    }

    private boolean got_func(OxTable mt, Stack<OxOperator> operators, OxFunction fn) {
        OxOperators.Unary_op op = _operators.new Unary_op(fn);

        // Constants like pi
        if (fn.getMaximumArguments() == 0) {
            mt.add(op);
            return true;
        }

        // Regular functions like sqrt(x)
        operators.push(op);
        return false;
    }

    private void got_var(OxSymbol symbol, OxTable table, String name) {
        if (symbol == null) {
            throw new OxException(_constants.ERROR_NO_SUCH_VARIABLE + ' ' + name);
        }

        table.add(symbol);
    }

    //
    // Handle the operator stack, have watch for precedence!
    //
    private void pop_opers(OxTable table, Stack<OxOperator> operators, OxOperator.PrecedenceEnum stopper) {
        while (!operators.isEmpty()) {
            OxOperator op = operators.peek();

            // Done if find a lower precedence operator
            if (op.getPrecedence().ordinal() < stopper.ordinal()) {
                return;
            }

            // Special case for "a < x <= b"
            // Want right-to-left instead of normal left-to-right
            if (op.getPrecedence() == OxOperator.PrecedenceEnum.REL_PRECEDENCE &&
                    stopper == OxOperator.PrecedenceEnum.REL_PRECEDENCE) {
                return; // Yep, more than one relational operator!
            }

            // Same or higher, means have to pop it
            // (Gives operators left-to-right order)
            // And put it in the evaluation table
            op = operators.pop();

            // If comma, bump somebody's count
            if (op instanceof OxOperators.Comma_op) {
                // Have to find next function in the operator stack
                // Starting at the top
                for (int i = operators.size() - 1; i >= 0; i--) {
                    OxOperator nextop = operators.get(i);

                    if (nextop instanceof OxOperators.Unary_op) {
                        OxOperators.Unary_op fnop = (OxOperators.Unary_op) nextop;
                        fnop.nargs++;

                        break;
                    }
                }

                return; // And throw it away!
            }

            // If function, check its arg count
            if (op instanceof OxOperators.Unary_op) {
                OxOperators.Unary_op fnop = (OxOperators.Unary_op) op;

                if (fnop.nargs < fnop.fn.getMinimumArguments()) {
                    throw new OxException(_constants.ERROR_TOO_FEW_ARGUMENTS + ' ' +
                            _constants.SYSVAR_CHAR + fnop.fn.getName());
                }

                if (fnop.nargs > fnop.fn.getMaximumArguments()) {
                    throw new OxException(_constants.ERROR_TOO_MANY_ARGUMENTS + ' ' +
                            _constants.SYSVAR_CHAR + fnop.fn.getName());
                }
            }

            table.add(op);
        }
    }

    //
    // Some utility classes
    //
    private class LoadLongConst implements OxTable.Evaluatable {
        private final long val;

        public LoadLongConst(long x) {
            val = x;
        }

        public String toString() {
            return Long.toString(val);
        }

        public void eval(OxStack top) {
            top.pushLong(val);
        }
    }

    private class LoadDoubleConst implements OxTable.Evaluatable {
        private final double val;

        public LoadDoubleConst(double x) {
            val = x;
        }

        public String toString() {
            return Double.toString(val);
        }

        public void eval(OxStack top) {
            top.pushDouble(val);
        }
    }

    private class LoadStringConst implements OxTable.Evaluatable {
        private final String val;

        public LoadStringConst(String x) {
            val = x;
        }

        public String toString() {
            return "\"" + doubleQ(val) + "\"";
        }

        public void eval(OxStack top) {
            top.pushString(val);
        }

        private String doubleQ(String lin) {
            if (lin == null) {
                return null;
            }
            if (lin.indexOf('"') < 0) {
                return lin;
            }
            return lin.replaceAll("\"", "\"\"");    // Double 'em up
        }
    }
}
