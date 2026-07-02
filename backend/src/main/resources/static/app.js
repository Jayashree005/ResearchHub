// ================================================================
// ResearchHub — Frontend JavaScript
// Connects to Spring Boot backend at localhost:8080
// ================================================================

const API_BASE = "/api";

// ================================================================
// INIT — runs when page loads
// ================================================================
window.onload = function () {
  loadStats();
  loadPapers();
  setupFileDrop();
};

// ================================================================
// LOAD ALL PAPERS
// ================================================================
function loadPapers() {
  document.getElementById("sectionTitle").textContent = "Recent Papers";
  fetchPapers(API_BASE + "/papers");
}

// ================================================================
// SEARCH PAPERS
// ================================================================
function searchPapers() {
  var query = document.getElementById("searchInput").value.trim();
  if (!query) {
    loadPapers();
    return;
  }
  document.getElementById("sectionTitle").textContent = 'Results for "' + query + '"';
  fetchPapers(API_BASE + "/papers?search=" + encodeURIComponent(query));
}

// ================================================================
// FILTER BY DOMAIN
// ================================================================
function filterByDomain(domain) {
  document.getElementById("searchInput").value = "";
  document.getElementById("sectionTitle").textContent = "Domain: " + domain;
  fetchPapers(API_BASE + "/papers?domain=" + encodeURIComponent(domain));
}

// ================================================================
// FILTER BY YEAR
// ================================================================
function filterByYear(year) {
  document.getElementById("searchInput").value = "";
  document.getElementById("sectionTitle").textContent = "Year: " + year;
  fetchPapers(API_BASE + "/papers?year=" + year);
}

// ================================================================
// CORE FETCH FUNCTION — fetches from any URL and renders cards
// ================================================================
function fetchPapers(url) {
  showLoading(true);
  hideEmptyState();

  fetch(url)
    .then(function (response) {
      if (!response.ok) throw new Error("Server error: " + response.status);
      return response.json();
    })
    .then(function (papers) {
      showLoading(false);
      renderPapers(papers);
    })
    .catch(function (error) {
      showLoading(false);
      console.error("Fetch error:", error);
      showToast("Could not connect to backend. Is Spring Boot running?", "error");
      document.getElementById("papersContainer").innerHTML = "";
      showEmptyState();
    });
}

// ================================================================
// RENDER PAPERS LIST
// ================================================================
function renderPapers(papers) {
  var container = document.getElementById("papersContainer");
  var countEl = document.getElementById("paperCount");
  container.innerHTML = "";

  if (!papers || papers.length === 0) {
    countEl.textContent = "";
    showEmptyState();
    return;
  }

  hideEmptyState();
  countEl.textContent = "(" + papers.length + " result" + (papers.length !== 1 ? "s" : "") + ")";

  papers.forEach(function (paper, index) {
    var card = buildCard(paper, index);
    container.appendChild(card);
  });
}

// ================================================================
// BUILD A SINGLE CARD ELEMENT
// ================================================================
function buildCard(paper, index) {
  var card = document.createElement("div");
  card.className = "card";
  card.style.animationDelay = (index * 60) + "ms";

  // Tags HTML
  var tagsHtml = "";
  if (paper.tags) {
    paper.tags.split(",").forEach(function (tag) {
      tagsHtml += "<span>" + escapeHtml(tag.trim()) + "</span>";
    });
  }

  // Download button (only if PDF exists)
  var downloadHtml = "";
  if (paper.pdfFileName) {
    downloadHtml = '<a href="' + API_BASE + '/papers/' + paper.id + '/download" class="btn-download" target="_blank">⬇ Download PDF</a>';
  } else {
    downloadHtml = '<a href="#" onclick="showToast(\'No PDF uploaded for this paper.\', \'info\'); return false;">No PDF</a>';
  }

  card.innerHTML = [
    '<h3>' + escapeHtml(paper.title) + '</h3>',
    '<div class="meta">',
      '👤 ' + escapeHtml(paper.author || "—"),
      ' &nbsp;|&nbsp; 📅 ' + (paper.year || "—"),
      ' &nbsp;|&nbsp; 🏷 ' + escapeHtml(paper.domain || "—"),
    '</div>',
    '<p>' + escapeHtml(paper.abstractText || "") + '</p>',
    '<div class="tags">' + tagsHtml + '</div>',
    '<div class="actions">',
      downloadHtml,
      '<a href="#" onclick="viewAbstract(' + paper.id + '); return false;">View Abstract</a>',
      '<a href="#" onclick="copyCitation(\'' + escapeJs(paper.title) + '\', \'' + escapeJs(paper.author) + '\', ' + paper.year + '); return false;">📋 Cite</a>',
      '<button class="btn-edit" onclick="openEditModal(' + paper.id + ')">✏ Edit</button>',
      '<button class="btn-delete" onclick="deletePaper(' + paper.id + ', this)">🗑 Delete</button>',
    '</div>',
    '<div class="counters">👁 ' + (paper.views || 0) + ' views &nbsp;·&nbsp; ⬇ ' + (paper.downloads || 0) + ' downloads</div>'
  ].join("");

  return card;
}

// ================================================================
// LOAD STATS (counter bar)
// ================================================================
function loadStats() {
  fetch(API_BASE + "/stats")
    .then(function (res) { return res.json(); })
    .then(function (stats) {
      animateCount("stat-papers",    stats.totalPapers    || 0);
      animateCount("stat-domains",   stats.totalDomains   || 0);
      animateCount("stat-authors",   stats.totalAuthors   || 0);
      animateCount("stat-downloads", stats.totalDownloads || 0);
    })
    .catch(function () {
      // fail silently — stats aren't critical
    });
}

// Animate a number counting up
function animateCount(elementId, target) {
  var el = document.getElementById(elementId);
  if (!el) return;
  var current = 0;
  var step = Math.max(1, Math.floor(target / 40));
  var timer = setInterval(function () {
    current += step;
    if (current >= target) {
      current = target;
      clearInterval(timer);
    }
    el.textContent = current.toLocaleString();
  }, 30);
}

// ================================================================
// UPLOAD PAPER — handles the modal form submit
// ================================================================
function submitUpload(event) {
  event.preventDefault();

  var form    = document.getElementById("uploadForm");
  var btn     = document.getElementById("submitBtn");
  var formData = new FormData(form);

  btn.disabled = true;
  btn.textContent = "Uploading...";

  fetch(API_BASE + "/papers", {
    method: "POST",
    body: formData
    // Note: Do NOT set Content-Type header — browser sets it automatically for FormData
  })
    .then(function (response) {
      if (!response.ok) throw new Error("Upload failed: " + response.status);
      return response.json();
    })
    .then(function (paper) {
      btn.disabled = false;
      btn.textContent = "Upload Paper";
      closeUploadModal();
      form.reset();
      resetFileLabel();
      showToast("✅ Paper uploaded successfully!", "success");
      loadPapers();   // refresh list
      loadStats();    // refresh counters
    })
    .catch(function (error) {
      btn.disabled = false;
      btn.textContent = "Upload Paper";
      console.error("Upload error:", error);
      showToast("Upload failed. Check your backend connection.", "error");
    });
}

// ================================================================
// EDIT MODAL — open, pre-fill, submit, close
// ================================================================
function openEditModal(id) {
  fetch(API_BASE + "/papers/" + id)
    .then(function (res) {
      if (!res.ok) throw new Error("Not found");
      return res.json();
    })
    .then(function (paper) {
      document.getElementById("edit-id").value      = paper.id;
      document.getElementById("edit-title").value   = paper.title    || "";
      document.getElementById("edit-author").value  = paper.author   || "";
      document.getElementById("edit-year").value    = paper.year     || "";
      document.getElementById("edit-domain").value  = paper.domain   || "";
      document.getElementById("edit-tags").value    = paper.tags     || "";
      document.getElementById("edit-abstract").value = paper.abstractText || "";
      // Reset PDF label
      document.getElementById("editFileLabel").textContent = paper.pdfFileName
        ? "Current: " + paper.pdfFileName + " (upload new to replace)"
        : "Click to browse a new PDF (optional)";
      document.getElementById("editFileDrop").classList.remove("has-file");
      document.getElementById("edit-file").value = "";

      document.getElementById("editModal").style.display = "flex";
      document.body.style.overflow = "hidden";
    })
    .catch(function () {
      showToast("Could not load paper details.", "error");
    });
}

function closeEditModal() {
  document.getElementById("editModal").style.display = "none";
  document.body.style.overflow = "";
}

function closeOnOverlayEdit(event) {
  if (event.target === document.getElementById("editModal")) {
    closeEditModal();
  }
}

function submitEdit(event) {
  event.preventDefault();
  var id  = document.getElementById("edit-id").value;
  var btn = document.getElementById("editSubmitBtn");
  var formData = new FormData(document.getElementById("editForm"));

  btn.disabled = true;
  btn.textContent = "Saving...";

  fetch(API_BASE + "/papers/" + id, {
    method: "PUT",
    body: formData
  })
    .then(function (response) {
      if (!response.ok) throw new Error("Update failed: " + response.status);
      return response.json();
    })
    .then(function () {
      btn.disabled = false;
      btn.textContent = "Save Changes";
      closeEditModal();
      showToast("✅ Paper updated successfully!", "success");
      loadPapers();
      loadStats();
    })
    .catch(function (error) {
      btn.disabled = false;
      btn.textContent = "Save Changes";
      console.error("Edit error:", error);
      showToast("Update failed. Check your backend connection.", "error");
    });
}

function updateEditFileLabel(input) {
  var label   = document.getElementById("editFileLabel");
  var dropZone = document.getElementById("editFileDrop");
  if (input.files && input.files[0]) {
    var file = input.files[0];
    var sizeKb = (file.size / 1024).toFixed(1);
    label.textContent = "✅ " + file.name + " (" + sizeKb + " KB)";
    dropZone.classList.add("has-file");
  }
}

// ================================================================
// DELETE PAPER
// ================================================================
function deletePaper(id, btnEl) {
  if (!confirm("Are you sure you want to delete this paper? This will also delete the PDF file.")) return;

  fetch(API_BASE + "/papers/" + id, { method: "DELETE" })
    .then(function (response) {
      if (response.status === 204) {
        // Remove the card from DOM smoothly
        var card = btnEl.closest(".card");
        card.style.transition = "opacity 0.3s, transform 0.3s";
        card.style.opacity = "0";
        card.style.transform = "scale(0.95)";
        setTimeout(function () { card.remove(); }, 300);
        showToast("Paper deleted.", "info");
        loadStats();
      } else {
        showToast("Delete failed.", "error");
      }
    })
    .catch(function () {
      showToast("Error connecting to backend.", "error");
    });
}

// ================================================================
// VIEW ABSTRACT (fetch single paper details)
// ================================================================
function viewAbstract(id) {
  fetch(API_BASE + "/papers/" + id)
    .then(function (res) { return res.json(); })
    .then(function (paper) {
      alert("📄 " + paper.title + "\n\nAuthor: " + paper.author + "\nYear: " + paper.year + "\n\n" + paper.abstractText);
    })
    .catch(function () {
      showToast("Could not fetch paper details.", "error");
    });
}

// ================================================================
// COPY CITATION (simple APA format)
// ================================================================
function copyCitation(title, author, year) {
  var citation = author + " (" + year + "). " + title + ". ResearchHub Repository.";
  navigator.clipboard.writeText(citation)
    .then(function () { showToast("📋 Citation copied to clipboard!", "success"); })
    .catch(function ()  { showToast("Could not copy citation.", "error"); });
}

// ================================================================
// MODAL CONTROLS
// ================================================================
function openUploadModal() {
  document.getElementById("uploadModal").style.display = "flex";
  document.body.style.overflow = "hidden";
}

function closeUploadModal() {
  document.getElementById("uploadModal").style.display = "none";
  document.body.style.overflow = "";
}

// Close modal when clicking the dark overlay (not the box itself)
function closeOnOverlay(event) {
  if (event.target === document.getElementById("uploadModal")) {
    closeUploadModal();
  }
}

// ================================================================
// FILE DROP ZONE
// ================================================================
function setupFileDrop() {
  var dropZone = document.getElementById("fileDrop");
  if (!dropZone) return;

  dropZone.addEventListener("dragover", function (e) {
    e.preventDefault();
    dropZone.classList.add("has-file");
  });

  dropZone.addEventListener("dragleave", function () {
    dropZone.classList.remove("has-file");
  });

  dropZone.addEventListener("drop", function (e) {
    e.preventDefault();
    var file = e.dataTransfer.files[0];
    if (file && file.type === "application/pdf") {
      // Programmatically assign the file to the input
      var dt = new DataTransfer();
      dt.items.add(file);
      document.getElementById("up-file").files = dt.files;
      updateFileLabel(document.getElementById("up-file"));
    } else {
      showToast("Please drop a PDF file.", "error");
      dropZone.classList.remove("has-file");
    }
  });
}

function updateFileLabel(input) {
  var label = document.getElementById("fileLabel");
  var dropZone = document.getElementById("fileDrop");
  if (input.files && input.files[0]) {
    var file = input.files[0];
    var sizeKb = (file.size / 1024).toFixed(1);
    label.textContent = "✅ " + file.name + " (" + sizeKb + " KB)";
    dropZone.classList.add("has-file");
  }
}

function resetFileLabel() {
  var label = document.getElementById("fileLabel");
  var dropZone = document.getElementById("fileDrop");
  if (label) label.textContent = "Click to browse or drag & drop PDF here";
  if (dropZone) dropZone.classList.remove("has-file");
}

// ================================================================
// SPINNER / EMPTY STATE
// ================================================================
function showLoading(visible) {
  document.getElementById("loadingSpinner").style.display = visible ? "block" : "none";
  if (visible) document.getElementById("papersContainer").innerHTML = "";
}

function showEmptyState() {
  document.getElementById("emptyState").style.display = "block";
}

function hideEmptyState() {
  document.getElementById("emptyState").style.display = "none";
}

// ================================================================
// TOAST NOTIFICATION
// ================================================================
function showToast(message, type) {
  var toast = document.getElementById("toast");
  toast.textContent = message;
  toast.className = "toast " + (type || "info");
  toast.style.display = "block";
  clearTimeout(toast._timer);
  toast._timer = setTimeout(function () {
    toast.style.display = "none";
  }, 3500);
}

// ================================================================
// SCROLL HELPER
// ================================================================
function scrollToSection(id) {
  var el = document.getElementById(id);
  if (el) el.scrollIntoView({ behavior: "smooth" });
}

// ================================================================
// UTILITY: Escape HTML to prevent XSS
// ================================================================
function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function escapeJs(str) {
  if (!str) return "";
  return String(str).replace(/'/g, "\\'").replace(/"/g, '\\"');
}
