package com.aamrtu.aictestudent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyCalendar {

    static public String getTodayDate() {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        return df.format(Calendar.getInstance().getTime());
    }

    static public String getCalendarDate() {
        return new SimpleDateFormat("d", Locale.ENGLISH).format(Calendar.getInstance().getTime());
    }

    static public String getCalendarMonth() {
        return new SimpleDateFormat("M", Locale.ENGLISH).format(Calendar.getInstance().getTime());
    }

    static public String getCalendarMonthByName() {
        return new SimpleDateFormat("MMMM", Locale.ENGLISH).format(Calendar.getInstance().getTime());
    }

    static public String getCalendarYear() {
        return new SimpleDateFormat("yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime());
    }
}
