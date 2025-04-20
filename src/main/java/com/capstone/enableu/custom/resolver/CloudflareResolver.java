package com.capstone.enableu.custom.resolver;

import com.capstone.enableu.custom.service.CloudflareService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@AllArgsConstructor
@Controller
public class CloudflareResolver {

    private final CloudflareService cloudflareService;

    @QueryMapping("generateUploadPresignedUrl")
    public String generatePresignedUrl(@Argument String objectKey) {
        return cloudflareService.generatePresignedUrl(objectKey);
    }
}
