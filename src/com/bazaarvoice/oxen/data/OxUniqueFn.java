package com.bazaarvoice.oxen.data;

import com.bazaarvoice.oxen.OxException;
import com.bazaarvoice.oxen.OxSymbol;
import com.bazaarvoice.oxen.expressions.OxFunction;
import com.bazaarvoice.oxen.expressions.OxStack;
import com.bazaarvoice.oxen.expressions.OxValue;
import com.bazaarvoice.oxen.symbols.OxStringSymbol;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by steve.ohara
 * Date: 10/8/12 2:24 PM
 */

public class OxUniqueFn extends OxFunction {
    private static OxMemoryHistorian _testHistorian;

    public OxUniqueFn(String fixName, String name, String tip) {
        super(fixName, name, tip, 2, 99);
    }

    public void evalFn(OxStack top, int nargs) {
        double days = top.getDouble();
        OxValue value = top.popTop();
        OxSymbol uniqueSymbol = value.getSymbol();
        if (uniqueSymbol == null) {
            throw new OxException("Function " + this.getName() + "() only accepts variables.");
        }

        ArrayList<OxSymbol> symbols = new ArrayList<OxSymbol>();
        for (int i = 0; i < nargs-2; i++) {
            value = top.popTop();
            OxSymbol symbol = value.getSymbol();
            if (symbol == null) {
                throw new OxException("Function " + this.getName() + "() only accepts variables.");
            }
            symbols.add(symbol);
        }
        long milliseconds = Math.round(days * 24 * 60 * 60 * 1000);
        int unique = _testHistorian.unique(uniqueSymbol, symbols, milliseconds);
        top.pushLong(unique);
    }

    public void selfTest() {
        _testHistorian = new OxMemoryHistorian();
        OxStringSymbol client = new OxStringSymbol("client");
        OxStringSymbol user = new OxStringSymbol("user");
        OxStringSymbol ip1 = new OxStringSymbol("ip1");
        OxStringSymbol ip2 = new OxStringSymbol("ip2");
        _testSymbolTable.addSymbol(client);
        _testSymbolTable.addSymbol(user);
        _testSymbolTable.addSymbol(ip1);
        _testSymbolTable.addSymbol(ip2);
        _testSymbolTable.addSymbol(new OxStringSymbol("trash", "trash"));

        // Create 100 sample data points
        Calendar cal = Calendar.getInstance();
        cal.set(2010, 1, 1);                                // Jan 1, 2010
        for (int ticks = 0; ticks < 100; ticks++) {         // 100 days
            int sc = ticks % 4;
            client.setString("ABCD".substring(sc, sc+1));   // A B C D A B C D and repeat
            user.setString("user" + ((ticks % 10) + 1));    // user1 user2 user3 ... user10 and repeat
            ip1.setString("100.200.300." + (ticks+1));      // they are all different, 1 to 100
            ip2.setString("100.200.300." + (ticks+1-(ticks%10)));      // ten of each, ten 1's then ten 11's etc
            _testHistorian.persist(_testSymbolTable.clone(), cal.getTime().getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);              // First 100 days of 2010
        }

        shouldWork("unique(client, ip1, 71.9)", "18");        // In the past 72 days (every 4th)
        shouldWork("unique(user, ip1, 19.9)", "2");           // In the past 20 days (every 10th)
        shouldWork("unique(client, user, ip1, 79.9)", "4");   // In the past 80 days (every 20th)

        shouldWork("unique(client, ip2, 71.9)", "7");         // In the past 72 days (part of every 4th)
        shouldWork("unique(user, ip2, 19.9)", "2");           // In the past 20 days (part of every 10th)
        shouldWork("unique(client, user, ip2, 79.9)", "4");   // In the past 80 days (part of every 20th)

        shouldFail("unique(34, 45)");
        shouldFail("unique(temp, trash)");
        shouldFail("unique(george, 22)");
    }
}
