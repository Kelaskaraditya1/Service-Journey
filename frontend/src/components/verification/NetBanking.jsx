import "./Verification.css";

let NetBanking = () => {
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
            class="btn btn-primary submitButton"
            onClick={() => navigate("/confirm-you")}
            style={{ width: "400px" }}
          >{`Confirm >`}</button>
        </center>
      </div>
    </div>
  );
};

export default NetBanking;
