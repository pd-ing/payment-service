package com.pding.paymentservice.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapperUtils {

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    public static String toString(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T toObject(String value, Class<T> clazz) {
        try {
            return MAPPER.readValue(value, clazz);
        } catch (Exception e) {
            return null;
        }
    }
}
