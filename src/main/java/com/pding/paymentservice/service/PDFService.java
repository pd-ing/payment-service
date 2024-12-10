package com.pding.paymentservice.service;

import com.lowagie.text.DocumentException;
import com.pding.paymentservice.models.other.services.tables.dto.DonorData;
import com.pding.paymentservice.payload.response.SalesHistoryData;
import com.pding.paymentservice.util.DateTimeUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;

import java.io.*;
import java.util.List;

@Service
public class PDFService {
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    EmailSenderService emailSenderService;

    private static final long MAX_DIR_SIZE = 200 * 1024 * 1024; // 200MB
    private static final String FILE_PREFIX = "pding_report_";
    private static final String FILE_SUFFIX = ".pdf";

    @Value("${app.temp.dir:${java.io.tmpdir}}")
    private String tempDirPath;
    public ByteArrayOutputStream generateTempFilePDFDonation(List<DonorData> donorDataList, String userId, String nickname) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            String headerHtml = generateSponsorHtml(donorDataList, userId, nickname);
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(headerHtml);
            renderer.layout();
            renderer.createPDF(byteArrayOutputStream);

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return byteArrayOutputStream;
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

    public ByteArrayOutputStream generateTempPDFSellerHistory( SalesHistoryData salesHistoryData) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            String headerHtml = generateSellerHtml(salesHistoryData);
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(headerHtml);
            renderer.layout();
            renderer.createPDF(byteArrayOutputStream);

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return byteArrayOutputStream;
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

    private String generateSponsorHtml(List<DonorData> donorDataList, String userId, String nickname) {
        Context context = new Context();
        context.setVariable("donorDataList", donorDataList);
        context.setVariable("userId", userId);
        context.setVariable("nickname", nickname);
        context.setVariable("issueDate",DateTimeUtil.getCurrentTimeNow());
        return templateEngine.process("pdf-sponsor", context);
    }

    private String generateSellerHtml(SalesHistoryData salesHistoryData) {
        Context context = new Context();
        context.setVariable("salesHistoryData", salesHistoryData);
        context.setVariable("issueDate",DateTimeUtil.getCurrentTimeNow());
        return templateEngine.process("pdf-seller-history", context);
    }

    public void cachePdfContent(String reportId, byte[] pdfBytes) throws IOException {

        File tempFile = new File(tempDirPath, FILE_PREFIX+ reportId + ".pdf");
        long fileSize = tempFile.length();
        checkAndClearTmpDir(fileSize);
        File dir = new File(tempDirPath);
        // Check if the directory exists, create it if it doesn't
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Failed to create temp directory: " + tempDirPath);
            }
        }
        if (tempFile.exists()) {
            tempFile.delete();
        }

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(pdfBytes);
        }

    }

    public byte[] getPDF(String reportId) throws IOException {
        File file = new File(tempDirPath, FILE_PREFIX + reportId + ".pdf");
        if (!file.exists()) {
            throw new IOException("PDF not found for reportId: " + reportId);
        }
        return Files.readAllBytes(file.toPath());
    }

    public void deletePDF(String reportId) {
        File file = new File(tempDirPath, FILE_PREFIX + reportId + ".pdf");
        if (file.exists()) {
            file.delete();
        }
    }

    public void checkAndClearTmpDir(long requiredFreeSpace) throws IOException {
        // Get the path to the temporary directory
        File tmpDir = new File(tempDirPath);

        // Check if the directory exists, create it if it doesn't
        if (!tmpDir.exists()) {
            return;
        }
        // Calculate the total size of the temporary directory
        long totalSpace = getDirectorySize(tmpDir);

        // If the directory size exceeds the maximum allowed size, attempt to free up space
        if (totalSpace > MAX_DIR_SIZE) {
            // Calculate the amount of space to free
            long spaceToFree = totalSpace - MAX_DIR_SIZE + requiredFreeSpace;
            // Attempt to free up space by deleting old files
            freeUpSpace(tmpDir, spaceToFree);
        }
    }

    private long getDirectorySize(File dir) {
        long size = 0;
        // List all files and directories in the specified directory
        File[] files = dir.listFiles();
        if (files != null) {
            // Iterate through each file or subdirectory
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursively add the size of subdirectories
                    size += getDirectorySize(file);
                } else {
                    // Add the size of the individual file
                    size += file.length();
                }
            }
        }
        return size;
    }

    private void freeUpSpace(File dir, long spaceToFree){
        // List all files and directories in the specified directory
        File[] files = dir.listFiles();
        if (files != null) {
            // Iterate through each file in the directory
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));

            for (File file : files) {
                // Check if the file starts with "report_" and ends with ".pdf"
                if (file.getName().startsWith(FILE_PREFIX) && file.getName().endsWith(FILE_SUFFIX)) {
                    // Try to delete the file
                    if (file.delete()) {
                        // Reduce the required free space by the size of the deleted file
                        spaceToFree -= file.length();
                        // If enough space has been freed, exit the loop
                        if (spaceToFree <= 0) {
                            break;
                        }
                    }
                }
            }
        }
    }

}
