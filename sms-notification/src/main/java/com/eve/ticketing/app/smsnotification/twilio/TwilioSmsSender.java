package com.eve.ticketing.app.smsnotification.twilio;

import com.eve.ticketing.app.smsnotification.SmsNotification;
import com.eve.ticketing.app.smsnotification.SmsSender;
import com.eve.ticketing.app.smsnotification.exception.Error;
import com.eve.ticketing.app.smsnotification.exception.SmsNotificationProcessingException;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service("twilio")
@AllArgsConstructor
public class TwilioSmsSender implements SmsSender {

    private final TwilioConfiguration twilioConfiguration;

    @Override
    public void sendSms(SmsNotification smsNotification) throws IllegalArgumentException {
        if (!isPhoneNumberValid(smsNotification.getPhoneNumber())) {
            Error error = Error.builder().method("POST").field("phone_number").value(smsNotification.getPhoneNumber()).description("phone number is invalid").build();
            throw new SmsNotificationProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        PhoneNumber to = new PhoneNumber(smsNotification.getPhoneNumber());
        PhoneNumber from = new PhoneNumber(twilioConfiguration.getTrialNumber());
        String message = smsNotification.getMessage();
        MessageCreator messageCreator = Message.creator(to, from, message);
        messageCreator.create();
    }

    private boolean isPhoneNumberValid(String phoneNumberAsString) {
        try {
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phoneNumberAsString, "");
            return phoneNumberUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException e) {
            return false;
        }
    }
}
