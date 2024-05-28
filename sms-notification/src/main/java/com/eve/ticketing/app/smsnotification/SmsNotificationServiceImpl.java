package com.eve.ticketing.app.smsnotification;

import com.eve.ticketing.app.smsnotification.dto.SmsNotificationFilterDto;
import com.eve.ticketing.app.smsnotification.exception.Error;
import com.eve.ticketing.app.smsnotification.exception.SmsNotificationProcessingException;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static com.eve.ticketing.app.smsnotification.SmsNotificationSpecification.smsNotificationMessageEqual;
import static com.eve.ticketing.app.smsnotification.SmsNotificationSpecification.smsNotificationPhoneNumberEqual;

@Slf4j
@AllArgsConstructor
@Service
public class SmsNotificationServiceImpl implements SmsNotificationService {

    private final SmsNotificationRepository smsNotificationRepository;

    @Override
    public Page<SmsNotification> getSmsNotificationList(int page, int size, SmsNotificationFilterDto smsNotificationFilterDto) {
        Specification<SmsNotification> smsNotificationSpecification = Specification.where(smsNotificationPhoneNumberEqual(smsNotificationFilterDto.getPhoneNumber()))
                .and(smsNotificationMessageEqual(smsNotificationFilterDto.getMessage()));
        Pageable pageable = PageRequest.of(page, size);

        return smsNotificationRepository.findAll(smsNotificationSpecification, pageable);
    }

    @Override
    public SmsNotification getSmsNotificationById(long id) throws SmsNotificationProcessingException {
        return smsNotificationRepository.findById(id).orElseThrow(() -> {
            Error error = Error.builder().method("GET").field("id").value(id).description("id not found").build();
            log.error(error.toString());
            return new SmsNotificationProcessingException(HttpStatus.NOT_FOUND, error);
        });
    }

    @Override
    @Transactional
    public void createSmsNotification(SmsNotification smsNotification) throws SmsNotificationProcessingException, ConstraintViolationException {
        try {
            smsNotificationRepository.save(smsNotification);
            log.info("Sms notification (phoneNumber=\"{}\", ticketId=\"{}\") was created", smsNotification.getPhoneNumber(), smsNotification.getTicketId());
        } catch (RuntimeException e) {
            Error error = Error.builder().method("POST").field("").value(smsNotification).description("invalid parameters").build();
            log.error(error.toString());
            throw new SmsNotificationProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteSmsNotificationById(long id) throws SmsNotificationProcessingException {
        try {
            smsNotificationRepository.deleteById(id);
            log.info("Sms notification (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            Error error = Error.builder().method("DELETE").field("id").value(id).description("id not found").build();
            log.error(error.toString());
            throw new SmsNotificationProcessingException(HttpStatus.NOT_FOUND, error);
        }
    }
}
