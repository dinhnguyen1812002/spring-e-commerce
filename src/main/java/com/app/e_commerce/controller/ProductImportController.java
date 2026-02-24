package com.app.e_commerce.controller;

// import com.app.e_commerce.DTO.*;
// import com.app.e_commerce.services.ProductImportService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;
//
// import java.io.IOException;
// import java.util.List;
// import java.util.Map;
//
// @RestController
// @RequestMapping("/api/import")
// @CrossOrigin(origins = "*")
// public class ProductImportController {
//
// @Autowired
// private ProductImportService productImportService;
//
// @PostMapping("/upload")
// public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file)
// {
// try {
// if (file.isEmpty()) {
// return ResponseEntity.badRequest().body("File is empty");
// }
//
// String fileName = file.getOriginalFilename();
// String fileType = getFileType(fileName);
//
// List<String> sheets = productImportService.getAvailableSheets(file,
// fileType);
//
// return ResponseEntity.ok().body(Map.of(
// "fileName", fileName,
// "fileType", fileType,
// "sheets", sheets
// ));
//
// } catch (IOException e) {
// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
// .body("Error processing file: " + e.getMessage());
// }
// }
//
// @PostMapping("/preview")
// public ResponseEntity<?> previewImport(@RequestParam("file") MultipartFile
// file,
// @RequestParam("sheetName") String sheetName) {
// try {
// String fileType = getFileType(file.getOriginalFilename());
// ImportPreviewDTO preview = productImportService.previewImport(file, fileType,
// sheetName);
//
// return ResponseEntity.ok(preview);
//
// } catch (IOException e) {
// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
// .body("Error previewing file: " + e.getMessage());
// }
// }
//
// @PostMapping("/preview-google-sheets")
// public ResponseEntity<?> previewGoogleSheetsImport(@RequestBody Map<String,
// String> request) {
// try {
// String spreadsheetId = request.get("spreadsheetId");
// String sheetName = request.get("sheetName");
//
// if (spreadsheetId == null || sheetName == null) {
// return ResponseEntity.badRequest().body("spreadsheetId and sheetName are
// required");
// }
//
// ImportPreviewDTO preview =
// productImportService.previewGoogleSheetsImport(spreadsheetId, sheetName);
//
// return ResponseEntity.ok(preview);
//
// } catch (Exception e) {
// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
// .body("Error previewing Google Sheets: " + e.getMessage());
// }
// }
//
// @GetMapping("/mapping-info")
// public ResponseEntity<ColumnMappingDTO> getColumnMappingInfo(@RequestParam
// List<String> columns) {
// ColumnMappingDTO mappingInfo =
// productImportService.getColumnMappingInfo(columns);
// return ResponseEntity.ok(mappingInfo);
// }
//
// @PostMapping("/start")
// public ResponseEntity<?> startImport(@RequestBody ProductImportRequestDTO
// request) {
// try {
// ProductImportResponseDTO response =
// productImportService.startImport(request);
// return ResponseEntity.ok(response);
//
// } catch (Exception e) {
// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
// .body("Error starting import: " + e.getMessage());
// }
// }
//
// @GetMapping("/status/{importId}")
// public ResponseEntity<?> getImportStatus(@PathVariable Long importId) {
// try {
// ProductImportResponseDTO response =
// productImportService.getImportStatus(importId);
// return ResponseEntity.ok(response);
//
// } catch (RuntimeException e) {
// return ResponseEntity.notFound().build();
// } catch (Exception e) {
// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
// .body("Error getting import status: " + e.getMessage());
// }
// }
//
// @GetMapping("/all")
// public ResponseEntity<List<ProductImportResponseDTO>> getAllImports() {
// List<ProductImportResponseDTO> imports =
// productImportService.getAllImports();
// return ResponseEntity.ok(imports);
// }
//
// @PostMapping("/cancel/{importId}")
// public ResponseEntity<?> cancelImport(@PathVariable Long importId) {
// try {
// productImportService.cancelImport(importId);
// return ResponseEntity.ok().body("Import cancelled successfully");
//
// } catch (Exception e) {
// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
// .body("Error cancelling import: " + e.getMessage());
// }
// }
//
// @GetMapping("/error-report/{importId}")
// public ResponseEntity<?> downloadErrorReport(@PathVariable Long importId) {
// try {
// byte[] report = productImportService.downloadErrorReport(importId);
// return ResponseEntity.ok()
// .header("Content-Disposition", "attachment; filename=import-errors-" +
// importId + ".xlsx")
// .body(report);
//
// } catch (UnsupportedOperationException e) {
// return ResponseEntity.notImplemented().body("Error report download not yet
// implemented");
// } catch (Exception e) {
// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
// .body("Error generating error report: " + e.getMessage());
// }
// }
//
// @PostMapping("/extract-google-sheets-id")
// public ResponseEntity<?> extractGoogleSheetsId(@RequestBody Map<String,
// String> request) {
// try {
// String url = request.get("url");
// if (url == null) {
// return ResponseEntity.badRequest().body("URL is required");
// }
//
// // This would use the GoogleSheetsService to extract the ID
// // For now, return a placeholder
// return ResponseEntity.ok().body(Map.of(
// "spreadsheetId", "extracted-id",
// "message", "Google Sheets ID extraction not yet fully implemented"
// ));
//
// } catch (Exception e) {
// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
// .body("Error extracting Google Sheets ID: " + e.getMessage());
// }
// }
//
// private String getFileType(String fileName) {
// if (fileName == null) {
// return "UNKNOWN";
// }
//
// String extension = fileName.substring(fileName.lastIndexOf(".") +
// 1).toLowerCase();
//
// switch (extension) {
// case "xlsx":
// case "xls":
// return "EXCEL";
// default:
// return "UNKNOWN";
// }
// }
// }
