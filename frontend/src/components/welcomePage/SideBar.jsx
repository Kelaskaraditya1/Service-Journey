import "./WelcomePage.css";
import { useSession } from "../../context/SessionContext.jsx";
import { endSession } from "../../services/SessionService.js";
import { useNavigate } from "react-router-dom";

let Sidebar = () => {

  let { user, sessionId, logoutUser } = useSession();
  let navigate = useNavigate();

  let handleLogout = async () => {
    console.log("[Sidebar] Logout clicked, ending session:", sessionId);

    if (sessionId) {
      try {
        await endSession(sessionId);
        console.log("[Sidebar] Session end signal sent");
      } catch (error) {
        console.warn("[Sidebar] Failed to end session, logging out anyway:", error);
      }
    }

    logoutUser();
    navigate("/");
  };

  let displayName = user?.name || "User";

  return (
    <div className="sidebar">
      {/* User Card */}

      <div className="userCard">
        <div className="userIcon">
          <i className="fa-regular fa-user"></i>
        </div>

        <div>
          <p className="welcomeText">Welcome back,</p>

          <h4 className="verifiedText">
            {displayName}
          </h4>
        </div>
      </div>

      {/* Secure Message Button */}

      <button className="secureButton">Secure Message</button>

      {/* Main Navigation */}

      <div className="sidebarMenu">
        <div className="menuItem activeMenu">
          <i className="fa-solid fa-building-columns"></i>

          <span>Accounts</span>
        </div>

        <div className="menuItem">
          <i className="fa-solid fa-right-left"></i>

          <span>Transfers</span>
        </div>

        <div className="menuItem">
          <i className="fa-regular fa-clipboard"></i>

          <span>Bill Pay</span>
        </div>

        <div className="menuItem">
          <i className="fa-regular fa-credit-card"></i>

          <span>Cards</span>
        </div>
      </div>

      {/* Bottom Section */}

      <div className="bottomMenu">
        <div className="menuItem">
          <i className="fa-solid fa-gear"></i>

          <span>Settings</span>
        </div>

        <div className="menuItem">
          <i className="fa-solid fa-shield-halved"></i>

          <span>Security</span>
        </div>

        <div className="menuItem" onClick={handleLogout} style={{ cursor: "pointer", color: "#dc3545" }}>
          <i className="fa-solid fa-right-from-bracket"></i>

          <span style={{ fontWeight: 600 }}>Logout</span>
        </div>
      </div>
    </div>
  );
};

export default Sidebar;
