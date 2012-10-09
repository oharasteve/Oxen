package com.bazaarvoice.oxen.data;

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.expressions.OxFunction;
import com.bazaarvoice.oxen.expressions.OxStack;
import com.bazaarvoice.oxen.expressions.OxValue;
import com.bazaarvoice.oxen.symbols.OxDoubleSymbol;
import com.bazaarvoice.oxen.symbols.OxStringSymbol;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by steve.ohara
 * Date: 10/8/12 2:24 PM
 */

public class OxCountFn extends OxFunction {
    private static OxMemoryHistorian _testHistorian;

    public OxCountFn(String fixName, String name, String tip) {
        super(fixName, name, tip, 1, 99);
    }

    public void evalFn(OxStack top, int nargs) {
        double days = top.getDouble();
        ArrayList<OxSymbol> symbols = new ArrayList<OxSymbol>();
        for (int i = 0; i < nargs-1; i++) {
            OxValue value = top.popTop();
            OxSymbol symbol = value.getSymbol();
            if (symbol == null) {
                throw new OxException("Function " + this.getName() + "() only accepts variables.");
            }
            symbols.add(symbol);
        }
        long milliseconds = Math.round(days * 24 * 60 * 60 * 1000);
        int count = _testHistorian.count(symbols, milliseconds);
        top.pushLong(count);
    }

    public void selfTest() {
        _testHistorian = new OxMemoryHistorian();
        OxDoubleSymbol temp = new OxDoubleSymbol("temp");
        OxDoubleSymbol pressure = new OxDoubleSymbol("pressure");
        _testSymbolTable.addSymbol(temp);
        _testSymbolTable.addSymbol(pressure);
        _testSymbolTable.addSymbol(new OxStringSymbol("junk", "junk"));

        // Create 100 sample data points
        Calendar cal = Calendar.getInstance();
        cal.set(2010, 1, 1);                            // Jan 1, 2010
        for (int ticks = 0; ticks < 100; ticks++) {     // 100 days
            temp.setDouble(70 + (ticks % 10));          // 70 71 72 ... 79 and repeat
            pressure.setDouble(29 + (ticks % 4));       // 29 30 31 32 and repeat
            _testHistorian.persist(_testSymbolTable.clone(), cal.getTime().getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);          // First 100 days of 2010
        }

        shouldWork("count(temp, 69.9)", "7");             // In the past 70 days (every 10th)
        shouldWork("count(pressure, 19.9)", "5");         // In the past 20 days (every 4th)
        shouldWork("count(temp, pressure, 39.9)", "2");   // In the past 40 days (just 1 of every 20)

        shouldFail("count(34, 45)");
        shouldFail("count(temp, junk)");
        shouldFail("count(george, 22)");
    }
}
