package com.eve.ticketing.app.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotBlank(message = "Field \"name\" should not be blank")
    private String name;

    @NotBlank(message = "Field \"description\" should not be blank")
    @Size(min = 1, max = 500, message = "Field \"description\" should have size between \"1\" - \"500\" characters")
    private String description;

    @JsonProperty("max_ticket_amount")
    @NotNull(message = "Field \"max_ticket_amount\" should not be \"null\"")
    @Min(value = 0, message = "Field \"max_ticket_amount\" should be greater than or equal \"0\"")
    private Long maxTicketAmount;

    @JsonProperty("is_sold_out")
    @NotNull(message = "Field \"is_sold_out\" should not be \"null\"")
    private Boolean isSoldOut;

    @JsonProperty("unit_price")
    @NotNull(message = "Field \"unit_price\" should not be \"null\"")
    @Min(value = 0, message = "Field \"unit_price\" should be greater than or equal \"0\"")
    private BigDecimal unitPrice;

    @NotBlank(message = "Field \"currency\" should not be blank")
    private String currency;

    @JsonProperty("children_discount")
    @NotNull(message = "Field \"children_discount\" should not be \"null\"")
    @Min(value = 0, message = "Field \"children_discount\" should be greater than or equal \"0\"")
    @Max(value = 100, message = "Field \"children_discount\" should be less than or equal \"100\"")
    private BigDecimal childrenDiscount;

    @JsonProperty("students_discount")
    @NotNull(message = "Field \"students_discount\" should not be \"null\"")
    @Min(value = 0, message = "Field \"students_discount\" should be greater than or equal \"0\"")
    @Max(value = 100, message = "Field \"students_discount\" should be less than or equal \"100\"")
    private BigDecimal studentsDiscount;

    @JsonProperty("start_at")
    @NotNull(message = "Field \"start_at\" should not be \"null\"")
    @FutureOrPresent(message = "Field \"start_at\" should be present or future date")
    private Date startAt;

    @JsonProperty("end_at")
    @NotNull(message = "Field \"end_at\" should not be \"null\"")
    @FutureOrPresent(message = "Field \"end_at\" should be present or future date")
    private Date endAt;

    @NotBlank(message = "Field \"country\" should not be blank")
    private String country;

    @NotBlank(message = "Field \"address\" should not be blank")
    private String address;

    @JsonProperty("localization_name")
    @NotBlank(message = "Field \"localization_name\" should not be blank")
    private String localizationName;

    @JsonProperty("is_without_seats")
    @NotNull(message = "Field \"is_without_seats\" should not be \"null\"")
    private Boolean isWithoutSeats;
}
