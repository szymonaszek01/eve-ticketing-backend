package com.eve.ticketing.app.smsnotification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsNotificationRepository extends JpaRepository<SmsNotification, Long>, JpaSpecificationExecutor<SmsNotification> {
}
