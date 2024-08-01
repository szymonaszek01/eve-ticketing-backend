package com.eve.ticketing.app.ticket.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FirebaseDto {

    private String filename;

    private String link;
}
