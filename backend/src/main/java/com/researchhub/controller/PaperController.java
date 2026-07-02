package com.researchhub.controller;

import com.researchhub.model.Paper;
import com.researchhub.service.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

/**
 * REST Controller — exposes all /api/* endpoints.
 * CORS is handled globally in CorsConfig.java.
 */
@RestController
@RequestMapping("/api")
public class PaperController {

    @Autowired
    private PaperService paperService;

    // ================================================================
    // GET /api/papers
    // Optional query params: ?search=, ?domain=, ?year=
    // ================================================================
    @GetMapping("/papers")
    public ResponseEntity<List<Paper>> getPapers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) Integer year) {

        List<Paper> papers;

        if (search != null && !search.isBlank()) {
            papers = paperService.searchPapers(search);
        } else if (domain != null && !domain.isBlank()) {
            papers = paperService.filterByDomain(domain);
        } else if (year != null) {
            papers = paperService.filterByYear(year);
        } else {
            papers = paperService.getAllPapers();
        }

        return ResponseEntity.ok(papers);
    }

    // ================================================================
    // GET /api/papers/{id}  — also increments view count
    // ================================================================
    @GetMapping("/papers/{id}")
    public ResponseEntity<Paper> getPaper(@PathVariable Long id) {
        return paperService.getPaperById(id)
                .map(paper -> {
                    paperService.incrementViews(paper);
                    return ResponseEntity.ok(paper);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ================================================================
    // POST /api/papers  — multipart/form-data: metadata + PDF file
    // ================================================================
    @PostMapping(value = "/papers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Paper> uploadPaper(
            @RequestParam("title")        String title,
            @RequestParam("author")       String author,
            @RequestParam("domain")       String domain,
            @RequestParam("year")         int year,
            @RequestParam("abstractText") String abstractText,
            @RequestParam(value = "tags",  required = false) String tags,
            @RequestParam(value = "file",  required = false) MultipartFile file) throws IOException {

        Paper paper = new Paper();
        paper.setTitle(title);
        paper.setAuthor(author);
        paper.setDomain(domain);
        paper.setYear(year);
        paper.setAbstractText(abstractText);
        paper.setTags(tags);

        Paper saved = paperService.savePaper(paper, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ================================================================
    // GET /api/papers/{id}/download  — streams PDF + increments counter
    // ================================================================
    @GetMapping("/papers/{id}/download")
    public ResponseEntity<Resource> downloadPaper(@PathVariable Long id) {
        Optional<Paper> paperOpt = paperService.getPaperById(id);

        if (paperOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Paper paper = paperOpt.get();

        if (paper.getPdfPath() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .build();
        }

        try {
            Path filePath = Paths.get(paper.getPdfPath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Increment download counter
            paperService.incrementDownload(paper);

            String fileName = paper.getPdfFileName() != null
                    ? paper.getPdfFileName()
                    : "paper.pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ================================================================
    // PUT /api/papers/{id}  — update metadata (and optionally replace PDF)
    // ================================================================
    @PutMapping(value = "/papers/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Paper> updatePaper(
            @PathVariable Long id,
            @RequestParam("title")        String title,
            @RequestParam("author")       String author,
            @RequestParam("domain")       String domain,
            @RequestParam("year")         int year,
            @RequestParam("abstractText") String abstractText,
            @RequestParam(value = "tags",  required = false) String tags,
            @RequestParam(value = "file",  required = false) MultipartFile file) throws IOException {

        return paperService.updatePaper(id, title, author, domain, year, abstractText, tags, file)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ================================================================
    // DELETE /api/papers/{id}  — removes DB record + deletes file
    // ================================================================
    @DeleteMapping("/papers/{id}")
    public ResponseEntity<Void> deletePaper(@PathVariable Long id) throws IOException {
        if (paperService.getPaperById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        paperService.deletePaper(id);
        return ResponseEntity.noContent().build();
    }

    // ================================================================
    // GET /api/stats  — dashboard counters
    // ================================================================
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(paperService.getStats());
    }
}
