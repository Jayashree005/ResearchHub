-- ============================================================
-- Seed data for Research Hub
-- Runs once when Spring Boot starts (spring.sql.init.mode=always)
-- Only inserts if the table is empty
-- ============================================================

INSERT INTO papers (title, author, domain, year, abstract_text, tags, pdf_path, pdf_file_name, views, downloads, uploaded_at)
SELECT 'Deep Learning for Disease Prediction in Healthcare',
       'Dr. Anil Kumar', 'Artificial Intelligence', 2024,
       'This paper explores the use of deep neural networks to predict diseases from patient records and medical imaging data with high accuracy. The model achieves 94% accuracy on benchmark datasets.',
       'Deep Learning,Healthcare,Neural Networks',
       NULL, NULL, 45, 12, '2024-01-15'
WHERE NOT EXISTS (SELECT 1 FROM papers LIMIT 1);

INSERT INTO papers (title, author, domain, year, abstract_text, tags, pdf_path, pdf_file_name, views, downloads, uploaded_at)
SELECT 'Federated Learning for Privacy-Preserving Data Analysis',
       'Dr. Priya Sharma', 'Machine Learning', 2023,
       'This study investigates federated learning approaches that enable model training on distributed data without sharing raw information. Results demonstrate comparable accuracy to centralized models.',
       'Federated Learning,Privacy,Data Security',
       NULL, NULL, 38, 9, '2023-07-20'
WHERE NOT EXISTS (SELECT 1 FROM papers WHERE title = 'Federated Learning for Privacy-Preserving Data Analysis');

INSERT INTO papers (title, author, domain, year, abstract_text, tags, pdf_path, pdf_file_name, views, downloads, uploaded_at)
SELECT 'Natural Language Processing for Sentiment Analysis',
       'Prof. Ravi Menon', 'Artificial Intelligence', 2023,
       'An analysis of transformer-based NLP models for real-time sentiment classification in social media data with improved accuracy metrics. BERT fine-tuning achieves 97% F1 score.',
       'NLP,Transformers,Sentiment',
       NULL, NULL, 62, 21, '2023-09-05'
WHERE NOT EXISTS (SELECT 1 FROM papers WHERE title = 'Natural Language Processing for Sentiment Analysis');

INSERT INTO papers (title, author, domain, year, abstract_text, tags, pdf_path, pdf_file_name, views, downloads, uploaded_at)
SELECT 'Blockchain-Based Secure Data Sharing in Cloud Environments',
       'Dr. Meena Iyer', 'Cybersecurity', 2024,
       'This paper proposes a blockchain-based framework for secure and auditable data sharing in multi-cloud environments, addressing challenges of trust, integrity, and access control.',
       'Blockchain,Cloud,Security,Data Integrity',
       NULL, NULL, 29, 7, '2024-03-10'
WHERE NOT EXISTS (SELECT 1 FROM papers WHERE title = 'Blockchain-Based Secure Data Sharing in Cloud Environments');

INSERT INTO papers (title, author, domain, year, abstract_text, tags, pdf_path, pdf_file_name, views, downloads, uploaded_at)
SELECT 'Automated Question Paper Generation Using Bloom''s Taxonomy',
       'Prof. Jayashree S', 'Data Science', 2024,
       'A systematic review of automated question paper generation systems leveraging Bloom''s Taxonomy cognitive levels. The paper proposes a hybrid NLP-rule-based model for academic assessment.',
       'AQPG,Bloom Taxonomy,Education,NLP',
       NULL, NULL, 51, 14, '2024-05-22'
WHERE NOT EXISTS (SELECT 1 FROM papers WHERE title = 'Automated Question Paper Generation Using Bloom''s Taxonomy');
