package com.bazaarvoice.oxen.commands;

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.OxSymbolTable;
import com.bazaarvoice.oxen.expressions.OxFunction;
import com.bazaarvoice.oxen.expressions.OxFunctions;
import com.bazaarvoice.oxen.expressions.OxOperators;

import java.util.Locale;

/**
 * Created by steve.ohara
 * Date: 9/19/12 12:25 PM
 */

public class OxParseProgram {
    private final OxCommands _commands;
    private final OxFunctions _functions;
    private final OxConstants _constants;

    public OxParseProgram() {
        // Use defaults for everything
        Locale _locale = Locale.getDefault();
        _constants = new OxConstants();
        _functions = new OxFunctions(_constants, _locale);
        OxOperators _operators = new OxOperators(_constants);
        _commands = new OxCommands(_constants, _functions, _operators);
    }

    public OxParseProgram(OxConstants c, OxCommands cm, OxFunctions f) {
        _constants = c;
        _commands = cm;
        _functions = f;
    }

    //
    // Main routine. Parse a very long string and produce an OxProgram from it
    //
    public OxProgram parse(String program, OxSymbolTable symbolTable) {
        OxProgram pgm = new OxProgram(_constants);
        int seq = 0;

        // Pick up one line at a time
        int pgmec = -1; // Careful, see the pgmec+1 below
        int pgmnc = program.length();

        while (pgmec < pgmnc) {
            String line = "";
            int linenum = seq + 1;

            seq++;

            int pgmsc = pgmec + 1;
            pgmec = program.indexOf('\n', pgmsc);

            if (pgmec < 0) {
                pgmec = pgmnc; // Grab the rest
            }

            line += program.substring(pgmsc, pgmec).trim();

            int nc = line.length();
            if (nc > 0 && line.charAt(nc - 1) == _constants.CONTINUATION_CHAR) {
                // Yes, continued
                line = line.substring(0, nc - 1).trim() + ' ';
            }

            // Process one line at a time
            if (line.length() > 0) {
                parseLine(line, linenum, pgm, symbolTable);
            }
        }

        // Make sure everything matches up (for/endfor, if/else/endif)
        pgm.matchCommands();

        return pgm;
    }

    private void parseLine(String line, int linenum, OxProgram pgm, OxSymbolTable symb) {
        OxCommand cmd;
        char ch;

        int nc = line.length();
        int sc = 0;
        int ec;

        while (sc < nc) {
            ch = line.charAt(sc);

            if (ch == ' ') {
                sc++;
                continue;
            }

            // Just a comment?
            if (ch == _constants.COMMENT_CHAR) {
                return; // Yep, do nothing
            }

            // Four cases:
            //   1. command, like for
            //   2. variable assignment, like x = 5
            //   3. function call, like print("hi")
            //   4. method call on an object, like obj.setAttr(4)
            ch = line.charAt(sc);

            boolean isVar = false;
            boolean isCmd = false;
            String token;

            if (ch == _constants.USERVAR_CHAR) {
                // Must be an assignment
                ec = line.indexOf(_constants.USERVAR_CHAR, sc);

                if (sc < 0) {
                    throw new OxException(_constants.ERROR_MISSING_USERVAR_CHAR +
                            ' ' + line.substring(sc));
                }

                isVar = true;
                token = line.substring(sc + 1, ec);
                ec++;
            } else if (ch == _constants.SYSVAR_CHAR) {
                // Must be a function call
                ec = line.indexOf(_constants.LPAREN_CHAR, sc);

                if (sc < 0) {
                    throw new OxException(_constants.ERROR_MISSING_USERVAR_CHAR +
                            ' ' + line.substring(sc));
                }

                token = line.substring(sc, ec);
            } else {
                // Could be a command, an assignment or a function call
                ec = sc;

                while (ec < nc) {
                    ch = line.charAt(ec);

                    if (ch == ' ' || ch == _constants.LPAREN_CHAR || ch == _constants.ASSIGNMENT_CHAR) {
                        break;
                    }

                    ec++;
                }

                token = line.substring(sc, ec);

                while (ch == ' ' && ec < nc) {
                    ec++;
                    ch = line.charAt(ec);
                }

                if (ch == _constants.ASSIGNMENT_CHAR) {
                    isVar = true;
                } else if (ch != _constants.LPAREN_CHAR) {
                    isCmd = true;
                }
            }

            // Now do something with the 'token' (isVar, isFn or isCmd)
            if (isVar) {
                OxSymbol symbol = symb.findSymbol(token);

                if (symbol == null) {
                    throw new OxException(_constants.ERROR_NO_SUCH_VARIABLE + ' ' +
                            token);
                }

                cmd = _commands.fakeSetCmd();
                ec = sc; // Start back at the variable again
            } else if (isCmd) {
                cmd = _commands.cloneCmd(token);

                if (cmd == null) {
                    throw new OxException(_constants.ERROR_NO_SUCH_COMMAND + ' ' +
                            token);
                }
            } else // isFn
            {
                OxFunction fn = _functions.findFunction(token);
                if (fn == null) {
                    throw new OxException(_constants.ERROR_UNDEFINED_FUNCTION + ' ' +
                            token);
                }

                cmd = _commands.fakeEvalCmd();
                ec = sc; // Start back at the function again
            }

            cmd.setLineNumber(linenum);

            // Now call the command's parser ... with the rest of the line
            cmd.parse(line.substring(ec).trim(), symb);

            // And add it the list of _commands to execute
            pgm.add(cmd);

            break;
        }
    }
}
