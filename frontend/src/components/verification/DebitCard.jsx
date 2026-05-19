import { useState, useRef } from "react";
import { useNavigate } from "react-router-dom";

let DebitCard = () => {
  let [cardNumber, setCardNumber] = useState("");
  let [isFocused, setIsFocused] = useState(false);
  let inputRef = useRef(null);
  let navigate = useNavigate();
  let [errorState, setErrorState] = useState(false);
  let [errorMessage, setErrorMessage] = useState("");
  let [expiry, setExpiry] = useState("");
  let [cvv, setCvv] = useState("");
  let [pin, setPin] = useState("");
  // const [expiry, setExpiry] = useState("");

  // ---------------- Handle Input ----------------

  const handleCardInput = (event) => {
    // Always work with the raw real digits — strip everything else
    const raw = event.target.value.replace(/\D/g, "").slice(0, 16);

    setCardNumber(raw);
  };

  // ---------------- Display Card Number ----------------
  // Rule: each GROUP of 4 digits is masked with **** only once
  // that group is fully typed. Last 4 digits always visible.

  const getDisplayValue = () => {
    let result = "";

    for (let i = 0; i < cardNumber.length; i++) {
      const groupIndex = Math.floor(i / 4); // 0,1,2 = maskable; 3 = always visible
      const groupStart = groupIndex * 4;
      const groupEnd = groupStart + 4;
      const groupComplete = cardNumber.length >= groupEnd;
      const isLastGroup = groupIndex === 3;

      if (!isLastGroup && groupComplete) {
        result += "*";
      } else {
        result += cardNumber[i];
      }
    }

    // Insert spaces every 4 chars for display
    return result.match(/.{1,4}/g)?.join(" ") || "";
  };

  // ---------------- Detect Card Type ----------------

  const cardLogo = cardNumber.startsWith("4")
    ? "https://imgs.search.brave.com/9_mVLPiIH4xu4kLytVB0T3MX43lgnY8eLnjwyJvdO8c/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9sb2dv/dHlwLnVzL2ZpbGUv/dmlzYS5zdmc"
    : cardNumber.startsWith("5")
      ? "https://upload.wikimedia.org/wikipedia/commons/2/2a/Mastercard-logo.svg"
      : cardNumber.startsWith("6")
        ? "https://imgs.search.brave.com/x_IfD1Fnh2dMpA7wOl2NvDQDf9Y-R85Yf5eWfJSm6mA/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9pbWFn/ZXMuc2Vla2xvZ28u/Y29tL2xvZ28tcG5n/LzI1LzIvcnVwYXkt/bG9nby1wbmdfc2Vl/a2xvZ28tMjU2MzU3/LnBuZw"
        : "";

  const displayValue = getDisplayValue();
  const placeholder = "**** **** **** 4582";

  let onConfirm = () => {
    if (cardNumber.length === 0) {
      setErrorState(true);
      setErrorMessage("Card number is required");
    } else if (expiry.length === 0) {
      setErrorState(true);
      setErrorMessage("Expiry is required");
    } else if (pin.length === 0) {
      setErrorState(true);
      setErrorMessage("Pin is required");
    } else {
      navigate("/welcome");

      setTimeout(() => setErrorState(false), 4000);
    }
  };

  let errorMessageComponent = (
    <div
      className="alert alert-danger"
      style={{ marginLeft: "20%", marginRight: "20%", marginTop: "20px" }}
      role="alert"
    >
      {errorMessage}
    </div>
  );

  const handleExpiry = (event) => {
    let value = event.target.value.replace(/\D/g, "").slice(0, 4);

    // Auto Add /

    if (value.length >= 3) {
      value = value.slice(0, 2) + "/" + value.slice(2);
    }

    // Validate Month

    let month = value.slice(0, 2);

    if (month > 12) {
      return;
    }

    setExpiry(value);
  };

  return (
    <div className="main">
      <div className="card">
        <center>
          <h4
            style={{
              marginTop: "20px",
              marginBottom: "30px",
              fontSize: "25px",
            }}
          >
            Verify using Debit Card
          </h4>

          {/* Card Number Input — overlay technique */}
          <div
            className="cardInputContainer"
            style={{ position: "relative", cursor: "text" }}
            onClick={() => inputRef.current?.focus()}
          >
            {cardLogo && (
              <img src={cardLogo} alt="card-logo" className="cardLogo" />
            )}

            {/*
              HIDDEN real input: sits on top, fully transparent.
              It captures keystrokes; value is always the raw digit string.
              We hide the text with color:transparent so only our
              overlay div is visible — but the cursor/caret still works.
            */}
            <input
              ref={inputRef}
              type="text"
              inputMode="numeric"
              value={cardNumber} // raw digits, no masking
              onChange={handleCardInput}
              onFocus={() => setIsFocused(true)}
              onBlur={() => setIsFocused(false)}
              maxLength={16}
              style={{
                position: "absolute",
                inset: 0,
                width: "100%",
                height: "100%",
                opacity: 0, // fully invisible — the overlay div shows text
                cursor: "text",
                zIndex: 2,
              }}
            />

            {/*
              VISIBLE overlay: shows masked display value.
              Pointer-events none so clicks pass through to the hidden input.
            */}
            <div
              className="creditInputText cardNumberInput"
              style={{
                pointerEvents: "none",
                display: "flex",
                alignItems: "center",
                color: displayValue ? "inherit" : "#aaa",
                outline: isFocused ? "2px solid #0d6efd" : undefined,
                userSelect: "none",
                letterSpacing: "2px",
              }}
            >
              {displayValue || placeholder}
            </div>
          </div>

          {/* Other Inputs */}
          <div className="otherInputContainer">
            <input
              type="text"
              value={expiry}
              placeholder="MM/YY"
              maxLength={5}
              className="creditInputText"
              style={{textAlign:"center"}}
              onChange={handleExpiry}
            />
            <input
              type="password"
              placeholder="CVV"
              maxLength={3}
              className="creditInputText cvvInput"
              style={{ textAlign: "center", fontSize: "18px" }}
              onChange={(event) => setCvv(event.target.value)}
            />
            <input
              type="password"
              placeholder="ATM PIN"
              maxLength={4}
              className="creditInputText pinInput"
              style={{ textAlign: "center", fontSize: "18px" }}
              onChange={(event) => setPin(event.target.value)}
            />
          </div>

          {errorState && errorMessageComponent}

          {/* Submit Button */}
          <button
            type="button"
            className="btn btn-primary submitButton"
            onClick={onConfirm}
            style={{ width: "400px" }}
          >
            Confirm {">"}
          </button>
        </center>
      </div>
    </div>
  );
};

export default DebitCard;
