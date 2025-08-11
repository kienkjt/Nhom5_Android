package com.nhom5.healthtracking.util;

import java.util.Date;

public class DateUtils {
  public static Date getStartOfDay(Date date) {
    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.setTime(date);
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
    cal.set(java.util.Calendar.MINUTE, 0);
    cal.set(java.util.Calendar.SECOND, 0);
    cal.set(java.util.Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  public static Date getEndOfDay(Date date) {
    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.setTime(date);
    cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
    cal.set(java.util.Calendar.MINUTE, 59);
    cal.set(java.util.Calendar.SECOND, 59);
    cal.set(java.util.Calendar.MILLISECOND, 999);
    return cal.getTime();
  }
}