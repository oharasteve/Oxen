package com.bazaarvoice.oxen.expressions;

import com.bazaarvoice.oxen.OxConstants;

/**
 * Created by steve.ohara
 * Date: 9/20/12 7:06 AM
 */

@SuppressWarnings ({"UnusedDeclaration"})
public class OxFunctionsStrings {
    private final OxConstants _constants;

    public OxFunctionsStrings(OxFunctions functions, OxConstants constants) {
        _constants = constants;
        functions.findAllFunctions(this);
    }

    //
    // Abstract Shared Templates
    //

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

    private abstract class SfnSL extends OxFunction {
        public SfnSL(String fixName, String name, String tip) {
            super(fixName, name, tip, 2, 2);
        }

        public abstract String SevalSL(String s, long i);

        public void evalFn(OxStack top, int nargs) {
            long i = top.getLong();
            String s = top.getString();
            String t = SevalSL(s, i);
            top.pushString(t);
        }
    }

    private abstract class SfnSLL extends OxFunction {
        public SfnSLL(String fixName, String name, String tip) {
            super(fixName, name, tip, 3, 3);
        }

        public abstract String SevalSLL(String s, long i, long j);

        public void evalFn(OxStack top, int nargs) {
            long j = top.getLong();
            long i = top.getLong();
            String s = top.getString();
            String t = SevalSLL(s, i, j);
            top.pushString(t);
        }
    }

    private abstract class LfnSS extends OxFunction {
        public LfnSS(String fixName, String name, String tip) {
            super(fixName, name, tip, 2, 2);
        }

        public abstract long LevalSS(String s, String t);

        public void evalFn(OxStack top, int nargs) {
            String t = top.getString();
            String s = top.getString();
            long i = LevalSS(s, t);
            top.pushLong(i);
        }
    }

    private abstract class JustifyFn extends SfnSL {
        private final int frac;

        public JustifyFn(String fixName, String name, String tip, int fraction) {
            super(fixName, name, tip);
            frac = fraction;
        }

        public String SevalSL(String str, long nc) {
            long len = str.length();

            if (nc <= len) {
                return str;
            }

            long before = ((nc - len) * frac) / 2;
            long after = nc - len - before;
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < before; i++) {
                result.append(' ');
            }

            result.append(str);

            for (int i = 0; i < after; i++) {
                result.append(' ');
            }

            return result.toString();
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

    //
    // Actual OxFunction Definitions
    //

    /**
     * len(s) : length of a string
     */
    private class LenFn extends LfnS {
        public LenFn() {
            super("len", _constants.LEN_NAME, _constants.LEN_TIP);
        }

        public long LevalS(String s) {
            return s.length();
        }

        public void selfTest() {
            shouldWork("len('abc')", "3");
        }
    }

    /**
     * lower(s) : convert a string to lower case
     */
    private class LowerFn extends SfnS {
        public LowerFn() {
            super("lower", _constants.LOWER_NAME, _constants.LOWER_TIP);
        }

        public String SevalS(String s) {
            return s.toLowerCase();
        }

        public void selfTest() {
            shouldWork("lower('AbCdEfG123')", "abcdefg123");
        }
    }

    /**
     * upper(s) : convert a string to upper case
     */
    private class UpperFn extends SfnS {
        public UpperFn() {
            super("upper", _constants.UPPER_NAME, _constants.UPPER_TIP);
        }

        public String SevalS(String s) {
            return s.toUpperCase();
        }

        public void selfTest() {
            shouldWork("upper('AbCdEfG123')", "ABCDEFG123");
        }
    }

    /**
     * trim(s) : trim leading and trailing spaces from a string
     */
    private class TrimFn extends SfnS {
        public TrimFn() {
            super("trim", _constants.TRIM_NAME, _constants.TRIM_TIP);
        }

        public String SevalS(String s) {
            return s.trim();
        }

        public void selfTest() {
            shouldWork("trim('    AbCdEfG  123   ')", "AbCdEfG  123");
        }
    }

    /**
     * lj(s,nc) : left justify a string
     * cj(s,nc) : center justify a string
     * rj(s,nc) : right justify a string
     */
    private class LjFn extends JustifyFn {
        public LjFn() {
            super("lj", _constants.LJ_NAME, _constants.LJ_TIP, 0);
        }

        public void selfTest() {
            shouldWork("lj('ABC', 9)", "ABC      ");
            shouldWork("lj('ABCDEFGHIJKL', 2)", "ABCDEFGHIJKL");
        }
    }

    private class CjFn extends JustifyFn {
        public CjFn() {
            super("cj", _constants.CJ_NAME, _constants.CJ_TIP, 1);
        }

        public void selfTest() {
            shouldWork("cj('ABC', 9)", "   ABC   ");
            shouldWork("cj('ABC', 8)", "  ABC   ");
        }
    }

    private class RjFn extends JustifyFn {
        public RjFn() {
            super("rj", _constants.RJ_NAME, _constants.RJ_TIP, 2);
        }

        public void selfTest() {
            shouldWork("rj('ABC', 9)", "      ABC");
        }
    }

    /**
     * mid(str,sc,ec) : extract a substring
     */
    private class MidFn extends SfnSLL {
        public MidFn() {
            super("mid", _constants.MID_NAME, _constants.MID_TIP);
        }

        public String SevalSLL(String str, long sc0, long ec0) {
            final int len = str.length();
            int sc = (sc0 < 1 ? 1 : (int) sc0);
            int ec = (ec0 > len ? len : (int) ec0);
            return str.substring(sc - 1, ec);
        }

        public void selfTest() {
            shouldWork("mid('AbCdEfG123',3,5)", "CdE");
        }
    }

    /**
     * left(str,nc) : extract leftmost characters from a string
     */
    private class LeftFn extends SfnSL {
        public LeftFn() {
            super("left", _constants.LEFT_NAME, _constants.LEFT_TIP);
        }

        public String SevalSL(String str, long nc) {
            if (nc >= str.length()) {
                return str;
            }

            return str.substring(0, (int)nc);
        }

        public void selfTest() {
            shouldWork("left('AbCdEfG123',4)", "AbCd");
        }
    }

    /**
     * right(str,nc) : extract rightmost characters from a string
     */
    private class RightFn extends SfnSL {
        public RightFn() {
            super("right", _constants.RIGHT_NAME, _constants.RIGHT_TIP);
        }

        public String SevalSL(String str, long nc) {
            final int len = str.length();

            if (nc >= len) {
                return str;
            }

            return str.substring(len - (int) nc);
        }

        public void selfTest() {
            shouldWork("right('AbCdEfG123',4)", "G123");
        }
    }

    /**
     * startswith(str, substring) : see if string starts with substring
     */
    private class StartsWithFn extends BfnSS {
        public StartsWithFn() {
            super("startswith", _constants.STARTSWITH_NAME, _constants.STARTSWITH_TIP);
        }

        public boolean BevalSS(String s, String t) {
            return s.startsWith(t);
        }

        public void selfTest() {
            shouldWork("startswith('AbCdE','AbC')", "true");
            shouldWork("startswith('AbCdE','ABC')", "false");
        }
    }

    /**
     * endswith(str, substring) : see if string ends with substring
     */
    private class EndsWithFn extends BfnSS {
        public EndsWithFn() {
            super("endswith", _constants.ENDSWITH_NAME, _constants.ENDSWITH_TIP);
        }

        public boolean BevalSS(String s, String t) {
            return s.endsWith(t);
        }

        public void selfTest() {
            shouldWork("endswith('AbCdE','CdE')", "true");
            shouldWork("endswith('AbCdE','CDE')", "false");
        }
    }

    /**
     * indexof(str, substring) : find substring in a string
     */
    private class IndexOfFn extends LfnSS {
        public IndexOfFn() {
            super("indexof", _constants.INDEXOF_NAME, _constants.INDEXOF_TIP);
        }

        public long LevalSS(String s, String t) {
            long indx = s.indexOf(t);

            return (indx < 0 ? indx : (indx + 1));
        }

        public void selfTest() {
            shouldWork("indexof('AbCdE','CdE')", "3");
            shouldWork("indexof('AbCdE','CDE')", "-1");
        }
    }
}
