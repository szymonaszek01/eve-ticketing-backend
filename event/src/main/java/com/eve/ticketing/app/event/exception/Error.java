package com.eve.ticketing.app.event.exception;

import lombok.*;
import org.json.JSONObject;

import java.util.HashMap;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Error {

    private String method;

    private String field;

    private Object value;

    private String message;

    @Override
    public String toString() {
        HashMap<String, Object> hashMap = new HashMap<>(4);
        hashMap.put("method", method);
        hashMap.put("field", field);
        hashMap.put("value", value);
        hashMap.put("message", message);
        JSONObject jsonObject = new JSONObject(hashMap);
        return jsonObject.toString();
    }
}
