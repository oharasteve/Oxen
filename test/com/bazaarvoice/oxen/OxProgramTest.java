package com.bazaarvoice.oxen;

/**
 * Created by steve.ohara
 * Date: 9/19/12 7:13 PM
 */

import com.bazaarvoice.oxen.commands.OxCommands;
import com.bazaarvoice.oxen.commands.OxParseProgram;
import com.bazaarvoice.oxen.commands.OxProgram;
import com.bazaarvoice.oxen.expressions.OxFunctions;
import com.bazaarvoice.oxen.expressions.OxOperators;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class OxProgramTest {
    private final StringBuffer _program = new StringBuffer();
    private final StringBuffer result = new StringBuffer();

    private final Locale _locale = Locale.getDefault();
    private final OxConstants _constants = new OxConstants();
    private final OxFunctions _functions = new OxFunctions(_constants, _locale);
    private final OxOperators _operators = new OxOperators(_constants);
    private final OxCommands _commands = new OxCommands(_constants, _functions, _operators);

    private void addProgram(final String line) {
        _program.append(line).append('\n');
    }

    private void result(final String line) {
        result.append(line).append('\n');
    }

    @SuppressWarnings ({"BooleanMethodIsAlwaysInverted"})
    boolean runTest() {
        OxSymbolTable symbolTable = new OxSymbolTable();
        OxParseProgram parser = new OxParseProgram(_constants, _commands, _functions);
        OxProgram pgm;
        String prog = _program.toString();
        String res = result.toString();
        _program.setLength(0);
        result.setLength(0);

        pgm = parser.parse(prog, symbolTable);

        // Redirect stdout to a byte array
        ByteArrayOutputStream pgmOutput = new ByteArrayOutputStream();
        PrintStream stdout = new PrintStream(pgmOutput);
        PrintStream save = System.out;
        System.setOut(stdout);
        pgm.run();
        System.setOut(save);

        System.out.println();
        System.out.println("Test Program: ");
        System.out.println(pgm);

        if (pgmOutput.toString().equals(res)) {
            System.out.println("Got expected result: ");
            System.out.println(res);
            return true;
        } else {
            System.err.println("Test failed");
            System.err.println();
            System.err.println("Expected result: ");
            System.err.println(res);
            System.err.println();
            System.err.println("Actual result: ");
            System.err.println(pgmOutput);
            return false;
        }
    }

    // For some reason, there has to be at least one public function ...
    @Test public void testFor() {
        addProgram("for i=1 to 4");
        addProgram("  print('i='+i)");
        addProgram("endfor");
        result("i=1");
        result("i=2");
        result("i=3");
        result("i=4");
        if (!runTest()) Assert.fail(this.getClass().getName() + " failed");
    }

    @Test private void testNestedFor() {
        addProgram("print('000')");
        addProgram("for i=1 to 6 by 2");
        addProgram("  for j=7 to 3 by -3");
        addProgram("    print(100*i+j)");
        addProgram("  endfor");
        addProgram("endfor");
        addProgram("print('600')");
        result("000");
        result("107");
        result("104");
        result("307");
        result("304");
        result("507");
        result("504");
        result("600");
        if (!runTest()) Assert.fail(this.getClass().getName() + " failed");
    }

    @Test private void testNestedIf() {
        addProgram("FOR I=1 to 4");
        addProgram("  IF I <= 2");
        addProgram("    if i = 1");
        addProgram("      print('A')");
        addProgram("    else");
        addProgram("      print('B')");
        addProgram("    endif");
        addProgram("  ELSE");
        addProgram("    if i = 3");
        addProgram("      print('C')");
        addProgram("    else");
        addProgram("      print('D')");
        addProgram("    endif");
        addProgram("  ENDIF");
        addProgram("ENDFOR");
        result("A");
        result("B");
        result("C");
        result("D");
        if (!runTest()) Assert.fail(this.getClass().getName() + " failed");
    }

    @Test private void testVariables() {
        addProgram("int i = 34");
        addProgram("print(i)");
        addProgram("set i= 104");
        addProgram("print(i)");
        addProgram("i = i + 10");
        addProgram("print(i)");
        result("34");
        result("104");
        result("114");
        if (!runTest()) Assert.fail(this.getClass().getName() + " failed");
    }

    @Test private void testSubroutine() {
        addProgram("sub A");
        addProgram("  print('in A')");
        addProgram("endsub");
        addProgram("sub B");
        addProgram("  print('in B')");
        addProgram("endsub");
        addProgram("sub C");
        addProgram("  call B");
        addProgram("  print('in C')");
        addProgram("endsub");
        addProgram("call a");
        addProgram("call c");
        addProgram("call a");
        result("in A");
        result("in B");
        result("in C");
        result("in A");
        if (!runTest()) Assert.fail(this.getClass().getName() + " failed");
    }
}
