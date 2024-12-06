package com.pding.paymentservice.service;

import com.lowagie.text.DocumentException;
import com.pding.paymentservice.models.other.services.tables.dto.DonorData;
import com.pding.paymentservice.payload.response.SalesHistoryData;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.List;

@Service
public class PDFService {
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    EmailSenderService emailSenderService;

    public void generatePDFDonation(HttpServletResponse response, List<DonorData> donorDataList, String userId, String email, String nickname) throws IOException, MessagingException {
        File tempFile = File.createTempFile("donation_report_", ".pdf");
        tempFile.deleteOnExit();

        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            String headerHtml = generateSponsorHtml(donorDataList, userId, email, nickname);
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(headerHtml);
            renderer.layout();
            renderer.createPDF(byteArrayOutputStream);
            byteArrayOutputStream.writeTo(outputStream);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        emailSenderService.sendEmailWithAttachment(email, "Your Requested PDF Report is Ready for Download", "PDF Report", tempFile);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=donation_report.pdf");

        downloadFilePDF(response, tempFile);

    }

    private void downloadFilePDF(HttpServletResponse response, File tempFile) throws IOException {
        try (InputStream inputStream = new FileInputStream(tempFile); OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    public void generatePDFSellerHistory(HttpServletResponse httpServletResponse, SalesHistoryData salesHistoryData) throws IOException, MessagingException {

        File tempFile = File.createTempFile("sales_report_", ".pdf");
        tempFile.deleteOnExit();

        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            String headerHtml = generateSellerHtml(salesHistoryData);
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(headerHtml);
            renderer.layout();
            renderer.createPDF(byteArrayOutputStream);
            byteArrayOutputStream.writeTo(outputStream);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        emailSenderService.sendEmailWithAttachment(salesHistoryData.getEmail(), "Your Requested PDF Report is Ready for Download", "PDF Report", tempFile);

        httpServletResponse.setContentType("application/pdf");
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=sales_report.pdf");
        downloadFilePDF(httpServletResponse, tempFile);

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
