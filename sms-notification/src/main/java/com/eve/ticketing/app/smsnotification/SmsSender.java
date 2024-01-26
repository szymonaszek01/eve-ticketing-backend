package com.eve.ticketing.app.smsnotification;

public interface SmsSender {

    void sendSms(SmsNotification smsNotification) throws IllegalArgumentException;
}
