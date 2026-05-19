import "./WelcomePage.css";

let Sidebar = () => {
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
            Aditya
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
      </div>
    </div>
  );
};

export default Sidebar;
