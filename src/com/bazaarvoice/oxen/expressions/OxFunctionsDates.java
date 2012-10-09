package com.bazaarvoice.oxen.expressions;

/**
 * Created by steve.ohara
 * Date: 9/20/12 7:06 AM
 */

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings ({"UnusedDeclaration"})
public class OxFunctionsDates {
    private final OxConstants _constants;

    private final Calendar _calendar;
    private final SimpleDateFormat _simpleDateFormat;
    private final String[] _monthNames;
    private final String[] _weekdayNames;

    public OxFunctionsDates(OxFunctions functions, OxConstants constants, Locale locale) {
        _constants = constants;

        _calendar = Calendar.getInstance(locale);
        DateFormatSymbols _dateFormatSymbols = new DateFormatSymbols(locale);
        _simpleDateFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG, locale);
        _monthNames = _dateFormatSymbols.getMonths();
        _weekdayNames = _dateFormatSymbols.getWeekdays();

        functions.findAllFunctions(this);
    }

    //
    // Abstract Shared Templates
    //

    private abstract class LfnT extends OxFunction {
        public LfnT(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 1);
        }

        public abstract long LevalT(Date d);

        public void evalFn(OxStack top, int nargs) {
            Date d = top.getDate();
            long i = LevalT(d);
            top.pushLong(i);
        }
    }

    private abstract class GetDateFn extends LfnT {
        final int _which;
        final int _offset;

        public GetDateFn(String fixName, String name, String tip, int which, int offset) {
            super(fixName, name, tip);
            _which = which;
            _offset = offset;
        }

        public long LevalT(Date d) {
            _calendar.setTime(d);
            return _calendar.get(_which) + _offset;
        }
    }

    private abstract class SfnT extends OxFunction {
        public SfnT(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 1);
        }

        public abstract String SevalT(Date d);

        public void evalFn(OxStack top, int nargs) {
            Date d = top.getDate();
            String s = SevalT(d);
            top.pushString(s);
        }
    }

    private abstract class GetDateNameFn extends SfnT {
        private final int _which;
        private final String[] _names;

        public GetDateNameFn(String fixName, String name, String tip, int which, String[] names) {
            super(fixName, name, tip);
            _which = which;
            _names = names;
        }

        public String SevalT(Date d) {
            _calendar.setTime(d);
            int indx = _calendar.get(_which);
            if (indx < 0 || indx >= _names.length) return "";
            return _names[indx];
        }
    }

    private abstract class TfnSs extends OxFunction {
        public TfnSs(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 2);
        }

        public abstract Date TevalS(String s);

        public abstract Date TevalSS(String s, String t);

        public void evalFn(OxStack top, int nargs) {
            if (nargs == 1) {
                String s = top.getString();
                Date d = TevalS(s);
                top.pushDate(d);
            } else {
                String t = top.getString();
                String s = top.getString();
                Date d = TevalSS(s, t);
                top.pushDate(d);
            }
        }
    }

    private abstract class SfnTs extends OxFunction {
        public SfnTs(String fixName, String name, String tip) {
            super(fixName, name, tip, 1, 2);
        }

        public abstract String SevalT(Date d);

        public abstract String SevalTS(Date d, String s);

        public void evalFn(OxStack top, int nargs) {
            if (nargs == 1) {
                Date d = top.getDate();
                String s = SevalT(d);
                top.pushString(s);
            } else {
                String s = top.getString();
                Date d = top.getDate();
                String t = SevalTS(d, s);
                top.pushString(t);
            }
        }
    }

    //
    // Actual OxFunction Definitions
    //

    /**
     * $now : current date/time
     */
    private class NowFn extends OxFunctions.ConstantFn {
        public NowFn() {
            super("now", _constants.NOW_NAME, _constants.NOW_TIP);
        }

        public void evalFn(OxStack top, int nargs) {
            top.pushDate(new Date());
        }

        public void selfTest() {
            shouldWork("2000 <= $year($now)", "true");
            shouldWork("$year($now) <= 2100", "true");
        }
    }

    /**
     * $clock : milliseconds since Jan 1, 1970 methinks
     */
    private class ClockFn extends OxFunctions.ConstantFn {
        public ClockFn() {
            super("clock", _constants.CLOCK_NAME, _constants.CLOCK_TIP);
        }

        public void evalFn(OxStack top, int nargs) {
            top.pushLong(System.currentTimeMillis());
        }

        public void selfTest() {
            // July 3, 2002 at about 1am
            shouldWork("1025676250550 < $clock", "true");
            shouldWork("$clock < 1400000000000", "true");
        }
    }

    /**
     * year(x) : get year from a date
     * month(x) : get month from a date (1 to 12)
     * day(x) : get day of the month from a date (1 to 31)
     * yearday(x) : get day of the year from a date/time (1 to 366)
     * weekday(x) : get day of the week from a date/time (1 to 7)
     * week(x) : get week of the year from a date/time (1 to 53)
     * hour(x) : get hour from a date/time (0 to 23)
     * minute(x) : get minute from a date/time (0 to 59)
     * second(x) : get seconds from a date/time (0 to 59)
     * millisec(x) : get milliseconds from a date/time (0 to 999)
     */
    private class YearFn extends GetDateFn {
        public YearFn() {
            super("year", _constants.YEAR_NAME, _constants.YEAR_TIP, Calendar.YEAR, 0);
        }

        public void selfTest() {
            shouldWork("year(now) > 2000", "true");
        }
    }

    private class MonthFn extends GetDateFn {
        public MonthFn() {
            super("month", _constants.MONTH_NAME, _constants.MONTH_TIP, Calendar.MONTH, 1);
        }

        public void selfTest() {
            shouldWork("1 <= $month($now)", "true");
            shouldWork("$month($now) <= 12", "true");
        }
    }

    private class DayFn extends GetDateFn {
        public DayFn() {
            super("day", _constants.DAY_NAME, _constants.DAY_TIP, Calendar.DAY_OF_MONTH, 0);
        }

        public void selfTest() {
            shouldWork("1 <= $day($now)", "true");
            shouldWork("$day($now) <= 31", "true");
        }
    }

    private class YearDayFn extends GetDateFn {
        public YearDayFn() {
            super("yearday", _constants.YEARDAY_NAME, _constants.YEARDAY_TIP,
                    Calendar.DAY_OF_YEAR, 0);
        }

        public void selfTest() {
            shouldWork("1 <= $yearday($now)", "true");
            shouldWork("$yearday($now) <= 366", "true");
        }
    }

    private class WeekDayFn extends GetDateFn {
        public WeekDayFn() {
            super("weekday", _constants.WEEKDAY_NAME, _constants.WEEKDAY_TIP,
                    Calendar.DAY_OF_WEEK, 0);
        }

        public void selfTest() {
            shouldWork("1 <= $weekday($now)", "true");
            shouldWork("$weekday($now) <= 7", "true");
        }
    }

    private class WeekOfYearFn extends GetDateFn {
        public WeekOfYearFn() {
            super("weekofyear", _constants.WEEKOFYEAR_NAME, _constants.WEEKOFYEAR_TIP,
                    Calendar.WEEK_OF_YEAR, 0);
        }

        public void selfTest() {
            shouldWork("1 <= $weekofyear($now)", "true");
            shouldWork("$weekofyear($now) <= 53", "true");
        }
    }

    private class WeekOfMonthFn extends GetDateFn {
        public WeekOfMonthFn() {
            super("weekofmonth", _constants.WEEKOFMONTH_NAME, _constants.WEEKOFMONTH_TIP,
                    Calendar.WEEK_OF_MONTH, 0);
        }

        public void selfTest() {
            shouldWork("1 <= $weekofmonth($now)", "true");
            shouldWork("$weekofmonth($now) <= 6", "true");
        }
    }

    private class HourFn extends GetDateFn {
        public HourFn() {
            super("hour", _constants.HOUR_NAME, _constants.HOUR_TIP, Calendar.HOUR_OF_DAY, 0);
        }

        public void selfTest() {
            shouldWork("0 <= $hour($now)", "true");
            shouldWork("$hour($now) <= 23", "true");
        }
    }

    private class MinuteFn extends GetDateFn {
        public MinuteFn() {
            super("minute", _constants.MINUTE_NAME, _constants.MINUTE_TIP, Calendar.MINUTE, 0);
        }

        public void selfTest() {
            shouldWork("0 <= $minute($now)", "true");
            shouldWork("$minute($now) <= 59", "true");
        }
    }

    private class SecondFn extends GetDateFn {
        public SecondFn() {
            super("second", _constants.SECOND_NAME, _constants.SECOND_TIP, Calendar.SECOND, 0);
        }

        public void selfTest() {
            shouldWork("0 <= $second($now)", "true");
            shouldWork("$second($now) <= 59", "true");
        }
    }

    private class MilliSecFn extends GetDateFn {
        public MilliSecFn() {
            super("millisec", _constants.MILLISEC_NAME, _constants.MILLISEC_TIP,
                    Calendar.MILLISECOND, 0);
        }

        public void selfTest() {
            shouldWork("0 <= $millisec($now)", "true");
            shouldWork("$millisec($now) <= 999", "true");
        }
    }

    /**
     * monthname(x) : get _name of the month from a date ("January", ...)
     * weekdayname(x) : get _name of the day of the week from a date/time ("Sunday", ...)
     */

    private class MonthNameFn extends GetDateNameFn {
        public MonthNameFn() {
            super("monthname", _constants.MONTHNAME_NAME, _constants.MONTHNAME_TIP, Calendar.MONTH, _monthNames);
        }

        public void selfTest() {
            shouldWork("3 <= len($monthname($now))", "true");
            shouldWork("len($monthname($now)) <= 9", "true");
        }
    }

    private class WeekDayNameFn extends GetDateNameFn {
        public WeekDayNameFn() {
            super("weekdayname", _constants.WEEKDAYNAME_NAME, _constants.WEEKDAYNAME_TIP, Calendar.DAY_OF_WEEK, _weekdayNames);
        }

        public void selfTest() {
            shouldWork("6 <= len($weekdayname($now))", "true");
            shouldWork("len($weekdayname($now)) <= 9", "true");
        }
    }

    /**
     * parsedate(s [,fmt]) : convert a string to a date
     */
    private class ParseDateFn extends TfnSs {
        public ParseDateFn() {
            super("parsedate", _constants.PARSEDATE_NAME, _constants.PARSEDATE_TIP);
        }

        public Date TevalS(String s) {
            try {
                return _simpleDateFormat.parse(s);
            } catch (ParseException ex) {
                throw new OxException("Unable to parse " + s, ex);
            }
        }

        public Date TevalSS(String s, String fmt) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(fmt);
                return f.parse(s);
            } catch (ParseException ex) {
                throw new OxException(_constants.ERROR_PARSING_DATE + ' ' + s);
            }
        }

        public void selfTest() {
            shouldWork("parsedate('Aug 3, 2006')", "Thu Aug 03 00:00:00 CDT 2006");
            shouldWork("parsedate('11/22/2008 08:30','MM/dd/yyyy hh:mm')", "Sat Nov 22 08:30:00 CST 2008");
        }
    }

    /**
     * formatdate(s [,fmt]) : convert a date to a string
     */
    private class FormatDateFn extends SfnTs {
        public FormatDateFn() {
            super("formatdate", _constants.FORMATDATE_NAME, _constants.FORMATDATE_TIP);
        }

        public String SevalT(Date d) {
            return _simpleDateFormat.format(d);
        }

        public String SevalTS(Date d, String fmt) {
            SimpleDateFormat f = new SimpleDateFormat(fmt);
            return f.format(d);
        }

        public void selfTest() {
            shouldWork("formatdate(parsedate('Aug 03, 2006'))", "August 3, 2006");
            shouldWork("formatdate(parsedate('11/22/2008 08:30','MM/dd/yyyy hh:mm'))", "November 22, 2008");
            shouldWork("formatdate(parsedate('11/22/2008 8:30','MM/dd/yyyy h:mm'), 'hh:mm')", "08:30");
        }
    }
}
