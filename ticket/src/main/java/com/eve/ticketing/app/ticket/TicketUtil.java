package com.eve.ticketing.app.ticket;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TicketUtil {

    public static Date getDateFromString(String dateString) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            return simpleDateFormat.parse(dateString);
        } catch (NullPointerException | ParseException e) {
            return null;
        }
    }
}
