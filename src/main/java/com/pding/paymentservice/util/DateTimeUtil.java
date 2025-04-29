package com.pding.paymentservice.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    static public LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    static public String formatLocalDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : "N/A";
    }

    static public LocalDateTime convertStringToLocaltime(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        return LocalDateTime.parse(dateTimeString, formatter);
    }

    static public LocalDateTime convertStringToLocaltime(String dateTimeString, DateTimeFormatter formatter) {
        return LocalDateTime.parse(dateTimeString, formatter);
    }

    static public String getCurrentTimeNow(){
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return currentDate.format(formatter);
    }
}
