package com.bazaarvoice.oxen.expressions;

/**
 * Created by steve.ohara
 * Date: 9/19/12 12:14 PM
 */

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

@SuppressWarnings ({"UnusedDeclaration"})
public class OxOperators {
    private final HashMap<String, OxOperator> _operators = new HashMap<String, OxOperator>();
    private final OxConstants _constants;

    public OxOperators(OxConstants c) {
        _constants = c;

        Class classes[] = this.getClass().getDeclaredClasses();
        for (Class cls : classes) {
            if (OxOperator.class.isAssignableFrom(cls) &&
                    !FakeBinOp.class.isAssignableFrom(cls) &&
                    !Modifier.isAbstract(cls.getModifiers())) {
                try {
                    //System.out.println("**** class[" + i + "] = " + cls.getName());
                    Class<?> cl[] = new Class<?>[] { OxOperators.class };

                    @SuppressWarnings ({"unchecked"})
                    Constructor<? extends OxOperator> con = cls.getConstructor(cl);
                    Object args[] = new Object[] { this };

                    OxOperator op = con.newInstance(args);
                    addOp(op);
                } catch (Exception ex) {
                    throw new OxException(_constants.CANNOT_CREATE_CLASS + " " + cls.getName(), ex);
                }
            }
        }
    }

    @SuppressWarnings ({"UnnecessaryUnboxing"})
    private void pushResult(OxStack top, Object result) {
        if (result instanceof Byte) {
            top.pushLong(((Byte) result).longValue());
        } else if (result instanceof Short) {
            top.pushLong(((Short) result).longValue());
        } else if (result instanceof Integer) {
            top.pushLong(((Integer) result).longValue());
        } else if (result instanceof Long) {
            top.pushLong(((Long) result).longValue());
        } else if (result instanceof Float) {
            top.pushDouble(((Float) result).doubleValue());
        } else if (result instanceof Double) {
            top.pushDouble(((Double) result).doubleValue());
        } else if (result instanceof Character) {
            top.pushString(result.toString());
        } else if (result instanceof String) {
            top.pushString((String) result);
        } else if (result instanceof Date) {
            top.pushDate((Date) result);
        } else {
            top.pushString(result.toString());
        }
    }

    //
    // Manage list of operators
    //
    public OxOperator findBinOp(String nam) {
        return _operators.get(nam.toLowerCase());
    }

    public OxOperator[] allOperators() {
        Collection<OxOperator> coll = _operators.values();
        int sz = coll.size();
        return coll.toArray(new OxOperator[sz]);
    }

    public void addOp(OxOperator op) {
        String name = op.getName();
        String lowName = name.toLowerCase();
        if (_operators.containsKey(lowName)) {
            throw new OxException("Duplicate operator: " + name);
        }
        _operators.put(lowName, op);

        // Also add the fixname, unless it is the same as mine
        String fixName = op.getFixName();
        lowName = fixName.toLowerCase();
        if (!lowName.equalsIgnoreCase(name)) {
            if (_operators.containsKey(lowName)) {
                throw new OxException("Duplicate operator: " + fixName);
            }
            _operators.put(lowName, op);
        }
    }

    // Some fake operators like comma, lparen, etc
    public abstract class FakeBinOp extends OxOperator {
        public FakeBinOp(OxOperator.PrecedenceEnum p) {
            //noinspection NullableProblems
            super(null, null, null, p);
        }

        public void eval(OxStack top) {
            // Should not be possible to get here
            throw new OxException(_constants.STACK_ERROR);
        }

        public void selfTest() {
            // Should not get here, but so what if it does?
        }
    }

    //
    // More special classes
    //
    // Treat a left paren like a very low priority binop, sort-of
    class LParen_op extends FakeBinOp {
        public LParen_op() {
            super(OxOperator.PrecedenceEnum.LPAREN_PRECEDENCE);
        }
    }

    // Treat a comma like a very low priority binop, sort-of
    class Comma_op extends FakeBinOp {
        public Comma_op() {
            super(OxOperator.PrecedenceEnum.COMMA_PRECEDENCE);
        }
    }

    // Treat a function like a very high priority binop, sort-of
    class Unary_op extends FakeBinOp {
        final OxFunction fn;
        int nargs;

        public Unary_op(OxFunction f) {
            super(OxOperator.PrecedenceEnum.UNARY_PRECEDENCE);
            fn = f;
            nargs = (f.getMaximumArguments() == 0 ? 0 : 1);
        }

        public void eval(OxStack top) {
            fn.evalFn(top, nargs);
        }
    }

    //
    // Some useful classes for parameter types
    //
    private abstract class Bop extends OxOperator {
        public Bop(String fixName, String name, String tip, OxOperator.PrecedenceEnum precedence) {
            super(fixName, name, tip, precedence);
        }

        public abstract boolean BevalBB(boolean p, boolean q);

        public void eval(OxStack top) {
            boolean q = top.getBoolean();
            boolean p = top.getBoolean();
            boolean r = BevalBB(p, q);
            top.pushBoolean(r);
        }
    }

    private abstract class Dop extends OxOperator {
        public Dop(String fixName, String name, String tip, OxOperator.PrecedenceEnum precedence) {
            super(fixName, name, tip, precedence);
        }

        public abstract double DevalDD(double x, double y);

        public void eval(OxStack top) {
            double y = top.getDouble();
            double x = top.getDouble();
            double z = DevalDD(x, y);
            top.pushDouble(z);
        }
    }

    private abstract class LDop extends OxOperator {
        public LDop(String fixName, String name, String tip, OxOperator.PrecedenceEnum precedence) {
            super(fixName, name, tip, precedence);
        }

        public abstract long LevalLL(long x, long y);

        public abstract double DevalDD(double x, double y);

        public void eval(OxStack top) {
            if (top.bothLong()) {
                long j = top.getLong();
                long i = top.getLong();
                long k = LevalLL(i, j);
                top.pushLong(k);
            } else {
                double y = top.getDouble();
                double x = top.getDouble();
                double z = DevalDD(x, y);
                top.pushDouble(z);
            }
        }
    }

    private abstract class SLDop extends OxOperator {
        public SLDop(String fixName, String name, String tip, OxOperator.PrecedenceEnum precedence) {
            super(fixName, name, tip, precedence);
        }

        public abstract long LevalLL(long i, long j);

        public abstract double DevalDD(double x, double y);

        public abstract String SevalSS(String s, String t);

        public void eval(OxStack top) {
            if (top.bothLong()) {
                long j = top.getLong();
                long i = top.getLong();
                long k = LevalLL(i, j);
                top.pushLong(k);
            } else if (top.bothNumeric()) {
                double y = top.getDouble();
                double x = top.getDouble();
                double z = DevalDD(x, y);
                top.pushDouble(z);
            } else if (top.eitherString()) {
                String t = top.getString();
                String s = top.getString();
                String r = SevalSS(s, t);
                top.pushString(r);
            } else {
                throw new OxException("Unexpected stack contents: " + top.getString());
            }
        }
    }

    ///////////////////////////////////////////////////////
    // Start Operator Implementations

    /**
     * x + y : add two numbers (or concatenate two strings)
     */
    private class AddOp extends SLDop {
        public AddOp() {
            super("add", _constants.ADD_OP, _constants.ADD_TIP, OxOperator.PrecedenceEnum.PLUS_PRECEDENCE);
        }

        public String SevalSS(String s, String t) {
            return s + t;
        }

        public long LevalLL(long i, long j) {
            return i + j;
        }

        public double DevalDD(double x, double y) {
            return x + y;
        }

        public void selfTest() {
            shouldWork("1+2+3", "6");
            shouldWork("  1  + 2 +3+  4+5  ", "15");
            shouldWork("'abc'+4+\"def\"", "abc4def");
        }
    }

    /**
     * x - y : subtract two numbers
     */
    private class SubOp extends LDop {
        public SubOp() {
            super("subtract", _constants.SUB_OP, _constants.SUB_TIP, OxOperator.PrecedenceEnum.PLUS_PRECEDENCE);
        }

        public long LevalLL(long i, long j) {
            return i - j;
        }

        public double DevalDD(double x, double y) {
            return x - y;
        }

        public void selfTest() {
            shouldWork(" 10 - 4 + 1 ", "7");
            shouldWork(" -10 --4 + -1 ", "-7");
        }
    }

    /**
     * x * y : multiply two numbers
     */
    private class MultOp extends LDop {
        public MultOp() {
            super("multiply", _constants.MULT_OP, _constants.MULT_TIP, OxOperator.PrecedenceEnum.TIMES_PRECEDENCE);
        }

        public long LevalLL(long i, long j) {
            return i * j;
        }

        public double DevalDD(double x, double y) {
            return x * y;
        }

        public void selfTest() {
            shouldWork(" -2 * 4 * 7 ", "-56");
        }
    }

    /**
     * x / y : divide two numbers
     */
    private class DivOp extends Dop {
        public DivOp() {
            super("divide", _constants.DIV_OP, _constants.DIV_TIP, OxOperator.PrecedenceEnum.TIMES_PRECEDENCE);
        }

        public double DevalDD(double x, double y) {
            if (y == 0.0) {
                throw new OxException("Attempt to divide by zero");
            }

            return x / y;
        }

        public void selfTest() {
            shouldWork(" 210 / 30 ", "7.0");
            shouldFail("  666  /  0  ");
        }
    }

    /**
     * x $mod y : remainder after division
     */
    private class ModOp extends LDop {
        public ModOp() {
            super("modulus", _constants.MOD_OP, _constants.MOD_TIP, OxOperator.PrecedenceEnum.TIMES_PRECEDENCE);
        }

        public long LevalLL(long i, long j) {
            if (j == 0) {
                throw new OxException("Attempt to do modulus division by zero");
            }

            return i % j;
        }

        public double DevalDD(double x, double y) {
            if (y == 0.0) {
                throw new OxException("Attempt to do modulus division by zero");
            }

            return x % y;
        }

        public void selfTest() {
            shouldWork(" 14 mod 3 ", "2");
            shouldWork(" -14 mod 3 ", "-2");
            shouldWork(" 14 mod -3 ", "2");
            shouldWork(" -14 mod -3 ", "-2");
            shouldFail("  666  MOD  0  ");
        }
    }

    /**
     * x ^ y : raise to the power
     */
    private class PowOp extends Dop {
        public PowOp() {
            super("power", _constants.POW_OP, _constants.POW_TIP, OxOperator.PrecedenceEnum.POWER_PRECEDENCE);
        }

        public double DevalDD(double x, double y) {
            if (x < 0 && y != 0) {
                // Special case for cube root, and any other odd root
                double invy = 1.0 / y;

                if (invy % 2 == 1) {
                    return -Math.pow(-x, y);
                }
            }

            return Math.pow(x, y);
        }

        public void selfTest() {
            shouldWork("4 ^ 3", "64.0");
            shouldWork("0 ^ 0", "1.0");
            shouldWork("(-27) ^ (1/3)", "-3.0");
            shouldWork("(-243) ^ (1/5)", "-3.0");
        }
    }

    /**
     * p $and p : logical and
     */
    private class AndOp extends Bop {
        public AndOp() {
            super("and", _constants.AND_OP, _constants.AND_TIP, OxOperator.PrecedenceEnum.AND_PRECEDENCE);
        }

        public boolean BevalBB(boolean p, boolean q) {
            return p && q;
        }

        public void selfTest() {
            shouldWork("true and true", "true");
            shouldWork("true and false", "false");
            shouldWork("false and true", "false");
            shouldWork("false and false", "false");
        }
    }

    /**
     * p $or p : logical or
     */
    private class OrOp extends Bop {
        public OrOp() {
            super("or", _constants.OR_OP, _constants.OR_TIP, OxOperator.PrecedenceEnum.OR_PRECEDENCE);
        }

        public boolean BevalBB(boolean p, boolean q) {
            return p || q;
        }

        public void selfTest() {
            shouldWork("true or true", "true");
            shouldWork("true or false", "true");
            shouldWork("false or true", "true");
            shouldWork("false or false", "false");
        }
    }

    /**
     * p $xor p : logical exclusive or
     */
    private class XorOp extends Bop {
        public XorOp() {
            super("xor", _constants.XOR_OP, _constants.XOR_TIP, OxOperator.PrecedenceEnum.OR_PRECEDENCE);
        }

        public boolean BevalBB(boolean p, boolean q) {
            return p ^ q;
        }

        public void selfTest() {
            shouldWork("true xor true", "false");
            shouldWork("true xor false", "true");
            shouldWork("false xor true", "true");
            shouldWork("false xor false", "false");
        }
    }

    /**
     * p <RELOP> p : the six relational operators (< <= = <> >= >)
     * spread across all the data types (int, boolean, etc)
     */
    public abstract class RelOp extends OxOperator {
        private final boolean caselt;
        private final boolean caseeq;
        private final boolean casegt;
        private final int MAXRELOPS = 100;
        private RelOp saveop[] = new RelOp[MAXRELOPS];

        public RelOp(String fixn, String n, String t, boolean iflt,
                     boolean ifeq, boolean ifgt) {
            super(fixn, n, t, OxOperator.PrecedenceEnum.REL_PRECEDENCE);
            caselt = iflt;
            caseeq = ifeq;
            casegt = ifgt;
        }

        private boolean BevalBB(boolean p, boolean q) {
            return (p == q) ? caseeq : ((!p && q) ? caselt : casegt);
        }

        private boolean BevalLL(long i, long j) {
            return (i == j) ? caseeq : (i < j ? caselt : casegt);
        }

        private boolean BevalDD(double x, double y) {
            return (x == y) ? caseeq : (x < y ? caselt : casegt);
        }

        private boolean BevalTT(Date c, Date d) {
            int compT = c.compareTo(d);
            return (compT == 0 ? caseeq : (compT < 0 ? caselt : casegt));
        }

        private boolean BevalSS(String s, String t) {
            int compS = s.compareTo(t);
            return (compS == 0 ? caseeq : (compS < 0 ? caselt : casegt));
        }

        public void eval(OxStack top) {
            if (top.bothBoolean()) {
                boolean q = top.getBoolean();
                boolean p = top.getBoolean();
                boolean b = BevalBB(p, q);
                top.pushBoolean(b);
            } else if (top.bothLong()) {
                long j = top.getLong();
                long i = top.getLong();
                boolean b = BevalLL(i, j);
                top.pushBoolean(b);
            } else if (top.bothNumeric()) {
                double y = top.getDouble();
                double x = top.getDouble();
                boolean b = BevalDD(x, y);
                top.pushBoolean(b);
            } else if (top.bothDate()) {
                Date d = top.getDate();
                Date c = top.getDate();
                boolean b = BevalTT(c, d);
                top.pushBoolean(b);
            } else if (top.eitherString()) {
                String t = top.getString();
                String s = top.getString();
                boolean b = BevalSS(s, t);
                top.pushBoolean(b);
            } else {
                throw new OxException("Unexpected stack contents: " + top.getString());
            }
        }
    }

    private class LTOp extends RelOp {
        public LTOp() {
            super("lt", _constants.LT_OP, _constants.LT_TIP, true, false, false);
        }

        public void selfTest() {
            shouldWork("4 < 3", "false");
            shouldWork("2 <= 3", "true");
            shouldWork("1 < 2 and 2 <= 3 and 4 > 3 and 2 > 1", "true");
            shouldWork("'Dog' < 'Cat'", "false");
        }
    }

    private class LEOp extends RelOp {
        public LEOp() {
            super("le", _constants.LE_OP, _constants.LE_TIP, true, true, false);
        }

        public void selfTest() {
            shouldWork("4 <= 3", "false");
            shouldWork("'Dog' <= 'Cat'", "false");
        }
    }

    private class EQOp extends RelOp {
        public EQOp() {
            super("eq", _constants.EQ_OP, _constants.EQ_TIP, false, true, false);
        }

        public void selfTest() {
            shouldWork("4 = 3", "false");
            shouldWork("4.0 = 4", "true");
            shouldWork("'Dog' = 'Cat'", "false");
            shouldWork("$now = parsedate('jan 1, 1804')", "false");
        }
    }

    private class NEOp extends RelOp {
        public NEOp() {
            super("ne", _constants.NE_OP, _constants.NE_TIP, true, false, true);
        }

        public void selfTest() {
            shouldWork("4 <> 3", "true");
            shouldWork("'Dog' <> 'Cat'", "true");
        }
    }

    private class GEOp extends RelOp {
        public GEOp() {
            super("ge", _constants.GE_OP, _constants.GE_TIP, false, true, true);
        }

        public void selfTest() {
            shouldWork("4 >= 3", "true");
            shouldWork("4 >= 5", "false");
            shouldWork("'Dog' >= 'Cat'", "true");
        }
    }

    private class GTOp extends RelOp {
        public GTOp() {
            super("gt", _constants.GT_OP, _constants.GT_TIP, false, false, true);
        }

        public void selfTest() {
            shouldWork("4 > 3", "true");
            shouldWork("4 > 5", "false");
            shouldWork("4.0 > 3", "true");
            shouldWork("'Dog' > 'Cat'", "true");
        }
    }
}
