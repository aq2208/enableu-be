package com.capstone.enableu.custom.util;

import com.capstone.enableu.custom.service.StorageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TextToSpeech {


    private final StorageService storageService;

    @Value("${tts.api-key}")
    private String apiKey;
    @Value("${tts.api-url}")
    private String apiUrl;

    public String getAsyncLink(String inputString) {
        try {
            // Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("api-key", apiKey)
                    .header("speed", "0.5")
                    .header("voice", "banmai")
                    .POST(HttpRequest.BodyPublishers.ofString(inputString))
                    .build();
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String asyncLink = jsonNode.get("async").asText();
            // download the file from the link
            String localFileName = System.currentTimeMillis() + UUID.randomUUID().toString() + "_tts_file.mp3"; // Adjust the file name as needed

            File downloadedFile = null;
            do {
                downloadedFile = downloadFile(asyncLink, localFileName);
            } while (downloadedFile == null);
            return storageService.uploadFile(downloadedFile);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private File downloadFile(String fileUrl, String localFileName) {
        try (InputStream in = new URL(fileUrl).openStream()) {
            Files.copy(in, Paths.get(localFileName));
            System.out.println("File downloaded successfully: " + localFileName);
            return new File(localFileName); // Return File object
        } catch (IOException e) {
            return null;
        }
    }



}
