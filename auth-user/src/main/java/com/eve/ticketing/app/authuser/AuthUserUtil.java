package com.eve.ticketing.app.authuser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class AuthUserUtil {

    public static Date getDateFromString(String dateString) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return simpleDateFormat.parse(dateString);
        } catch (NullPointerException | ParseException e) {
            return null;
        }
    }
}
