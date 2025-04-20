package com.capstone.enableu.custom.service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.capstone.enableu.custom.enums.ResponseMessage;
import com.capstone.enableu.custom.response.VoiceSearchResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VoiceSearchService {
    @Value("${viettel-ai.access-key}")
    private String token;
    private final static String API_URL = "https://viettelai.vn/asr/recognize";

    public VoiceSearchResponse getTranscript(MultipartFile file) {
        try {
            String boundary = Long.toHexString(System.currentTimeMillis());
            // Open connection to external API
            HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                        .append(file.getOriginalFilename()).append("\"\r\n");
                writer.append("Content-Type: application/octet-stream\r\n\r\n").flush();
                os.write(file.getBytes());
                os.flush();
                writer.append("\r\n").flush();
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"token\"\r\n\r\n");
                writer.append(token).append("\r\n").flush();
                writer.append("--").append(boundary).append("--\r\n").flush();
            }

            int responseCode = connection.getResponseCode();
            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }

            if (responseCode == 200) {
                // Parse JSON response and extract transcript
                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                JSONArray resultArray = jsonResponse.getJSONObject("response").getJSONArray("result");
                if (!resultArray.isEmpty()) {
                    String transcript = resultArray.getJSONObject(0).getString("transcript");
                    if (transcript.isBlank()) {
                        return new VoiceSearchResponse(null, ResponseMessage.VOICE_SEARCH_EMPTY.name());
                    }
                    if (transcript.endsWith(".")) {
                        transcript = transcript.substring(0, transcript.length() - 1);
                    }
                    return new VoiceSearchResponse(transcript, ResponseMessage.VOICE_SEARCH_FETCHED_SUCCESS.name());
                } else {
                    return new VoiceSearchResponse(null, ResponseMessage.VOICE_SEARCH_FETCHED_FAILED.name());
                }
            } else {
                return new VoiceSearchResponse(null, ResponseMessage.VOICE_SEARCH_FETCHED_FAILED.name());
            }
        } catch (Exception e) {
            return new VoiceSearchResponse(null, ResponseMessage.VOICE_SEARCH_FETCHED_FAILED.name());
        }

    }



}
