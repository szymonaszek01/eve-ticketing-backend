package com.eve.ticketing.app.smsnotification;

import com.eve.ticketing.app.smsnotification.dto.SmsNotificationFilterDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Sms notification", description = "Sms notification management APIs")
@RequestMapping("/api/v1/sms-notification")
@RequiredArgsConstructor
@RestController
public class SmsNotificationController {

    private final SmsNotificationServiceImpl smsNotificationService;

    @GetMapping("/id/{id}")
    public ResponseEntity<SmsNotification> getSmsNotificationById(@PathVariable long id) {
        try {
            SmsNotification smsNotification = smsNotificationService.getSmsNotificationById(id);
            return new ResponseEntity<>(smsNotification, HttpStatus.OK);
        } catch (SmsNotificationProcessingException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<SmsNotification>> getSmsNotificationList(@RequestParam(value = "page") int page,
                                                                        @RequestParam(value = "size") int size,
                                                                        SmsNotificationFilterDto smsNotificationFilterDto) {
        Page<SmsNotification> smsNotificationPage = smsNotificationService.getSmsNotificationList(page, size, smsNotificationFilterDto);
        return new ResponseEntity<>(smsNotificationPage, HttpStatus.OK);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteSmsNotificationById(@PathVariable long id) {
        try {
            smsNotificationService.deleteSmsNotificationById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (SmsNotificationProcessingException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
