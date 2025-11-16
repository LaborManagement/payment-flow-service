package com.example.paymentflow.utilities.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.shared.utilities.logger.LoggerFactoryProvider;

/**
 * Utility class for parsing files into raw row data (String[]).
 * No domain mapping logic here.
 */
public class FileParsingUtils {
    private static final Logger log = LoggerFactoryProvider.getLogger(FileParsingUtils.class);

    public static List<String[]> parseFile(File file, String filename) throws IOException {
        String extension = getFileExtension(filename);
        switch (extension.toLowerCase()) {
            case "csv":
                return parseCsvFile(file);
            case "xls":
                return parseXlsFile(file);
            case "xlsx":
                return parseXlsxFile(file);
            default:
                throw new IllegalArgumentException("Unsupported file format: " + extension);
        }
    }

    public static List<String[]> parseCsvFile(File file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        log.info("Parsing CSV file: {}", file.getName());
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                rows.add(nextLine);
            }
        } catch (CsvValidationException e) {
            log.error("CSV validation error while parsing file: {}", file.getName(), e);
            throw new IOException("CSV validation error: " + e.getMessage(), e);
        }
        return rows;
    }

    public static List<String[]> parseXlsFile(File file) throws IOException {
        return parseExcelFile(file, false);
    }

    public static List<String[]> parseXlsxFile(File file) throws IOException {
        return parseExcelFile(file, true);
    }

    private static List<String[]> parseExcelFile(File file, boolean isXlsx) throws IOException {
        List<String[]> rows = new ArrayList<>();
        log.info("Parsing Excel file: {}", file.getName());
        try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = isXlsx ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                int lastCell = row.getLastCellNum();
                String[] rowData = new String[lastCell];
                for (int i = 0; i < lastCell; i++) {
                    Cell cell = row.getCell(i);
                    rowData[i] = getCellValueAsString(cell);
                }
                rows.add(rowData);
            }
        }
        return rows;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
