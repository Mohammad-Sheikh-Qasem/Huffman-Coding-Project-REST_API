// Developed by: Mohammad Sheikh Qasem
package com.huffman.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huffman.model.CompressionResult;
import com.huffman.model.HuffmanNode;
import com.huffman.service.CompressionService;
import com.huffman.service.FrequencyAnalysisService;
import com.huffman.service.HuffmanService;
import com.huffman.service.HuffmanTreeService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;


@RestController
@RequestMapping("/api/huffman")
@CrossOrigin(origins = "*")
public class HuffmanController{

    private final HuffmanService huffmanService;
    private final FrequencyAnalysisService frequencyService;
    private final HuffmanTreeService treeService;
    private final CompressionService compressionService;
    private final ObjectMapper objectMapper;


    public HuffmanController(HuffmanService huffmanService,
                             FrequencyAnalysisService frequencyService,
                             HuffmanTreeService treeService,
                             CompressionService compressionService,
                             ObjectMapper objectMapper){

        this.huffmanService = huffmanService;
        this.frequencyService = frequencyService;
        this.treeService = treeService;
        this.compressionService = compressionService;
        this.objectMapper = objectMapper;
    }


    /** Generic text input wrapper. */
    public record TextRequest(String text){}

    /** Extended mode input (text + k pairs). */
    public record ExtendedRequest(String text, int k){}

    /** Benchmark request (text + list of k values to test). */
    public record BenchmarkRequest(String text, List<Integer> kValues){}


    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health(){

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",    "UP");
        body.put("service",   "Huffman Coding Compression System");
        body.put("timestamp", new Date().toString());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/compress")
    public ResponseEntity<?> compress(@RequestBody TextRequest request){

        try{
            CompressionResult result = huffmanService.compressAndDecompress(request.text());
            return ResponseEntity.ok(buildResponse(result, false));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }


    @PostMapping(value = "/compress/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> compressFile(@RequestParam("file") MultipartFile file){

        try {
            // ── Validate the uploaded file ─────────────────────
            String validationError = validateFile(file);
            if (validationError != null){
                return ResponseEntity.badRequest().body(errorBody(validationError));
            }

            // ── Read file content as UTF-8 string ─────────────
            String text = new String(file.getBytes(), StandardCharsets.UTF_8).trim();

            if (text.isBlank()){
                return ResponseEntity.badRequest().body(errorBody("The uploaded file is empty"));
            }

            // ── Run the standard pipeline ──────────────────────
            CompressionResult result = huffmanService.compressAndDecompress(text);

            // ── Build response with file metadata ──────────────
            Map<String, Object> response = buildResponse(result, false);
            response.put("fileName",file.getOriginalFilename());
            response.put("fileSizeBytes",file.getSize());

            return ResponseEntity.ok(response);

        } catch (IOException e){

            return ResponseEntity.internalServerError()
                    .body(errorBody("Failed to read file: " + e.getMessage()));

        } catch (Exception e){
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    /** POST /api/huffman/compress/download — returns a self-contained .huf file */
    @PostMapping(value = "/compress/download", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> compressDownload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "k", defaultValue = "1") int k){

        try {
            String validationError = validateFile(file);
            if (validationError != null){
                return ResponseEntity.badRequest().body(errorBody(validationError));
            }

            if (k < 1){
                return ResponseEntity.badRequest().body(errorBody("k must be at least 1"));
            }

            String text = new String(file.getBytes(), StandardCharsets.UTF_8).trim();
            if (text.isBlank()){
                return ResponseEntity.badRequest().body(errorBody("The uploaded file is empty"));
            }

            // Build tree and compress (extended when k > 1)
            Map<Character, Integer> frequencies = frequencyService.analyse(text);
            HuffmanNode root = (k == 1)
                    ? treeService.buildTree(frequencies)
                    : treeService.buildTreeExtended(frequencies, k, new ArrayList<>());
            Map<Character, String> codes = treeService.generateCodes(root);
            int[] compressedData = compressionService.compress(text, codes);

            // Serialize to .huf JSON
            String originalFilename = file.getOriginalFilename();
            byte[] hufBytes = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(buildHufPayload(originalFilename, frequencies, compressedData, k));

            String hufFilename = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(0, originalFilename.lastIndexOf('.')) + ".huf"
                    : "compressed.huf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(hufFilename).build());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(hufBytes);

        } catch (IOException e){
            return ResponseEntity.internalServerError().body(errorBody("Failed to read file: " + e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    /** POST /api/huffman/decompress/download — accepts a .huf file, returns the original .txt */
    @PostMapping(value = "/decompress/download", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> decompressDownload(@RequestParam("file") MultipartFile file){

        try {
            if (file == null || file.isEmpty()){
                return ResponseEntity.badRequest().body(errorBody("No file was uploaded"));
            }
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".huf")){
                return ResponseEntity.badRequest().body(errorBody("Only .huf files are accepted. Received: " + filename));
            }

            // Parse the .huf JSON payload
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(file.getBytes(), Map.class);

            String originalFilename = (String) payload.get("originalFilename");
            int padBits = ((Number) payload.get("padBits")).intValue();
            int k = payload.containsKey("k") ? ((Number) payload.get("k")).intValue() : 1;

            @SuppressWarnings("unchecked")
            Map<String, Object> freqStrKeys = (Map<String, Object>) payload.get("frequencies");

            @SuppressWarnings("unchecked")
            List<Integer> dataList = (List<Integer>) payload.get("data");

            // Convert string keys back to characters
            Map<Character, Integer> frequencies = new LinkedHashMap<>();
            freqStrKeys.forEach((key, v) -> {
                char ch = "SPACE".equals(key) ? ' ' : "COMMA".equals(key) ? ',' : key.charAt(0);
                frequencies.put(ch, ((Number) v).intValue());
            });

            // Reconstruct int[] = [padBits, data...]
            int[] compressedData = new int[1 + dataList.size()];
            compressedData[0] = padBits;
            for (int i = 0; i < dataList.size(); i++){
                compressedData[i + 1] = dataList.get(i);
            }

            // Rebuild the exact same tree that was used during compression
            HuffmanNode root = (k == 1)
                    ? treeService.buildTree(frequencies)
                    : treeService.buildTreeExtended(frequencies, k, new ArrayList<>());
            String decompressed = compressionService.decompress(compressedData, root);

            // Derive output filename
            String outFilename = (originalFilename != null && !originalFilename.isBlank())
                    ? originalFilename
                    : "decompressed.txt";
            if (!outFilename.toLowerCase().endsWith(".txt")){
                outFilename = outFilename.contains(".")
                        ? outFilename.substring(0, outFilename.lastIndexOf('.')) + ".txt"
                        : outFilename + ".txt";
            }

            byte[] textBytes = decompressed.getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(outFilename).build());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                    .body(textBytes);

        } catch (IOException e){
            return ResponseEntity.internalServerError().body(errorBody("Failed to read file: " + e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    @PostMapping("/compress/extended")
    public ResponseEntity<?> compressExtended(@RequestBody ExtendedRequest request){

        try {
            CompressionResult result = huffmanService
                    .compressAndDecompressExtended(request.text(), request.k());
            return ResponseEntity.ok(buildResponse(result, true));

        } catch (Exception e){
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }


    @PostMapping(value = "/compress/extended/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> compressExtendedFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "k", defaultValue = "1") int k){

        try {
            // ── Validate the uploaded file ─────────────────────
            String validationError = validateFile(file);
            if (validationError != null) {
                return ResponseEntity.badRequest().body(errorBody(validationError));
            }

            if (k < 1){
                return ResponseEntity.badRequest().body(errorBody("k must be at least 1"));
            }

            // ── Read file content ──────────────────────────────
            String text = new String(file.getBytes(), StandardCharsets.UTF_8).trim();

            if (text.isBlank()){
                return ResponseEntity.badRequest().body(errorBody("The uploaded file is empty"));
            }

            // ── Run the extended pipeline ──────────────────────
            CompressionResult result = huffmanService.compressAndDecompressExtended(text, k);

            // ── Build response with file metadata ──────────────
            Map<String, Object> response = buildResponse(result, true);
            response.put("fileName",file.getOriginalFilename());
            response.put("fileSizeBytes",file.getSize());

            return ResponseEntity.ok(response);

        } catch (IOException e){
            return ResponseEntity.internalServerError()
                    .body(errorBody("Failed to read file: " + e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    @PostMapping("/benchmark")
    public ResponseEntity<?> benchmark(@RequestBody BenchmarkRequest request){

        try{
            List<CompressionResult> results = huffmanService.benchmarkKValues(request.text(), request.kValues());

            List<Map<String, Object>> summaries = new ArrayList<>();

            for (CompressionResult r : results){

                Map<String, Object> summary = new LinkedHashMap<>();

                summary.put("k",r.getKValue());
                summary.put("originalBytes",r.getOriginalBytes());
                summary.put("compressedBytes",r.getCompressedBytes());
                summary.put("compressionRatio",r.getCompressionRatio());
                summary.put("compressionPercent",r.getCompressionPercent());
                summary.put("roundTripSuccess",r.isRoundTripSuccess());
                summary.put("heapIterations",r.getHeapSteps().size());
                summaries.add(summary);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("inputText", request.text());
            response.put("results",   summaries);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }


    /** POST /api/huffman/frequencies — Body: { "text": "..." } */
    @PostMapping("/frequencies")
    public ResponseEntity<?> frequencies(@RequestBody TextRequest request){

        try{
            Map<Character, Integer> freqs = huffmanService.getFrequencies(request.text());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("text",           request.text());
            body.put("frequencies",    convertKeyToString(freqs));
            body.put("formattedTable", frequencyService.formatFrequencyTable(freqs));
            body.put("distinctChars",  freqs.size());
            body.put("acceptedChars",  frequencyService.countAcceptedChars(request.text()));
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    /** POST /api/huffman/codes — Body: { "text": "..." } */
    @PostMapping("/codes")
    public ResponseEntity<?> codes(@RequestBody TextRequest request){

        try{

            Map<Character, String> codes = huffmanService.getHuffmanCodes(request.text());
            Map<String, Object> body = new LinkedHashMap<>();

            body.put("text",request.text());
            body.put("huffmanCodes",convertKeyToString(codes));
            body.put("formattedTable",treeService.formatCodeTable(codes));

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    /** POST /api/huffman/tree — Body: { "text": "..." } */
    @PostMapping("/tree")
    public ResponseEntity<?> tree(@RequestBody TextRequest request){

        try{

            String diagram = huffmanService.getTreeDiagram(request.text());
            Map<String, Object> body = new LinkedHashMap<>();

            body.put("text",    request.text());
            body.put("diagram", diagram);

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }


    /** Builds the Map that gets serialized as the .huf JSON file. */
    private Map<String, Object> buildHufPayload(String originalFilename,
                                                Map<Character, Integer> frequencies,
                                                int[] compressedData,
                                                int k){
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("originalFilename", originalFilename);
        payload.put("k", k);

        // Convert char keys to string labels (SPACE / COMMA / char)
        Map<String, Integer> freqStr = new LinkedHashMap<>();
        frequencies.forEach((ch, freq) -> {
            String label = (ch == ' ') ? "SPACE" : (ch == ',') ? "COMMA" : String.valueOf(ch);
            freqStr.put(label, freq);
        });
        payload.put("frequencies", freqStr);

        // Split padBits (index 0) from the actual data (index 1..n)
        payload.put("padBits", compressedData[0]);
        List<Integer> dataList = new ArrayList<>(compressedData.length - 1);
        for (int i = 1; i < compressedData.length; i++){
            dataList.add(compressedData[i]);
        }
        payload.put("data", dataList);
        return payload;
    }

    private String validateFile(MultipartFile file){

        if (file == null || file.isEmpty()){

            return "No file was uploaded";
        }
        String filename = file.getOriginalFilename();

        if (filename == null || !filename.toLowerCase().endsWith(".txt")){

            return "Only .txt files are supported. Received: " + filename;
        }

        long maxBytes = 5L * 1024 * 1024; // 5 MB

        if (file.getSize() > maxBytes){

            return "File is too large. Maximum allowed size is 5 MB";
        }
        return null; // valid
    }

    /** Builds the full JSON response map from a CompressionResult. */
    private Map<String, Object> buildResponse(CompressionResult r, boolean includeSteps){

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("characterFrequencies",convertKeyToString(r.getCharacterFrequencies()));
        body.put("huffmanCodes",convertKeyToString(r.getHuffmanCodes()));
        body.put("originalText",r.getOriginalText());
        body.put("encodedBinary",r.getEncodedBinary());
        body.put("originalBytes",r.getOriginalBytes());
        body.put("compressedBytes",r.getCompressedBytes());
        body.put("compressionRatio",r.getCompressionRatio());
        body.put("compressionPercent",r.getCompressionPercent());
        body.put("decompressedText",r.getDecompressedText());
        body.put("roundTripSuccess",r.isRoundTripSuccess());
        body.put("kValue",r.getKValue());

        if (includeSteps) {

            body.put("heapSteps", r.getHeapSteps());
        }
        return body;
    }

    /** Converts Map<Character, V> to Map<String, V> for clean JSON output. */
    private <V> Map<String, V> convertKeyToString(Map<Character, V> map){

        Map<String, V> result = new LinkedHashMap<>();
        if (map == null) return result;

        map.forEach((k, v) ->{

            String label = (k == ' ') ? "SPACE" : (k == ',') ? "COMMA" : String.valueOf(k);
            result.put(label, v);
        });
        return result;
    }

    /** Standard error response body. */
    private Map<String, String> errorBody(String message) {
        return Map.of("error", message);
    }
}



/**
 * ============================================================
 * HuffmanController.java
 * ------------------------------------------------------------
 * Spring REST Controller that exposes the Huffman Coding
 * pipeline over HTTP.
 *
 * Base path: /api/huffman
 *
 * Endpoints:
 * ──────────────────────────────────────────────────────────
 *  POST /compress                  – standard pipeline (JSON text)
 *  POST /compress/file             – standard pipeline (upload .txt file)
 *  POST /compress/extended         – extended k-pair pipeline (JSON text)
 *  POST /compress/extended/file    – extended k-pair pipeline (upload .txt file)
 *  POST /benchmark                 – compare k values side-by-side
 *  POST /frequencies               – character frequency analysis only
 *  POST /codes                     – Huffman code table only
 *  POST /tree                      – ASCII tree diagram
 *  GET  /health                    – health/status check
 * ──────────────────────────────────────────────────────────
 */

// ─────────────────────────────────────────────────────────
// Dependencies
// ─────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────
// Request / Response DTOs
// ─────────────────────────────────────────────────────────


// ─────────────────────────────────────────────────────────
// Health
// ─────────────────────────────────────────────────────────

/**
 * GET /api/huffman/health
 * Simple liveness check.
 */

// ─────────────────────────────────────────────────────────
// Standard Compress — JSON text
// ─────────────────────────────────────────────────────────

/**
 * POST /api/huffman/compress
 * Body: { "text": "Hello World" }
 */

// ─────────────────────────────────────────────────────────
// Standard Compress — File Upload
// ─────────────────────────────────────────────────────────

/**
 * POST /api/huffman/compress/file
 *
 * Accepts a .txt file upload (multipart/form-data).
 * Reads the file content and runs the standard Huffman pipeline.
 *
 * In Postman:
 *   - Body → form-data
 *   - Key:  "file"  (type = File)
 *   - Value: select your .txt file
 */
// ─────────────────────────────────────────────────────────
// Extended Compress — JSON text
// ─────────────────────────────────────────────────────────

/**
 * POST /api/huffman/compress/extended
 * Body: { "text": "Hello World", "k": 2 }
 */

// ─────────────────────────────────────────────────────────
// Extended Compress — File Upload
// ─────────────────────────────────────────────────────────

/**
 * POST /api/huffman/compress/extended/file
 *
 * Accepts a .txt file upload + a k parameter (number of pairs).
 *
 * In Postman:
 *   - Body → form-data
 *   - Key: "file"  (type = File)  → select your .txt file
 *   - Key: "k"     (type = Text)  → e.g. 2
 */

// ─────────────────────────────────────────────────────────
// Benchmark
// ─────────────────────────────────────────────────────────

/**
 * POST /api/huffman/benchmark
 * Body: { "text": "Hello World", "kValues": [1,2,3,4] }
 */
// ─────────────────────────────────────────────────────────
// Frequencies / Codes / Tree
// ─────────────────────────────────────────────────────────


// ─────────────────────────────────────────────────────────
// Private helpers
// ─────────────────────────────────────────────────────────

/**
 * Validates an uploaded file: must exist, be a .txt, and not exceed 5 MB.
 * Returns an error message string, or null if the file is valid.
 */
