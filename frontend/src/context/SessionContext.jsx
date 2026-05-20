import { createContext, useContext, useState } from "react";

/**
 * SessionContext — Global state for user session management.
 *
 * Stores:
 *   - user        → user data from /auth/login response
 *   - sessionId   → from /session/start response
 *   - currentPage → current screen name (for transition tracking)
 *   - isLoggedIn  → whether user has an active session
 *
 * This context survives navigation across routes because
 * SessionProvider wraps the entire RouterProvider.
 */

const SessionContext = createContext(null);

export let SessionProvider = ({ children }) => {

  let [user, setUser] = useState(null);
  let [sessionId, setSessionId] = useState(null);
  let [currentPage, setCurrentPage] = useState(null);
  let [isLoggedIn, setIsLoggedIn] = useState(false);

  let loginUser = (userData, newSessionId) => {
    setUser(userData);
    setSessionId(newSessionId);
    setIsLoggedIn(true);
    console.log("[SessionContext] User logged in, session:", newSessionId);
  };

  let logoutUser = () => {
    console.log("[SessionContext] User logged out, clearing session:", sessionId);
    setUser(null);
    setSessionId(null);
    setCurrentPage(null);
    setIsLoggedIn(false);
  };

  let updateCurrentPage = (pageName) => {
    setCurrentPage(pageName);
  };

  let value = {
    user,
    sessionId,
    currentPage,
    isLoggedIn,
    loginUser,
    logoutUser,
    updateCurrentPage,
  };

  return (
    <SessionContext.Provider value={value}>
      {children}
    </SessionContext.Provider>
  );
};

/**
 * Custom hook to access session context.
 * Usage: let { user, sessionId, ... } = useSession();
 */
export let useSession = () => {
  let context = useContext(SessionContext);
  if (!context) {
    throw new Error("useSession must be used within a SessionProvider");
  }
  return context;
};

export default SessionContext;
