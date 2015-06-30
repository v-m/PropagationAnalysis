package com.vmusco.softminer.tests.cases.real.testPracticalApacheCommonLang3TestSMTP;

import java.text.Format;
import java.util.Locale;
import java.util.TimeZone;

abstract class FormatCache<F extends Format> {
    
	public F getInstance(String pattern, TimeZone timeZone, Locale locale) {
        F format = null;
        format = createInstance(pattern, timeZone, locale);
        return format;
    }

    abstract protected F createInstance(String pattern, TimeZone timeZone, Locale locale);
   
}
