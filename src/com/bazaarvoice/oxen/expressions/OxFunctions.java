package com.bazaarvoice.oxen.expressions;

/**
 * Created by steve.ohara
 * Date: 9/19/12 12:07 PM
 */

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.data.OxDataHistorian;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings ({"UnusedDeclaration"})
public class OxFunctions {
    private final HashMap<String, OxFunction> _functions = new HashMap<String, OxFunction>();
    private final OxConstants _constants;

    public OxFunctions(OxConstants constants, Locale locale) {
        _constants = constants;
        findAllFunctions(this);

        // Register all the functions in each of these
        // Don't need to keep around the instance pointers
        new OxFunctionsMath(this, constants);
        new OxFunctionsStrings(this, constants);
        new OxFunctionsDates(this, constants, locale);

        OxDataHistorian.addFunctions(this);
    }

    protected void findAllFunctions(Object functions) {
        Class classes[] = functions.getClass().getDeclaredClasses();
        for (Class cls : classes) {
            if (OxFunction.class.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers())) {
                try {
                    // System.out.println("**** found class " + cls.getName());
                    Class<?> cl[] = new Class<?>[] { functions.getClass() };

                    @SuppressWarnings ({"unchecked"})
                    Constructor<? extends OxFunction> constructor = cls.getConstructor(cl);
                    Object args[] = new Object[] { functions };

                    OxFunction fn = constructor.newInstance(args);
                    addFunction(fn);
                } catch (Exception ex) {
                    throw new OxException(_constants.CANNOT_CREATE_CLASS + " " + cls.getName(), ex);
                }
            }
        }
    }

    //
    // Manage the list of functions
    //
    public OxFunction findFunction(String nam) {
        return _functions.get(nam.toLowerCase());
    }

    public OxFunction[] allFunctions() {
        Collection<OxFunction> coll = _functions.values();
        int sz = coll.size();
        return coll.toArray(new OxFunction[sz]);
    }

    public void addFunction(OxFunction fn) {
        // Put in their _name
        String name = fn.getName();
        String lowName = name.toLowerCase();
        if (_functions.containsKey(lowName)) {
            throw new OxException(_constants.DUPLICATE_FUNCTION + name);
        }
        _functions.put(lowName, fn);

        // And put in my fixed _name (unless it is the same)
        String fixName = fn.getFixName();
        if (!fixName.equalsIgnoreCase(name)) {
            lowName = fixName.toLowerCase();
            if (_functions.containsKey(lowName)) {
                throw new OxException(_constants.DUPLICATE_FUNCTION + fixName);
            }
            _functions.put(lowName, fn);
        }
    }

    protected abstract static class ConstantFn extends OxFunction {
        public ConstantFn(String fixName, String name, String tip) {
            super(fixName, name, tip, 0, 0);
        }
    }

    //
    // Abstract Shared Templates
    //

    private abstract class BfnB extends OxFunction {
        public BfnB(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 1);
        }

        public abstract boolean BevalB(boolean p);

        public void evalFn(OxStack top, int nargs) {
            boolean p = top.getBoolean();
            boolean q = BevalB(p);
            top.pushBoolean(q);
        }
    }

    private abstract class LfnS extends OxFunction {
        public LfnS(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 1);
        }

        public abstract long LevalS(String s);

        public void evalFn(OxStack top, int nargs) {
            String s = top.getString();
            long i = LevalS(s);
            top.pushLong(i);
        }
    }

    private abstract class BfnS extends OxFunction {
        public BfnS(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 1);
        }

        public abstract boolean BevalS(String s);

        public void evalFn(OxStack top, int nargs) {
            String s = top.getString();
            boolean p = BevalS(s);
            top.pushBoolean(p);
        }
    }

    private abstract class DfnS extends OxFunction {
        public DfnS(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 1);
        }

        public abstract double DevalS(String s);

        public void evalFn(OxStack top, int nargs) {
            String s = top.getString();
            double x = DevalS(s);
            top.pushDouble(x);
        }
    }

    private abstract class SfnS extends OxFunction {
        public SfnS(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 1);
        }

        public abstract String SevalS(String s);

        public void evalFn(OxStack top, int nargs) {
            String s = top.getString();
            String t = SevalS(s);
            top.pushString(t);
        }
    }

    private abstract class BfnSS extends OxFunction {
        public BfnSS(String fixName, String name, String tip) {
            super(fixName, name, tip, 2, 2);
        }

        public abstract boolean BevalSS(String s, String t);

        public void evalFn(OxStack top, int nargs) {
            String t = top.getString();
            String s = top.getString();
            boolean p = BevalSS(s, t);
            top.pushBoolean(p);
        }
    }

    private abstract class SfnDS extends OxFunction {
        public SfnDS(String fixName, String name, String tip) {
            super(fixName, name, tip, 2, 2);
        }

        public abstract String SevalDS(double x, String s);

        public void evalFn(OxStack top, int nargs) {
            String s = top.getString();
            double x = top.getDouble();
            String t = SevalDS(x, s);
            top.pushString(t);
        }
    }

    /////////////////////////////////////////////////////////////////
    // START FUNCTION IMPLEMENTATIONS

    /**
     * $true : logical true
     */
    private class TrueFn extends ConstantFn {
        public TrueFn() {
            super("true", _constants.TRUE_NAME, _constants.TRUE_TIP);
        }

        public void evalFn(OxStack top, int nargs) {
            top.pushBoolean(true);
        }

        public void selfTest() {
            shouldWork("not true", "false");
        }
    }

    /**
     * $false : logical false
     */
    private class FalseFn extends ConstantFn {
        public FalseFn() {
            super("false", _constants.FALSE_NAME, _constants.FALSE_TIP);
        }

        public void evalFn(OxStack top, int nargs) {
            top.pushBoolean(false);
        }

        public void selfTest() {
            shouldWork("not false", "true");
        }
    }

    /**
     * if(cond,t,f) : if cond is true, return t else f
     */
    private class IfFn extends OxFunction {
        public IfFn() {
            super("if", _constants.IF_NAME, _constants.IF_TIP, 3, 3);
        }

        public void evalFn(OxStack top, int nargs) {
            if (top.bothLong()) {
                long j = top.getLong();
                long i = top.getLong();
                boolean cond = top.getBoolean();
                top.pushLong(cond ? i : j);
            } else if (top.bothNumeric()) {
                double y = top.getDouble();
                double x = top.getDouble();
                boolean cond = top.getBoolean();
                top.pushDouble(cond ? x : y);
            } else if (top.bothBoolean()) {
                boolean q = top.getBoolean();
                boolean p = top.getBoolean();
                boolean cond = top.getBoolean();
                top.pushBoolean(cond ? p : q);
            } else if (top.bothDate()) {
                Date e = top.getDate();
                Date d = top.getDate();
                boolean cond = top.getBoolean();
                top.pushDate(cond ? d : e);
            } else if (top.eitherString()) {
                String t = top.getString();
                String s = top.getString();
                boolean cond = top.getBoolean();
                top.pushString(cond ? s : t);
            } else {
                throw new OxException("Unexpected stack contents: " + top.getString());
            }
        }

        public void selfTest() {
            shouldWork("if(3<4, 5, 6)", "5");
            shouldWork("if(3>4, 5.1, 6.1)", "6.1");
        }
    }

    /**
     * $not x : logical not
     */
    private class NotFn extends BfnB {
        public NotFn() {
            super("not", _constants.NOT_NAME, _constants.NOT_TIP);
        }

        public boolean BevalB(boolean p) {
            return !p;
        }

        public void selfTest() {
            shouldWork("not (4 > 3)", "false");
            shouldWork("not not (4 > 3)", "true");
            shouldWork("not not not (4 > 3)", "false");
        }
    }

    /**
     * parselong(s) : convert a string to a long integer
     */
    private class ParseLongFn extends LfnS {
        public ParseLongFn() {
            super("parselong", _constants.PARSELONG_NAME, _constants.PARSELONG_TIP);
        }

        public long LevalS(String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ex) {
                throw new OxException("Unable to parse " + s, ex);
            }
        }

        public void selfTest() {
            shouldWork("parselong('  -00234  ')", "-234");
        }
    }

    /**
     * parsedouble(s) : convert a string to a double
     */
    private class ParseDoubleFn extends DfnS {
        public ParseDoubleFn() {
            super("parsedouble", _constants.PARSEDOUBLE_NAME, _constants.PARSEDOUBLE_TIP);
        }

        public double DevalS(String s) {
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException ex) {
                throw new OxException("Unable to parse " + s, ex);
            }
        }

        public void selfTest() {
            shouldWork("parsedouble('  -00234.1200  ')", "-234.12");
        }
    }

    /**
     * formatdouble(s [,fmt]) : convert a double to a string
     */
    private class FormatDoubleFn extends SfnDS {
        public FormatDoubleFn() {
            super("formatdouble", _constants.FORMATDOUBLE_NAME, _constants.FORMATDOUBLE_TIP);
        }

        public String SevalDS(double x, String fmt) {
            DecimalFormat df = new DecimalFormat(fmt);
            return df.format(x);
        }

        public void selfTest() {
            shouldWork("formatdouble(70056.782, '#,##0.00')", "70,056.78");
            shouldWork("formatdouble(123.4567, '0.00')", "123.46");
        }
    }

    /**
     * readfile(fname) : read a whole file into a string
     */
    private class ReadFileFn extends SfnS {
        public ReadFileFn() {
            super("readfile", _constants.READFILE_NAME, _constants.READFILE_TIP);
        }

        public String SevalS(String fname) {
            try {
                StringBuilder result = new StringBuilder();
                BufferedReader inp = new BufferedReader(new FileReader(fname));
                String nextline;

                // Read the whole file into a string
                while ((nextline = inp.readLine()) != null) {
                    result.append(nextline);
                }

                return result.toString();
            } catch (Exception ex) {
                throw new OxException(_constants.UNABLE_TO_READ + ' ' + fname);
            }
        }

        public void selfTest() {
            // Test this with writefile()
        }
    }

    /**
     * writefile(fname, str) : write a string to a file
     * appendfile(fname, str) : append a string to a file
     */
    private abstract class WrtAppFileFn extends BfnSS {
        private final boolean append;

        public WrtAppFileFn(String fixName, String name, String tip, boolean app) {
            super(fixName, name, tip);
            append = app;
        }

        public boolean BevalSS(String fname, String txt)

        {
            try {
                FileWriter fw = new FileWriter(fname, append);
                fw.write(txt);
                fw.close();

                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    private class WriteFileFn extends WrtAppFileFn {
        public WriteFileFn() {
            super("writefile", _constants.WRITEFILE_NAME, _constants.WRITEFILE_TIP, false);
        }

        public void selfTest() {
            // Careful, backslashes do not seem to work at all (on Windows 98)
            shouldWork("writefile('/tmp/delete.me','hi ')", "true");
            shouldWork("appendfile('/tmp/delete.me','there')", "true");
            shouldWork("readfile('/tmp/delete.me')", "hi there");
            shouldWork("fileexists('/tmp/delete.me')", "true");
            shouldWork("fileexists('/tmp/doesnt.exist')", "false");
        }
    }

    private class AppendFileFn extends WrtAppFileFn {
        public AppendFileFn() {
            super("appendfile", _constants.APPENDFILE_NAME, _constants.APPENDFILE_TIP, true);
        }

        public void selfTest() {
            // Test this with writefile()
        }
    }

    /**
     * fileexists(fname) : see if a file exists
     */
    private class FileExistsFn extends BfnS {
        public FileExistsFn() {
            super("fileexists", _constants.FILEEXISTS_NAME, _constants.FILEEXISTS_TIP);
        }

        public boolean BevalS(String fname) {
            try {
                File f = new File(fname);
                return f.exists();
            } catch (Exception ex) {
                return false;
            }
        }

        public void selfTest() {
            // Test this with writefile()
        }
    }

    /**
     * print(str) : print a string to stdout
     */
    private class PrintFn extends BfnS {
        public PrintFn() {
            super("print", _constants.PRINT_NAME, _constants.PRINT_TIP);
        }

        public boolean BevalS(String txt) {
            System.out.println(txt);
            return true;
        }

        public void selfTest() {
            // shouldWork("print('If you do NOT see this line, then this test failed')", "true");
        }
    }
}
