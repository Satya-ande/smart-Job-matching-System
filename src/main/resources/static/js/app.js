/**
 * SmartJob V2 — Interactive Single Page Application
 * Fixed version: corrects API field names, auth /me, applications endpoint,
 * job skill rendering, and login state management.
 */

const API_BASE = '';
let authToken = localStorage.getItem('smartjob_jwt') || '';
let currentUser = null;   // { id, name, email, role }
let currentCandidate = null; // CandidateResponse for logged-in CANDIDATE users
let allCandidates = [];
let allJobs = [];

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

  if (authToken) {
    await fetchCurrentUser();
  } else {
    // Auto demo-login as Satya (Candidate) for a rich first-impression
    await quickLogin('satya@example.com', 'password123', 'Satya Prakash', false);
  }

  // Load candidates and jobs for dropdowns AFTER potential auth
  await loadCandidatesAndJobsForSelectors();
});

// ==========================================
// API Client
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
        showToast('Session expired. Please sign in again.', 'error');
        logout(false);
      }
      return null;
    }

    if (response.status === 204) return true;

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
// Navigation
// ==========================================
function switchView(viewName) {
  document.querySelectorAll('.view-section').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));

  const section = document.getElementById(`view-${viewName}`);
  const navItem = document.getElementById(`nav-${viewName}`);
  if (section) section.classList.add('active');
  if (navItem) navItem.classList.add('active');

  if (viewName === 'applications') loadApplications();
  if (viewName === 'profile') loadCandidateProfile();
  if (viewName === 'matcher') { populateMatcherDropdowns(); }
  if (viewName === 'gap') { populateGapDropdowns(); }
}

// ==========================================
// Authentication
// ==========================================
async function quickLogin(email, password, displayName, notify = true) {
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
    // The login response has accessToken or token field
    authToken = data.accessToken || data.token || '';
    localStorage.setItem('smartjob_jwt', authToken);

    // Populate currentUser from login response directly (no extra /me call needed)
    currentUser = {
      id: data.id,
      name: data.name,
      email: data.email,
      role: data.role
    };

    updateUserStatusUI();
    if (notify) showToast(`Signed in as ${displayName || data.name}`, 'success');

    // Refresh all views
    await loadCandidatesAndJobsForSelectors();
    loadJobs();
    loadApplications();

  } catch (e) {
    showToast(e.message, 'error');
  }
}

async function fetchCurrentUser() {
  if (!authToken) return;

  const data = await apiCall('/api/auth/me');
  if (!data) {
    // Token is invalid/expired
    logout(false);
    return;
  }

  currentUser = {
    id: data.id,
    name: data.name,
    email: data.email,
    role: data.role
  };

  updateUserStatusUI();
}

function logout(notify = true) {
  authToken = '';
  currentUser = null;
  currentCandidate = null;
  localStorage.removeItem('smartjob_jwt');
  updateUserStatusUI();
  if (notify) showToast('Signed out successfully', 'info');
  loadJobs();
}

function updateUserStatusUI() {
  const section = document.getElementById('userStatusSection');
  if (!section) return;

  if (!currentUser) {
    section.innerHTML = `
      <button class="btn btn-secondary btn-sm" onclick="openModal('loginModal')">Sign In</button>
    `;
    return;
  }

  const initials = currentUser.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  const roleBadge = (currentUser.role || '').replace('ROLE_', '');

  const recruiterBtn = (currentUser.role === 'RECRUITER' || currentUser.role === 'ADMIN')
    ? `<button class="btn btn-primary btn-sm" onclick="openModal('postJobModal')">+ Post Job</button>`
    : '';

  section.innerHTML = `
    ${recruiterBtn}
    <div class="user-pill">
      <div class="user-avatar">${initials}</div>
      <div class="user-info">
        <span class="user-name">${currentUser.name}</span>
        <span class="user-role-badge">${roleBadge}</span>
      </div>
    </div>
    <button class="btn btn-secondary btn-sm" onclick="logout()">Sign Out</button>
  `;
}

// ==========================================
// Stats & dropdown population
// ==========================================
async function loadInitialStats() {
  const [jobsRes, candCountRes] = await Promise.all([
    apiCall('/api/jobs?size=1'),
    apiCall('/api/candidates/count')
  ]);

  if (jobsRes) document.getElementById('statTotalJobs').textContent = jobsRes.totalElements ?? '-';
  if (candCountRes) document.getElementById('statTotalCandidates').textContent = candCountRes.count ?? '-';
}

async function loadCandidatesAndJobsForSelectors() {
  // Candidates returns a flat List, jobs returns a Page
  const [cRes, jRes] = await Promise.all([
    apiCall('/api/candidates'),
    apiCall('/api/jobs?size=50')
  ]);

  if (Array.isArray(cRes)) {
    allCandidates = cRes;

    // Find current candidate profile if user is logged in as CANDIDATE
    if (currentUser && currentUser.role === 'CANDIDATE') {
      currentCandidate = allCandidates.find(c => c.email === currentUser.email)
        || allCandidates[0];
    }
  }

  if (jRes && jRes.content) {
    allJobs = jRes.content;
  }

  populateMatcherDropdowns();
  populateGapDropdowns();
}

function populateMatcherDropdowns() {
  populateCandidateSelect('matchCandidateSelect');
  populateJobSelect('matchJobSelect');
}

function populateGapDropdowns() {
  populateCandidateSelect('gapCandidateSelect');
  populateJobSelect('gapJobSelect');
}

function populateCandidateSelect(selectId) {
  const el = document.getElementById(selectId);
  if (!el || allCandidates.length === 0) return;
  el.innerHTML = allCandidates.map(c =>
    `<option value="${c.id}">${c.name} — ${c.preferredRole || 'Candidate'} (${c.experience} yrs)</option>`
  ).join('');

  // Pre-select current candidate if logged in
  if (currentCandidate) el.value = currentCandidate.id;
}

function populateJobSelect(selectId) {
  const el = document.getElementById(selectId);
  if (!el || allJobs.length === 0) return;
  el.innerHTML = allJobs.map(j =>
    `<option value="${j.id}">${j.title} @ ${j.company} (${j.location})</option>`
  ).join('');
}

// ==========================================
// Browse Jobs
// ==========================================
function setupFilterListeners() {
  let timer;
  const trigger = () => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      jobFilters.keyword  = document.getElementById('searchKeyword').value.trim();
      jobFilters.location = document.getElementById('filterLocation').value.trim();
      jobFilters.jobType  = document.getElementById('filterJobType').value;
      jobFilters.sort     = document.getElementById('sortJobs').value;
      jobFilters.page = 0;
      loadJobs();
    }, 280);
  };

  ['searchKeyword', 'filterLocation'].forEach(id =>
    document.getElementById(id)?.addEventListener('input', trigger));
  ['filterJobType', 'sortJobs'].forEach(id =>
    document.getElementById(id)?.addEventListener('change', trigger));
}

function resetJobFilters() {
  document.getElementById('searchKeyword').value = '';
  document.getElementById('filterLocation').value = '';
  document.getElementById('filterJobType').value = '';
  document.getElementById('sortJobs').value = 'id,desc';
  jobFilters = { keyword: '', location: '', jobType: '', sort: 'id,desc', page: 0, size: 6 };
  loadJobs();
}

async function loadJobs() {
  const container = document.getElementById('jobsGridContainer');
  container.innerHTML = `<div style="grid-column:1/-1;text-align:center;padding:48px;color:var(--text-muted)">Loading jobs...</div>`;

  let query = `/api/jobs?page=${jobFilters.page}&size=${jobFilters.size}&sort=${jobFilters.sort}`;
  if (jobFilters.keyword)  query += `&keyword=${encodeURIComponent(jobFilters.keyword)}`;
  if (jobFilters.location) query += `&location=${encodeURIComponent(jobFilters.location)}`;
  if (jobFilters.jobType)  query += `&jobType=${encodeURIComponent(jobFilters.jobType)}`;

  const res = await apiCall(query);
  if (!res || !res.content || res.content.length === 0) {
    container.innerHTML = `<div style="grid-column:1/-1;text-align:center;padding:48px;color:var(--text-secondary)">No jobs found. Try adjusting your filters.</div>`;
    renderPagination(0, 0);
    return;
  }

  container.innerHTML = res.content.map(job => renderJobCard(job)).join('');
  renderPagination(res.number, res.totalPages);

  // Keep allJobs in sync so dropdowns stay populated
  if (allJobs.length === 0) allJobs = res.content;
}

function extractSkills(job) {
  // API returns: job.skills = [{name, required, weight}]
  const req = [], pref = [];
  (job.skills || []).forEach(s => {
    if (s.required) req.push(s.name);
    else pref.push(s.name);
  });
  return { req, pref };
}

function renderJobCard(job) {
  const { req, pref } = extractSkills(job);
  const salaryFmt = `₹${(job.salaryMin / 100000).toFixed(1)}L – ₹${(job.salaryMax / 100000).toFixed(1)}L`;
  const jobTypeFmt = (job.jobType || 'FULL_TIME').replace(/_/g, ' ');

  return `
  <div class="job-card">
    <div>
      <div class="job-header">
        <div>
          <h3 class="job-title">${job.title}</h3>
          <div class="job-company">🏢 ${job.company}</div>
        </div>
        <span class="badge badge-primary">${jobTypeFmt}</span>
      </div>

      <div class="job-details">
        <span class="job-meta-item">📍 ${job.location}</span>
        <span class="job-meta-item">⏱️ ${job.experienceRequired} yrs req.</span>
      </div>

      <p class="job-description">${job.description || 'No description provided.'}</p>

      <div class="skills-wrapper">
        <div class="skills-heading">Required Skills (Weight: 5)</div>
        <div class="skills-list">
          ${req.length ? req.map(s => `<span class="skill-chip skill-req">${s}</span>`).join('') : '<span style="color:var(--text-muted);font-size:0.75rem">None specified</span>'}
        </div>
      </div>

      ${pref.length ? `
      <div class="skills-wrapper">
        <div class="skills-heading">Preferred Skills (Weight: 2)</div>
        <div class="skills-list">
          ${pref.map(s => `<span class="skill-chip skill-pref">${s}</span>`).join('')}
        </div>
      </div>` : ''}
    </div>

    <div class="job-footer">
      <div class="job-salary">${salaryFmt}</div>
      <div class="job-card-actions">
        <button class="btn btn-secondary btn-sm" onclick="matchSpecificJob(${job.id})">⚡ Match</button>
        <button class="btn btn-primary btn-sm" onclick="applyForJob(${job.id})">Apply</button>
      </div>
    </div>
  </div>`;
}

function renderPagination(currentPage, totalPages) {
  const c = document.getElementById('jobsPagination');
  if (!c || totalPages <= 1) { if (c) c.innerHTML = ''; return; }
  c.innerHTML = `
    <button class="btn btn-secondary btn-sm" ${currentPage === 0 ? 'disabled' : ''} onclick="changeJobPage(${currentPage - 1})">← Prev</button>
    <span style="display:flex;align-items:center;color:var(--text-secondary);font-size:0.88rem">Page ${currentPage + 1} of ${totalPages}</span>
    <button class="btn btn-secondary btn-sm" ${currentPage >= totalPages - 1 ? 'disabled' : ''} onclick="changeJobPage(${currentPage + 1})">Next →</button>
  `;
}

function changeJobPage(p) { jobFilters.page = p; loadJobs(); }

async function applyForJob(jobId) {
  if (!authToken || !currentUser) {
    showToast('Please sign in to apply for jobs', 'error');
    openModal('loginModal');
    return;
  }

  if (currentUser.role === 'RECRUITER' || currentUser.role === 'ADMIN') {
    showToast('Only candidates can apply for jobs', 'error');
    return;
  }

  // Resolve candidateId
  let candidateId = currentCandidate?.id;
  if (!candidateId) {
    const cand = allCandidates.find(c => c.email === currentUser.email);
    candidateId = cand?.id;
  }
  if (!candidateId) {
    showToast('Candidate profile not found. Please set up your profile first.', 'error');
    return;
  }

  const res = await apiCall(`/api/jobs/${jobId}/apply?candidateId=${candidateId}`, { method: 'POST' });
  if (res) {
    showToast('Application submitted! Track it in the Applications tab.', 'success');
  }
}

function matchSpecificJob(jobId) {
  const sel = document.getElementById('matchJobSelect');
  if (sel) sel.value = jobId;
  switchView('matcher');
  runMatchCalculation();
}

// ==========================================
// Smart Matcher (DSA Engine)
// ==========================================
async function runMatchCalculation() {
  const candidateId = document.getElementById('matchCandidateSelect')?.value;
  const jobId = document.getElementById('matchJobSelect')?.value;
  if (!candidateId || !jobId) return;

  // Correct endpoint: GET /api/candidates/{id}/jobs/{jobId}/skill-gap
  const gapRes = await apiCall(`/api/candidates/${candidateId}/jobs/${jobId}/skill-gap`);
  if (!gapRes) return;

  // Fields: matchScore, skillScore, experienceScore, locationScore
  const overall  = gapRes.matchScore ?? gapRes.overallScore ?? 0;
  const skillSc  = gapRes.skillScore ?? 0;
  const expSc    = gapRes.experienceScore ?? 0;
  const locSc    = gapRes.locationScore ?? 0;

  updateRadialScore(overall);
  setBar('skillScoreBar', 'skillScoreText', skillSc, '#6366f1');
  setBar('expScoreBar',   'expScoreText',   expSc,   '#06b6d4');
  setBar('locScoreBar',   'locScoreText',   locSc,   '#10b981');
}

function setBar(barId, textId, value, color) {
  const bar = document.getElementById(barId);
  const txt = document.getElementById(textId);
  if (bar) { bar.style.width = `${value}%`; bar.style.background = color; }
  if (txt) txt.textContent = `${value.toFixed(1)}%`;
}

function updateRadialScore(score) {
  const circle = document.getElementById('dialScoreCircle');
  const text   = document.getElementById('dialScoreValue');
  if (!circle || !text) return;
  const circumference = 2 * Math.PI * 40; // 251.327
  circle.style.strokeDashoffset = circumference - (score / 100) * circumference;
  text.textContent = `${score.toFixed(1)}%`;
}

async function loadTopJobMatches() {
  const candidateId = document.getElementById('matchCandidateSelect')?.value;
  if (!candidateId) { showToast('Select a candidate first', 'error'); return; }

  const container = document.getElementById('topRecommendationsContainer');
  const tbody     = document.getElementById('topRecTableBody');
  container.style.display = 'block';
  tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:16px;color:var(--text-muted)">Calculating top matches…</td></tr>`;

  // Correct endpoint: GET /api/candidates/{id}/matches?topK=5
  const matches = await apiCall(`/api/candidates/${candidateId}/matches?topK=5`);
  if (!matches || matches.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-muted)">No recommendations available.</td></tr>`;
    return;
  }

  tbody.innerHTML = matches.map(m => {
    // Fields from MatchResultResponse
    const score   = m.matchScore ?? m.overallScore ?? 0;
    const salMin  = ((m.salaryMin || 0) / 100000).toFixed(1);
    const salMax  = ((m.salaryMax || 0) / 100000).toFixed(1);
    const cls     = score >= 75 ? 'badge-success' : 'badge-warning';
    return `
      <tr>
        <td><strong>${m.jobTitle || '–'}</strong></td>
        <td>${m.company || '–'}</td>
        <td>${m.location || '–'}</td>
        <td>₹${salMin}L – ₹${salMax}L</td>
        <td><span class="badge ${cls}">${score.toFixed(1)}%</span></td>
        <td><button class="btn btn-primary btn-sm" onclick="applyForJob(${m.jobId})">Apply</button></td>
      </tr>`;
  }).join('');
}

// ==========================================
// Skill Gap Analysis
// ==========================================
async function runSkillGapAnalysis() {
  const candidateId = document.getElementById('gapCandidateSelect')?.value;
  const jobId       = document.getElementById('gapJobSelect')?.value;
  if (!candidateId || !jobId) return;

  // Correct endpoint: GET /api/candidates/{id}/jobs/{jobId}/skill-gap
  const gap = await apiCall(`/api/candidates/${candidateId}/jobs/${jobId}/skill-gap`);
  if (!gap) return;

  // Fields: matchingRequiredSkills, missingRequiredSkills, matchingPreferredSkills, missingPreferredSkills
  renderSkillChips('gapMatchedRequired',   toArray(gap.matchingRequiredSkills),  'skill-req',  false);
  renderSkillChips('gapMatchedPreferred',  toArray(gap.matchingPreferredSkills), 'skill-pref', false);
  renderSkillChips('gapMissingRequired',   toArray(gap.missingRequiredSkills),   'skill-req',  true);
  renderSkillChips('gapMissingPreferred',  toArray(gap.missingPreferredSkills),  'skill-pref', true);
}

function toArray(val) {
  if (!val) return [];
  if (Array.isArray(val)) return val;
  return Array.from(val);
}

function renderSkillChips(containerId, skills, chipClass, isMissing) {
  const el = document.getElementById(containerId);
  if (!el) return;
  if (!skills || skills.length === 0) {
    el.innerHTML = `<span style="color:var(--text-muted);font-size:0.85rem">None</span>`;
    return;
  }
  el.innerHTML = skills.map(s => `
    <span class="skill-chip ${chipClass}" style="${isMissing ? 'border-style:dashed;opacity:0.85' : ''}">
      ${isMissing ? '✕' : '✓'} ${s}
    </span>`).join('');
}

// ==========================================
// Applications
// ==========================================
async function loadApplications() {
  const tbody = document.getElementById('applicationsTableBody');
  if (!tbody) return;
  tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:16px;color:var(--text-muted)">Loading…</td></tr>`;

  if (!authToken || !currentUser) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-secondary)">Please sign in to view applications.</td></tr>`;
    return;
  }

  let apps = null;
  const isRecruiter = currentUser.role === 'RECRUITER' || currentUser.role === 'ADMIN';

  if (isRecruiter) {
    // Load applications for their first posted job as a preview
    if (allJobs.length > 0) {
      apps = await apiCall(`/api/jobs/${allJobs[0].id}/applications`);
    }
  } else {
    // Candidate: load by candidateId (correct endpoint is /api/candidates/{id}/applications)
    let cId = currentCandidate?.id;
    if (!cId) cId = allCandidates.find(c => c.email === currentUser.email)?.id;
    if (cId) {
      apps = await apiCall(`/api/candidates/${cId}/applications`);
    }
  }

  if (!apps || apps.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-muted)">No applications found. Apply to a job to see it here!</td></tr>`;
    return;
  }

  tbody.innerHTML = apps.map(a => {
    const candidateName = a.candidateName || (currentUser?.name) || 'Candidate';
    const jobTitle = a.jobTitle ? `${a.jobTitle} @ ${a.company || ''}` : 'Job';
    const dateStr  = a.appliedDate || '–';

    const actionHtml = isRecruiter
      ? `<select class="app-status-select" onchange="updateAppStatus(${a.id}, this.value)">
           ${['APPLIED','SHORTLISTED','INTERVIEW','SELECTED','REJECTED'].map(s =>
             `<option value="${s}" ${a.status === s ? 'selected' : ''}>${s}</option>`
           ).join('')}
         </select>`
      : `<span class="badge ${getStatusBadgeClass(a.status)}">${a.status}</span>`;

    return `
      <tr>
        <td>#${a.id}</td>
        <td><strong>${candidateName}</strong></td>
        <td>${jobTitle}</td>
        <td>${dateStr}</td>
        <td><span class="badge ${getStatusBadgeClass(a.status)}">${a.status}</span></td>
        <td>${actionHtml}</td>
      </tr>`;
  }).join('');
}

function getStatusBadgeClass(status) {
  switch(status) {
    case 'SHORTLISTED': return 'badge-primary';
    case 'INTERVIEW':   return 'badge-warning';
    case 'SELECTED':    return 'badge-success';
    case 'REJECTED':    return 'badge-danger';
    default:            return 'badge-primary';
  }
}

async function updateAppStatus(appId, newStatus) {
  const res = await apiCall(`/api/applications/${appId}/status?status=${newStatus}`, { method: 'PUT' });
  if (res) showToast(`Application #${appId} updated to ${newStatus}`, 'success');
}

// ==========================================
// Candidate Profile
// ==========================================
async function loadCandidateProfile() {
  const container = document.getElementById('profileDetailsContainer');
  if (!container) return;

  if (!currentUser || currentUser.role === 'RECRUITER' || currentUser.role === 'ADMIN') {
    container.innerHTML = `
      <div style="text-align:center;padding:32px">
        <p style="color:var(--text-secondary);margin-bottom:16px">You are signed in as <strong>${currentUser?.role || 'Guest'}</strong>. Profile management is for candidates.</p>
        <button class="btn btn-primary" onclick="quickLogin('satya@example.com','password123','Satya Prakash')">
          Switch to Candidate (Satya)
        </button>
      </div>`;
    return;
  }

  if (!currentCandidate) {
    currentCandidate = allCandidates.find(c => c.email === currentUser?.email) || allCandidates[0];
  }

  if (!currentCandidate) {
    container.innerHTML = `<p style="color:var(--text-secondary)">No candidate profile found. Please contact support.</p>`;
    return;
  }

  const cand = currentCandidate;
  container.innerHTML = `
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:24px">
      <div>
        <div style="font-size:0.75rem;color:var(--text-muted);text-transform:uppercase;letter-spacing:0.05em;margin-bottom:4px">FULL NAME</div>
        <div style="font-size:1.1rem;font-weight:700">${cand.name}</div>
      </div>
      <div>
        <div style="font-size:0.75rem;color:var(--text-muted);text-transform:uppercase;letter-spacing:0.05em;margin-bottom:4px">PREFERRED ROLE</div>
        <div style="font-size:1.1rem;font-weight:700">${cand.preferredRole || '–'}</div>
      </div>
      <div>
        <div style="font-size:0.75rem;color:var(--text-muted);text-transform:uppercase;letter-spacing:0.05em;margin-bottom:4px">EXPERIENCE</div>
        <div style="font-size:1.1rem;font-weight:700">${cand.experience} Years</div>
      </div>
      <div>
        <div style="font-size:0.75rem;color:var(--text-muted);text-transform:uppercase;letter-spacing:0.05em;margin-bottom:4px">PREFERRED LOCATION</div>
        <div style="font-size:1.1rem;font-weight:700">${cand.preferredLocation || '–'}</div>
      </div>
    </div>

    <div style="border-top:1px solid var(--border-subtle);padding-top:20px">
      <h3 style="font-size:1rem;font-weight:700;margin-bottom:12px">Your Skill Tags</h3>
      <div class="skills-list" style="margin-bottom:16px">
        ${(cand.skills || []).map(s => `
          <span class="skill-chip skill-req" style="cursor:pointer" onclick="removeSkill('${s}',${cand.id})" title="Click to remove">
            ${s} ✕
          </span>`).join('') || '<span style="color:var(--text-muted)">No skills added yet.</span>'}
      </div>

      <div style="display:flex;gap:10px">
        <input type="text" id="newSkillInput" class="input-field" placeholder="Add a skill e.g. Kubernetes, React..." style="flex:1"
               onkeydown="if(event.key==='Enter')addSkill(${cand.id})">
        <button class="btn btn-primary btn-sm" onclick="addSkill(${cand.id})">+ Add</button>
      </div>
    </div>`;
}

async function addSkill(candidateId) {
  const input = document.getElementById('newSkillInput');
  const name  = input?.value.trim();
  if (!name) return;

  const cand = allCandidates.find(c => c.id === candidateId) || currentCandidate;
  const updated = Array.from(new Set([...(cand.skills || []), name]));

  // PUT /api/candidates/{id}/skills replaces the full skill list
  const res = await apiCall(`/api/candidates/${candidateId}/skills`, {
    method: 'PUT',
    body: JSON.stringify(updated)
  });

  if (res) {
    showToast(`Added "${name}" to your profile`, 'success');
    cand.skills = res.skills || updated;
    if (currentCandidate?.id === candidateId) currentCandidate = { ...cand, skills: res.skills || updated };
    if (input) input.value = '';
    loadCandidateProfile();
  }
}

async function removeSkill(skillName, candidateId) {
  const cand = allCandidates.find(c => c.id === candidateId) || currentCandidate;
  const updated = (cand.skills || []).filter(s => s !== skillName);

  // PUT /api/candidates/{id}/skills replaces the full skill list
  const res = await apiCall(`/api/candidates/${candidateId}/skills`, {
    method: 'PUT',
    body: JSON.stringify(updated)
  });

  if (res) {
    showToast(`Removed "${skillName}"`, 'info');
    cand.skills = res.skills || updated;
    if (currentCandidate?.id === candidateId) currentCandidate = { ...cand, skills: res.skills || updated };
    loadCandidateProfile();
  }
}

// ==========================================
// Post a Job (Recruiter)
// ==========================================
async function handlePostJob(e) {
  e.preventDefault();

  // Build skills in the correct {name, required, weight} format expected by JobCreateRequest
  const reqSkills  = document.getElementById('jobReqSkills').value
    .split(',').map(s => s.trim()).filter(Boolean)
    .map(name => ({ name, required: true,  weight: 5 }));
  const prefSkills = document.getElementById('jobPrefSkills').value
    .split(',').map(s => s.trim()).filter(Boolean)
    .map(name => ({ name, required: false, weight: 2 }));

  const payload = {
    title:              document.getElementById('jobTitle').value.trim(),
    company:            document.getElementById('jobCompany').value.trim(),
    location:           document.getElementById('jobLocation').value.trim(),
    jobType:            document.getElementById('jobPostType').value,
    experienceRequired: parseFloat(document.getElementById('jobExp').value) || 0,
    salaryMin:          parseFloat(document.getElementById('jobSalaryMin').value) || 0,
    salaryMax:          parseFloat(document.getElementById('jobSalaryMax').value) || 0,
    description:        document.getElementById('jobDescription').value.trim(),
    skills:             [...reqSkills, ...prefSkills]
  };

  // recruiterId is the currentUser's id (User entity linked to Recruiter/Admin role)
  const recruiterId = currentUser?.id;
  if (!recruiterId) {
    showToast('Could not resolve recruiter ID. Please sign in again.', 'error');
    return;
  }

  const res = await apiCall(`/api/jobs?recruiterId=${recruiterId}`, { method: 'POST', body: JSON.stringify(payload) });
  if (res) {
    showToast(`"${payload.title}" posted successfully!`, 'success');
    closeModal('postJobModal');
    document.getElementById('postJobForm').reset();
    await loadJobs();
    await loadCandidatesAndJobsForSelectors();
  }
}

// ==========================================
// Login Modal
// ==========================================
async function handleCustomLogin(e) {
  e.preventDefault();
  const email    = document.getElementById('loginEmail').value.trim();
  const password = document.getElementById('loginPassword').value;
  closeModal('loginModal');
  await quickLogin(email, password, email);
}

// ==========================================
// Modals
// ==========================================
function openModal(id) {
  document.getElementById(id)?.classList.add('active');
}

function closeModal(id) {
  document.getElementById(id)?.classList.remove('active');
}

// Close modals on overlay click
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('active');
  }
});

// ==========================================
// Toasts
// ==========================================
function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  const icons = { success: '✓', error: '✕', info: 'ℹ', warning: '⚠' };
  toast.innerHTML = `<span style="font-weight:700">${icons[type] || 'ℹ'}</span><span>${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.transition = 'all 0.3s ease';
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 320);
  }, 3800);
}
