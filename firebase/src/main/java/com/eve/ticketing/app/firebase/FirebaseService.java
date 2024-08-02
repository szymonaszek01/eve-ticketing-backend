package com.eve.ticketing.app.firebase;

import com.eve.ticketing.app.firebase.dto.FirebaseDto;
import com.eve.ticketing.app.firebase.exception.FirebaseProcessingException;
import org.springframework.web.multipart.MultipartFile;

public interface FirebaseService {

FirebaseDto upload(MultipartFile multipartFile, String entity, Long id, String field, String contentType, boolean update, String token) throws FirebaseProcessingException;
}
