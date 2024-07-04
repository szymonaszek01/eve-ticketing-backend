package com.eve.ticketing.app.firebase;

import com.eve.ticketing.app.firebase.dto.FirebaseDto;
import com.eve.ticketing.app.firebase.exception.FirebaseProcessingException;
import org.springframework.web.multipart.MultipartFile;

public interface FirebaseService {

    FirebaseDto upload(MultipartFile file, String token) throws FirebaseProcessingException;
}
