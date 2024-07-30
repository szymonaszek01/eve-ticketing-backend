package com.eve.ticketing.app.pdf.exception;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PdfApiException {

    private Integer status;

    private String message;

    private List<Error> errors;
}
