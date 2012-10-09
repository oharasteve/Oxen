package com.bazaarvoice.oxen.expressions;

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;

/**
 * Created by steve.ohara
 * Date: 9/19/12 12:21 PM
 */

public class OxParseNumber {
    public static abstract class OxParseNumberResult {
        public int _nextSC;
    }

    public static class OxParsedLong extends OxParseNumberResult {
        private final long _value;

        public OxParsedLong(boolean isNeg, long value) {
            _value = isNeg ? -value : value;
        }

        public long getValue() {
            return _value;
        }
    }

    public static class OxParsedDouble extends OxParseNumberResult {
        private final double _value;

        public OxParsedDouble(boolean isNeg, double value) {
            _value = isNeg ? -value : value;
        }

        public double getValue() {
            return _value;
        }
    }

    //
    // See if there is a number (int or double) in the string
    // starting at the given position
    //
    public OxParseNumberResult parseNumber(OxConstants constants, String rec, int sc, int ec) {
        boolean isNeg;

        // Initialize results
        isNeg = false;

        int pos = sc;

        // throw away leading spaces, if any
        while (pos < ec) {
            char ch = rec.charAt(pos);

            if (!Character.isWhitespace(ch)) {
                break;
            }

            pos++;
        }

        // if nothing there at all, just return
        if (pos >= ec) {
            return null;
        }

        // See if we have a digit
        // Or a + or - followed by a digit or a .
        // Or a . followed by a digit
        char ch = rec.charAt(pos);

        if (ch == constants.PLUS_SIGN || ch == constants.MINUS_SIGN) {
            if (ch == constants.MINUS_SIGN) {
                isNeg = true;
            }

            if (pos + 1 < ec) {
                ch = rec.charAt(pos + 1);

                if (Character.isDigit(ch)) {
                    return getNumber(constants, isNeg, rec, pos + 1, ec);
                }
            }

            if (pos + 2 < ec) {
                ch = rec.charAt(pos + 1);

                if (ch == constants.DECIMAL_POINT) {
                    ch = rec.charAt(pos + 2);

                    if (Character.isDigit(ch)) {
                        return getNumber(constants, isNeg, rec, pos + 1, ec);
                    }
                }
            }
        } else if (ch == constants.DECIMAL_POINT) {
            if (pos + 1 < ec) {
                ch = rec.charAt(pos + 1);

                if (Character.isDigit(ch)) {
                    return getNumber(constants, isNeg, rec, pos, ec);
                }
            }
        } else if (Character.isDigit(ch)) {
            return getNumber(constants, isNeg, rec, pos, ec);
        }

        // Oh well, didn't match anything
        return null;
    }

    // Extract the number, we KNOW it is a number here
    // The leading + or - (if any) is not here
    private OxParseNumberResult getNumber(OxConstants constants, boolean isNeg, String rec, int sc, int ec) {
        boolean gotDecPt = false;
        boolean gotExpon = false;
        int pos = sc;

        // Start eating up digits until we find the end
        while (pos < ec) {
            char ch = rec.charAt(pos);

            if (ch == constants.DECIMAL_POINT) {
                if (gotDecPt || gotExpon) {
                    // Nope, we don't like this 1.2.3 or 1e4.5
                    break;
                }

                gotDecPt = true;
            } else if (Character.toUpperCase(ch) == constants.EXPONENT_LETTER) {
                if (gotExpon) {
                    // Ugh, two exponents
                    break;
                }

                // Better be a digit or a + or - then a digit!
                if (pos + 1 >= ec) {
                    // Line ended with an E, strange!
                    break;
                }

                ch = rec.charAt(pos + 1);

                if (!Character.isDigit(ch)) {
                    if (ch == constants.PLUS_SIGN || ch == constants.MINUS_SIGN) {
                        if (pos + 2 >= ec) {
                            // Was an E and a + or -, but that was the end, strange!
                            break;
                        }

                        ch = rec.charAt(pos + 2);

                        if (!Character.isDigit(ch)) {
                            // Was an E and a + or -, but it had junk following it
                            break;
                        }

                        pos++;
                    }
                }

                gotExpon = true;
            } else if (!Character.isDigit(ch)) {
                // We're done ... some oddball character is there (maybe space)
                break;
            }

            // keep going
            pos++;
        }

        // Ok, we know for sure where the durn thing starts (sc) and ends (pos)
        // So we let java parse it
        String piece = rec.substring(sc, pos);

        OxParseNumberResult result;
        if (gotDecPt || gotExpon) {
            try {
                result = new OxParsedDouble(isNeg, Double.parseDouble(piece));
            } catch (NumberFormatException ex) {
                throw new OxException(constants.ERROR_PARSING_DOUBLE + ' ' + piece);
            }
        } else {
            try {
                result = new OxParsedLong(isNeg, Long.parseLong(piece));
            } catch (NumberFormatException ex) {
                throw new OxException(constants.ERROR_PARSING_INTEGER + ' ' + piece);
            }
        }

        // For next parsing action
        result._nextSC = pos;

        return result;
    }
}
