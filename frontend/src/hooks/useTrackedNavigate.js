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

      try {
        let result = await transitionEvent(payload);

        // If session expired, apiFetch is already redirecting to /authentication.
        // Block this navigation — don't let the user proceed to the next screen.
        if (result._sessionExpired) {
          console.warn(
            "[useTrackedNavigate] Session expired. Navigation to", toPath, "blocked."
          );
          return; // <-- Stop here. apiFetch is handling the redirect.
        }
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
