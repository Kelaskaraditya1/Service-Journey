/**
 * SessionService — API service for session lifecycle.
 *
 * Methods:
 *   startSession(userId)      → POST /session/start
 *   transitionEvent(payload)  → POST /session/event-transition
 *   endSession(sessionId)     → POST /session/end
 *
 * All calls go through apiFetch which handles session-expiry detection.
 * If any call returns a 401/403 or expired response, the user is
 * automatically redirected to /authentication. No component needs
 * to handle expiry individually.
 */

import apiFetch from "../utility/apiFetch.js";

const baseUrl = "http://localhost:8080/api/v1";

/**
 * Builds headers for session-related API calls.
 * Attaches X-Session-Id when sessionId is provided.
 */
let buildHeaders = (sessionId) => {
  let headers = { "Content-Type": "application/json" };
  if (sessionId) {
    headers["X-Session-Id"] = sessionId;
  }
  return headers;
};

/**
 * POST /session/start
 * Starts a Temporal workflow-backed session.
 *
 * Note: skipExpiryCheck=true because this is a fresh session creation.
 * There is no existing session to expire at this point.
 *
 * @param {string} userId - The user ID from login response
 * @returns {object} Response with sessionId and workflowId
 */
export let startSession = async (userId) => {
  try {
    let data = await apiFetch(
      `${baseUrl}/session/start`,
      {
        method: "POST",
        headers: buildHeaders(null),
        body: JSON.stringify({ userId }),
      },
      { skipExpiryCheck: true }
    );

    console.log("[SessionService] startSession response:", data);
    return data;
  } catch (error) {
    console.error("[SessionService] startSession failed:", error);
    return { success: false, message: "Failed to start session" };
  }
};

/**
 * POST /session/event-transition
 * Signals the Temporal workflow to transition between screens.
 *
 * If the session has expired (workflow ended due to inactivity/absolute timeout),
 * apiFetch will detect the 401 response and redirect to login automatically.
 *
 * @param {object} payload - { sessionId, previousScreenName, nextScreenName, timestamp }
 * @returns {object} Response confirming transition signal was sent
 */
export let transitionEvent = async (payload) => {
  try {
    let data = await apiFetch(
      `${baseUrl}/session/event-transition`,
      {
        method: "POST",
        headers: buildHeaders(payload.sessionId),
        body: JSON.stringify(payload),
      }
    );

    // If session expired, apiFetch already triggered redirect
    if (data._sessionExpired) {
      return data;
    }

    console.log(
      `[SessionService] transitionEvent: '${payload.previousScreenName || "START"}' → '${payload.nextScreenName}'`,
      data
    );
    return data;
  } catch (error) {
    console.error("[SessionService] transitionEvent failed:", error);
    return { success: false, message: "Failed to transition event" };
  }
};

/**
 * POST /session/end
 * Signals the Temporal workflow to end the session.
 *
 * Note: skipExpiryCheck=true because we are intentionally ending the session.
 * A 401 here just means the session already expired — that's fine, we're logging out anyway.
 *
 * @param {string} sessionId - The session to end
 * @returns {object} Response confirming end signal was sent
 */
export let endSession = async (sessionId) => {
  try {
    let data = await apiFetch(
      `${baseUrl}/session/end`,
      {
        method: "POST",
        headers: buildHeaders(sessionId),
        body: JSON.stringify({
          sessionId,
          expiryReasons: "LOGOUT",
        }),
      },
      { skipExpiryCheck: true }
    );

    console.log("[SessionService] endSession response:", data);
    return data;
  } catch (error) {
    console.error("[SessionService] endSession failed:", error);
    return { success: false, message: "Failed to end session" };
  }
};
