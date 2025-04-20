package com.capstone.enableu.custom.util;

import com.capstone.enableu.custom.dto.HighlightDuration;
import com.capstone.enableu.custom.enums.ResponseMessage;
import com.capstone.enableu.custom.exception.BadRequestException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

public class Validate {
    public static String getUsername(String username) {
        return username.replaceFirst("^0", "");
    }

    public static boolean isNumeric(String str) {
        return str != null && str.matches("[0-9]+");
    }

    public static Date ConvertStringToDate(String dateStr) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date startDate;
        try {
            startDate = df.parse(dateStr);
            return startDate;
        } catch (ParseException e) {
            throw new BadRequestException(ResponseMessage.INCORRECT_DATE_FORMAT.toString());
        }
    }

    public static String convertDateToString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    public static boolean isCurrentTimeInAnyRange(List<HighlightDuration> durations) {
        if (durations == null || durations.isEmpty()) {
            return false;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        // Timezone Vietnam
        ZoneId zoneId = ZoneOffset.of("+07");
        LocalTime currentTime = LocalTime.now(zoneId);

        for (HighlightDuration duration : durations) {
            try {
                LocalTime startTime = LocalTime.parse(duration.getStartTime(), formatter);
                LocalTime endTime = LocalTime.parse(duration.getEndTime(), formatter);

                // Check if current time is within the range
                if (startTime.isBefore(endTime)) {
                    // Normal case: startTime < endTime
                    if (!currentTime.isBefore(startTime) && !currentTime.isAfter(endTime)) {
                        return true;
                    }
                }
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid time format for startTime or endTime");
            }
        }

        return false; // No matching range found
    }

    public static String validatePhoneNumber(String input) {
        if (isNumeric(input)) {
            if (input != null && input.startsWith("0")) {
                return input.substring(1);
            }
            return input;
        }
        return null;
    }
    public static LocalTime validateAndConvertParseTime(String timeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            return LocalTime.parse(timeString, formatter); // Return the LocalTime if parsing is successful
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid time format. Expected format is HH:mm.");
        }
    }

    public static void validateListHighlightDuration(List<HighlightDuration> highlightDuration) {
        for (HighlightDuration highlightDurationItem : highlightDuration) {
            validateAndConvertParseTime(highlightDurationItem.getStartTime());
            validateAndConvertParseTime(highlightDurationItem.getEndTime());
        }
    }

}
