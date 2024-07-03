package com.eve.ticketing.app.firebase.dto;

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
