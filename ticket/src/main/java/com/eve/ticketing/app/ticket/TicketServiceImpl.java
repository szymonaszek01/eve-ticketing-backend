package com.eve.ticketing.app.ticket;

import com.eve.ticketing.app.ticket.dto.*;
import com.eve.ticketing.app.ticket.exception.Error;
import com.eve.ticketing.app.ticket.exception.TicketProcessingException;
import com.eve.ticketing.app.ticket.kafka.KafkaEmailProducer;
import com.eve.ticketing.app.ticket.kafka.KafkaNotificationProducer;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Stream;

import static com.eve.ticketing.app.ticket.TicketSpecification.*;

@Slf4j
@Service
@AllArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final String TICKET_EMAIL_TEMPLATE_NAME = "ticketemail";
    private static final String TICKET_CANCELED_EMAIL_TEMPLATE_NAME = "ticketcanceled";
    private final TicketRepository ticketRepository;
    private final RestTemplate restTemplate;
    private final KafkaNotificationProducer kafkaNotificationProducer;
    private final KafkaEmailProducer kafkaEmailProducer;

    @Override
    public Page<Ticket> getTicketList(int page, int size, TicketFilterDto ticketFilterDto, String[] sortArray, String token) {
        final UserDto userDto;
        try {
            userDto = validateToken(token);
            if (!"ADMIN".equalsIgnoreCase(userDto.getRole()) && !userDto.getId().equals(ticketFilterDto.getUserId())) {
                ticketFilterDto.setUserId(userDto.getId());
            }
        } catch (TicketProcessingException e) {
            return Page.empty();
        }

        Date minDate = TicketUtil.getDateFromString(ticketFilterDto.getMinDate());
        Date maxDate = TicketUtil.getDateFromString(ticketFilterDto.getMaxDate());
        Specification<Ticket> ticketSpecification = Specification.where(ticketCodeEqual(ticketFilterDto.getCode()))
                .and(ticketFirstnameEqual(ticketFilterDto.getFirstname()))
                .and(ticketLastnameEqual(ticketFilterDto.getLastname()))
                .and(ticketPhoneNumberEqual(ticketFilterDto.getPhoneNumber()))
                .and(ticketCostBetween(ticketFilterDto.getMinCost(), ticketFilterDto.getMaxCost()))
                .and(ticketCreatedAtBetween(minDate, maxDate))
                .and(ticketUserIdEqual(userDto.getId()))
                .and(ticketEventIdEqual(ticketFilterDto.getEventId()))
                .and(ticketSeatIdEqual(ticketFilterDto.getSeatId()))
                .and(ticketPaidEqual(ticketFilterDto.getPaid()));

        List<String> allowedSortProperties = Stream.of("id", "createdAt", "cost", "maxTicketAmount", "userId", "eventId", "seatId").toList();
        List<String> allowedSortDirections = Stream.of(Sort.Direction.ASC.toString(), Sort.Direction.DESC.toString()).toList();
        if (!sortArray[0].contains(",")) {
            sortArray = new String[]{String.join(",", sortArray)};
        }
        List<Sort.Order> orderList = Arrays.stream(sortArray)
                .filter(sort -> {
                    String[] splitedSort = sort.split(",");
                    if (splitedSort.length < 2) {
                        return false;
                    }
                    if (!"ADMIN".equalsIgnoreCase(userDto.getRole()) && "userId".equals(toCamelCase(splitedSort[0]))) {
                        return false;
                    }
                    return allowedSortProperties.contains(toCamelCase(splitedSort[0])) && allowedSortDirections.contains(splitedSort[1].toUpperCase());
                })
                .map(sort -> {
                    String[] splitedSort = sort.split(",");
                    return new Sort.Order(Sort.Direction.fromString(splitedSort[1]), toCamelCase(splitedSort[0]));
                })
                .toList();

        Pageable pageable = PageRequest.of(page, size, Sort.by(orderList));

        return ticketRepository.findAll(ticketSpecification, pageable);
    }

    @Override
    public Ticket getTicketById(long id) throws TicketProcessingException {
        return ticketRepository.findById(id).orElseThrow(() -> {
            Error error = Error.builder().method("GET").field("id").value(id).description("id not found").build();
            log.error(error.toString());
            return new TicketProcessingException(HttpStatus.NOT_FOUND, error);
        });
    }

    @Override
    @Transactional
    public Ticket createTicket(TicketDto ticketDto, String token) throws TicketProcessingException, ConstraintViolationException {
        try {
            if (!isPhoneNumberValid(ticketDto.getPhoneNumber())) {
                Error error = Error.builder().method("POST").field("phone_number").value(ticketDto.getPhoneNumber()).description("phone number is invalid").build();
                log.error(error.toString());
                throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
            }

            UserDto userDto = validateToken(token);
            EventDto eventDto = getEvent(ticketDto.getEventId());
            Ticket ticket = Ticket.builder()
                    .code(UUID.randomUUID().toString())
                    .createdAt(new Date(System.currentTimeMillis()))
                    .firstname(ticketDto.getFirstname())
                    .lastname(ticketDto.getLastname())
                    .phoneNumber(ticketDto.getPhoneNumber())
                    .isAdult(ticketDto.getIsAdult())
                    .isStudent(ticketDto.getIsStudent())
                    .eventId(ticketDto.getEventId())
                    .userId(userDto.getId())
                    .build();

            if (!Boolean.TRUE.equals(ticket.getIsAdult()) && ticket.getIsStudent()) {
                ticket.setIsStudent(false);
            }
            ticket.setCost(getTicketCost(eventDto, ticket.getIsAdult(), ticket.getIsStudent()));
            ticket.setPaid(false);
            SeatDto seatDto = null;
            if (!eventDto.isWithoutSeats()) {
                HashMap<String, Object> seatValues = new HashMap<>(3);
                seatValues.put("event_id", ticket.getEventId());
                seatValues.put("reserve", true);
                seatDto = updateSeat(seatValues);
                ticket.setSeatId(seatDto.getId());
            }
            createPdf(ticket, eventDto, seatDto, userDto, token);

            ticketRepository.save(ticket);
            String message = "Hi " + ticket.getFirstname() + ".\nYou have reserved on the " + ticket.getCreatedAt() +
                    " a ticket with code \"" + ticket.getCode() + "\" for " + eventDto.getName() + ".\nSee yoo soon,\nEve ticketing system";
            publishNotification(ticket, message);
            publishEmail(ticket, eventDto, seatDto, userDto, TICKET_EMAIL_TEMPLATE_NAME);

            log.info("Ticket (code=\"{}\", eventId={}) was created", ticket.getCode(), ticket.getEventId());
            return ticket;
        } catch (RuntimeException e) {
            Error error = Error.builder().method("POST").field("").value(ticketDto).description("invalid parameters").build();
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Ticket updateTicket(HashMap<String, Object> values, String token) throws TicketProcessingException, ConstraintViolationException {
        Error error = Error.builder().method("PUT").build();
        if (values == null) {
            error.setField("");
            error.setValue("");
            error.setDescription("empty values");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        if (values.get("id") == null || !(values.get("id") instanceof Number)) {
            error.setField("id");
            error.setValue(Objects.toString(values.get("id"), ""));
            error.setDescription("can not be null or has different type than Number");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        Ticket ticket = getTicketById(((Number) values.remove("id")).longValue());
        Stream.of("code", "created_at", "cost", "event_id", "user_id").forEach(values::remove);
        EventDto eventDto = getEvent(ticket.getId());
        Set<String> updatedFields = new HashSet<>();
        SeatDto seatDto = null;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String convertedKey = toCamelCase(entry.getKey());
            try {
                Field field = ticket.getClass().getDeclaredField(convertedKey);
                field.setAccessible(true);
                error.setField(entry.getKey());
                error.setValue(entry.getValue());
                if (entry.getValue() instanceof String) {
                    if (!"phoneNumber".equals(convertedKey) || isPhoneNumberValid((String) entry.getValue())) {
                        field.set(ticket, entry.getValue());
                        updatedFields.add(convertedKey);
                    }
                }
                if (entry.getValue() instanceof Boolean) {
                    field.set(ticket, entry.getValue());
                    updatedFields.add(convertedKey);
                }
                if (entry.getValue() instanceof Number && "seatId".equals(convertedKey) && !eventDto.isWithoutSeats()) {
                    HashMap<String, Object> seatValues = new HashMap<>(3);
                    seatValues.put("event_id", ticket.getEventId());
                    if (ticket.getSeatId() != null) {
                        seatValues.put("id", ticket.getSeatId());
                        seatValues.put("occupied", false);
                        updateSeat(seatValues);
                    }
                    seatValues.put("id", entry.getValue());
                    seatValues.put("occupied", true);
                    seatDto = updateSeat(seatValues);
                    field.set(ticket, seatDto.getId());
                    updatedFields.add(convertedKey);
                }
            } catch (NullPointerException e) {
                error.setDescription("field can not be null");
                log.error(error.toString());
            } catch (NoSuchFieldException e) {
                error.setDescription("field does not exists");
                log.error(error.toString());
            } catch (IllegalAccessException e) {
                error.setDescription("illegal access to field");
                log.error(error.toString());
            }
        }

        if (updatedFields.contains("isAdult") || updatedFields.contains("isStudent")) {
            if (!Boolean.TRUE.equals(ticket.getIsAdult()) && ticket.getIsStudent()) {
                ticket.setIsStudent(false);
            }
            ticket.setCost(getTicketCost(eventDto, ticket.getIsAdult(), ticket.getIsStudent()));
        }
        if (updatedFields.isEmpty()) {
            return ticket;
        }

        UserDto userDto = validateToken(token);
        createPdf(ticket, eventDto, seatDto, userDto, token);
        String message = "Hi " + ticket.getFirstname() + ".\nYou have updated a ticket with code \"" + ticket.getCode()
                + "\" for " + eventDto.getName() + ". Please, see the details on our page. \nSee yoo soon,\nEve ticketing system";
        publishNotification(ticket, message);
        publishEmail(ticket, eventDto, seatDto, userDto, TICKET_EMAIL_TEMPLATE_NAME);
        ticketRepository.flush();

        return ticket;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteTicketById(long id, String token) throws TicketProcessingException {
        try {
            Ticket ticket = getTicketById(id);
            EventDto eventDto = getEvent(ticket.getEventId());
            SeatDto seatDto = cancelSeat(ticket, eventDto);
            UserDto userDto = validateToken(token);
            ticketRepository.deleteById(id);
            String message = "Hi " + ticket.getFirstname() + ".\nYour ticket with code \"" + ticket.getCode()
                    + "\" for " + eventDto.getName() + " was canceled. Please, see the details on our page. \nSee yoo soon,\nEve ticketing system";
            publishNotification(ticket, message);
            String filename = "ticket-" + ticket.getCode() + ".pdf";
            ticket.setPdfName(filename);
            publishEmail(ticket, eventDto, seatDto, userDto, TICKET_CANCELED_EMAIL_TEMPLATE_NAME);
            log.info("Ticket (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            Error error = Error.builder().method("DELETE").field("id").value(id).description("id not found").build();
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.NOT_FOUND, error);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteTicket(Ticket ticket) throws TicketProcessingException {
        try {
            EventDto eventDto = getEvent(ticket.getEventId());
            cancelSeat(ticket, eventDto);
            ticketRepository.delete(ticket);
            log.info("Ticket (id=\"{}\") was deleted by scheduler", ticket.getId());
        } catch (RuntimeException e) {
            Error error = Error.builder().method("DELETE").field("ticket").value(ticket).description("id not found").build();
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.NOT_FOUND, error);
        }
    }

    @Override
    public HashMap<String, Object> getTicketField(long id, String fieldName, String token) throws TicketProcessingException {
        UserDto userDto = validateToken(token);
        Ticket ticket = getTicketById(id);
        Error error = Error.builder().method("GET").field(fieldName).build();
        if (!Objects.equals(ticket.getUserId(), userDto.getId())) {
            error.setDescription("you do not have access to this field");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        String convertedKey = toCamelCase(fieldName);
        try {
            Field field = ticket.getClass().getDeclaredField(convertedKey);
            field.setAccessible(true);
            HashMap<String, Object> response = new HashMap<>(3);
            response.put("id", id);
            response.put("key", fieldName);
            response.put("value", field.get(ticket));
            return response;
        } catch (NullPointerException e) {
            error.setDescription("field can not be null");
            log.error(error.toString());
        } catch (NoSuchFieldException e) {
            error.setDescription("field does not exists");
            log.error(error.toString());
        } catch (IllegalAccessException e) {
            error.setDescription("illegal access to field");
            log.error(error.toString());
        }
        log.error(error.toString());
        throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void payForTicketList(HashMap<String, Object> values, String token) throws TicketProcessingException {
        List<Number> ids = new ArrayList<>();
        if (values.containsKey("id")) {
            ids.add((Number) values.get("id"));
        }
        if (values.containsKey("ids")) {
            ids.addAll((List<Number>) values.get("ids"));
        }
        ids = ids.stream().filter(Objects::nonNull).toList();
        Error error = Error.builder().method("PUT").build();
        if (ids.isEmpty()) {
            error.setField("body");
            error.setValue(values);
            error.setDescription("empty ticket id list");
        }
        List<Ticket> ticketList = ticketRepository.findAllById(ids.stream().map(Number::longValue).toList());
        if (ticketList.size() != ids.size()) {
            error.setField("ids");
            error.setValue(ids);
            error.setDescription("not all tickets with provided ids found");
        }
        isAnyTicketExpired(ticketList);
        ticketList.forEach(ticket -> ticket.setPaid(true));
        UserDto userDto = validateToken(token);
        for (Ticket ticket : ticketList) {
            EventDto eventDto = getEvent(ticket.getEventId());
            SeatDto seatDto = getSeat(ticket.getSeatId(), token);
            createPdf(ticket, eventDto, seatDto, userDto, token);
            publishEmail(ticket, eventDto, seatDto, userDto, TICKET_EMAIL_TEMPLATE_NAME);
        }
        ticketRepository.flush();
    }

    public List<Ticket> getTicketListByPaidIsFalseAndCreatedAtBefore(Date maxDate) {
        return ticketRepository.findAllByPaidIsFalseAndCreatedAtBefore(maxDate);
    }

    private BigDecimal getTicketCost(EventDto eventDto, boolean isAdult, boolean isStudent) throws TicketProcessingException {
        if (!isAdult && eventDto.getChildrenDiscount() != null) {
            return eventDto.getUnitPrice().multiply(BigDecimal.ONE.subtract(eventDto.getChildrenDiscount().divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN)));
        }
        if (isAdult && isStudent && eventDto.getStudentsDiscount() != null) {
            return eventDto.getUnitPrice().multiply(BigDecimal.ONE.subtract(eventDto.getStudentsDiscount().divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN)));
        }
        if (isAdult) {
            return eventDto.getUnitPrice();
        }

        Error error = Error.builder().method("").field("cost").value("").description("unknown discounts configuration").build();
        log.error(error.toString());
        throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
    }

    private void publishNotification(Ticket ticket, String message) {
        kafkaNotificationProducer.publish(NotificationDto.builder()
                .phoneNumber(ticket.getPhoneNumber())
                .firstname(ticket.getFirstname())
                .message(message)
                .ticketId(ticket.getId())
                .build());
    }

    private void publishEmail(Ticket ticket, EventDto eventDto, SeatDto seatDto, UserDto userDto, String template) {
        kafkaEmailProducer.publish(EmailDto.builder()
                .to(userDto.getEmail())
                .subject(ticket.getPdfName())
                .template(template)
                .data(getData(ticket, eventDto, seatDto, userDto))
                .attachment(ticket.getPdfAsByteArray())
                .attachmentName(ticket.getPdfName())
                .attachmentType(MediaType.APPLICATION_PDF_VALUE)
                .build());
    }

    private UserDto validateToken(String token) throws TicketProcessingException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            return restTemplate.exchange(
                    "http://AUTH-USER/api/v1/auth-user/validate-token/{token}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    UserDto.class,
                    token.substring(7)
            ).getBody();
        } catch (RestClientException e) {
            Error error = Error.builder().method("").field("token").value(token).description("unable to communicate with auth user server").build();
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }

    private EventDto getEvent(long eventId) throws TicketProcessingException {
        EventDto eventDto;
        Error error = Error.builder().method("").field("event_id").value(eventId).build();
        try {
            eventDto = restTemplate.getForObject(
                    "http://EVENT/api/v1/event/id/{id}",
                    EventDto.class,
                    eventId
            );
        } catch (RestClientException e) {
            error.setDescription("unable to communicate with event server");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        if (eventDto == null) {
            error.setDescription("id not found");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        if (new Date(System.currentTimeMillis()).after(eventDto.getStartAt())) {
            error.setDescription("event has started");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        if (eventDto.isSoldOut()) {
            error.setDescription("tickets sold out");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        return eventDto;
    }

    private SeatDto getSeat(Long seatId, String token) throws TicketProcessingException {
        if (seatId == null) {
            return null;
        }
        Error error = Error.builder().method("").field("seat_id").value(seatId).build();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            return restTemplate.exchange(
                    "http://SEAT/api/v1/seat/id/" + seatId,
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers),
                    SeatDto.class
            ).getBody();
        } catch (RestClientException e) {
            error.setDescription("unable to communicate with seat server");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }

    private void createPdf(Ticket ticket, EventDto eventDto, SeatDto seatDto, UserDto userDto, String token) throws TicketProcessingException {
        Error error = Error.builder().field("pdf").build();
        byte[] bytes;
        try {
            HashMap<String, Object> data = getData(ticket, eventDto, seatDto, userDto);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            bytes = restTemplate.exchange(
                    "http://PDF/api/v1/pdf/create",
                    HttpMethod.POST,
                    new HttpEntity<>(PdfDto.builder().templateName("ticketpdf").data(data).build(), headers),
                    byte[].class
            ).getBody();
        } catch (RestClientException e) {
            error.setDescription("unable to communicate with pdf server");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        if (bytes == null) {
            error.setDescription("unable to create pdf file");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            String filename = "ticket-" + ticket.getCode() + ".pdf";
            ticket.setPdfName(filename);
            ticket.setPdfAsByteArray(bytes);
            File file = convertToFile(bytes, filename);
            builder.part("file", new FileSystemResource(file));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", token);
            FirebaseDto firebaseDto = restTemplate.exchange(
                    "http://FIREBASE/api/v1/firebase/upload?entity=ticket&id=" + (ticket.getId() == null ? 0 : ticket.getId()) + "&field=pdf&content-type=" + MediaType.APPLICATION_PDF_VALUE + "&update=false",
                    HttpMethod.POST,
                    new HttpEntity<>(builder.build(), headers),
                    FirebaseDto.class).getBody();
            if (!file.delete()) {
                error.setField("file");
                error.setValue(filename);
                error.setDescription("unable to delete file");
                log.error(error.toString());
                throw new TicketProcessingException(HttpStatus.INTERNAL_SERVER_ERROR, error);
            }
            if (firebaseDto != null) {
                ticket.setPdf(firebaseDto.getLink());
            }
        } catch (IOException e) {
            error.setDescription("unable to convert bytes to file");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        } catch (RestClientException e) {
            error.setDescription("unable to communicate with firebase server");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }

    private SeatDto updateSeat(HashMap<String, Object> values) throws TicketProcessingException {
        Long eventId = values.get("event_id") != null ? ((Number) values.get("event_id")).longValue() : null;
        Error error = Error.builder().method("").field("event_id").value(eventId).build();
        if (eventId == null) {
            error.setDescription("should not be null");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        try {
            return restTemplate.exchange(
                    "http://SEAT/api/v1/seat/update",
                    HttpMethod.PUT,
                    new HttpEntity<>(values),
                    SeatDto.class
            ).getBody();
        } catch (RestClientException e) {
            error.setDescription("unable to communicate with seat server");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }

    private File convertToFile(byte[] bytes, String fileName) throws IOException {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(bytes);
        }
        return tempFile;
    }

    private SeatDto cancelSeat(Ticket ticket, EventDto eventDto) throws TicketProcessingException {
        SeatDto seatDto = null;
        if (!eventDto.isWithoutSeats()) {
            HashMap<String, Object> seatValues = new HashMap<>(4);
            seatValues.put("id", ticket.getSeatId());
            seatValues.put("event_id", ticket.getEventId());
            seatValues.put("is_sold_out", eventDto.isSoldOut());
            seatValues.put("occupied", false);
            seatDto = updateSeat(seatValues);
        }
        return seatDto;
    }

    private String toCamelCase(String value) {
        String[] parts = value.split("_");
        if (parts.length < 1) {
            return "";
        }

        StringBuilder camelCaseString = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            camelCaseString.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1).toLowerCase());
        }

        return camelCaseString.toString();
    }

    private HashMap<String, Object> getData(Ticket ticket, EventDto eventDto, SeatDto seatDto, UserDto userDto) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("email", userDto.getEmail());
        data.put("userFirstname", userDto.getFirstname());
        data.put("userLastname", userDto.getLastname());
        data.put("userPhoneNumber", userDto.getPhoneNumber());
        data.put("event", eventDto.getName());
        data.put("code", ticket.getCode());
        data.put("firstname", ticket.getFirstname());
        data.put("lastname", ticket.getLastname());
        data.put("phoneNumber", ticket.getPhoneNumber());
        data.put("adult", ticket.getIsAdult());
        data.put("student", ticket.getIsStudent());
        data.put("paid", ticket.getPaid());
        if (seatDto != null) {
            data.put("sector", seatDto.getSector());
            data.put("row", seatDto.getRow());
            data.put("number", seatDto.getNumber());
        }
        return data;
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

    private void isAnyTicketExpired(List<Ticket> ticketList) throws TicketProcessingException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.MINUTE, -10);
        List<Ticket> filteredTicketList = ticketList.stream().filter(ticket -> ticket.getCreatedAt().after(calendar.getTime())).toList();
        if (filteredTicketList.size() != ticketList.size()) {
            Error error = Error.builder()
                    .field("ids")
                    .value(ticketList.stream().map(Ticket::getId).toList())
                    .description("some tickets with provided ids are expired")
                    .build();
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }
}
