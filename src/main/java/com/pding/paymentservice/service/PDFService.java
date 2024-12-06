package com.pding.paymentservice.service;

import com.lowagie.text.DocumentException;
import com.pding.paymentservice.models.other.services.tables.dto.DonorData;
import com.pding.paymentservice.payload.response.SalesHistoryData;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class PDFService {
    @Autowired
    private TemplateEngine templateEngine;

    public void generatePDFDonation(HttpServletResponse response, List<DonorData> donorDataList,
                                    String userId, String email, String nickname) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=example.pdf");

        try (OutputStream outputStream = response.getOutputStream()) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // Add the header HTML
            String headerHtml = generateSponsorHtml(donorDataList, userId, email, nickname);
            // Main body HTML with donor data
//            String bodyHtml = generateBodyHtml(donorDataList);
//            String fullHtml = headerHtml + bodyHtml;
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(headerHtml);
            renderer.layout();
            renderer.createPDF(byteArrayOutputStream);
            // Add the main content
            byteArrayOutputStream.writeTo(outputStream);

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

    }

    public void generatePDFSellerHistory(HttpServletResponse httpServletResponse, SalesHistoryData salesHistoryData) throws IOException {
        httpServletResponse.setContentType("application/pdf");
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=example.pdf");

        try (OutputStream outputStream = httpServletResponse.getOutputStream()) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // Add the HTML
            String headerHtml = generateSellerHtml(salesHistoryData);

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(headerHtml);
            renderer.layout();
            renderer.createPDF(byteArrayOutputStream);
            // Add the main content
            byteArrayOutputStream.writeTo(outputStream);

        } catch (com.lowagie.text.DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateSponsorHtml(List<DonorData> donorDataList, String userId, String email, String nickname) {
        Context context = new Context();
        context.setVariable("donorDataList", donorDataList);
        context.setVariable("userId", userId);
        context.setVariable("email", email);
        context.setVariable("nickname", nickname);
        return templateEngine.process("pdf-sponsor", context);
    }

    private String generateSellerHtml(SalesHistoryData salesHistoryData) {
        Context context = new Context();
        context.setVariable("salesHistoryData", salesHistoryData);
        return templateEngine.process("pdf-seller-history", context);
    }

}
