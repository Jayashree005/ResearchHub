package com.researchhub.service;

import com.researchhub.model.Paper;
import com.researchhub.repository.PaperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Business logic layer for paper management.
 * Handles file storage and database operations.
 */
@Service
public class PaperService {

    @Autowired
    private PaperRepository paperRepository;

    // Injected from application.properties: file.upload-dir
    @Value("${file.upload-dir}")
    private String uploadDir;

    // ---- READ -------------------------------------------------------

    public List<Paper> getAllPapers() {
        return paperRepository.findAll();
    }

    public List<Paper> searchPapers(String query) {
        return paperRepository
                .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrTagsContainingIgnoreCase(
                        query, query, query);
    }

    public List<Paper> filterByDomain(String domain) {
        return paperRepository.findByDomainIgnoreCase(domain);
    }

    public List<Paper> filterByYear(int year) {
        return paperRepository.findByYear(year);
    }

    public Optional<Paper> getPaperById(Long id) {
        return paperRepository.findById(id);
    }

    // ---- CREATE / UPDATE -------------------------------------------

    /**
     * Saves a paper with optional PDF file.
     * If a file is provided, stores it on disk and records the path.
     */
    public Paper savePaper(Paper paper, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            // Ensure upload directory exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Unique filename: timestamp + original name
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            paper.setPdfPath(filePath.toAbsolutePath().toString());
            paper.setPdfFileName(file.getOriginalFilename());
        }
        return paperRepository.save(paper);
    }

    // ---- COUNTERS ---------------------------------------------------

    public Paper incrementDownload(Paper paper) {
        paper.setDownloads(paper.getDownloads() + 1);
        return paperRepository.save(paper);
    }

    public Paper incrementViews(Paper paper) {
        paper.setViews(paper.getViews() + 1);
        return paperRepository.save(paper);
    }

    // ---- UPDATE ----------------------------------------------------

    /**
     * Updates metadata fields and optionally replaces the PDF file.
     * If a new file is provided, the old one is deleted from disk first.
     */
    public Optional<Paper> updatePaper(Long id, String title, String author, String domain,
                                       int year, String abstractText, String tags,
                                       MultipartFile file) throws IOException {
        Optional<Paper> paperOpt = paperRepository.findById(id);
        if (paperOpt.isEmpty()) return Optional.empty();

        Paper paper = paperOpt.get();
        paper.setTitle(title);
        paper.setAuthor(author);
        paper.setDomain(domain);
        paper.setYear(year);
        paper.setAbstractText(abstractText);
        paper.setTags(tags);

        if (file != null && !file.isEmpty()) {
            // Delete old file if it exists
            if (paper.getPdfPath() != null) {
                Files.deleteIfExists(Paths.get(paper.getPdfPath()));
            }
            // Save new file
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            paper.setPdfPath(filePath.toAbsolutePath().toString());
            paper.setPdfFileName(file.getOriginalFilename());
        }

        return Optional.of(paperRepository.save(paper));
    }

    // ---- DELETE ----------------------------------------------------

    /**
     * Deletes the DB record and the actual PDF file from disk (if it exists).
     */
    public void deletePaper(Long id) throws IOException {
        Optional<Paper> paperOpt = paperRepository.findById(id);
        if (paperOpt.isPresent()) {
            Paper paper = paperOpt.get();
            if (paper.getPdfPath() != null) {
                Files.deleteIfExists(Paths.get(paper.getPdfPath()));
            }
            paperRepository.deleteById(id);
        }
    }

    // ---- STATS ------------------------------------------------------

    /**
     * Returns aggregate statistics for the dashboard counter bar.
     */
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalPapers",    paperRepository.count());
        stats.put("totalDomains",   paperRepository.countDistinctDomains());
        stats.put("totalAuthors",   paperRepository.countDistinctAuthors());
        stats.put("totalDownloads", paperRepository.sumAllDownloads());
        return stats;
    }
}
