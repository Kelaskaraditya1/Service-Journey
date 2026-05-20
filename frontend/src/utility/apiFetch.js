/**
 * apiFetch — Centralized fetch wrapper with session-expiry detection.
 *
 * Every session-related API call goes through this wrapper.
 * If the backend returns a session-expired response, this:
 *   1. Logs the expiry reason
 *   2. Forces a redirect to /authentication
 *   3. The page reload naturally clears all React state (context, hooks, etc.)
 *
 * Detected expiry signals:
 *   - HTTP 401 (Unauthorized)  → interceptor detected absolute/inactivity timeout
 *   - HTTP 403 (Forbidden)     → access denied / workflow terminated
 *   - Response body with session-related error messages
 *
 * This is the SINGLE place that handles session expiry.
 * No component needs to check for expiry individually.
 */

/**
 * Checks whether a response indicates the session has expired.
 *
 * @param {Response} response — raw fetch Response object
 * @param {object} data — parsed JSON body
 * @returns {boolean} true if session is expired/invalid
 */
let isSessionExpired = (response, data) => {

  // HTTP 401 = interceptor detected timeout (absolute or inactivity)
  if (response.status === 401) return true;

  // HTTP 403 = forbidden / workflow terminated
  if (response.status === 403) return true;

  // Check response body for session-expired indicators
  if (data && data.success === false) {
    let message = (data.message || "").toLowerCase();

    if (
      message.includes("expired") ||
      message.includes("not active") ||
      message.includes("does not exist") ||
      message.includes("terminated") ||
      message.includes("unauthorized")
    ) {
      return true;
    }
  }

  return false;
};

/**
 * Forces a full redirect to the login page.
 * Using window.location (not React navigate) because:
 *   - It works from anywhere (not just inside React components)
 *   - The page reload clears ALL React state (context, hooks, memory)
 *   - Clean slate — no stale session data left behind
 *
 * @param {string} reason — why the session expired (for logging)
 */
let forceRedirectToLogin = (reason) => {
  console.error(`[apiFetch] SESSION EXPIRED — ${reason}. Redirecting to login...`);

  // Prevent multiple redirects if several API calls fail simultaneously
  if (window.__sessionExpiredRedirecting) return;
  window.__sessionExpiredRedirecting = true;

  // Small delay so the console.error is visible in DevTools
  setTimeout(() => {
    window.location.href = "/authentication";
  }, 100);
};

/**
 * Fetch wrapper that checks every response for session expiry.
 *
 * Usage (drop-in replacement for fetch):
 *   let data = await apiFetch(url, options);
 *   // If session is expired, user is already being redirected.
 *   // The returned data will have { _sessionExpired: true } so callers can bail out.
 *
 * @param {string} url — fetch URL
 * @param {object} options — fetch options (method, headers, body, etc.)
 * @param {object} config — additional config
 * @param {boolean} config.skipExpiryCheck — set true for login/start APIs that don't need expiry check
 * @returns {object} parsed JSON response, or expiry marker
 */
let apiFetch = async (url, options = {}, config = {}) => {

  let response = await fetch(url, options);
  let data = await response.json();

  // Skip expiry check for login and session-start APIs
  if (config.skipExpiryCheck) {
    return data;
  }

  // Check for session expiry
  if (isSessionExpired(response, data)) {
    let reason = data.message || `HTTP ${response.status}`;
    forceRedirectToLogin(reason);

    // Return a marker so calling code can bail out early
    return { ...data, _sessionExpired: true };
  }

  return data;
};

export default apiFetch;
export { isSessionExpired, forceRedirectToLogin };
