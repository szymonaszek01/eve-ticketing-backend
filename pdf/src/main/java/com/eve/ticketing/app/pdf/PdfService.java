package com.eve.ticketing.app.pdf;

import com.eve.ticketing.app.pdf.dto.PdfDto;
import com.eve.ticketing.app.pdf.exception.PdfProcessingException;

public interface PdfService {

    byte[] createPdf(PdfDto ticketDto) throws PdfProcessingException;
}
