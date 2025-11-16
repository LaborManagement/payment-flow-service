package com.example.paymentflow.utilities.file;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.example.paymentflow.worker.entity.WorkerPayment;
import com.shared.utilities.logger.LoggerFactoryProvider;

@Component
public class FileParsingUtil {
    private static final Logger log = LoggerFactoryProvider.getLogger(FileParsingUtil.class);

    /**
     * Parses a file and maps its rows to WorkerPayment domain objects.
     * Uses FileParsingUtils for file parsing.
     */
    public List<WorkerPayment> parseFile(File file, String filename) throws IOException {
        List<String[]> rows = FileParsingUtils.parseFile(file, filename);
        List<WorkerPayment> payments = new ArrayList<>();
        boolean isFirstRow = true;
        for (String[] row : rows) {
            if (isFirstRow) {
                isFirstRow = false;
                continue; // skip header
            }
            WorkerPayment payment = createWorkerPayment(row);
            if (payment != null) {
                payments.add(payment);
            }
        }
        log.info("Parsed {} worker payment records from file {}", payments.size(), filename);
        return payments;
    }

    private WorkerPayment createWorkerPayment(String[] rowData) {
        try {
            if (rowData.length < 8) {
                log.warn("Row has insufficient columns, skipping: {}", java.util.Arrays.toString(rowData));
                return null;
            }

            WorkerPayment payment = new WorkerPayment();
            payment.setWorkerRef(rowData[0] != null ? rowData[0].trim() : "");
            payment.setRegId(rowData[1] != null ? rowData[1].trim() : "");
            payment.setName(rowData[2] != null ? rowData[2].trim() : "");
            payment.setToli(rowData[3] != null ? rowData[3].trim() : "");
            payment.setAadhar(rowData[4] != null ? rowData[4].trim() : "");
            payment.setPan(rowData[5] != null ? rowData[5].trim() : "");
            payment.setBankAccount(rowData[6] != null ? rowData[6].trim() : "");

            // Parse payment amount
            String amountStr = rowData[7] != null ? rowData[7].trim() : "0";
            try {
                // Handle numeric values that might be in scientific notation
                double amount = Double.parseDouble(amountStr);
                payment.setPaymentAmount(BigDecimal.valueOf(amount));
            } catch (NumberFormatException e) {
                log.warn("Invalid payment amount '{}', setting to 0", amountStr);
                payment.setPaymentAmount(BigDecimal.ZERO);
            }

            // Validate required fields
            if (payment.getWorkerRef().isEmpty() || payment.getName().isEmpty()) {
                log.warn("Row missing required fields (workerRef or name), skipping");
                return null;
            }

            return payment;
        } catch (Exception e) {
            log.error("Error creating WorkerPayment from row data: {}", java.util.Arrays.toString(rowData), e);
            return null;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
