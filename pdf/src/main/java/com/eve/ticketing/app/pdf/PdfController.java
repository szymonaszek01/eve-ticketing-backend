package com.eve.ticketing.app.pdf;

import com.eve.ticketing.app.pdf.dto.PdfDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@Tag(name = "Pdf", description = "Pdf management APIs")
@RequestMapping("/api/v1/pdf")
@RequiredArgsConstructor
@RestController
public class PdfController {

    private final PdfServiceImpl pdfService;

    @PostMapping("/create")
    public ResponseEntity<?> createPdf(@Valid @RequestBody PdfDto pdfDto) {
        byte[] bytes = pdfService.createPdf(pdfDto);
        return new ResponseEntity<>(bytes, HttpStatus.CREATED);
    }
}
