package com.bazaarvoice.oxen.commands;

/**
 * Created by steve.ohara
 * Date: 9/19/12 12:24 PM
 */

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.expressions.OxFunctions;
import com.bazaarvoice.oxen.expressions.OxOperators;
import com.bazaarvoice.oxen.expressions.OxParseExpression;
import com.bazaarvoice.oxen.expressions.OxStack;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.OxSymbolTable;
import com.bazaarvoice.oxen.expressions.OxTable;
import com.bazaarvoice.oxen.symbols.OxBooleanSymbol;
import com.bazaarvoice.oxen.symbols.OxDateSymbol;
import com.bazaarvoice.oxen.symbols.OxDoubleSymbol;
import com.bazaarvoice.oxen.symbols.OxIntegerSymbol;
import com.bazaarvoice.oxen.symbols.OxLongSymbol;
import com.bazaarvoice.oxen.symbols.OxStringSymbol;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

@SuppressWarnings ({"UnusedDeclaration"})
public class OxCommands {
    private final Hashtable<String, OxCommand> _commands = new Hashtable<String, OxCommand>();
    private int _endChar = 0; // Used to simplify parsing
    private final OxParseExpression _parser;
    private final OxConstants _constants;

    public OxCommands(OxConstants constants, OxFunctions functions, OxOperators operators) {
        _constants = constants;
        _parser = new OxParseExpression(_constants, functions, operators);

        Class[] classes = this.getClass().getDeclaredClasses();
        for (Class cls : classes) {
            if (OxCommand.class.isAssignableFrom(cls) &&
                    !Modifier.isAbstract(cls.getModifiers())) {
                try {
                    // System.out.println("**** found class " + cls.getName());
                    Class<?> cl[] = new Class<?>[] { OxCommands.class };

                    @SuppressWarnings ({"unchecked"})
                    Constructor<? extends OxCommand> con = cls.getConstructor(cl);
                    Object args[] = new Object[] { this };

                    OxCommand cmd = con.newInstance(args);
                    addCmd(cmd);
                } catch (Exception ex) {
                    throw new OxException(_constants.CANNOT_CREATE_CLASS + " " + cls.getName(), ex);
                }
            }
        }
    }

    private void addCmd(OxCommand command) {
        String name = command.getName();
        String lowName = name.toLowerCase();
        if (_commands.containsKey(lowName)) {
            throw new OxException("Duplicate command: " + name);
        }
        _commands.put(lowName, command);

        // Put the fixname also, unless it is the same
        String fixName = command.getFixName();
        if (! name.equalsIgnoreCase(fixName)) {
            lowName = fixName.toLowerCase();
            if (_commands.containsKey(lowName)) {
                throw new OxException("Duplicate command: " + fixName);
            }
            _commands.put(lowName, command);
        }
    }

    public OxCommand cloneCmd(String nam) {
        OxCommand cmd = _commands.get(nam.toLowerCase());
        if (cmd == null) return null;

        try {
            Class c[] = new Class[] { this.getClass() };
            Constructor con = cmd.getClass().getConstructor(c);
            Object args[] = new Object[] { this };

            return (OxCommand) con.newInstance(args);
        } catch (Exception ex) {
            throw new OxException(_constants.CANNOT_CREATE_CLASS + " " + cmd.getName());
        }
    }

    public OxCommand[] allCommands() {
        Collection<OxCommand> coll = _commands.values();
        int sz = coll.size();
        return coll.toArray(new OxCommand[sz]);
    }

    //
    // Parsing utilities
    //

    private OxTable parseExpr(String expr, OxSymbolTable symb) {
        return _parser.parse(expr, symb);
    }

    private String getVar(String line) {
        String var;

        _endChar = 0;
        char ch = line.charAt(0);

        if (ch == _constants.USERVAR_CHAR) {
            // Find end of user variable, inside !
            _endChar = line.indexOf(_constants.USERVAR_CHAR, 1);

            if (_endChar < 0) {
                throw new OxException(_constants.ERROR_MISSING_USERVAR_CHAR + ' ' +
                        line);
            }

            var = line.substring(1, _endChar);
            _endChar++; // Get past the second !

            return var;
        }

        // Maybe it is just a variable with no !
        int nc = line.length();

        while (_endChar < nc) {
            ch = line.charAt(_endChar);

            if (ch == ' ') {
                break;
            }

            if (ch == _constants.UNDERSCORE_CHAR || Character.isDigit(ch) ||
                    Character.isLetter(ch)) {
                _endChar++;

                continue;
            }

            break; // Not part of an identifier
        }

        return line.substring(0, _endChar).trim();
    }

    // Required punctuation, like =
    private boolean getPunct(String line, char punct, boolean force) {
        // Find the punct
        int nc = line.length();
        while (_endChar < nc) {
            char ch = line.charAt(_endChar);
            _endChar++;

            if (ch == punct) return true;
            if (ch == ' ') continue;

            if (force) {
                throw new OxException(_constants.PARSE_EXPECTED_THIS + ' ' + punct +
                        _constants.PARSE_NOT_THIS + ' ' + ch);
            }

            _endChar--; // Went one too far
        }
        return false;
    }

    // Careful, have to count quotes, etc!
    @SuppressWarnings ({"ConstantConditions"})
    private int getKeyword(String line, String key) {
        boolean inq1 = false;
        boolean inq2 = false;
        boolean invar = false;
        int keylen = key.length();
        int endch = line.length() - keylen;

        for (int i = _endChar; i < endch; i++) {
            char ch = line.charAt(i);

            if (ch == _constants.SINGLE_QUOTE_CHAR && !inq2 && !invar) {
                inq1 = !inq1;
            } else if (ch == _constants.DOUBLE_QUOTE_CHAR && !inq1 && !invar) {
                inq2 = !inq2;
            } else if (ch == _constants.USERVAR_CHAR && !inq1 && !inq2) {
                invar = !invar;
            } else if (ch == ' ' && !inq1 && !inq2 && !invar) {
                if (line.startsWith(key, i + 1)) {
                    if (line.charAt(i + 1 + keylen) == ' ') {
                        return i + 1; // Found it!
                    }
                }
            }
        }

        return -1; // Nope, not there
    }

    public EvalCmd fakeEvalCmd() {
        return new EvalCmd();
    }

    public SetCmd fakeSetCmd() {
        return new SetCmd();
    }

    public abstract class StartBlockCmd extends OxCommand {
        public String _endName;
        public int _middleIndex; // -1 means none
        public int _endIndex;

        public StartBlockCmd(String fixn, String n, String t) {
            super(fixn, n, t);
        }

        protected int connectCommands(Vector<OxCommand> commands, int i, int depth, OxConstants cons) {
            OxCommands.MiddleBlockCmd middleCmd;
            OxCommands.EndBlockCmd endCmd;

            // Find it's matching middle and end
            _middleIndex = -1;

            int j;
            int ncommands = commands.size();
            for (j = i + 1; j < ncommands; j++) {
                OxCommand cmd2 = commands.get(j);

                if (cmd2 instanceof OxCommands.StartBlockCmd) {
                    depth++;
                } else if (depth > 0) {
                    if (cmd2 instanceof OxCommands.EndBlockCmd) {
                        depth--;
                    }
                } else if (cmd2 instanceof OxCommands.MiddleBlockCmd) {
                    if (_middleIndex >= 0) {
                        throw new OxException(cons.CANT_HAVE_DUPLICATE +
                                ' ' + getName());
                    }

                    // Better be my 'else' !!!
                    middleCmd = (OxCommands.MiddleBlockCmd) cmd2;

                    if (!middleCmd._startName.equals(getName())) {
                        throw new OxException(middleCmd.getName() + ' ' +
                                cons.IS_MISSING_THE + ' ' + middleCmd._startName);
                    }

                    _middleIndex = j;
                    middleCmd._startIndex = i;

                    // System.out.println("Set " + startCmd._name + " _middleIndex=" + j);
                    // System.out.println("Set " + middleCmd._name + " _startIndex=" + i);
                } else if (cmd2 instanceof OxCommands.EndBlockCmd) {
                    // Better be my 'endif' !!!
                    endCmd = (OxCommands.EndBlockCmd) cmd2;

                    if (!endCmd._startName.equals(getName())) {
                        throw new OxException(endCmd.getName() + ' ' +
                                cons.IS_MISSING_THE + ' ' + endCmd._startName);
                    }

                    _endIndex = j;
                    endCmd._startIndex = i;

                    // System.out.println("Set " + startCmd._name + " _endIndex=" + j);
                    // System.out.println("Set " + endCmd._name + " _startIndex=" + i);
                    if (_middleIndex >= 0) {
                        middleCmd = (OxCommands.MiddleBlockCmd) commands.get(_middleIndex);
                        middleCmd._endIndex = j;

                        // System.out.println("Set " + middleCmd._name + " _endIndex=" + j);
                    }

                    break; // End of the block
                }
            }

            if (j == ncommands) {
                throw new OxException(getName() + ' ' + cons.IS_MISSING_THE + ' ' + _endName);
            }

            return depth;
        }
    }

    public abstract class MiddleBlockCmd extends OxCommand {
        public String _startName;
        public int _startIndex;
        public int _endIndex;

        public MiddleBlockCmd(String fixn, String n, String t) {
            super(fixn, n, t);
        }

        protected int connectCommands(Vector<OxCommand> commands, int i, int depth, OxConstants cons) {
            // See if we have 'else' but no matching 'if'
            if (_startIndex < 0) {
                throw new OxException(getName() + ' ' + cons.IS_MISSING_THE + ' ' + _startName);
            }
            return depth;
        }
    }

    public abstract class EndBlockCmd extends OxCommand {
        public String _startName;
        public int _startIndex;
        public OxCommand _startCmd;

        public EndBlockCmd(String fixn, String n, String t) {
            super(fixn, n, t);
        }

        protected int connectCommands(Vector<OxCommand> commands, int i, int depth, OxConstants cons) {
            // See if we have 'endif' but no matching 'if'
            if (_startIndex < 0) {
                throw new OxException(getName() + ' ' + cons.IS_MISSING_THE + ' ' + _startName);
            }
            _startCmd = commands.get(_startIndex);
            return depth;
        }
    }

    //
    // Start actual _commands ...
    //

    /**
     * For Loops
     */
    private class ForCmd extends StartBlockCmd {
        private OxLongSymbol loopVar;
        private OxTable startExpr;
        private OxTable toExpr;
        private OxTable byExpr;
        private long startVal;
        private long toVal;
        private long byVal;

        public ForCmd() {
            super("for", _constants.FOR_COMMAND, _constants.FOR_CMDTIP);
            _endName = _constants.ENDFOR_COMMAND;
        }

        public void parse(String line, OxSymbolTable symbolTable) {
            String var = getVar(line);

            OxSymbol theVar = symbolTable.findSymbol(var);

            if (theVar == null) {
                // Create the loop variable now, with no value
                loopVar = new OxLongSymbol(var);
                symbolTable.addSymbol(loopVar);
            } else {
                if (!(theVar instanceof OxLongSymbol)) {
                    throw new OxException(_constants.LOOPVAR_MUST_BE_LONG + ' ' + theVar.getName());
                }

                loopVar = (OxLongSymbol) theVar;
            }

            getPunct(line, _constants.ASSIGNMENT_CHAR, true);

            // Find the word "to"
            int toPos = getKeyword(line, _constants.FOR_TO_CLAUSE);

            if (toPos < 0) {
                throw new OxException(_constants.FOR_MISSING_TO);
            }

            startExpr = parseExpr(line.substring(_endChar, toPos), symbolTable);

            // See if the word "by" is there
            _endChar = toPos + _constants.FOR_TO_CLAUSE.length() + 1;

            int byPos = getKeyword(line, _constants.FOR_BY_CLAUSE);

            if (byPos < 0) {
                // Nope, no "by"
                toExpr = parseExpr(line.substring(_endChar), symbolTable);
                byExpr = null;
            } else {
                // Yep, got "by"
                toExpr = parseExpr(line.substring(_endChar, byPos), symbolTable);
                _endChar = byPos + _constants.FOR_BY_CLAUSE.length() + 1;
                byExpr = parseExpr(line.substring(_endChar), symbolTable);
            }
        }

        public void exec(OxProgram pgm) {
            startVal = startExpr.evaluateLong();
            loopVar.setLong(startVal);
            toVal = toExpr.evaluateLong();

            if (byExpr == null) {
                byVal = 1;
            } else {
                byVal = byExpr.evaluateLong();
            }

            // Initial check, may never fall into the loop
            if ((byVal > 0 && startVal > toVal) ||
                    (byVal <= 0 && startVal < toVal)) {
                // Skip past the whole loop
                pgm.setNextCommand(_endIndex + 1);
            }
        }

        public String toString() {
            String most = getName() + " " + loopVar.toString() + " " +
                    _constants.ASSIGNMENT_CHAR + " " + startExpr.toString() + " " +
                    _constants.FOR_TO_CLAUSE + " " + toExpr.toString();

            if (byExpr == null) {
                return most;
            }

            return most + " " + _constants.FOR_BY_CLAUSE + " " + byExpr.toString();
        }
    }

    private class EndForCmd extends EndBlockCmd {
        public EndForCmd() {
            super("endfor", _constants.ENDFOR_COMMAND, _constants.ENDFOR_CMDTIP);
            _startName = _constants.FOR_COMMAND;
        }

        public void parse(String line, OxSymbolTable symb) {
            if (line.length() != 0) {
                throw new OxException(_constants.ENDFOR_COMMAND +
                        _constants.SHOULD_BE_BY_ITSELF);
            }
        }

        public void exec(OxProgram pgm) {
            ForCmd theFor = (ForCmd) (pgm.getCommands().get(_startIndex));
            long currVal = theFor.loopVar.getLong();
            currVal += theFor.byVal;
            theFor.loopVar.setLong(currVal);

            // See if we are done ...
            if (!((theFor.byVal > 0 && currVal > theFor.toVal) ||
                    (theFor.byVal <= 0 && currVal < theFor.toVal))) {
                // No, not done. Go back up
                pgm.setNextCommand(_startIndex + 1);
            }
        }
    }

    /**
     * Subroutines
     */
    private class SubCmd extends StartBlockCmd {
        private String _subName;
        public CallCmd _calledFrom;

        public SubCmd() {
            super("sub", _constants.SUB_COMMAND, _constants.SUB_CMDTIP);
            _endName = _constants.ENDSUB_COMMAND;
        }

        public void parse(String line, OxSymbolTable symbolTable) {
            _subName = getVar(line);
        }

        protected int connectCommands(Vector<OxCommand> commands, int i, int depth, OxConstants cons) {
            // Make sure it is not a duplicate subroutine name
            for (OxCommand cmd : commands) {
                if (cmd instanceof SubCmd && cmd != this) {
                    SubCmd otherSub = (SubCmd) cmd;
                    if (otherSub._subName.equalsIgnoreCase(_subName)) {
                        throw new OxException("Duplicate " + cons.SUB_COMMAND + " named " + _subName);
                    }
                }
            }

            return super.connectCommands(commands, i, depth, cons);
        }

        public void exec(OxProgram pgm) {
            // Skip past the whole subroutine if we fall into it
            pgm.setNextCommand(_endIndex + 1);
        }

        public String toString() {
            return getName() + " " + _subName;
        }
    }

    private class EndSubCmd extends EndBlockCmd {
        private SubCmd theSubroutine;

        public EndSubCmd() {
            super("endsub", _constants.ENDSUB_COMMAND, _constants.ENDSUB_CMDTIP);
            _startName = _constants.SUB_COMMAND;
        }

        public void parse(String line, OxSymbolTable symb) {
            if (line.length() != 0) {
                throw new OxException(_constants.ENDSUB_COMMAND +
                        _constants.SHOULD_BE_BY_ITSELF);
            }
        }

        public void exec(OxProgram pgm) {
            // Done, go back to where we were called from
            SubCmd theSub = (SubCmd) _startCmd;
            pgm.setNextCommand(theSub._calledFrom.getLineNumber());
        }
    }

    public class CallCmd extends OxCommand {
        private String subName;
        private SubCmd theSubroutine;

        public CallCmd() {
            super("call", _constants.CALL_COMMAND, _constants.CALL_CMDTIP);
        }

        public void parse(String line, OxSymbolTable symb) {
            subName = getVar(line);
        }

        protected int connectCommands(Vector<OxCommand> commands, int i, int depth, OxConstants cons) {
            // Look up the subroutine name
            for (OxCommand cmd : commands) {
                if (cmd instanceof SubCmd) {
                    theSubroutine = (SubCmd) cmd;
                    if (theSubroutine._subName.equalsIgnoreCase(subName)) {
                        theSubroutine._calledFrom = this;
                        return depth;
                    }
                }
            }
            throw new OxException("Unable to find a " + cons.SUB_COMMAND + " named " + subName);
        }

        public void exec(OxProgram pgm) {
            theSubroutine._calledFrom = this;
            pgm.setNextCommand(theSubroutine.getLineNumber());   // Jump into the subroutine
        }

        public String toString() {
            return getName() + " " + theSubroutine._subName;
        }
    }

    /**
     * If Statements
     */
    private class IfCmd extends StartBlockCmd {
        private OxTable _table;
        private boolean _condition;

        public IfCmd() {
            super("if", _constants.IF_COMMAND, _constants.IF_CMDTIP);
            _endName = _constants.ENDIF_COMMAND;
        }

        public void parse(String line, OxSymbolTable symb) {
            _table = parseExpr(line, symb);
        }

        public void exec(OxProgram pgm) {
            _condition = _table.evaluateBoolean();

            if (!_condition) {
                // Skip past the "true" part
                if (_middleIndex >= 0) {
                    pgm.setNextCommand(_middleIndex + 1);
                } else {
                    pgm.setNextCommand(_endIndex + 1);
                }
            }

            // else just fall through to the "true" part
        }

        public String toString() {
            return getName() + " " + _table.toString();
        }
    }

    private class ElseCmd extends MiddleBlockCmd {
        public ElseCmd() {
            super("else", _constants.ELSE_COMMAND, _constants.ELSE_CMDTIP);
            _startName = _constants.IF_COMMAND;
        }

        public void parse(String line, OxSymbolTable symb) {
            if (line.length() != 0) {
                throw new OxException(_constants.ELSE_COMMAND +
                        _constants.SHOULD_BE_BY_ITSELF);
            }
        }

        public void exec(OxProgram pgm) {
            // If we get here, it means we MUST have done the "true" part, so skip the false part
            pgm.setNextCommand(_endIndex + 1);
        }
    }

    private class EndIfCmd extends EndBlockCmd {
        public EndIfCmd() {
            super("endif", _constants.ENDIF_COMMAND, _constants.ENDIF_CMDTIP);
            _startName = _constants.IF_COMMAND;
        }

        public void parse(String line, OxSymbolTable symb) {
            if (line.length() != 0) {
                throw new OxException(_constants.ENDIF_COMMAND +
                        _constants.SHOULD_BE_BY_ITSELF);
            }
        }

        public void exec(OxProgram pgm) {
            // Nothing to do, just fall through to next statement
        }
    }

    //
    // Evaluate a function. The "eval" verb is optional
    //
    public class EvalCmd extends OxCommand {
        private OxTable theExpr;

        public EvalCmd() {
            super("eval", _constants.EVAL_COMMAND, _constants.EVAL_CMDTIP);
        }

        public void parse(String line, OxSymbolTable symb) {
            theExpr = parseExpr(line, symb);
        }

        public void exec(OxProgram pgm) {
            theExpr.evaluate(); // Ignore result, if any
        }

        public String toString() {
            if (getName() == null) {
                return theExpr.toString(); // fake version
            }

            return getName() + " " + theExpr.toString();
        }
    }

    //
    // Set the value of a variable. The "set" verb is optional
    //
    private class SetCmd extends OxCommand {
        private OxSymbol theVar;
        private OxTable theExpr;

        public SetCmd() {
            super("set", _constants.SET_COMMAND, _constants.SET_CMDTIP);
        }

        public void parse(String line, OxSymbolTable symb) {
            String var = getVar(line);

            theVar = symb.findSymbol(var);

            if (theVar == null) {
                throw new OxException(_constants.ERROR_NO_SUCH_VARIABLE + ' ' + var);
            }

            getPunct(line, _constants.ASSIGNMENT_CHAR, true);
            theExpr = parseExpr(line.substring(_endChar), symb);
        }

        public void exec(OxProgram pgm) {
            OxStack stack = theExpr.evaluate();
            theVar.setValue(stack);
        }

        public String toString() {
            String prefix = "";
            if (getName() != null) {
                prefix = getName() + " ";
            }

            return prefix + theVar.toString() + " " + _constants.ASSIGNMENT_CHAR + " " + theExpr.toString();
        }
    }

    //
    // Create a new variable. Initial value is optional
    //
    private abstract class DataCmd extends OxCommand {
        private OxSymbol _symbol;
        private OxTable _table;

        public DataCmd(String fixName, String name, String tip) {
            super(fixName, name, tip);
        }

        protected abstract OxSymbol generateSymbol(String var);

        public void parse(String line, OxSymbolTable symbolTable) {
            String var = getVar(line);

            _symbol = symbolTable.findSymbol(var);

            if (_symbol != null) {
                throw new OxException(_constants.DUPLICATE_VARIABLE + ' ' + var);
            }

            _table = null;
            boolean hasEquals = getPunct(line, _constants.ASSIGNMENT_CHAR, false);
            if (hasEquals) {
                _table = parseExpr(line.substring(_endChar), symbolTable);
            }

            _symbol = generateSymbol(var);
            symbolTable.addSymbol(_symbol);
        }

        public void exec(OxProgram pgm) {
            if (_table != null) {
                OxStack stack = _table.evaluate();
                _symbol.setValue(stack);
            }
        }

        public String toString() {
            return getName() + " " + _symbol.toString() + " " + _constants.ASSIGNMENT_CHAR + " " + _table.toString();
        }
    }

    private class BooleanCmd extends DataCmd {
        public BooleanCmd() {
            super("boolean", _constants.BOOLEAN_COMMAND, _constants.BOOLEAN_CMDTIP);
        }

        protected OxSymbol generateSymbol(String var) {
            return new OxBooleanSymbol(var);
        }
    }

    private class IntCmd extends DataCmd {
        public IntCmd() {
            super("int", _constants.INT_COMMAND, _constants.INT_CMDTIP);
        }

        protected OxSymbol generateSymbol(String var) {
            return new OxIntegerSymbol(var);
        }
    }

    private class LongCmd extends DataCmd {
        public LongCmd() {
            super("long", _constants.LONG_COMMAND, _constants.LONG_CMDTIP);
        }

        protected OxSymbol generateSymbol(String var) {
            return new OxLongSymbol(var);
        }
    }

    private class FloatCmd extends DataCmd {
        public FloatCmd() {
            super("float", _constants.FLOAT_COMMAND, _constants.FLOAT_CMDTIP);
        }

        protected OxSymbol generateSymbol(String var) {
            return new OxDoubleSymbol(var);
        }
    }

    private class DoubleCmd extends DataCmd {
        public DoubleCmd() {
            super("double", _constants.DOUBLE_COMMAND, _constants.DOUBLE_CMDTIP);
        }

        protected OxSymbol generateSymbol(String var) {
            return new OxDoubleSymbol(var);
        }
    }

    private class DateCmd extends DataCmd {
        public DateCmd() {
            super("date", _constants.DATE_COMMAND, _constants.DATE_CMDTIP);
        }

        protected OxSymbol generateSymbol(String var) {
            return new OxDateSymbol(var);
        }
    }

    private class StringCmd extends DataCmd {
        public StringCmd() {
            super("string", _constants.STRING_COMMAND, _constants.STRING_CMDTIP);
        }

        protected OxSymbol generateSymbol(String var) {
            return new OxStringSymbol(var);
        }
    }
}
