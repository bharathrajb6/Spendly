package com.example.transaction_service.controller;

import com.example.transaction_service.service.ReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Generates a report for the specified user and date range.
     * 
     * @param username  the username of the user
     * @param type      the type of report to generate
     * @param startDate the start date of the report
     * @param endDate   the end date of the report
     * @return a response entity indicating the status of the report generation
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/report", method = RequestMethod.GET)
    public ResponseEntity<?> generateReportData(@RequestHeader("X-Username") String username,
            @RequestParam("type") String type, @RequestParam("start") String startDate,
            @RequestParam("end") String endDate) throws JsonProcessingException {
        reportService.generateReport(username, type, startDate, endDate);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
