package com.researchhub.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Paper entity — maps to the 'papers' table in MySQL.
 * Getters/setters written explicitly (avoids Lombok annotation processor issues with Java 25).
 */
@Entity
@Table(name = "papers")
public class Paper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    private String domain;

    private int year;

    @Column(name = "abstract_text", columnDefinition = "TEXT")
    private String abstractText;

    /** Comma-separated tags, e.g. "AI,Healthcare,Neural Networks" */
    private String tags;

    /** Absolute path on disk where the PDF is stored */
    @Column(name = "pdf_path")
    private String pdfPath;

    /** Original filename of the uploaded PDF */
    @Column(name = "pdf_file_name")
    private String pdfFileName;

    private int views = 0;
    private int downloads = 0;

    @Column(name = "uploaded_at")
    private LocalDate uploadedAt;

    /** Auto-set upload date before first save */
    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = LocalDate.now();
        }
    }

    // ---- No-arg constructor ----
    public Paper() {}

    // ---- All-arg constructor ----
    public Paper(Long id, String title, String author, String domain, int year,
                 String abstractText, String tags, String pdfPath, String pdfFileName,
                 int views, int downloads, LocalDate uploadedAt) {
        this.id           = id;
        this.title        = title;
        this.author       = author;
        this.domain       = domain;
        this.year         = year;
        this.abstractText = abstractText;
        this.tags         = tags;
        this.pdfPath      = pdfPath;
        this.pdfFileName  = pdfFileName;
        this.views        = views;
        this.downloads    = downloads;
        this.uploadedAt   = uploadedAt;
    }

    // ---- Getters ----
    public Long getId()            { return id; }
    public String getTitle()       { return title; }
    public String getAuthor()      { return author; }
    public String getDomain()      { return domain; }
    public int getYear()           { return year; }
    public String getAbstractText(){ return abstractText; }
    public String getTags()        { return tags; }
    public String getPdfPath()     { return pdfPath; }
    public String getPdfFileName() { return pdfFileName; }
    public int getViews()          { return views; }
    public int getDownloads()      { return downloads; }
    public LocalDate getUploadedAt(){ return uploadedAt; }

    // ---- Setters ----
    public void setId(Long id)                       { this.id = id; }
    public void setTitle(String title)               { this.title = title; }
    public void setAuthor(String author)             { this.author = author; }
    public void setDomain(String domain)             { this.domain = domain; }
    public void setYear(int year)                    { this.year = year; }
    public void setAbstractText(String abstractText) { this.abstractText = abstractText; }
    public void setTags(String tags)                 { this.tags = tags; }
    public void setPdfPath(String pdfPath)           { this.pdfPath = pdfPath; }
    public void setPdfFileName(String pdfFileName)   { this.pdfFileName = pdfFileName; }
    public void setViews(int views)                  { this.views = views; }
    public void setDownloads(int downloads)          { this.downloads = downloads; }
    public void setUploadedAt(LocalDate uploadedAt)  { this.uploadedAt = uploadedAt; }
}
