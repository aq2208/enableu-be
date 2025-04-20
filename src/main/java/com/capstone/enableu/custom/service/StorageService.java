package com.capstone.enableu.custom.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class StorageService {
    @Value("${application.bucket.name}")
    private String bucketName;

    @Value("${application.bucket.url}")
    private String bucketUrl;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadFile(MultipartFile file) {
        System.out.println("uploading file.....");
        System.out.println(file.getOriginalFilename());
        File fileObj = convertMultiPartFileToFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
        fileObj.delete();
        return bucketUrl + "/" + fileName;
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            System.out.println("error");
        }
        return convertedFile;
    }

    public String uploadFile(File file) {
        System.out.println("uploading file.....");
        System.out.println(file.getName());
        String fileName = System.currentTimeMillis() + "_" + file.getName();
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));
        file.delete();
        return bucketUrl + "/" + fileName;
    }
}
