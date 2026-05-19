import "./WelcomePage.css";
import { useState } from "react";

let Header = () => {
  let [activeNav, setActiveNav] = useState("Dashboard");

  return (
    <div className="headerContainer">
      <header
        className="navbar navbar-expand-lg bg-white shadow-sm px-4"
        style={{ width: "100%" }}
      >
        <div className="container-fluid d-flex align-items-center">
          {/* Logo */}
          <a
            className="navbar-brand fw-bold"
            href="#"
            style={{
              color: "red",
              fontSize: "25px",
              width: "220px",
            }}
          >
            HDFC Bank
          </a>

          {/* Center Navigation */}
          <div
            style={{
              flex: 1,
              display: "flex",
              justifyContent: "center",
            }}
          >
            <ul
              className="navbar-nav d-flex flex-row gap-4"
              style={{
                listStyle: "none",
                marginBottom: "0",
              }}
            >
              <li className="nav-item">
                <a
                  className="nav-link fw-semibold"
                  href="#"
                  onClick={() => setActiveNav("Dashboard")}
                  style={{
                    color: activeNav === "Dashboard" ? "blue" : "black",

                    borderBottom:
                      activeNav === "Dashboard" ? "2px solid blue" : "none",

                    cursor: "pointer",
                  }}
                >
                  Dashboard
                </a>
              </li>

              <li className="nav-item">
                <a
                  className="nav-link"
                  href="#"
                  onClick={() => setActiveNav("Payments")}
                  style={{
                    color: activeNav === "Payments" ? "blue" : "black",

                    borderBottom:
                      activeNav === "Payments" ? "2px solid blue" : "none",

                    cursor: "pointer",
                  }}
                >
                  Payments
                </a>
              </li>

              <li className="nav-item">
                <a
                  className="nav-link"
                  href="#"
                  onClick={() => setActiveNav("Investments")}
                  style={{
                    color: activeNav === "Investments" ? "blue" : "black",

                    borderBottom:
                      activeNav === "Investments" ? "2px solid blue" : "none",

                    cursor: "pointer",
                  }}
                >
                  Investments
                </a>
              </li>

              <li className="nav-item">
                <a
                  className="nav-link"
                  href="#"
                  onClick={() => setActiveNav("Offers")}
                  style={{
                    color: activeNav === "Offers" ? "blue" : "black",

                    borderBottom:
                      activeNav === "Offers" ? "2px solid blue" : "none",

                    cursor: "pointer",
                  }}
                >
                  Offers
                </a>
              </li>
            </ul>
          </div>

          {/* Right Side Icons */}
          <div
            className="d-flex align-items-center gap-4"
            style={{
              fontSize: "18px",
              width: "220px",
              justifyContent: "flex-end",
            }}
          >
            <i className="fa-regular fa-bell"></i>

            <i className="fa-regular fa-circle-question"></i>

            <i className="fa-solid fa-arrow-right-from-bracket"></i>

            <img
              src="https://i.pravatar.cc/40"
              alt="profile"
              style={{
                width: "35px",
                height: "35px",
                borderRadius: "50%",
                cursor: "pointer",
              }}
            />
          </div>
        </div>
      </header>
    </div>
  );
};

export default Header;
