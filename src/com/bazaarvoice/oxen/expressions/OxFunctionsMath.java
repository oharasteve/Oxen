package com.bazaarvoice.oxen.expressions;

/**
 * Created by steve.ohara
 * Date: 9/20/12 7:06 AM
 */

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;

import java.util.Random;

@SuppressWarnings ({"UnusedDeclaration"})
public class OxFunctionsMath {
    private final OxConstants _constants;

    private Random _randomSeed = null;

    public OxFunctionsMath(OxFunctions functions, OxConstants constants) {
        _constants = constants;
        functions.findAllFunctions(this);
    }

    //
    // Abstract Shared Templates
    //

    private abstract class NfnN extends OxFunction {
        public NfnN(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 1);
        }

        public abstract long LevalL(long i);

        public abstract double DevalD(double x);

        public void evalFn(OxStack top, int nargs) {
            if (top.isLong()) {
                long i = top.getLong();
                long j = LevalL(i);
                top.pushLong(j);
            } else {
                double x = top.getDouble();
                double y = DevalD(x);
                top.pushDouble(y);
            }
        }
    }

    private abstract class LfnN extends OxFunction {
        public LfnN(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 1);
        }

        public abstract long LevalL(long i);

        public abstract long LevalD(double x);

        public void evalFn(OxStack top, int nargs) {
            if (top.isLong()) {
                long i = top.getLong();
                long j = LevalL(i);
                top.pushLong(j);
            } else {
                double x = top.getDouble();
                long i = LevalD(x);
                top.pushLong(i);
            }
        }
    }

    private abstract class LfnL extends OxFunction {
        public LfnL(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 1);
        }

        public abstract long LevalL(long i);

        public void evalFn(OxStack top, int nargs) {
            long i = top.getLong();
            long j = LevalL(i);
            top.pushLong(j);
        }
    }

    protected abstract class DfnD extends OxFunction {
        public DfnD(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 1);
        }

        public abstract double DevalD(double x);

        public void evalFn(OxStack top, int nargs) {
            double x = top.getDouble();
            double y = DevalD(x);
            top.pushDouble(y);
        }
    }

    protected abstract class DfnDd extends OxFunction {
        public DfnDd(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 2);
        }

        public abstract double DevalD(double x);

        public abstract double DevalDD(double x, double y);

        public void evalFn(OxStack top, int nargs) {
            if (nargs == 1) {
                double x = top.getDouble();
                double y = DevalD(x);
                top.pushDouble(y);
            } else {
                double y = top.getDouble();
                double x = top.getDouble();
                double z = DevalDD(x, y);
                top.pushDouble(z);
            }
        }
    }

    protected abstract class DfnDL extends OxFunction {
        public DfnDL(String fixName, String name, String tip) {
            super(fixName, name, tip, 2, 2);
        }

        public abstract double DevalDL(double x, long i);

        public void evalFn(OxStack top, int nargs) {
            long i = top.getLong();
            double x = top.getDouble();
            double z = DevalDL(x, i);
            top.pushDouble(z);
        }
    }

    protected abstract class LfnLL extends OxFunction {
        public LfnLL(String fixName, String name, String tip) {
            super(fixName, name, tip, 2, 2);
        }

        public abstract long LevalLL(long i, long j);

        public void evalFn(OxStack top, int nargs) {
            long j = top.getLong();
            long i = top.getLong();
            long k = LevalLL(i, j);
            top.pushLong(k);
        }
    }

    //
    // Actual OxFunction Definitions
    //

    /**
     * $pi : 3.14159 ...
     */
    private class PiFn extends OxFunctions.ConstantFn {
        public PiFn() {
            super("pi", _constants.PI_NAME, _constants.PI_TIP);
        }

        public void evalFn(OxStack top, int nargs) {
            top.pushDouble(Math.PI);
        }

        public void selfTest() {
            shouldWork("($pi > $e) = $true", "true");
        }
    }

    /**
     * $e : 2.71828 ...
     */
    private class EFn extends OxFunctions.ConstantFn {
        public EFn() {
            super("e", _constants.E_NAME, _constants.E_TIP);
        }

        public void evalFn(OxStack top, int nargs) {
            top.pushDouble(Math.E);
        }

        public void selfTest() {
            shouldWork("(pi <= e) = false", "true");
        }
    }

    /**
     * -x : negative of a number
     */
    private class NegFn extends NfnN {
        public NegFn() {
            super("neg", _constants.NEG_SIGN, _constants.NEG_TIP);
        }

        public long LevalL(long i) {
            return -i;
        }

        public double DevalD(double x) {
            return -x;
        }

        public void selfTest() {
            shouldWork("---4", "-4");
            shouldWork(" - - - - 4 ", "4");
        }
    }

    /**
     * abs(x) : absolute value of a number
     */
    private class AbsFn extends NfnN {
        public AbsFn() {
            super("abs", _constants.ABS_NAME, _constants.ABS_TIP);
        }

        public long LevalL(long i) {
            return (i < 0 ? -i : i);
        }

        public double DevalD(double x) {
            return (x < 0 ? -x : x);
        }

        public void selfTest() {
            shouldWork("abs(-56) + abs(1.1)", "57.1");
        }
    }

    /**
     * sign(x) : sign of a number (-1, 0 or 1)
     */
    private class SignFn extends LfnN {
        public SignFn() {
            super("sign", _constants.SIGN_NAME, _constants.SIGN_TIP);
        }

        public long LevalL(long i) {
            return (i < 0) ? -1 : ((i > 0) ? 1 : 0);
        }

        public long LevalD(double x) {
            return (x < 0) ? -1 : ((x > 0) ? 1 : 0);
        }

        public void selfTest() {
            shouldWork("sign(-5.3)", "-1");
            shouldWork("sign(0.0)", "0");
            shouldWork("sign(5.3)", "1");
        }
    }

    /**
     * sqrt(x) : take square root of a number
     */
    private class SqrtFn extends DfnD {
        public SqrtFn() {
            super("sqrt", _constants.SQRT_NAME, _constants.SQRT_TIP);
        }

        public double DevalD(double x) {
            if (x < 0) {
                throw new OxException("Attempt to take sqrt of a negative number");
            }

            return Math.sqrt(x);
        }

        public void selfTest() {
            shouldWork("sqrt(16.00)", "4.0");
            shouldWork("5 + sqrt((((80) + 1))) + 0.0", "14.0");
            shouldWork("$sqrt(--9.0)", "3.0");
            shouldFail("sqrt(-5)");
        }
    }

    /**
     * root(x, n) : take n'th root of a number
     */
    private class RootFn extends DfnDL {
        public RootFn() {
            super("root", _constants.ROOT_NAME, _constants.ROOT_TIP);
        }

        public double DevalDL(double x, long i) {
            if (i == 0) {
                if (x == 0) {
                    throw new OxException("Cannot raise zero to the zero'th power");
                }

                return 1.0;
            }

            int sign = 1;

            if (i % 2 == 1 && x < 0) {
                sign = -1;
            }

            return sign * Math.pow(sign * x, 1.0 / i);
        }

        public void selfTest() {
            shouldWork("root(5.6, 0)", "1.0");
            shouldWork("root(-27, 3)", "-3.0");
        }
    }

    /**
     * sin(x) : sine of a number (radians)
     * sind(x) : sine of a number (degrees)
     */
    private class SinFn extends DfnD {
        public SinFn() {
            super("sin", _constants.SIN_NAME, _constants.SIN_TIP);
        }

        public double DevalD(double x) {
            return Math.sin(x);
        }

        public void selfTest() {
            shouldWork("sin(pi/2)", "1.0");
        }
    }

    private class SinDFn extends DfnD {
        public SinDFn() {
            super("sind", _constants.SIND_NAME, _constants.SIND_TIP);
        }

        public double DevalD(double x) {
            return Math.sin(x * Math.PI / 180);
        }

        public void selfTest() {
            shouldWork("abs(sind(180))<1e-14", "true");
        }
    }

    /**
     * cos(x) : cosine of a number (radians)
     * cosd(x) : cosine of a number (degrees)
     */
    private class CosFn extends DfnD {
        public CosFn() {
            super("cos", _constants.COS_NAME, _constants.COS_TIP);
        }

        public double DevalD(double x) {
            return Math.cos(x);
        }

        public void selfTest() {
            shouldWork("cos(pi)", "-1.0");
        }
    }

    private class CosDFn extends DfnD {
        public CosDFn() {
            super("cosd", _constants.COSD_NAME, _constants.COSD_TIP);
        }

        public double DevalD(double x) {
            return Math.cos(x * Math.PI / 180);
        }

        public void selfTest() {
            shouldWork("abs(cosd(90))<1e-14", "true");
        }
    }

    /**
     * tan(x) : tangent of a number (radians)
     * tand(x) : tangent of a number (degrees)
     */
    private class TanFn extends DfnD {
        public TanFn() {
            super("tan", _constants.TAN_NAME, _constants.TAN_TIP);
        }

        public double DevalD(double x) {
            return Math.tan(x);
        }

        public void selfTest() {
            shouldWork("abs(tan(pi/4)-1.0) < 0.0001", "true");
        }
    }

    private class TanDFn extends DfnD {
        public TanDFn() {
            super("tand", _constants.TAND_NAME, _constants.TAND_TIP);
        }

        public double DevalD(double x) {
            return Math.tan(x * Math.PI / 180);
        }

        public void selfTest() {
            shouldWork("abs(tand(45)-1.0) < 0.0001", "true");
        }
    }

    /**
     * asin(x) : arcsine of angle (radians)
     * asind(x) : arcsine of angle (degrees)
     */
    private class ASinFn extends DfnD {
        public ASinFn() {
            super("asin", _constants.ASIN_NAME, _constants.ASIN_TIP);
        }

        public double DevalD(double x) {
            return Math.asin(x);
        }

        public void selfTest() {
            shouldWork("pi / asin(1)", "2.0");
        }
    }

    private class ASinDFn extends DfnD {
        public ASinDFn() {
            super("asind", _constants.ASIND_NAME, _constants.ASIND_TIP);
        }

        public double DevalD(double x) {
            return Math.asin(x) * 180 / Math.PI;
        }

        public void selfTest() {
            shouldWork("asind(1)", "90.0");
        }
    }

    /**
     * acos(x) : arccosine of angle (radians)
     * acosd(x) : arccosine of angle (degrees)
     */
    private class ACosFn extends DfnD {
        public ACosFn() {
            super("acos", _constants.ACOS_NAME, _constants.ACOS_TIP);
        }

        public double DevalD(double x) {
            return Math.acos(x);
        }

        public void selfTest() {
            shouldWork("pi / acos(-1)", "1.0");
        }
    }

    private class ACosDFn extends DfnD {
        public ACosDFn() {
            super("acosd", _constants.ACOSD_NAME, _constants.ACOSD_TIP);
        }

        public double DevalD(double x) {
            return Math.acos(x) * 180 / Math.PI;
        }

        public void selfTest() {
            shouldWork("acosd(-1)", "180.0");
        }
    }

    /**
     * atan(x) : arctangent of angle (radians)
     */
    private class AtanFn extends DfnDd {
        public AtanFn() {
            super("atan", _constants.ATAN_NAME, _constants.ATAN_TIP);
        }

        public double DevalD(double x) {
            return Math.atan(x);
        }

        public double DevalDD(double x, double y) {
            return Math.atan2(x, y);
        }

        public void selfTest() {
            shouldWork("atan(1) / pi", "0.25");
            shouldWork("atan(-1,1) / pi", "-0.25");
        }
    }

    /**
     * atand(x) : arctangent of angle (degrees)
     */
    private class AtanDFn extends DfnDd {
        public AtanDFn() {
            super("atand", _constants.ATAND_NAME, _constants.ATAND_TIP);
        }

        public double DevalD(double x) {
            return Math.atan(x) * 180 / Math.PI;
        }

        public double DevalDD(double x, double y) {
            return Math.atan2(x, y) * 180 / Math.PI;
        }

        public void selfTest() {
            shouldWork("atand(1)", "45.0");
            shouldWork("atand(1,-1)", "135.0");
        }
    }

    /**
     * exp(x) : take exponential of a number (e to the power)
     */
    private class ExpFn extends DfnD {
        public ExpFn() {
            super("exp", _constants.EXP_NAME, _constants.EXP_TIP);
        }

        public double DevalD(double x) {
            return Math.exp(x);
        }

        public void selfTest() {
            shouldWork("abs(exp(3) - (e*e*e)) < 1e-8", "true");
            shouldWork("2.7*2.7 < exp(2)", "true");
            shouldWork("exp(2) < 2.8*2.8", "true");
        }
    }

    /**
     * ln(x) : take natural log (base e) of a number
     */
    private class LnFn extends DfnD {
        public LnFn() {
            super("ln", _constants.LN_NAME, _constants.LN_TIP);
        }

        public double DevalD(double x) {
            if (x <= 0) {
                if (x == 0) {
                    throw new OxException("Attempt to take log of zero");
                }
                throw new OxException("Attempt to take log of a negative number");
            }

            return Math.log(x);
        }

        public void selfTest() {
            shouldWork("ln(e*e*e*e)", "4.0");
            shouldFail("ln(-2)");
        }
    }

    /**
     * log(x, base) : take log (default base 10) of a number
     */
    private class LogFn extends DfnDd {
        public LogFn() {
            super("log", _constants.LOG_NAME, _constants.LOG_TIP);
        }

        public double DevalD(double x) {
            return DevalDD(x, 10.0);
        }

        public double DevalDD(double x, double y) {
            if (x <= 0 || y <= 0) {
                if (x == 0) {
                    throw new OxException("Attempt to take log of zero");
                }
                if (x < 0) {
                    throw new OxException("Attempt to take log of a negative number");
                }
                if (y == 0) {
                    throw new OxException("Attempt to take log base zero");
                }
                throw new OxException("Attempt to take log with a negative base");
            }

            return Math.log(x) / Math.log(y);
        }

        public void selfTest() {
            shouldWork("log(100000)", "5.0");
            shouldWork("log(6*6*6*6*6, 6)", "5.0");
        }
    }

    /**
     * ceil(x) : round a number up to next whole number
     */
    private class CeilFn extends DfnD {
        public CeilFn() {
            super("ceil", _constants.CEIL_NAME, _constants.CEIL_TIP);
        }

        public double DevalD(double x) {
            return Math.ceil(x);
        }

        public void selfTest() {
            shouldWork("ceil(-3.4)", "-3.0");
            shouldWork("ceil(3.4)", "4.0");
        }
    }

    /**
     * floor(x) : round a number down to next whole number
     */
    private class FloorFn extends DfnD {
        public FloorFn() {
            super("floor", _constants.FLOOR_NAME, _constants.FLOOR_TIP);
        }

        public double DevalD(double x) {
            return Math.floor(x);
        }

        public void selfTest() {
            shouldWork("floor(-3.4)", "-4.0");
            shouldWork("floor(3.4)", "3.0");
        }
    }

    /**
     * round(x, y) : round a number (default to nearest whole number)
     */
    private class RoundFn extends DfnDd {
        public RoundFn() {
            super("round", _constants.ROUND_NAME, _constants.ROUND_TIP);
        }

        public double DevalD(double x) {
            return Math.rint(x);
        }

        public double DevalDD(double x, double y) {
            if (y == 0.0) {
                return 0.0;
            }

            return Math.rint(x / y) * y;
        }

        public void selfTest() {
            shouldWork("round(123.456)", "123.0");
            shouldWork("round(-123.456)", "-123.0");
            shouldWork("round(1234.56)", "1235.0");
            shouldWork("round(-1234.56)", "-1235.0");
            shouldWork("round(12345.678, 100)", "12300.0");
            shouldWork("round(12.345678, 0.001)", "12.346");
        }
    }

    /**
     * rand(i, j) : i <= random number <= j
     */
    private class RandFn extends LfnLL {
        public RandFn() {
            super("rand", _constants.RAND_NAME, _constants.RAND_TIP);
        }

        public long LevalLL(long i, long j) {
            if (_randomSeed == null) {
                _randomSeed = new Random();
            }

            double x = (j - i + 1) * _randomSeed.nextDouble();

            return i + (long) x;
        }

        public void selfTest() {
            shouldWork("51 <= rand(51, 53)", "true");
            shouldWork("rand(51, 53) <= 53", "true");
        }
    }

    /**
     * random : 0.0 <= random number < 1.0
     */
    private class RandomFn extends OxFunctions.ConstantFn {
        public RandomFn() {
            super("random", _constants.RANDOM_NAME, _constants.RANDOM_TIP);
        }

        public void evalFn(OxStack top, int nargs) {
            if (_randomSeed == null) {
                _randomSeed = new Random();
            }

            top.pushDouble(_randomSeed.nextDouble());
        }

        public void selfTest() {
            shouldWork("0.0 <= $random", "true");
            shouldWork("$random < 1.0", "true");
        }
    }

    /**
     * randseed(i) : initialize random number generator
     */
    private class RandSeedFn extends LfnL {
        public RandSeedFn() {
            super("randseed", _constants.RANDSEED_NAME, _constants.RANDSEED_TIP);
        }

        public long LevalL(long i) {
            _randomSeed = new Random(i);

            return i;
        }

        public void selfTest() {
            shouldWork("randseed(1234321)", "1234321");
            shouldWork("rand(1,1000)", "314");
            shouldWork("rand(1,1000)", "491");
        }
    }

    /**
     * sum(v) : add up elements in a list
     */
    private class SumFn extends OxFunction {
        public SumFn() {
            super("sum", _constants.SUM_NAME, _constants.SUM_TIP, 1, 99);
        }

        public void evalFn(OxStack top, int nargs) {
            double sum = 0.0;
            for (int i = 0; i < nargs; i++) {
                double x = top.getDouble();
                sum += x;
            }

            top.pushDouble(sum);
        }

        public void selfTest() {
            shouldWork("sum(55.0)", "55.0");
            shouldWork("sum(5, 5.0, 15)", "25.0");
            shouldWork("sum(100.0, 55, 10.0)", "165.0");
        }
    }
}
