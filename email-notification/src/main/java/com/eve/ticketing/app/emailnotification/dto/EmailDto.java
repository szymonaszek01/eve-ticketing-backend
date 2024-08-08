package com.eve.ticketing.app.emailnotification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashMap;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailDto {

    @Email(regexp = ".+[@].+[\\.].+", message = "invalid email address")
    @NotBlank(message = "should not be blank")
    private String to;

    @NotBlank(message = "should not be blank")
    private String subject;

    @NotBlank(message = "should not be blank")
    private String template;

    @NotNull(message = "should not be null")
    private HashMap<String, Object> data;

    private String email;

    private byte[] attachment;

    private String attachmentName;

    private String attachmentType;
}
