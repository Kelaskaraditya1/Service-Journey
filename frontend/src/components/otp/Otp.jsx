import { useEffect, useState } from "react";
import "./Otp.css";
import useTrackedNavigate from "../../hooks/useTrackedNavigate.js";

let Otp = () => {
  let [timer, setTimer] = useState(0);
  let [otp, setOtp] = useState("");
  let [errorMessage, setErrorMessage] = useState('');
  let [errorMessageState, setErrorMessageState] = useState(false)
  let [invalidOtpCount,setInvalidOtpCount] = useState(3)

  let trackedNavigate = useTrackedNavigate();

  let resetTimer = () => {
    setTimer(10);
  };

  useEffect(() => {
    if (timer <= 0) return;

    let interval = setInterval(() => setTimer((prev) => prev - 1), 1000);

    return () => clearInterval(interval);
  }, [timer]);

  let onSubmit = () => {

            if(invalidOtpCount===1)
        trackedNavigate('/otp/error-page')

    if(otp.length !== 6){
      setErrorMessageState(true);
      setErrorMessage('Enter complete OTP.');
      setInvalidOtpCount(invalidOtpCount-1);

    }
    else if(otp !== '133526'){
      setErrorMessageState(true);
      setErrorMessage('Invalid OTP.');
      setInvalidOtpCount(invalidOtpCount-1);
    }
    else
      trackedNavigate("/confirm-you");

    setTimeout(
      ()=>setErrorMessageState(false),
      3000
    );

  };

  let errorMessageComponent = (
    <div className="alert alert-danger" style={{marginLeft:"50px", marginRight:"50px", marginBottom:"20px"}} role="alert">
      {errorMessage}
    </div>
  );

  let resendOtpButton = (
    <div>
      <h6
        style={{
          color: "oklch(0.6204 0.195 253.83)",
          fontSize: "17px",
          marginRight: "100px",
        }}
        onClick={resetTimer}
      >
        Resend Otp
      </h6>
    </div>
  );

  let counterText = (
    <div>
      <h6 style={{ color: "grey", fontSize: "17px", marginRight: "100px" }}>
        Resend Otp in {timer}
      </h6>
    </div>
  );

  return (
    <>
      <div className="mainContainer">
        <div className="card" style={{ width: "650px" }}>
          <center>
            <h2 style={{ margin: "30px", fontWeight: "600" }}>Enter OTP</h2>

            <h6
              style={{ marginTop: "20px", fontSize: "21px", fontWeight: "400" }}
            >
              we have sent you a <b>6 digit OTP</b> to your registered mobile
            </h6>
            <h6 style={{ fontSize: "21px", fontWeight: "400" }}>
              number ******9876
            </h6>

            <input
              className="otpInput"
              type="password"
              maxLength={6}
              onChange={(event) => {
                setOtp(event.target.value);
              }}
            ></input>
          </center>

          <div className="resendOtp">
            {timer > 0 ? counterText : resendOtpButton}

            <div>
              <h6
                style={{ color: "grey", fontSize: "17px", fontWeight: "500" }}
              >
                {`Remainning ${invalidOtpCount} remainning`}
              </h6>
            </div>
          </div>

          {errorMessageState && errorMessageComponent}

          <button
            type="button"
            class="btn btn-primary submitButton"
            onClick={onSubmit}
          >{`Submit >`}</button>
        </div>
      </div>
    </>
  );
};

export default Otp;
