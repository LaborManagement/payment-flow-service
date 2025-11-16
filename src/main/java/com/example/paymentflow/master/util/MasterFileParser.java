package com.example.paymentflow.master.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import com.example.paymentflow.master.entity.BoardMaster;
import com.example.paymentflow.master.entity.EmployerMaster;
import com.example.paymentflow.master.entity.ToliMaster;
import com.example.paymentflow.master.entity.WorkerMaster;

public class MasterFileParser {

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    public static List<ToliMaster> parseToliCsv(MultipartFile file, String boardId, String employerId)
            throws Exception {
        List<ToliMaster> list = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream());
                CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
                        .parse(reader)) {
            for (CSVRecord record : parser) {
                ToliMaster entity = new ToliMaster();
                entity.setBoardId(boardId);
                entity.setEmployerId(employerId);
                entity.setRegistrationNumber(getValue(record, "registration_number", "registration_no"));
                entity.setEmployerNameMarathi(getValue(record,
                        "employer_name_marathi",
                        "toli_name_marathi",
                        "employer_name",
                        "establishment_name"));
                entity.setAddress(getOptionalValue(record, "address", "address1", "address_line_1"));
                entity.setEmployerNameEnglish(getOptionalValue(record,
                        "employer_name_english",
                        "toli_name_english",
                        "establishment_name_english",
                        "establishment_name_secondary",
                        "employer_name_english_text"));
                entity.setMobileNumber(getOptionalValue(record, "mobile_number", "mobile_no", "phone_number"));
                entity.setEmailId(getOptionalValue(record, "email_id", "email"));
                String status = getOptionalValue(record, "status");
                if (status != null) {
                    entity.setStatus(status);
                }
                LocalDateTime createdAt = parseDateTime(getOptionalValue(record, "created_at"));
                if (createdAt != null) {
                    entity.setCreatedAt(createdAt);
                }
                LocalDateTime updatedAt = parseDateTime(getOptionalValue(record, "updated_at"));
                if (updatedAt != null) {
                    entity.setUpdatedAt(updatedAt);
                }
                list.add(entity);
            }
        }
        return list;
    }

    public static List<ToliMaster> parseToliXls(MultipartFile file, String boardId, String employerId)
            throws Exception {
        List<ToliMaster> list = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                ToliMaster entity = new ToliMaster();
                entity.setBoardId(boardId);
                entity.setEmployerId(employerId);
                entity.setRegistrationNumber(requireCellValue(row, 0, "registration_number"));
                entity.setEmployerNameMarathi(requireCellValue(row, 1, "employer_name_marathi"));
                entity.setAddress(getCellValue(row, 2));
                entity.setEmployerNameEnglish(getCellValue(row, 3));
                entity.setMobileNumber(getCellValue(row, 4));
                entity.setEmailId(getCellValue(row, 5));
                String status = getCellValue(row, 6);
                if (status != null) {
                    entity.setStatus(status);
                }
                LocalDateTime createdAt = parseDateTime(getCellValue(row, 7));
                if (createdAt != null) {
                    entity.setCreatedAt(createdAt);
                }
                LocalDateTime updatedAt = parseDateTime(getCellValue(row, 8));
                if (updatedAt != null) {
                    entity.setUpdatedAt(updatedAt);
                }
                list.add(entity);
            }
        }
        return list;
    }

    public static List<WorkerMaster> parseWorkerCsv(MultipartFile file, String boardId, String employerId)
            throws Exception {
        List<WorkerMaster> list = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream());
                CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
                        .parse(reader)) {
            for (CSVRecord record : parser) {
                WorkerMaster entity = new WorkerMaster();
                String boardIdFromFile = getOptionalValue(record, "board_id");
                entity.setBoardId(resolveBoardId(boardId, boardIdFromFile));
                entity.setWorkerNameMarathi(getValue(record,
                        "worker_name_marathi",
                        "workers_name_marathi",
                        "worker_name",
                        "labor_name_marathi"));
                entity.setWorkerNameEnglish(getOptionalValue(record,
                        "worker_name_english",
                        "workers_name_english",
                        "labor_name_english"));
                entity.setWitnessName1(getOptionalValue(record, "witness_name_1", "wit_name_1"));
                entity.setWitnessName2(getOptionalValue(record, "witness_name_2", "wit_name_2"));
                entity.setToliNumber(getOptionalValue(record, "toli_number", "toli_id"));
                entity.setRegistrationNumber(getValue(record, "registration_number", "registration_no"));
                entity.setPanNumber(getOptionalValue(record, "pan_number", "pan"));
                entity.setNationality(getOptionalValue(record, "nationality"));
                entity.setMotherName(getOptionalValue(record, "mother_name"));
                entity.setMobileNumber(getOptionalValue(record, "mobile_number", "mobile_no", "phone_number"));
                entity.setMobileNumber1(getOptionalValue(record, "mobile_number_1", "alternate_mobile_number"));
                entity.setMaritalStatus(getOptionalValue(record, "marital_status"));
                entity.setIfscCode(getOptionalValue(record, "ifsc_code"));
                entity.setBranchAddress(getOptionalValue(record, "branch_address"));
                entity.setBankName(getOptionalValue(record, "bank_name"));
                String age = getOptionalValue(record, "age");
                if (age != null) {
                    entity.setAge(parseInteger(age));
                }
                entity.setAddress1(getOptionalValue(record, "address1", "address_line_1"));
                entity.setAddress2(getOptionalValue(record, "address2", "address_line_2"));
                entity.setAccountNumber(getOptionalValue(record, "account_number", "bank_account_number"));
                entity.setAadharNumber(getValue(record,
                        "aadhar_number",
                        "aadhaar_number",
                        "aadhar_no",
                        "aadhaar_no",
                        "uid"));
                String status = getOptionalValue(record, "status", "worker_status", "current_status", "state");
                if (status != null) {
                    entity.setStatus(status);
                }
                LocalDateTime createdAt = parseDateTime(getOptionalValue(record, "created_at"));
                if (createdAt != null) {
                    entity.setCreatedAt(createdAt);
                }
                LocalDateTime updatedAt = parseDateTime(getOptionalValue(record, "updated_at"));
                if (updatedAt != null) {
                    entity.setUpdatedAt(updatedAt);
                }
                list.add(entity);
            }
        }
        return list;
    }

    public static List<WorkerMaster> parseWorkerXls(MultipartFile file, String boardId, String employerId)
            throws Exception {
        List<WorkerMaster> list = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return list;
            }
            Row headerRow = rowIterator.next();
            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow);
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowBlank(row)) {
                    continue;
                }
                WorkerMaster entity = new WorkerMaster();
                String boardIdFromFile = getOptionalCellValue(row, headerIndex, "board_id");
                entity.setBoardId(resolveBoardId(boardId, boardIdFromFile));
                entity.setWorkerNameMarathi(requireCellValue(row, headerIndex,
                        "worker_name_marathi",
                        "workers_name_marathi",
                        "worker_name"));
                entity.setWorkerNameEnglish(getOptionalCellValue(row, headerIndex,
                        "worker_name_english",
                        "workers_name_english"));
                entity.setWitnessName1(getOptionalCellValue(row, headerIndex, "witness_name_1", "wit_name_1"));
                entity.setWitnessName2(getOptionalCellValue(row, headerIndex, "witness_name_2", "wit_name_2"));
                entity.setToliNumber(getOptionalCellValue(row, headerIndex, "toli_number", "toli_id"));
                entity.setRegistrationNumber(
                        requireCellValue(row, headerIndex, "registration_number", "registration_no"));
                entity.setPanNumber(getOptionalCellValue(row, headerIndex, "pan_number", "pan"));
                entity.setNationality(getOptionalCellValue(row, headerIndex, "nationality"));
                entity.setMotherName(getOptionalCellValue(row, headerIndex, "mother_name"));
                entity.setMobileNumber(getOptionalCellValue(row, headerIndex, "mobile_number", "phone_number"));
                entity.setMobileNumber1(getOptionalCellValue(row, headerIndex, "mobile_number_1"));
                entity.setMaritalStatus(getOptionalCellValue(row, headerIndex, "marital_status"));
                entity.setIfscCode(getOptionalCellValue(row, headerIndex, "ifsc_code"));
                entity.setBranchAddress(getOptionalCellValue(row, headerIndex, "branch_address"));
                entity.setBankName(getOptionalCellValue(row, headerIndex, "bank_name"));
                String age = getOptionalCellValue(row, headerIndex, "age");
                if (age != null) {
                    entity.setAge(parseInteger(age));
                }
                entity.setAddress1(getOptionalCellValue(row, headerIndex, "address1", "address_line_1"));
                entity.setAddress2(getOptionalCellValue(row, headerIndex, "address2", "address_line_2"));
                entity.setAccountNumber(getOptionalCellValue(row, headerIndex, "account_number", "bank_account"));
                entity.setAadharNumber(requireCellValue(row, headerIndex,
                        "aadhar_number",
                        "aadhaar_number",
                        "aadhar_no",
                        "aadhaar_no",
                        "uid"));
                String status = getOptionalCellValue(row, headerIndex, "status", "worker_status", "current_status");
                if (status != null) {
                    entity.setStatus(status);
                }
                LocalDateTime createdAt = parseDateTime(getOptionalCellValue(row, headerIndex, "created_at"));
                if (createdAt != null) {
                    entity.setCreatedAt(createdAt);
                }
                LocalDateTime updatedAt = parseDateTime(getOptionalCellValue(row, headerIndex, "updated_at"));
                if (updatedAt != null) {
                    entity.setUpdatedAt(updatedAt);
                }
                list.add(entity);
            }
        }
        return list;
    }

    public static List<BoardMaster> parseBoardCsv(MultipartFile file, String boardId, String employerId)
            throws Exception {
        List<BoardMaster> list = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream());
                CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
                        .parse(reader)) {
            for (CSVRecord record : parser) {
                BoardMaster entity = new BoardMaster();
                String boardIdFromFile = getOptionalValue(record, "board_id");
                entity.setBoardId(resolveBoardId(boardId, boardIdFromFile));
                entity.setBoardName(getValue(record, "board_name"));
                entity.setBoardCode(getOptionalValue(record, "board_code"));
                entity.setStateName(getOptionalValue(record, "state_name"));
                entity.setDistrictName(getOptionalValue(record, "district_name"));
                entity.setAddress(getOptionalValue(record, "address"));
                // ... map other fields as needed ...
                list.add(entity);
            }
        }
        return list;
    }

    public static List<BoardMaster> parseBoardXls(MultipartFile file, String boardId, String employerId)
            throws Exception {
        List<BoardMaster> list = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                BoardMaster entity = new BoardMaster();
                String boardIdFromFile = getCellValue(row, 0);
                entity.setBoardId(resolveBoardId(boardId, boardIdFromFile));
                entity.setBoardName(requireCellValue(row, 1, "board_name"));
                entity.setBoardCode(getCellValue(row, 2));
                entity.setStateName(getCellValue(row, 3));
                entity.setDistrictName(getCellValue(row, 4));
                entity.setAddress(getCellValue(row, 5));
                // ... map other fields as needed ...
                list.add(entity);
            }
        }
        return list;
    }

    public static List<EmployerMaster> parseEmployerCsv(MultipartFile file, String boardId, String employerId)
            throws Exception {
        List<EmployerMaster> list = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream());
                CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
                        .parse(reader)) {
            for (CSVRecord record : parser) {
                EmployerMaster entity = new EmployerMaster();
                String boardIdFromFile = getOptionalValue(record, "board_id");
                entity.setBoardId(resolveBoardId(boardId, boardIdFromFile));
                entity.setRegistrationNo(getValue(record, "registration_number", "registration_no"));
                entity.setEstablishmentName(getValue(record, "establishment_name", "employer_name"));
                entity.setEmployerName(getOptionalValue(record, "employer_name"));
                entity.setAddress(getOptionalValue(record, "address"));
                entity.setOwnerName(getOptionalValue(record, "owner_name"));
                entity.setMobileNumber(getOptionalValue(record, "mobile_number"));
                entity.setEmailId(getOptionalValue(record, "email_id"));
                entity.setAadharNumber(getOptionalValue(record, "aadhar_number"));
                entity.setAadhaarNumber(getOptionalValue(record, "aadhaar_number", "aadhar_number"));
                entity.setPanNumber(getOptionalValue(record, "pan_number"));
                entity.setTanNumber(getOptionalValue(record, "tan_number"));
                entity.setVirtualBankAccountNumber(getOptionalValue(record, "virtual_bank_account_number"));
                String status = getOptionalValue(record, "status");
                if (status != null) {
                    entity.setStatus(status);
                }
                LocalDateTime createdAt = parseDateTime(getOptionalValue(record, "created_at"));
                if (createdAt != null) {
                    entity.setCreatedAt(createdAt);
                }
                LocalDateTime updatedAt = parseDateTime(getOptionalValue(record, "updated_at"));
                if (updatedAt != null) {
                    entity.setUpdatedAt(updatedAt);
                }
                list.add(entity);
            }
        }
        return list;
    }

    public static List<EmployerMaster> parseEmployerXls(MultipartFile file, String boardId, String employerId)
            throws Exception {
        List<EmployerMaster> list = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                EmployerMaster entity = new EmployerMaster();
                String boardIdFromFile = getCellValue(row, 14);
                entity.setBoardId(resolveBoardId(boardId, boardIdFromFile));
                entity.setRegistrationNo(requireCellValue(row, 1, "registration_number"));
                entity.setEstablishmentName(requireCellValue(row, 2, "establishment_name"));
                entity.setEmployerName(getCellValue(row, 4));
                entity.setAddress(getCellValue(row, 3));
                entity.setOwnerName(getCellValue(row, 16));
                entity.setMobileNumber(getCellValue(row, 5));
                entity.setEmailId(getCellValue(row, 6));
                entity.setAadharNumber(getCellValue(row, 7));
                entity.setAadhaarNumber(getCellValue(row, 15));
                entity.setPanNumber(getCellValue(row, 8));
                entity.setTanNumber(getCellValue(row, 9));
                entity.setVirtualBankAccountNumber(getCellValue(row, 10));
                String status = getCellValue(row, 11);
                if (status != null) {
                    entity.setStatus(status);
                }
                LocalDateTime createdAt = parseDateTime(getCellValue(row, 12));
                if (createdAt != null) {
                    entity.setCreatedAt(createdAt);
                }
                LocalDateTime updatedAt = parseDateTime(getCellValue(row, 13));
                if (updatedAt != null) {
                    entity.setUpdatedAt(updatedAt);
                }
                list.add(entity);
            }
        }
        return list;
    }

    private static String getValue(CSVRecord record, String... headerOptions) {
        for (String header : headerOptions) {
            if (header != null && record.isMapped(header) && record.isSet(header)) {
                String value = record.get(header);
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
        }
        throw new IllegalArgumentException(
                "Missing required column. Expected one of " + String.join(", ", headerOptions));
    }

    private static String getOptionalValue(CSVRecord record, String... headerOptions) {
        for (String header : headerOptions) {
            if (header != null && record.isMapped(header) && record.isSet(header)) {
                String value = record.get(header);
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
        }
        return null;
    }

    private static String resolveBoardId(String contextBoardId, String fileBoardId) {
        String resolved = null;
        if (contextBoardId != null && !contextBoardId.isBlank()) {
            resolved = contextBoardId;
        } else if (fileBoardId != null && !fileBoardId.isBlank()) {
            resolved = fileBoardId;
        }

        if (resolved == null || resolved.isBlank()) {
            throw new IllegalArgumentException("board_id is required either in the upload file or user context");
        }
        // Context board ID always wins; file value is informative only.
        return resolved;
    }

    private static LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return LocalDateTime.parse(trimmed);
        } catch (DateTimeParseException ignored) {
        }
        try {
            LocalDate date = LocalDate.parse(trimmed);
            return date.atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }
        throw new IllegalArgumentException("Unable to parse date value: " + value);
    }

    private static String getCellValue(Row row, int cellIndex) {
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }
        String value = DATA_FORMATTER.formatCellValue(cell);
        return value != null && !value.isBlank() ? value.trim() : null;
    }

    private static String requireCellValue(Row row, int cellIndex, String columnName) {
        String value = getCellValue(row, cellIndex);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing required column '" + columnName + "' at index " + cellIndex + " in Excel file");
        }
        return value;
    }

    private static Map<String, Integer> buildHeaderIndex(Row headerRow) {
        Map<String, Integer> headerIndex = new HashMap<>();
        if (headerRow == null) {
            return headerIndex;
        }
        short lastCellNum = headerRow.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            String header = getCellValue(headerRow, i);
            if (header == null || header.isBlank()) {
                continue;
            }
            String normalized = normalizeHeader(header);
            headerIndex.putIfAbsent(normalized, i);
        }
        return headerIndex;
    }

    private static String normalizeHeader(String header) {
        if (header == null) {
            return null;
        }
        return header.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    private static String getOptionalCellValue(Row row, Map<String, Integer> headerIndex, String... headerOptions) {
        if (headerIndex == null || headerIndex.isEmpty()) {
            return null;
        }
        for (String option : headerOptions) {
            if (option == null) {
                continue;
            }
            Integer idx = headerIndex.get(normalizeHeader(option));
            if (idx != null) {
                return getCellValue(row, idx);
            }
        }
        return null;
    }

    private static String requireCellValue(Row row, Map<String, Integer> headerIndex, String... headerOptions) {
        String value = getOptionalCellValue(row, headerIndex, headerOptions);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing required column. Expected one of " + String.join(", ", headerOptions));
        }
        return value;
    }

    private static boolean isRowBlank(Row row) {
        if (row == null) {
            return true;
        }
        short lastCellNum = row.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            String value = getCellValue(row, i);
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Unable to parse integer value: " + value, ex);
        }
    }

}
