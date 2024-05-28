package com.eve.ticketing.app.smsnotification;

import com.eve.ticketing.app.smsnotification.exception.SmsNotificationProcessingException;

public interface SmsSender {

    void sendSms(SmsNotification smsNotification) throws SmsNotificationProcessingException;
}
