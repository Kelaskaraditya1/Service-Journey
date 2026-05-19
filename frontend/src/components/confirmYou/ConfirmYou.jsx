import { useState } from "react";
import "./ConfirmYou.css";
import { useNavigate } from "react-router-dom";

let ConfirmYou = () => {
  let [getSelectedIdentity, setSelectedIdentity] = useState("DEBIT");

  let navigate = useNavigate();

  let submit = () => {
    if (getSelectedIdentity === "DEBIT") navigate("/verification/debit");
    else navigate("/verification/net");
  };

  return (
    <>
      <div className="main">
        <div className="card">
          <center>
            <h3 style={{ marginTop: "30px", marginBottom: "20px" }}>
              Confirm it's You
            </h3>
            <h6 style={{ marginTop: "20px" }}>
              Select an option to confirm your identity.
            </h6>

            <div className="identityContainer">
              <div className="identityCard">
                <i
                  className="fa-solid fa-credit-card"
                  style={{
                    color: "rgb(98, 162, 231)",
                    padding: "10px",
                    fontSize: "30px",
                  }}
                ></i>

                <div style={{ flexDirection: "column", marginLeft: "20px" }}>
                  <h3
                    style={{
                      paddingTop: "5px",
                      fontSize: "22px",
                      fontWeight: "650",
                    }}
                  >
                    Debit Card
                  </h3>
                  <h6
                    style={{
                      fontWeight: "350",
                      marginTop: "10px",
                      fontSize: "15px",
                    }}
                  >
                    Requires last 4 Digits , Expiry, CVV & PIN
                  </h6>
                </div>

                <input
                  type="radio"
                  style={{
                    marginBottom: "45px",
                    height: "15px",
                    width: "15px",
                    marginLeft: "2px",
                  }}
                  readOnly
                  checked={getSelectedIdentity === "DEBIT"}
                  onClick={() => setSelectedIdentity("DEBIT")}
                ></input>
              </div>

              <div className="identityCard">
                <i
                  className="fa-solid fa-globe"
                  style={{
                    color: "rgb(41, 244, 55)",
                    padding: "10px",
                    fontSize: "30px",
                  }}
                ></i>

                <div style={{ flexDirection: "column", marginLeft: "20px" }}>
                  <h3
                    style={{
                      paddingTop: "5px",
                      fontSize: "22px",
                      fontWeight: "650",
                    }}
                  >
                    Net Banking
                  </h3>
                  <h6
                    style={{
                      fontWeight: "350",
                      marginTop: "10px",
                      fontSize: "15px",
                    }}
                  >
                    Requires Customer ID & Password/PIN
                  </h6>
                </div>
                <input
                  type="radio"
                  style={{
                    marginBottom: "45px",
                    height: "15px",
                    width: "15px",
                    marginLeft: "18px",
                  }}
                  readOnly
                  checked={getSelectedIdentity === "NET"}
                  onClick={() => setSelectedIdentity("NET")}
                ></input>
              </div>
            </div>

            <div style={{ display: "flex", justifyContent: "center" }}>
              <i
                className="fa-solid fa-shield"
                style={{ color: "rgb(0, 0, 0)" }}
              ></i>
              <h6 style={{ marginBottom: "40px", marginLeft: "10px" }}>
                HDFC Bank does not store your information.
              </h6>
            </div>

            <button
              type="button"
              className="btn btn-primary"
              style={{
                width: "320px",
                marginBottom: "40px",
                borderRadius: "12px",
              }}
              onClick={submit}
            >{`Continue >`}</button>
            <div></div>
          </center>
        </div>
      </div>
    </>
  );
};

export default ConfirmYou;
