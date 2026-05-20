/**
 * SessionService — API service for session lifecycle.
 *
 * Methods:
 *   startSession(userId)      → POST /session/start
 *   transitionEvent(payload)  → POST /session/event-transition
 *   endSession(sessionId)     → POST /session/end
 *
 * All calls include X-Session-Id header when a sessionId is available.
 */

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
 * @param {string} userId - The user ID from login response
 * @returns {object} Response with sessionId and workflowId
 */
export let startSession = async (userId) => {
  try {
    let response = await fetch(`${baseUrl}/session/start`, {
      method: "POST",
      headers: buildHeaders(null),
      body: JSON.stringify({ userId }),
    });

    let data = await response.json();
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
 * @param {object} payload - { sessionId, previousScreenName, nextScreenName, timestamp }
 * @returns {object} Response confirming transition signal was sent
 */
export let transitionEvent = async (payload) => {
  try {
    let response = await fetch(`${baseUrl}/session/event-transition`, {
      method: "POST",
      headers: buildHeaders(payload.sessionId),
      body: JSON.stringify(payload),
    });

    let data = await response.json();
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
 * @param {string} sessionId - The session to end
 * @returns {object} Response confirming end signal was sent
 */
export let endSession = async (sessionId) => {
  try {
    let response = await fetch(`${baseUrl}/session/end`, {
      method: "POST",
      headers: buildHeaders(sessionId),
      body: JSON.stringify({
        sessionId,
        expiryReasons: "LOGOUT",
      }),
    });

    let data = await response.json();
    console.log("[SessionService] endSession response:", data);
    return data;
  } catch (error) {
    console.error("[SessionService] endSession failed:", error);
    return { success: false, message: "Failed to end session" };
  }
};
