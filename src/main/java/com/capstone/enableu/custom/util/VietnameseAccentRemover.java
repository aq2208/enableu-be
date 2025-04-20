package com.capstone.enableu.custom.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class VietnameseAccentRemover {

    public static String removeVietnameseAccent(String input) {
        if (input == null) {
            return null;
        }

        // Normalize the input string to decompose accented characters
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Use regex to remove all diacritical marks
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return  pattern.matcher(normalized)
                .replaceAll("")
                .replaceAll("đ", "d");
    }

}