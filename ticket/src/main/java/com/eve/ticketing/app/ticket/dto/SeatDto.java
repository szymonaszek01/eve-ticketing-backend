package com.eve.ticketing.app.ticket.dto;

import jakarta.persistence.Column;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeatDto {

    private Long id;

    private String sector;

    @Column(nullable = false)
    private Integer row;

    @Column(nullable = false)
    private Integer number;

}
