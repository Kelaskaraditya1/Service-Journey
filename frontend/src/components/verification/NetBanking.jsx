import "./Verification.css";
import useTrackedNavigate from "../../hooks/useTrackedNavigate.js";

let NetBanking = () => {

  let trackedNavigate = useTrackedNavigate();

  return (
    <div className="main">
      <div className="card">
        <center>
          <h3 style={{ marginTop: "20px", marginBottom: "30px" }}>
            Verify via NetBanking
          </h3>

          <div
            style={{
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
            }}
          >
            <input
              className="inputText"
              placeholder="Enter Customer ID"
            ></input>

            <input
              className="inputText"
              placeholder="Enter Password/PIN"
            ></input>
          </div>

          <button
            type="button"
            className="btn btn-primary submitButton"
            onClick={() => trackedNavigate("/welcome")}
            style={{ width: "400px" }}
          >{`Confirm >`}</button>
        </center>
      </div>
    </div>
  );
};

export default NetBanking;
