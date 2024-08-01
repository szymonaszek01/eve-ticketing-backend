package com.eve.ticketing.app.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashMap;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PdfDto {

    @NotBlank(message = "should not be blank")
    private String templateName;

    @NotNull(message = "should not be null")
    private HashMap<String, Object> data;
}
