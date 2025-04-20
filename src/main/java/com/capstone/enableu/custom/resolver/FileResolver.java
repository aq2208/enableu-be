package com.capstone.enableu.custom.resolver;

import com.capstone.enableu.custom.enums.Role;
import com.capstone.enableu.custom.service.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

@Controller
@AllArgsConstructor
public class FileResolver {
    private final StorageService storageService;

    @MutationMapping
    @Secured(Role.TRAINEE_CONSTANT)
    public String uploadFile(@Argument MultipartFile file) {
        return storageService.uploadFile(file);
    }
}
