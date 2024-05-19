package com.eve.ticketing.app.smsnotification;

import com.eve.ticketing.app.smsnotification.dto.SmsNotificationFilterDto;
import org.springframework.data.domain.Page;

public interface SmsNotificationService {

    Page<SmsNotification> getSmsNotificationList(int page, int size, SmsNotificationFilterDto SmsNotificationFilterDto);

    SmsNotification getSmsNotificationById(long id) throws SmsNotificationProcessingException;

    void createSmsNotification(SmsNotification smsNotification) throws SmsNotificationProcessingException;

    void deleteSmsNotificationById(long id) throws SmsNotificationProcessingException;
}
