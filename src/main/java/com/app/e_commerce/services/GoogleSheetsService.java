package com.app.e_commerce.services;

import com.app.e_commerce.DTO.ImportPreviewDTO;
import com.google.api.client.auth.oauth2.Credential;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;

import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.GsonFactoryBean;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

//@Service
//public class GoogleSheetsService {
//
//    private static final String APPLICATION_NAME = "E-Commerce Product Import";
//    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
////    private static final JsonFactory JSON_FACTORY = GsonFac
//    private static final String TOKENS_DIRECTORY_PATH = "tokens";
//    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
//    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
//
//    @Value("${google.sheets.credentials.path:}")
//    private String credentialsPath;
//
//    private static final int PREVIEW_ROWS = 10;
//
//    public List<String> getSheetNames(String spreadsheetId) throws IOException, GeneralSecurityException {
//        List<String> sheetNames = new ArrayList<>();
//
//        Sheets service = getSheetsService();
//        var spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
//
//        for (var sheet : spreadsheet.getSheets()) {
//            sheetNames.add(sheet.getProperties().getTitle());
//        }
//
//        return sheetNames;
//    }
//
//    public ImportPreviewDTO parseSheetPreview(String spreadsheetId, String sheetName) throws IOException, GeneralSecurityException {
//        ImportPreviewDTO preview = new ImportPreviewDTO();
//        List<Map<String, Object>> sampleData = new ArrayList<>();
//        List<String> columnHeaders = new ArrayList<>();
//        Map<String, String> suggestedMappings = new HashMap<>();
//        List<String> validationErrors = new ArrayList<>();
//
//        Sheets service = getSheetsService();
//
//        // Get the range for the sheet
//        String range = sheetName + "!A:Z"; // Assuming max 26 columns
//        ValueRange response = service.spreadsheets().values()
//                .get(spreadsheetId, range)
//                .execute();
//
//        List<List<Object>> values = response.getValues();
//
//        if (values == null || values.isEmpty()) {
//            throw new IllegalArgumentException("No data found in sheet: " + sheetName);
//        }
//
//        // Get header row
//        List<Object> headerRow = values.get(0);
//        for (Object header : headerRow) {
//            columnHeaders.add(header.toString());
//        }
//
//        // Generate suggested mappings
//        suggestedMappings = generateSuggestedMappings(columnHeaders);
//
//        // Parse sample data rows
//        int rowCount = 0;
//        for (int i = 1; i < Math.min(values.size(), PREVIEW_ROWS + 1); i++) {
//            List<Object> row = values.get(i);
//            Map<String, Object> rowData = new HashMap<>();
//            boolean hasData = false;
//
//            for (int j = 0; j < columnHeaders.size() && j < row.size(); j++) {
//                Object cellValue = row.get(j);
//                rowData.put(columnHeaders.get(j), cellValue);
//
//                if (cellValue != null && !cellValue.toString().trim().isEmpty()) {
//                    hasData = true;
//                }
//            }
//
//            if (hasData) {
//                sampleData.add(rowData);
//                rowCount++;
//            }
//        }
//
//        preview.setSampleData(sampleData);
//        preview.setColumnHeaders(columnHeaders);
//        preview.setSuggestedMappings(suggestedMappings);
//        preview.setTotalRows(values.size() - 1); // Exclude header row
//        preview.setValidationErrors(validationErrors);
//        preview.setHasErrors(!validationErrors.isEmpty());
//
//        return preview;
//    }
//
//    public List<Map<String, Object>> parseSheetData(String spreadsheetId, String sheetName,
//                                                    Map<String, String> columnMappings) throws IOException, GeneralSecurityException {
//        List<Map<String, Object>> data = new ArrayList<>();
//
//        Sheets service = getSheetsService();
//
//        // Get the range for the sheet
//        String range = sheetName + "!A:Z"; // Assuming max 26 columns
//        ValueRange response = service.spreadsheets().values()
//                .get(spreadsheetId, range)
//                .execute();
//
//        List<List<Object>> values = response.getValues();
//
//        if (values == null || values.isEmpty()) {
//            throw new IllegalArgumentException("No data found in sheet: " + sheetName);
//        }
//
//        // Get header row
//        List<Object> headerRow = values.get(0);
//        List<String> columnHeaders = new ArrayList<>();
//        for (Object header : headerRow) {
//            columnHeaders.add(header.toString());
//        }
//
//        // Create column index mapping
//        Map<String, Integer> columnIndexMap = new HashMap<>();
//        for (int i = 0; i < columnHeaders.size(); i++) {
//            columnIndexMap.put(columnHeaders.get(i), i);
//        }
//
//        // Parse data rows
//        for (int i = 1; i < values.size(); i++) {
//            List<Object> row = values.get(i);
//            Map<String, Object> rowData = new HashMap<>();
//            boolean hasData = false;
//
//            for (Map.Entry<String, String> mapping : columnMappings.entrySet()) {
//                String sheetColumn = mapping.getKey();
//                String productField = mapping.getValue();
//
//                Integer columnIndex = columnIndexMap.get(sheetColumn);
//                if (columnIndex != null && columnIndex < row.size()) {
//                    Object cellValue = row.get(columnIndex);
//                    rowData.put(productField, cellValue);
//
//                    if (cellValue != null && !cellValue.toString().trim().isEmpty()) {
//                        hasData = true;
//                    }
//                }
//            }
//
//            if (hasData) {
//                data.add(rowData);
//            }
//        }
//
//        return data;
//    }
//
//    public String extractSpreadsheetId(String url) {
//        // Extract spreadsheet ID from Google Sheets URL
//        // URL format: https://docs.google.com/spreadsheets/d/SPREADSHEET_ID/edit#gid=0
//        if (url.contains("/d/")) {
//            String[] parts = url.split("/d/");
//            if (parts.length > 1) {
//                String idPart = parts[1];
//                if (idPart.contains("/")) {
//                    return idPart.split("/")[0];
//                }
//                return idPart;
//            }
//        }
//        throw new IllegalArgumentException("Invalid Google Sheets URL: " + url);
//    }
//
//    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        Credential credential = getCredentials(HTTP_TRANSPORT);
//        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//    }
//
//    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
//        // Load client secrets
//        InputStream in = GoogleSheetsService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//        if (in == null) {
//            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//        }
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//        // Build flow and trigger user authorization request
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//                .setAccessType("offline")
//                .build();
//        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
//        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//    }
//
//    private Map<String, String> generateSuggestedMappings(List<String> columnHeaders) {
//        Map<String, String> mappings = new HashMap<>();
//
//        // Common mapping patterns
//        Map<String, String> commonMappings = new HashMap<>();
//        commonMappings.put("name", "name");
//        commonMappings.put("product name", "name");
//        commonMappings.put("title", "name");
//        commonMappings.put("product title", "name");
//        commonMappings.put("price", "price");
//        commonMappings.put("cost", "price");
//        commonMappings.put("amount", "price");
//        commonMappings.put("description", "description");
//        commonMappings.put("desc", "description");
//        commonMappings.put("details", "description");
//        commonMappings.put("category", "category");
//        commonMappings.put("cat", "category");
//        commonMappings.put("type", "category");
//        commonMappings.put("stock", "stock");
//        commonMappings.put("quantity", "stock");
//        commonMappings.put("qty", "stock");
//        commonMappings.put("image", "image");
//        commonMappings.put("photo", "image");
//        commonMappings.put("picture", "image");
//        commonMappings.put("url", "image");
//
//        for (String header : columnHeaders) {
//            String lowerHeader = header.toLowerCase().trim();
//
//            // Direct match
//            if (commonMappings.containsKey(lowerHeader)) {
//                mappings.put(header, commonMappings.get(lowerHeader));
//                continue;
//            }
//
//            // Partial match
//            for (Map.Entry<String, String> entry : commonMappings.entrySet()) {
//                if (lowerHeader.contains(entry.getKey()) || entry.getKey().contains(lowerHeader)) {
//                    mappings.put(header, entry.getValue());
//                    break;
//                }
//            }
//        }
//
//        return mappings;
//    }
//}
//