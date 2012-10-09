package com.bazaarvoice.oxen;

/**
 * Created by steve.ohara
 * Date: 9/20/12 10:00 AM
 */

import com.bazaarvoice.oxen.expressions.OxParseNumber;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class OxParseNumberTest {
    private final OxConstants _constants = new OxConstants();

    @DataProvider (name = "testLongData")
    public Object[][] longData() {
        return new Object[][] {
                { "123", 123 },
                { "  +123  ", 123 },
                { "  -123  ", -123 }
        };
    }

    @Test (dataProvider = "testLongData")
    private void testLongs(final String rec, final long desired) {
        int nc = rec.length();

        OxParseNumber parser = new OxParseNumber();
        OxParseNumber.OxParseNumberResult result = parser.parseNumber(_constants, rec, 0, nc);
        if (result != null) {
            if (result instanceof OxParseNumber.OxParsedLong) {
                long returned = ((OxParseNumber.OxParsedLong) result).getValue();
                if (returned == desired) {
                    System.out.println("\"" + rec + "\" returned " + returned + " as expected.");
                    return; // Test passed
                }
                System.err.println("\"" + rec + "\" returned " + returned + " instead of " + desired);
            } else {
                System.err.println(rec + " returned a double instead of " + desired);
            }
        } else {
            System.err.println(rec + " failed to parse");
        }
    }

    @DataProvider (name = "testDoubleData")
    public final Object[][] doubleData() {
        return new Object[][] {
                { "123.456", 123.456 },
                { "  +123.456  ", 123.456 },
                { "  -123.456  ", -123.456 },
                { "123.456e2", 12345.6 },
                { "123.456E+2", 12345.6 },
                { "123.456e-2", 1.23456 }
        };
    }

    @Test (dataProvider = "testDoubleData")
    private void testDoubles(final String rec, final double desired) {
        int nc = rec.length();

        OxParseNumber parser = new OxParseNumber();
        OxParseNumber.OxParseNumberResult result = parser.parseNumber(_constants, rec, 0, nc);
        if (result != null) {
            if (result instanceof OxParseNumber.OxParsedDouble) {
                double returned = ((OxParseNumber.OxParsedDouble) result).getValue();
                if (returned == desired) {
                    System.out.println("\"" + rec + "\" returned " + returned + " as expected.");
                    return; // Test passed
                }
                System.err.println("\"" + rec + "\" returned " + returned + " instead of " + desired);
            } else {
                System.err.println("\"" + rec + "\" returned a long instead of " + desired);
            }
        } else {
            System.err.println(rec + " failed to parse");
        }
    }
}
