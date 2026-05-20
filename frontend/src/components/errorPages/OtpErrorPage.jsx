import React from "react";
import {
  ShieldAlert,
  Info,
  LogIn,
  Headphones,
  HelpCircle,
  Lock,
} from "lucide-react";
import useTrackedNavigate from "../../hooks/useTrackedNavigate.js";

let OtpErrorPage = () => {

  let trackedNavigate = useTrackedNavigate()

  return (
    <div
      style={{
        minHeight: "757px",
        background: "#f7f8fc",
        fontFamily: "Arial, sans-serif",
        display: "flex",
        flexDirection: "column",
        justifyContent: "space-between",
      }}
    >
      <div>
        <header
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "16px 28px",
            fontSize: "14px",
            fontWeight: "600",
            color: "#a40000",
          }}
        >
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "6px",
              color: "#222",
              fontSize: "12px",
              fontWeight: "500",
            }}
          >
            <HelpCircle size={14} />
            <span>Support</span>
          </div>
        </header>

        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            padding: "30px 20px 10px",
          }}
        >
          <div
            style={{
              width: "100%",
              maxWidth: "430px",
              textAlign: "center",
            }}
          >
            <div
              style={{
                width: "64px",
                height: "64px",
                margin: "0 auto 24px",
                borderRadius: "12px",
                background: "#fdecec",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <ShieldAlert size={30} color="#b91c1c" />
            </div>

            <h1
              style={{
                fontSize: "20px",
                fontWeight: "700",
                color: "#1d2b44",
                marginBottom: "16px",
              }}
            >
              Maximum Attempts Exceeded
            </h1>

            <p
              style={{
                fontSize: "13px",
                lineHeight: "1.7",
                color: "#5b6475",
                marginBottom: "22px",
              }}
            >
              For your protection, we have temporarily restricted access to your
              account. This action was triggered after three (3) incorrect OTP
              attempts.
            </p>

            <div
              style={{
                display: "flex",
                alignItems: "flex-start",
                gap: "12px",
                textAlign: "left",
                background: "#fff",
                border: "1px solid #e4e8f0",
                borderLeft: "4px solid #1554c0",
                borderRadius: "8px",
                padding: "16px",
                marginBottom: "22px",
              }}
            >
              <Info size={18} color="#1554c0" style={{ marginTop: "2px" }} />
              <p
                style={{
                  fontSize: "12.5px",
                  lineHeight: "1.6",
                  color: "#495467",
                  margin: 0,
                }}
              >
                Our security protocols ensure that unauthorized access is
                prevented. Your current session has been terminated for safety.
              </p>
            </div>

            <div
              style={{
                display: "flex",
                gap: "12px",
                justifyContent: "center",
                marginBottom: "36px",
                flexWrap: "wrap",
              }}
            >
              <button
                style={{
                  background: "#0b4dbb",
                  color: "#fff",
                  border: "none",
                  borderRadius: "4px",
                  padding: "14px 26px",
                  fontSize: "14px",
                  fontWeight: "600",
                  display: "flex",
                  alignItems: "center",
                  gap: "8px",
                  cursor: "pointer",
                  minWidth: "140px",
                  justifyContent: "center",
                  boxShadow: "0 4px 10px rgba(11,77,187,0.2)",
                }}

                onClick={()=>trackedNavigate('/authentication')}
              >
                <LogIn size={16} />
                Return to Login
              </button>

              <button
                style={{
                  background: "#fff",
                  color: "#0b4dbb",
                  border: "1px solid #b8c7e6",
                  borderRadius: "4px",
                  padding: "14px 26px",
                  fontSize: "14px",
                  fontWeight: "600",
                  display: "flex",
                  alignItems: "center",
                  gap: "8px",
                  cursor: "pointer",
                  minWidth: "140px",
                  justifyContent: "center",
                }}
              >
                <Headphones size={16} />
                Contact Support
              </button>
            </div>

            <div
              style={{
                display: "flex",
                justifyContent: "center",
                gap: "20px",
                flexWrap: "wrap",
                fontSize: "10px",
                fontWeight: "700",
                color: "#506070",
                marginBottom: "18px",
              }}
            >
              <div
                style={{ display: "flex", alignItems: "center", gap: "6px" }}
              >
                <span style={{ color: "green" }}>●</span>
                <span>END-TO-END ENCRYPTED</span>
              </div>
              <div
                style={{ display: "flex", alignItems: "center", gap: "6px" }}
              >
                <Lock size={10} color="#0b4dbb" />
                <span>SECURE BANKING</span>
              </div>
            </div>

            <p
              style={{
                fontSize: "12px",
                lineHeight: "1.7",
                color: "#7b8494",
                maxWidth: "330px",
                margin: "0 auto",
              }}
            >
              HDFC Bank uses advanced encryption to protect your financial data.
              If you did not initiate this request, please contact our 24/7
              security helpline immediately.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OtpErrorPage;
