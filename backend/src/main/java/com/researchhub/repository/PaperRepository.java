package com.researchhub.repository;

import com.researchhub.model.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Paper.
 * Spring auto-generates the SQL implementations at runtime.
 */
@Repository
public interface PaperRepository extends JpaRepository<Paper, Long> {

    // Search across title, author, and tags fields (case-insensitive)
    List<Paper> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrTagsContainingIgnoreCase(
            String title, String author, String tags);

    // Filter by domain
    List<Paper> findByDomainIgnoreCase(String domain);

    // Filter by year
    List<Paper> findByYear(int year);

    // Stats queries
    @Query("SELECT COUNT(DISTINCT p.domain) FROM Paper p WHERE p.domain IS NOT NULL")
    long countDistinctDomains();

    @Query("SELECT COUNT(DISTINCT p.author) FROM Paper p WHERE p.author IS NOT NULL")
    long countDistinctAuthors();

    @Query("SELECT COALESCE(SUM(p.downloads), 0) FROM Paper p")
    long sumAllDownloads();
}
