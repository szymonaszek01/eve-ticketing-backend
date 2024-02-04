package com.eve.ticketing.app.smsnotification;

import com.eve.ticketing.app.smsnotification.dto.SmsNotificationFilterDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import static com.eve.ticketing.app.smsnotification.SmsNotificationSpecification.smsNotificationMessageEqual;
import static com.eve.ticketing.app.smsnotification.SmsNotificationSpecification.smsNotificationPhoneNumberEqual;

@Slf4j
@AllArgsConstructor
@Service
public class SmsNotificationServiceImpl implements SmsNotificationService {

    private final SmsNotificationRepository smsNotificationRepository;

    @Override
    public void createSmsNotification(SmsNotification smsNotification) throws SmsNotificationProcessingException {
        try {
            smsNotificationRepository.save(smsNotification);
            log.info("Sms notification (phoneNumber=\"{}\", ticketId=\"{}\") was created", smsNotification.getPhoneNumber(), smsNotification.getTicketId());
        } catch (RuntimeException e) {
            log.info("Sms notification (phoneNumber=\"{}\", ticketId=\"{}\") was not created", smsNotification.getPhoneNumber(), smsNotification.getTicketId());
            throw new SmsNotificationProcessingException("Sms notification was not created - " + e.getMessage());
        }
    }

    @Override
    public SmsNotification getSmsNotificationById(long id) throws SmsNotificationProcessingException {
        return smsNotificationRepository.findById(id).orElseThrow(() -> {
            log.error("Sms notification (id=\"{}\") was not found", id);
            throw new SmsNotificationProcessingException("Sms notification was not found - invalid sms notification id");
        });
    }

    @Override
    public Page<SmsNotification> getSmsNotificationList(int page, int size, SmsNotificationFilterDto smsNotificationFilterDto) {
        Specification<SmsNotification> smsNotificationSpecification = Specification.where(smsNotificationPhoneNumberEqual(smsNotificationFilterDto.getPhoneNumber()))
                .and(smsNotificationMessageEqual(smsNotificationFilterDto.getMessage()));
        Pageable pageable = PageRequest.of(page, size);

        return smsNotificationRepository.findAll(smsNotificationSpecification, pageable);
    }

    @Override
    public void deleteSmsNotificationById(long id) throws SmsNotificationProcessingException {
        try {
            smsNotificationRepository.deleteById(id);
            log.info("Sms notification (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            log.error("Event (id=\"{}\") was not deleted", id);
            throw new SmsNotificationProcessingException("Sms notification was not deleted - invalid sms notification id");
        }
    }
}
