package com.eve.ticketing.app.pdf;

import com.eve.ticketing.app.pdf.dto.PdfDto;
import com.eve.ticketing.app.pdf.exception.Error;
import com.eve.ticketing.app.pdf.exception.PdfProcessingException;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
@AllArgsConstructor
public class PdfServiceImpl implements PdfService {

    private SpringTemplateEngine springTemplateEngine;

    @Override
    public byte[] createPdf(PdfDto pdfDto) throws PdfProcessingException {
        try {
            Context context = new Context();
            context.setVariable("data", pdfDto.getData());
            String htmlToString = springTemplateEngine.process(pdfDto.getTemplateName(), context);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlToString, byteArrayOutputStream);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return new InputStreamResource(byteArrayInputStream).getContentAsByteArray();
        } catch (IOException e) {
            Error error = Error.builder().method("POST").field("file").description("unable to create pdf file").build();
            log.error(error.toString());
            throw new PdfProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }
}
