package com.company.socialanalytics.report;

import com.company.socialanalytics.security.CurrentUser;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportExportService reportExportService;

    public ReportController(ReportExportService reportExportService) {
        this.reportExportService = reportExportService;
    }

    @PostMapping("/csv")
    ResponseEntity<byte[]> csv(CurrentUser currentUser) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("social-analytics-report.csv")
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(reportExportService.csv(currentUser));
    }

    @PostMapping("/pdf")
    ResponseEntity<byte[]> pdf(CurrentUser currentUser) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("social-analytics-report.pdf")
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(reportExportService.pdf(currentUser));
    }
}
