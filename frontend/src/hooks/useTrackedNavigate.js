import { useNavigate } from "react-router-dom";
import { useSession } from "../context/SessionContext.jsx";
import { transitionEvent } from "../services/SessionService.js";

/**
 * Screen name mapping — maps route paths to human-readable screen names.
 * These names appear in the database Events table and Temporal workflow logs.
 */
const SCREEN_NAMES = {
  "/": "KYC_Checklist",
  "/authentication": "Authentication",
  "/otp": "OTP_Verification",
  "/otp/error-page": "OTP_Error",
  "/confirm-you": "Confirm_Identity",
  "/verification/debit": "Debit_Card_Verification",
  "/verification/net": "Net_Banking_Verification",
  "/welcome": "Welcome_Dashboard",
};

/**
 * useTrackedNavigate — Custom hook that wraps react-router navigate().
 *
 * Before every navigation:
 *   1. Calls POST /session/event-transition (if session is active)
 *   2. Updates the current page in context
 *   3. Then navigates to the new route
 *
 * Usage:
 *   let trackedNavigate = useTrackedNavigate();
 *   trackedNavigate("/otp");
 *
 * If no session is active, it just navigates normally (no API call).
 */
let useTrackedNavigate = () => {
  let navigate = useNavigate();
  let { sessionId, currentPage, updateCurrentPage } = useSession();

  let trackedNavigate = async (toPath) => {

    let nextScreenName = SCREEN_NAMES[toPath] || toPath;

    // Only call transition API if we have an active session
    if (sessionId) {

      let payload = {
        sessionId,
        previousScreenName: currentPage,
        nextScreenName,
        timestamp: Date.now(),
      };

      // Fire transition — don't block navigation on failure
      try {
        await transitionEvent(payload);
      } catch (error) {
        console.warn("[useTrackedNavigate] Transition failed, navigating anyway:", error);
      }
    }
    

    // Update context and navigate
    updateCurrentPage(nextScreenName);
    navigate(toPath);
  };

  return trackedNavigate;
};

export default useTrackedNavigate;
export { SCREEN_NAMES };
