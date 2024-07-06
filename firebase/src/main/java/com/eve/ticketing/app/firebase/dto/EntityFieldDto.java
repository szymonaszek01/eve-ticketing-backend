package com.eve.ticketing.app.firebase.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntityFieldDto<T> {

    private Number id;

    private String key;

    private T value;
}
