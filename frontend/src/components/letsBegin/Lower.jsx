import { useNavigate } from "react-router-dom";
import { UserRound } from "lucide-react";
import { Pencil } from "lucide-react";
import { IdCard } from "lucide-react";
import { CreditCard } from "lucide-react";
import { Smartphone } from "lucide-react";
import { ReceiptText } from "lucide-react";
import { TbRating18Plus } from "react-icons/tb";

let Lower = () => {
  let navigate = useNavigate();

  return (
    <div>
      <div className="policyText">
        <h4>If "Yes" is your answer to the below points, Let's begin </h4>
        <br />

        <div style={{ display: "flex", marginTop: "5px" }}>
          <TbRating18Plus style={{ fontSize: "25px" }} />
          <h6 style={{ marginLeft: "15px", fontSize: "15px" }}>
            I am 18 years or older
          </h6>
        </div>

        <div style={{ display: "flex", marginTop: "5px" }}>
          <UserRound />
          <h6 style={{ marginLeft: "15px", fontSize: "15px" }}>
            I am a Residient Indian/Non Authorised Indian/Authorised Signatory
            with an active Indian mobile number in bank record.
          </h6>
        </div>

        <div style={{ display: "flex", marginTop: "10px" }}>
          <Pencil />

          <h6 style={{ marginLeft: "15px", fontSize: "15px" }}>
            I hold an active Savings/Salery/Indivisual Current Account and/or
            Credit Card and/or Loan with HDFC Bank.
          </h6>
        </div>

        <div style={{ display: "flex", marginTop: "10px" }}>
          <IdCard />
          <h6 style={{ marginLeft: "15px", fontSize: "15px" }}>
            I have my Aadhar card (for resident) or official valid document (for
            NRI's) for KYC..
          </h6>
        </div>

        <div style={{ display: "flex", marginTop: "10px" }}>
          <CreditCard />
          <h6 style={{ marginLeft: "15px", fontSize: "15px" }}>
            I have my Debit/Credit card Net Banking Details for account
            verification.
          </h6>
        </div>

        <div style={{ display: "flex", marginTop: "10px" }}>
          <Smartphone />
          <h6 style={{ marginLeft: "15px", fontSize: "15px" }}>
            I have my registerd mobile number active for OTP verification.
          </h6>
        </div>

        <div style={{ display: "flex", marginTop: "10px" }}>
          <ReceiptText />
          <h6 style={{ marginLeft: "15px", fontSize: "15px" }}>
            I can choose to activate my singly held dormant accounts, if any,
            during the process (Residient only).
          </h6>
        </div>
      </div>

      <div className="buttonContainer">
        <button
          type="button"
          class="btn btn-primary LetsGoButton"
          onClick={() => navigate("/authentication")}
        >{`Let's Begin >`}</button>
      </div>
    </div>
  );
};

export default Lower;
