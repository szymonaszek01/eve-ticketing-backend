package com.eve.ticketing.app.smsnotification;

import com.eve.ticketing.app.smsnotification.dto.SmsNotificationFilterDto;
import org.springframework.data.domain.Page;

public interface SmsNotificationService {

    void createSmsNotification(SmsNotification smsNotification) throws SmsNotificationProcessingException;

    SmsNotification getSmsNotificationById(long id) throws SmsNotificationProcessingException;

    Page<SmsNotification> getSmsNotificationList(int page, int size, SmsNotificationFilterDto SmsNotificationFilterDto);

    void deleteSmsNotificationById(long id) throws SmsNotificationProcessingException;
}
