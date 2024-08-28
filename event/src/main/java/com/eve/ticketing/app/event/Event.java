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
    @NotBlank(message = "should not be blank")
    private String name;

    @NotBlank(message = "should not be blank")
    @Size(min = 1, max = 500, message = "should have size between 1 - 500 characters")
    private String description;

    @JsonProperty("max_ticket_amount")
    @NotNull(message = "should not be null")
    @Min(value = 0, message = "should be greater than or equal 0")
    private Long maxTicketAmount;

    @JsonProperty("is_sold_out")
    @NotNull(message = "should not be null")
    private Boolean isSoldOut;

    @JsonProperty("unit_price")
    @NotNull(message = "should not be null")
    @Min(value = 0, message = "should be greater than or equal 0")
    private BigDecimal unitPrice;

    @NotBlank(message = "should not be blank")
    private String currency;

    @JsonProperty("children_discount")
    @NotNull(message = "should not be null")
    @Min(value = 0, message = "should be greater than or equal 0")
    @Max(value = 100, message = "should be less than or equal 100")
    private BigDecimal childrenDiscount;

    @JsonProperty("students_discount")
    @NotNull(message = "should not be null")
    @Min(value = 0, message = "should be greater than or equal 0")
    @Max(value = 100, message = "should be less than or equal 100")
    private BigDecimal studentsDiscount;

    @JsonProperty("start_at")
    @NotNull(message = "should not be null")
    @FutureOrPresent(message = "should be present or future date")
    private Date startAt;

    @JsonProperty("end_at")
    @NotNull(message = "should not be null")
    @FutureOrPresent(message = "should be present or future date")
    private Date endAt;

    @NotBlank(message = "should not be blank")
    private String country;

    @NotBlank(message = "should not be blank")
    private String address;

    @JsonProperty("localization_name")
    @NotBlank(message = "should not be blank")
    private String localizationName;

    @JsonProperty("is_without_seats")
    @NotNull(message = "should not be null")
    private Boolean isWithoutSeats;

    private String image;

    @JsonProperty("admin_id")
    @NotNull(message = "should not be null")
    @Min(value = 1, message = "should be greater than 0")
    private Long adminId;
}
