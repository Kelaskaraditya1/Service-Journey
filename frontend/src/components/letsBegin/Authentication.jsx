import { useState, useEffect } from "react";
import login from "../../services/AuthService.js";
import { startSession } from "../../services/SessionService.js";
import { useSession } from "../../context/SessionContext.jsx";
import useTrackedNavigate from "../../hooks/useTrackedNavigate.js";
import {
  CUSTOMER_ID_REGEX,
  MOBILE_REGEX,
  PAN_REGEX,
} from "../../utility/RegexPatterns.js";

let Authentication = () => {
  let [getIdentity, setIdentity] = useState("DOB");
  let [contactNumber, setContactNumber] = useState("");
  let [identityValue, setIdentityValue] = useState("");

  let [errorMessage, setErrorMessage] = useState("");
  let [errorState, setErrorState] = useState(false);

  let [isValid, setIsValid] = useState(false);

  let trackedNavigate = useTrackedNavigate();
  let { loginUser, updateCurrentPage } = useSession();

  // let mobileRegex = /^[0-9]{10}$/;
  // let panRegex = /^[A-Z]{5}[0-9]{4}[A-Z]{1}$/;
  // let customerIdRegex = /^[A-Za-z0-9]{16}$/;

  useEffect(() => {
    setErrorState(false);

    if (contactNumber.length > 0 && !MOBILE_REGEX.test(contactNumber)) {
      setErrorMessage("Mobile number should be exactly 10 digits");

      setErrorState(true);

      setIsValid(false);

      return;
    }

    if (getIdentity === "DOB") {
      if (identityValue !== "") {
        let today = new Date();

        let enteredDate = identityValue.split("-");

        enteredDate = new Date(
          `${enteredDate[2]}-${enteredDate[1]}-${enteredDate[0]}`,
        );

        if (enteredDate >= today) {
          setErrorMessage("DOB should be less than today's date");

          setErrorState(true);

          setIsValid(false);

          return;
        }
      }
    }

    if (getIdentity === "PAN") {
      if (identityValue.length > 0 && !PAN_REGEX.test(identityValue)) {
        setErrorMessage(
          "PAN should contain 5 capital letters, 4 digits and 1 capital letter",
        );

        setErrorState(true);

        setIsValid(false);

        return;
      }
    }

    if (getIdentity === "UID") {
      if (identityValue.length > 0 && !CUSTOMER_ID_REGEX.test(identityValue)) {
        setErrorMessage("Customer ID should be 16 digit alphanumeric");

        setErrorState(true);

        setIsValid(false);

        return;
      }
    }

    if (
      MOBILE_REGEX.test(contactNumber) &&
      ((getIdentity === "DOB" && identityValue !== "") ||
        (getIdentity === "PAN" && PAN_REGEX.test(identityValue)) ||
        (getIdentity === "UID" && CUSTOMER_ID_REGEX.test(identityValue)))
    ) {
      setIsValid(true);
    } else {
      setIsValid(false);
    }
  }, [contactNumber, identityValue, getIdentity]);

  let loginApiCall = async () => {
    if (!isValid) return;

    let body = {
      contactNumber,
      identityType: getIdentity,
      identity: identityValue,
    };

    try {
      // Step 1: Login
      let response = await login(body);
      let status = response.status;

      if (status === "OK") {
        let userData = response.data;
        console.log("[Auth] Login successful:", userData);

        // Step 2: Start session (Temporal workflow)
        let sessionResponse = await startSession(userData.userId);

        if (sessionResponse.success) {
          let sessionId = sessionResponse.data.sessionId;
          console.log("[Auth] Session started:", sessionId);

          // Step 3: Store in context
          loginUser(userData, sessionId);
          updateCurrentPage("Authentication");

          // Step 4: Navigate with tracking
          trackedNavigate("/otp");
        } else {
          setErrorMessage("Failed to start session. Please try again.");
          setErrorState(true);
          setTimeout(() => setErrorState(false), 4000);
        }
      } else {
        setErrorMessage(response.message);
        setErrorState(true);
        setTimeout(() => setErrorState(false), 4000);
      }
    } catch (error) {
      console.error("[Auth] Login flow error:", error);
      setErrorMessage("Something went wrong. Please try again.");
      setErrorState(true);
      setTimeout(() => setErrorState(false), 4000);
    }
  };

  let DobComponent = (
    <div className="input-group authInput" style={{ marginTop: "10px" }}>
      <div className="form-floating">
        <input
          type="date"
          className="form-control"
          id="floatingInputGroup1"
          onChange={(event) => {
            if (event.target.value === "") {
              setIdentityValue("");

              return;
            }

            let date = event.target.value.split("-");

            date = `${date[2]}-${date[1]}-${date[0]}`;

            setIdentityValue(date);
          }}
        />

        <label htmlFor="floatingInputGroup1">Your Birth Date here</label>
      </div>
    </div>
  );

  let PanComponent = (
    <div className="input-group authInput" style={{ marginTop: "10px" }}>
      <div className="form-floating">
        <input
          type="text"
          className="form-control"
          id="floatingInputGroup1"
          onChange={(event) =>
            setIdentityValue(event.target.value.toUpperCase())
          }
        />

        <label htmlFor="floatingInputGroup1">Your PAN Id here</label>
      </div>
    </div>
  );

  let customerId = (
    <div className="input-group authInput" style={{ marginTop: "10px" }}>
      <div className="form-floating">
        <input
          type="text"
          className="form-control"
          id="floatingInputGroup1"
          onChange={(event) => setIdentityValue(event.target.value)}
        />

        <label htmlFor="floatingInputGroup1">Your Customer ID here</label>
      </div>
    </div>
  );

  let errorMessageComponent = (
    <div className="alert alert-danger" role="alert">
      {errorMessage}
    </div>
  );

  let radioComponent;

  switch (getIdentity) {
    case "DOB": {
      radioComponent = DobComponent;
      break;
    }

    case "PAN": {
      radioComponent = PanComponent;
      break;
    }

    case "UID": {
      radioComponent = customerId;
      break;
    }
  }

  return (
    <>
      <div className="authContainer">
        <div className="input-group authInput" style={{ marginTop: "5px" }}>
          <span className="input-group-text">+ 91</span>

          <div className="form-floating">
            <input
              type="text"
              className="form-control"
              id="floatingInputGroup1"
              onChange={(event) => {
                setContactNumber(event.target.value);
              }}
            />

            <label htmlFor="floatingInputGroup1">
              Your registered mobile number
            </label>
          </div>
        </div>

        <div
          style={{
            textAlign: "start",
            marginLeft: "10px",
          }}
        >
          <h6 style={{ marginTop: "10px" }}>Identify using</h6>
        </div>

        <div style={{ display: "flex" }}>
          <div
            className="form-check"
            style={{
              margin: "5px",
              marginTop: "10px",
            }}
          >
            <input
              className="form-check-input"
              type="radio"
              name="radioDefault"
              id="radioDefault1"
              defaultChecked
              onClick={() => {
                setIdentity("DOB");
                setIdentityValue("");
              }}
            />

            <label className="form-check-label" htmlFor="radioDefault1">
              Date of Birth
            </label>
          </div>

          <div className="form-check" style={{ margin: "5px" }}>
            <input
              className="form-check-input"
              type="radio"
              name="radioDefault"
              id="radioDefault2"
              onClick={() => {
                setIdentity("PAN");

                setIdentityValue("");
              }}
            />

            <label className="form-check-label" htmlFor="radioDefault2">
              PAN number
            </label>
          </div>

          <div className="form-check" style={{ margin: "5px" }}>
            <input
              className="form-check-input"
              type="radio"
              name="radioDefault"
              id="radioDefault3"
              onClick={() => {
                setIdentity("UID");

                setIdentityValue("");
              }}
            />

            <label className="form-check-label" htmlFor="radioDefault3">
              Customer ID
            </label>
          </div>
        </div>

        {radioComponent}

        <p className="authInfo">
          In case you are a sole proprioter or have a HUF account, please use
          your
          <b> Proprioter/Karta</b> credentials.
        </p>

        <p style={{ marginTop: "30px" }}>
          By tapping on 'GET OTP' I confirm that i have read & agreed to HDFC
          Bank's Privacy Policy & agreed to recive calls, SMS & Whatsapp
          messages from HDFC Bank.
        </p>

        <div>{errorState && errorMessageComponent}</div>

        <div className="buttonContainer">
          <button
            type="button"
            className="btn LetsGoButton"
            onClick={loginApiCall}
            disabled={!isValid}
            style={{
              backgroundColor: isValid ? "oklch(0.6204 0.195 253.83)" : "grey",

              border: "none",

              color: "white",

              cursor: isValid ? "pointer" : "not-allowed",
            }}
          >
            {`Get OTP`}
          </button>
        </div>
      </div>
    </>
  );
};

export default Authentication;
