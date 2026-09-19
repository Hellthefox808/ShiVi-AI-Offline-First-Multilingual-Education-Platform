# 61 — DOMAIN GLOSSARY, ACRONYM REGISTRY & TRIBAL TAXONOMY

> **Document ID:** `BS-ARCH-61-GLOSSARY`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** Enterprise Domain Terminology (§4 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Educational & Pedagogical Terminology

* **MTB-MLE (Mother-Tongue-Based Multilingual Education)**: A pedagogical approach where primary education begins in the child's mother tongue (L1) with gradual transition to regional (L2: Hindi) and national languages. Mandated by NEP 2020 §4.11.
* **FLN (Foundational Literacy and Numeracy)**: The foundational ability to read basic text with comprehension and perform basic arithmetic operations by Grade 3.
* **NIPUN Bharat**: National Initiative for Proficiency in Reading with Understanding and Numeracy; national mission setting time-bound FLN competency benchmarks across Indian states.
* **JCERT**: Jharkhand Council of Educational Research and Training; the state body responsible for primary school curricula, textbooks, and learning outcomes in Jharkhand.
* **Bloom's Taxonomy**: Educational framework categorizing educational objectives into levels: Remember, Understand, Apply, Analyze, Evaluate, and Create.

---

## 2. Linguistic & Regional Tribal Concepts

* **Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)**: The official alphabetic script created by Pandit Raghunath Murmu in 1925 for the Santhali language. Represented in Unicode `U+1C50`–`U+1C7F`.
* **Warang Chiti (ᱦᱚᱺ ᱡᱟᱜᱟᱨ)**: The indigenous writing system invented by Bodro Lako for the Ho language. Represented in Unicode `U+118A0`–`U+118FF`.
* **Sarhul (सरहुल)**: The grand festival of flowers celebrated by tribal communities in Jharkhand celebrating the blossoming of the sacred Sal tree, symbolizing nature conservation and fertility.
* **Sal / Sakhua (Shorea robusta)**: The primary indigenous forest tree of Jharkhand, revered in tribal folklore, utilized for leaves, timber, and traditional leaf plates (पत्तल).
* **Bilingual Relay Mode**: A pedagogical audio pattern where a Hindi sentence is spoken first, followed by a 450ms pause, and then the mother-tongue tribal translation at 0.72x speed.

---

## 3. Technical & Architectural Acronyms

* **DiskANN**: Fast Approximate Nearest Neighbor vector search graph algorithm optimized for SSD storage, reducing RAM consumption by 10x over HNSW.
* **RRF (Reciprocal Rank Fusion)**: An algorithm that combines ranked lists of search results from disparate search algorithms (e.g., BM25 lexical and BGE-M3 vector).
* **COMETKiwi**: A reference-free machine translation quality estimation neural model evaluating translation fluency and adequacy directly against the source sentence.
* **RLS (Row-Level Security)**: A PostgreSQL security feature that restricts which rows in a table can be returned or modified based on the current user session context.
* **VAD (Voice Activity Detection)**: The automated detection of human speech boundaries in an audio stream, separating speech frames from ambient classroom noise.
* **Outbox Pattern**: A transactional messaging pattern where state mutations and outbound event messages are committed together in a single local database transaction.
