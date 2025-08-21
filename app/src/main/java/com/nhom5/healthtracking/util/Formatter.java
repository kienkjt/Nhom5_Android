package com.nhom5.healthtracking.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Formatter {
    public static Date stringToDate(String dateString, String format) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        if (format == null || format.isEmpty()) {
            format = "dd/MM/yyyy";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date date = sdf.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
