package com.vmusco.softminer.tests.cases.real.testPracticalApacheCommonLang3TestSMTP;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.TimeZone;

public class FastDateFormat extends Format {

	private static final FormatCache<FastDateFormat> cache= new FormatCache<FastDateFormat>() {
        @Override
        protected FastDateFormat createInstance(String pattern, TimeZone timeZone, Locale locale) {
            return new FastDateFormat(pattern, timeZone, locale);
        }
    };

    public static FastDateFormat getInstance(String pattern, Locale locale) {
        return cache.getInstance(pattern, null, locale);
    }

    protected FastDateFormat(String pattern, TimeZone timeZone, Locale locale) {
        init();
    }

    private void init() {
        parsePattern();
    }

    //protected List<Rule> parsePattern() {
    protected String parsePattern() {
        return parseToken("0", new int[]{0});
    }

    protected String parseToken(String pattern, int[] indexRef) {
        /// BUG IS INSERTED HERE !!!
    	return null;
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    	return null;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return null;
    }

    /*private interface Rule {
        void appendTo(StringBuffer buffer, Calendar calendar);
    }*/  
}
