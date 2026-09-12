/**
 * SmartJob V2 — Interactive Single Page Application Logic
 */

const API_BASE = '';
let authToken = localStorage.getItem('smartjob_jwt') || '';
let currentUser = null;
let currentCandidate = null;
let allJobs = [];
let allCandidates = [];

let jobFilters = {
  keyword: '',
  location: '',
  jobType: '',
  sort: 'id,desc',
  page: 0,
  size: 6
};

// ==========================================
// Initialization
// ==========================================
document.addEventListener('DOMContentLoaded', async () => {
  setupFilterListeners();
  await loadInitialStats();
  await loadJobs();
  await loadCandidatesAndJobsForSelectors();

  // Try auto-login if token exists
  if (authToken) {
    await fetchCurrentUser();
  } else {
    // Default demo login as Satya (Candidate) for instant rich presentation!
    await quickLogin('satya@example.com', 'password123', 'Satya Prakash (Candidate)', false);
  }
});

// ==========================================
// API Client Helper
// ==========================================
async function apiCall(endpoint, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers
    });

    if (response.status === 401) {
      if (authToken) {
        showToast('Session expired. Please log in again.', 'error');
        logout();
      }
      return null;
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(errorData.message || `HTTP ${response.status}`);
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }
    return true;
  } catch (error) {
    showToast(error.message, 'error');
    console.error(`API error on ${endpoint}:`, error);
    return null;
  }
}

// ==========================================
// Navigation & Views
// ==========================================
function switchView(viewName) {
  document.querySelectorAll('.view-section').forEach(sec => sec.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));

  const targetSection = document.getElementById(`view-${viewName}`);
  const targetNav = document.getElementById(`nav-${viewName}`);

  if (targetSection) targetSection.classList.add('active');
  if (targetNav) targetNav.classList.add('active');

  if (viewName === 'applications') loadApplications();
  if (viewName === 'profile') loadCandidateProfile();
  if (viewName === 'matcher') runMatchCalculation();
  if (viewName === 'gap') runSkillGapAnalysis();
}

// ==========================================
// Authentication & User State
// ==========================================
async function quickLogin(email, password, displayName, showNotification = true) {
  try {
    const res = await fetch(`${API_BASE}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      showToast(err.message || 'Login failed', 'error');
      return;
    }

    const data = await res.json();
    authToken = data.token;
    localStorage.setItem('smartjob_jwt', authToken);

    await fetchCurrentUser();

    if (showNotification) {
      showToast(`Logged in as ${displayName}`, 'success');
    }
    
    // Refresh current views
    loadJobs();
    loadApplications();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

async function fetchCurrentUser() {
  const user = await apiCall('/api/auth/me');
  if (!user) return;

  currentUser = user;
  updateUserStatusUI();

  // If candidate, find candidate profile
  if (user.role === 'CANDIDATE') {
    const candidatesRes = await apiCall('/api/candidates?size=50');
    if (candidatesRes && candidatesRes.content) {
      currentCandidate = candidatesRes.content.find(c => c.user && c.user.id === user.id) || candidatesRes.content[0];
    }
  } else {
    currentCandidate = null;
  }
}

function updateUserStatusUI() {
  const section = document.getElementById('userStatusSection');
  if (!currentUser) {
    section.innerHTML = `
      <button class="btn btn-secondary btn-sm" onclick="openModal('loginModal')">Sign In</button>
    `;
    return;
  }

  const roleLabel = currentUser.role.replace('ROLE_', '');
  const initials = currentUser.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();

  let postJobBtn = '';
  if (currentUser.role === 'RECRUITER' || currentUser.role === 'ADMIN') {
    postJobBtn = `<button class="btn btn-primary btn-sm" onclick="openModal('postJobModal')">+ Post Job</button>`;
  }

  section.innerHTML = `
    ${postJobBtn}
    <div class="user-pill">
      <div class="user-avatar">${initials}</div>
      <div class="user-info">
        <span class="user-name">${currentUser.name}</span>
        <span class="user-role-badge">${roleLabel}</span>
      </div>
    </div>
    <button class="btn btn-secondary btn-sm" onclick="logout()" title="Log out">Sign Out</button>
  `;
}

function logout() {
  authToken = '';
  currentUser = null;
  currentCandidate = null;
  localStorage.removeItem('smartjob_jwt');
  updateUserStatusUI();
  showToast('Signed out successfully', 'info');
  loadJobs();
}

// ==========================================
// Statistics & Data Loading
// ==========================================
async function loadInitialStats() {
  const jobsRes = await apiCall('/api/jobs?size=1');
  const candRes = await apiCall('/api/candidates?size=1');

  if (jobsRes) {
    document.getElementById('statTotalJobs').textContent = jobsRes.totalElements || '6';
  }
  if (candRes) {
    document.getElementById('statTotalCandidates').textContent = candRes.totalElements || '6';
  }
}

async function loadCandidatesAndJobsForSelectors() {
  const cRes = await apiCall('/api/candidates?size=50');
  const jRes = await apiCall('/api/jobs?size=50');

  if (cRes && cRes.content) {
    allCandidates = cRes.content;
    populateCandidateDropdowns(allCandidates);
  }

  if (jRes && jRes.content) {
    allJobs = jRes.content;
    populateJobDropdowns(allJobs);
  }
}

function populateCandidateDropdowns(candidates) {
  const selects = ['matchCandidateSelect', 'gapCandidateSelect'];
  selects.forEach(id => {
    const el = document.getElementById(id);
    if (!el) return;
    el.innerHTML = candidates.map(c => `
      <option value="${c.id}">${c.name} (${c.preferredRole || 'Candidate'}) - ${c.experience} yrs</option>
    `).join('');
  });
}

function populateJobDropdowns(jobs) {
  const selects = ['matchJobSelect', 'gapJobSelect'];
  selects.forEach(id => {
    const el = document.getElementById(id);
    if (!el) return;
    el.innerHTML = jobs.map(j => `
      <option value="${j.id}">${j.title} @ ${j.company} (${j.location})</option>
    `).join('');
  });
}

// ==========================================
// Browse Jobs
// ==========================================
function setupFilterListeners() {
  const keywordInput = document.getElementById('searchKeyword');
  const locationInput = document.getElementById('filterLocation');
  const jobTypeSelect = document.getElementById('filterJobType');
  const sortSelect = document.getElementById('sortJobs');

  let debounceTimer;
  const triggerDebouncedSearch = () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      jobFilters.keyword = keywordInput.value.trim();
      jobFilters.location = locationInput.value.trim();
      jobFilters.jobType = jobTypeSelect.value;
      jobFilters.sort = sortSelect.value;
      jobFilters.page = 0;
      loadJobs();
    }, 250);
  };

  keywordInput.addEventListener('input', triggerDebouncedSearch);
  locationInput.addEventListener('input', triggerDebouncedSearch);
  jobTypeSelect.addEventListener('change', triggerDebouncedSearch);
  sortSelect.addEventListener('change', triggerDebouncedSearch);
}

function resetJobFilters() {
  document.getElementById('searchKeyword').value = '';
  document.getElementById('filterLocation').value = '';
  document.getElementById('filterJobType').value = '';
  document.getElementById('sortJobs').value = 'id,desc';

  jobFilters = {
    keyword: '',
    location: '',
    jobType: '',
    sort: 'id,desc',
    page: 0,
    size: 6
  };
  loadJobs();
}

async function loadJobs() {
  const container = document.getElementById('jobsGridContainer');
  container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 48px; color: var(--text-muted);">Loading jobs...</div>`;

  let query = `/api/jobs?page=${jobFilters.page}&size=${jobFilters.size}&sort=${jobFilters.sort}`;
  if (jobFilters.keyword) query += `&keyword=${encodeURIComponent(jobFilters.keyword)}`;
  if (jobFilters.location) query += `&location=${encodeURIComponent(jobFilters.location)}`;
  if (jobFilters.jobType) query += `&jobType=${encodeURIComponent(jobFilters.jobType)}`;

  const res = await apiCall(query);
  if (!res || !res.content || res.content.length === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 48px; color: var(--text-secondary);">No jobs found matching your criteria.</div>`;
    renderPagination(0, 0);
    return;
  }

  container.innerHTML = res.content.map(job => renderJobCard(job)).join('');
  renderPagination(res.number, res.totalPages);
}

function renderJobCard(job) {
  const salaryFmt = `₹${(job.salaryMin / 100000).toFixed(1)}L - ₹${(job.salaryMax / 100000).toFixed(1)}L`;
  const reqSkills = job.requiredSkills || [];
  const prefSkills = job.preferredSkills || [];

  return `
    <div class="job-card">
      <div>
        <div class="job-header">
          <div>
            <h3 class="job-title">${job.title}</h3>
            <div class="job-company">🏢 ${job.company}</div>
          </div>
          <span class="badge badge-primary">${(job.jobType || 'FULL_TIME').replace('_', ' ')}</span>
        </div>

        <div class="job-details">
          <span class="job-meta-item">📍 ${job.location}</span>
          <span class="job-meta-item">⏱️ ${job.experienceRequired} yrs req.</span>
        </div>

        <p class="job-description">${job.description || 'No description provided.'}</p>

        <div class="skills-wrapper">
          <div class="skills-heading">Required Skills (Weight: 5)</div>
          <div class="skills-list">
            ${reqSkills.map(s => `<span class="skill-chip skill-req">${s}</span>`).join('') || '<span style="color:var(--text-muted);font-size:0.75rem;">None</span>'}
          </div>
        </div>

        ${prefSkills.length > 0 ? `
          <div class="skills-wrapper">
            <div class="skills-heading">Preferred Skills (Weight: 2)</div>
            <div class="skills-list">
              ${prefSkills.map(s => `<span class="skill-chip skill-pref">${s}</span>`).join('')}
            </div>
          </div>
        ` : ''}
      </div>

      <div class="job-footer">
        <div class="job-salary">${salaryFmt}</div>
        <div class="job-card-actions">
          <button class="btn btn-secondary btn-sm" onclick="matchSpecificJob(${job.id})">⚡ Match</button>
          <button class="btn btn-primary btn-sm" onclick="applyForJob(${job.id})">Apply</button>
        </div>
      </div>
    </div>
  `;
}

function renderPagination(currentPage, totalPages) {
  const container = document.getElementById('jobsPagination');
  if (totalPages <= 1) {
    container.innerHTML = '';
    return;
  }

  container.innerHTML = `
    <button class="btn btn-secondary btn-sm" ${currentPage === 0 ? 'disabled' : ''} onclick="changeJobPage(${currentPage - 1})">Previous</button>
    <span style="display:flex;align-items:center;color:var(--text-secondary);font-size:0.88rem;">Page ${currentPage + 1} of ${totalPages}</span>
    <button class="btn btn-secondary btn-sm" ${currentPage >= totalPages - 1 ? 'disabled' : ''} onclick="changeJobPage(${currentPage + 1})">Next</button>
  `;
}

function changeJobPage(newPage) {
  jobFilters.page = newPage;
  loadJobs();
}

async function applyForJob(jobId) {
  if (!authToken) {
    showToast('Please sign in or use a demo account to apply', 'warning');
    openModal('loginModal');
    return;
  }

  const res = await apiCall(`/api/jobs/${jobId}/apply`, { method: 'POST' });
  if (res) {
    showToast('Application submitted successfully! Track it in Applications tab.', 'success');
  }
}

function matchSpecificJob(jobId) {
  const jobSelect = document.getElementById('matchJobSelect');
  if (jobSelect) jobSelect.value = jobId;
  switchView('matcher');
  runMatchCalculation();
}

// ==========================================
// Smart Matcher (DSA Engine)
// ==========================================
async function runMatchCalculation() {
  const candidateId = document.getElementById('matchCandidateSelect').value;
  const jobId = document.getElementById('matchJobSelect').value;

  if (!candidateId || !jobId) return;

  const gapRes = await apiCall(`/api/match/gap/candidate/${candidateId}/job/${jobId}`);
  if (!gapRes) return;

  const score = gapRes.overallScore !== undefined ? gapRes.overallScore : 85.0;
  updateRadialScore(score);

  // Update dimensional scores
  const skillScore = gapRes.skillScore || 0;
  const expScore = gapRes.experienceScore !== undefined ? gapRes.experienceScore : (gapRes.overallScore > 60 ? 100 : 75);
  const locScore = gapRes.locationScore !== undefined ? gapRes.locationScore : 100;

  document.getElementById('skillScoreText').textContent = `${skillScore.toFixed(1)}%`;
  document.getElementById('skillScoreBar').style.width = `${skillScore}%`;

  document.getElementById('expScoreText').textContent = `${expScore.toFixed(1)}%`;
  document.getElementById('expScoreBar').style.width = `${expScore}%`;

  document.getElementById('locScoreText').textContent = `${locScore.toFixed(1)}%`;
  document.getElementById('locScoreBar').style.width = `${locScore}%`;
}

function updateRadialScore(score) {
  const circle = document.getElementById('dialScoreCircle');
  const text = document.getElementById('dialScoreValue');

  const circumference = 251.2; // 2 * pi * 40
  const offset = circumference - (score / 100) * circumference;

  circle.style.strokeDashoffset = offset;
  text.textContent = `${score.toFixed(1)}%`;
}

async function loadTopJobMatches() {
  const candidateId = document.getElementById('matchCandidateSelect').value;
  if (!candidateId) return;

  const container = document.getElementById('topRecommendationsContainer');
  const tbody = document.getElementById('topRecTableBody');
  tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">Calculating PriorityQueue top matches...</td></tr>`;
  container.style.display = 'block';

  const matches = await apiCall(`/api/match/candidate/${candidateId}?limit=5`);
  if (!matches || matches.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No matching recommendations available.</td></tr>`;
    return;
  }

  tbody.innerHTML = matches.map(m => `
    <tr>
      <td><strong>${m.jobTitle || 'Developer'}</strong></td>
      <td>${m.company || 'Tech Corp'}</td>
      <td>${m.location || 'Remote'}</td>
      <td>₹${((m.salaryMin || 600000)/100000).toFixed(1)}L - ₹${((m.salaryMax || 1200000)/100000).toFixed(1)}L</td>
      <td><span class="badge ${m.overallScore >= 75 ? 'badge-success' : 'badge-warning'}">${m.overallScore.toFixed(1)}%</span></td>
      <td><button class="btn btn-primary btn-sm" onclick="applyForJob(${m.jobId})">Apply</button></td>
    </tr>
  `).join('');
}

// ==========================================
// Skill Gap Analysis
// ==========================================
async function runSkillGapAnalysis() {
  const candidateId = document.getElementById('gapCandidateSelect').value;
  const jobId = document.getElementById('gapJobSelect').value;

  if (!candidateId || !jobId) return;

  const gap = await apiCall(`/api/match/gap/candidate/${candidateId}/job/${jobId}`);
  if (!gap) return;

  renderSkillChips('gapMatchedRequired', gap.matchedRequiredSkills || [], 'skill-req');
  renderSkillChips('gapMatchedPreferred', gap.matchedPreferredSkills || [], 'skill-pref');
  renderSkillChips('gapMissingRequired', gap.missingRequiredSkills || [], 'skill-req', true);
  renderSkillChips('gapMissingPreferred', gap.missingPreferredSkills || [], 'skill-pref', true);
}

function renderSkillChips(containerId, skills, baseClass, isMissing = false) {
  const el = document.getElementById(containerId);
  if (!el) return;

  if (skills.length === 0) {
    el.innerHTML = `<span style="color:var(--text-muted);font-size:0.85rem;">None</span>`;
    return;
  }

  el.innerHTML = skills.map(s => `
    <span class="skill-chip ${baseClass}" style="${isMissing ? 'opacity: 0.85; border-style: dashed;' : ''}">
      ${isMissing ? '✕ ' : '✓ '}${s}
    </span>
  `).join('');
}

// ==========================================
// Applications Center
// ==========================================
async function loadApplications() {
  const tbody = document.getElementById('applicationsTableBody');
  tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">Loading applications...</td></tr>`;

  if (!authToken) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-secondary);">Please sign in to view applications.</td></tr>`;
    return;
  }

  // If recruiter, load job applications
  let apps = [];
  if (currentUser && (currentUser.role === 'RECRUITER' || currentUser.role === 'ADMIN')) {
    // Recruiter can view applications for job 1 as demo, or their posted jobs
    apps = await apiCall('/api/jobs/1/applications');
  } else {
    // Candidate applications
    apps = await apiCall('/api/applications/my');
  }

  if (!apps || apps.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-muted);">No applications found. Apply to a job to see it here!</td></tr>`;
    return;
  }

  const isRecruiter = currentUser && (currentUser.role === 'RECRUITER' || currentUser.role === 'ADMIN');

  tbody.innerHTML = apps.map(a => {
    const candidateName = a.candidate ? (a.candidate.name || 'Candidate') : (currentUser ? currentUser.name : 'Candidate');
    const jobTitle = a.job ? `${a.job.title} @ ${a.job.company}` : 'Job Opportunity';
    const dateStr = a.appliedDate || '2026-08-28';

    let actionHtml = '';
    if (isRecruiter) {
      actionHtml = `
        <select class="app-status-select" onchange="updateAppStatus(${a.id}, this.value)">
          <option value="APPLIED" ${a.status === 'APPLIED' ? 'selected' : ''}>APPLIED</option>
          <option value="SHORTLISTED" ${a.status === 'SHORTLISTED' ? 'selected' : ''}>SHORTLISTED</option>
          <option value="INTERVIEW" ${a.status === 'INTERVIEW' ? 'selected' : ''}>INTERVIEW</option>
          <option value="SELECTED" ${a.status === 'SELECTED' ? 'selected' : ''}>SELECTED</option>
          <option value="REJECTED" ${a.status === 'REJECTED' ? 'selected' : ''}>REJECTED</option>
        </select>
      `;
    } else {
      actionHtml = `<span style="color:var(--text-muted);font-size:0.8rem;">In Review</span>`;
    }

    return `
      <tr>
        <td>#${a.id}</td>
        <td><strong>${candidateName}</strong></td>
        <td>${jobTitle}</td>
        <td>${dateStr}</td>
        <td><span class="badge ${getStatusBadgeClass(a.status)}">${a.status}</span></td>
        <td>${actionHtml}</td>
      </tr>
    `;
  }).join('');
}

function getStatusBadgeClass(status) {
  switch (status) {
    case 'SHORTLISTED': return 'badge-primary';
    case 'INTERVIEW': return 'badge-warning';
    case 'SELECTED': return 'badge-success';
    case 'REJECTED': return 'badge-danger';
    default: return 'badge-primary';
  }
}

async function updateAppStatus(appId, newStatus) {
  const res = await apiCall(`/api/applications/${appId}/status?status=${newStatus}`, { method: 'PUT' });
  if (res) {
    showToast(`Application #${appId} status changed to ${newStatus}`, 'success');
  }
}

// ==========================================
// Candidate Profile & Skill Editor
// ==========================================
async function loadCandidateProfile() {
  const container = document.getElementById('profileDetailsContainer');

  if (!currentCandidate && (!currentUser || currentUser.role !== 'CANDIDATE')) {
    container.innerHTML = `
      <div style="text-align:center;padding:24px;">
        <p style="color:var(--text-secondary);margin-bottom:12px;">You are currently viewing as Recruiter or Guest.</p>
        <button class="btn btn-primary" onclick="quickLogin('satya@example.com', 'password123', 'Satya Prakash (Candidate)')">
          Switch to Candidate (Satya)
        </button>
      </div>
    `;
    return;
  }

  const cand = currentCandidate || allCandidates[0];
  if (!cand) return;

  container.innerHTML = `
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:24px;">
      <div>
        <label style="font-size:0.8rem;color:var(--text-muted);">FULL NAME</label>
        <div style="font-size:1.1rem;font-weight:700;color:#fff;">${cand.name || currentUser.name}</div>
      </div>
      <div>
        <label style="font-size:0.8rem;color:var(--text-muted);">PREFERRED ROLE</label>
        <div style="font-size:1.1rem;font-weight:700;color:#fff;">${cand.preferredRole || 'Software Engineer'}</div>
      </div>
      <div>
        <label style="font-size:0.8rem;color:var(--text-muted);">EXPERIENCE</label>
        <div style="font-size:1.1rem;font-weight:700;color:#fff;">${cand.experience} Years</div>
      </div>
      <div>
        <label style="font-size:0.8rem;color:var(--text-muted);">LOCATION</label>
        <div style="font-size:1.1rem;font-weight:700;color:#fff;">${cand.preferredLocation || 'Hyderabad'}</div>
      </div>
    </div>

    <div style="border-top:1px solid var(--border-subtle);padding-top:20px;">
      <h3 style="font-size:1rem;margin-bottom:12px;">Candidate Skill Set</h3>
      <div class="skills-list" id="profileSkillChips" style="margin-bottom:16px;">
        ${(cand.skills || []).map(s => `
          <span class="skill-chip skill-req" style="cursor:pointer;" onclick="removeProfileSkill('${s}', ${cand.id})" title="Click to remove">
            ${s} ✕
          </span>
        `).join('')}
      </div>

      <div style="display:flex;gap:10px;">
        <input type="text" id="newSkillInput" class="input-field" placeholder="Add skill (e.g. Docker, AWS, React)..." style="flex:1;">
        <button class="btn btn-primary btn-sm" onclick="addProfileSkill(${cand.id})">+ Add Skill</button>
      </div>
    </div>
  `;
}

async function addProfileSkill(candidateId) {
  const input = document.getElementById('newSkillInput');
  const skillName = input.value.trim();
  if (!skillName) return;

  const cand = currentCandidate || allCandidates.find(c => c.id === candidateId);
  const updatedSkills = Array.from(new Set([...(cand.skills || []), skillName]));

  const res = await apiCall(`/api/candidates/${candidateId}/skills`, {
    method: 'PUT',
    body: JSON.stringify(updatedSkills)
  });

  if (res) {
    showToast(`Added ${skillName} to profile skills!`, 'success');
    cand.skills = updatedSkills;
    loadCandidateProfile();
  }
}

async function removeProfileSkill(skillName, candidateId) {
  const cand = currentCandidate || allCandidates.find(c => c.id === candidateId);
  const updatedSkills = (cand.skills || []).filter(s => s !== skillName);

  const res = await apiCall(`/api/candidates/${candidateId}/skills`, {
    method: 'PUT',
    body: JSON.stringify(updatedSkills)
  });

  if (res) {
    showToast(`Removed ${skillName}`, 'info');
    cand.skills = updatedSkills;
    loadCandidateProfile();
  }
}

// ==========================================
// Recruiter Job Posting
// ==========================================
async function handlePostJob(e) {
  e.preventDefault();

  const title = document.getElementById('jobTitle').value.trim();
  const company = document.getElementById('jobCompany').value.trim();
  const location = document.getElementById('jobLocation').value.trim();
  const jobType = document.getElementById('jobPostType').value;
  const experienceRequired = parseFloat(document.getElementById('jobExp').value);
  const salaryMin = parseFloat(document.getElementById('jobSalaryMin').value);
  const salaryMax = parseFloat(document.getElementById('jobSalaryMax').value);
  const description = document.getElementById('jobDescription').value.trim();

  const reqSkills = document.getElementById('jobReqSkills').value
    .split(',')
    .map(s => s.trim())
    .filter(Boolean);

  const prefSkills = document.getElementById('jobPrefSkills').value
    .split(',')
    .map(s => s.trim())
    .filter(Boolean);

  const payload = {
    title,
    company,
    location,
    jobType,
    experienceRequired,
    salaryMin,
    salaryMax,
    description,
    requiredSkills: reqSkills,
    preferredSkills: prefSkills
  };

  const res = await apiCall('/api/jobs', {
    method: 'POST',
    body: JSON.stringify(payload)
  });

  if (res) {
    showToast(`Job "${title}" published successfully!`, 'success');
    closeModal('postJobModal');
    document.getElementById('postJobForm').reset();
    await loadJobs();
    await loadCandidatesAndJobsForSelectors();
  }
}

// ==========================================
// Modals & Toasts
// ==========================================
function openModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('active');
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('active');
}

async function handleCustomLogin(e) {
  e.preventDefault();
  const email = document.getElementById('loginEmail').value.trim();
  const password = document.getElementById('loginPassword').value;
  closeModal('loginModal');
  await quickLogin(email, password, email);
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;

  const icon = type === 'success' ? '✓' : (type === 'error' ? '✕' : 'ℹ');
  toast.innerHTML = `<span><strong>${icon}</strong></span> <span>${message}</span>`;

  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}
