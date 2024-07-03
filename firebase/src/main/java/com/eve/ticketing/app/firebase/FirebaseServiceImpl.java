package com.eve.ticketing.app.firebase;

import com.eve.ticketing.app.firebase.dto.FirebaseDto;
import com.eve.ticketing.app.firebase.exception.Error;
import com.eve.ticketing.app.firebase.exception.FirebaseProcessingException;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

@Slf4j
@Service
public class FirebaseServiceImpl implements FirebaseService {

    // 1) https://medium.com/@poojithairosha/image-uploading-with-spring-boot-firebase-cloud-storage-e5ef2fbf942d

    // 2) https://medium.com/teamarimac/file-upload-and-download-with-spring-boot-firebase-af068bc62614

    @Override
    public FirebaseDto upload(MultipartFile multipartFile) throws FirebaseProcessingException {
        Error error = Error.builder().method("POST").build();
        try {
            String filename = updateFilename(multipartFile.getOriginalFilename(), error);
            File file = convertToFile(multipartFile, filename);
            Storage storage = getStorageService();
            storage.create(BlobInfo.newBuilder(getBlobId(filename)).setContentType("media").build(), Files.readAllBytes(file.toPath()));
            if (!file.delete()) {
                error.setField("file");
                error.setValue(filename);
                error.setDescription("unable to delete file");
                log.error(error.toString());
                throw new FirebaseProcessingException(HttpStatus.INTERNAL_SERVER_ERROR, error);
            }
            return FirebaseDto.builder().filename(filename).link(getLink(filename)).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            error.setDescription("image could not be uploaded");
            throw new FirebaseProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private BlobId getBlobId(String filename) {
        return BlobId.of("eve-ticketing-app.appspot.com", filename);
    }

    private Storage getStorageService() throws IOException {
        InputStream inputStream = FirebaseServiceImpl.class.getClassLoader().getResourceAsStream("eve-ticketing-app-firebase-adminsdk-f1pct-43c0257796.json");
        if (inputStream == null) {
            throw new IOException("Could not read firebase credentials");
        }
        Credentials credentials = GoogleCredentials.fromStream(inputStream);
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    private String getLink(String filename) {
        return String.format("https://firebasestorage.googleapis.com/v0/b/eve-ticketing-app.appspot.com/o/%s?alt=media", URLEncoder.encode(filename, StandardCharsets.UTF_8));
    }

    private String updateFilename(String filename, Error error) throws FirebaseProcessingException {
        if (StringUtils.isBlank(filename)) {
            error.setField("filename");
            error.setValue(filename);
            error.setDescription("filename should not be blank");
            log.error(error.toString());
            throw new FirebaseProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        return UUID.randomUUID().toString().concat(getExtension(filename));
    }

    private File convertToFile(MultipartFile multipartFile, String fileName) throws IOException {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        return tempFile;
    }
}
