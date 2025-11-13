package com.example.export_report_service.service;

import com.example.export_report_service.kafka.ReportData;
import com.example.export_report_service.kafka.TransactionResponse;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.itextpdf.layout.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.List;

@Slf4j
@Service
public class ReportGenerateService {


    @KafkaListener(topics = "REPORT-CSV", groupId = "export-report-service", containerFactory = "reportDataKafkaListenerContainerFactory")
    public byte[] exportToCSV(ReportData reportData) throws IOException {

        List<TransactionResponse> transactions = reportData.getTransactions();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(out);
        CSVWriter writer = new CSVWriter(osw);

        String[] header = {"Transaction ID", "Type", "Category", "Amount", "Date"};
        writer.writeNext(header);

        DecimalFormat df = new DecimalFormat("#.00");
        for (TransactionResponse t : transactions) {
            writer.writeNext(new String[]{
                    t.getTransactionID(),
                    t.getPaymentType(),
                    t.getCategory(),
                    df.format(t.getAmount()),
                    t.getTransactionDate().toString()
            });
        }
        writer.close();
        return out.toByteArray();
    }


    @KafkaListener(topics = "REPORT-PDF", groupId = "export-report-service", containerFactory = "reportDataKafkaListenerContainerFactory")
    public byte[] exportToPDF(ReportData reportData) throws IOException {

        List<TransactionResponse> transactions = reportData.getTransactions();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Transaction Report").setBold().setFontSize(16).setMarginBottom(15));
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        String[] headers = {"Transaction ID", "Type", "Amount", "Date"};
        for (String h : headers)
            table.addHeaderCell(new Cell().add(new Paragraph(h).setBold()));

        DecimalFormat df = new DecimalFormat("#.00");
        for (TransactionResponse t : transactions) {
            table.addCell(t.getTransactionID());
            table.addCell(t.getType());
            table.addCell(df.format(t.getAmount()));
            table.addCell(t.getTransactionDate().toString());
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

}
