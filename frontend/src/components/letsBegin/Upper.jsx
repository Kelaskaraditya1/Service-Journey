import "./LetsBegin.css";
import welcome from "../../assets/welcome.png";

let Upper = () => {
  return (
    <div>
      <div className="letsBeginContainer">
        <div style={{display:"flex"}}>


          <div className="letsBeginText">
            <h1 className="titleText">Update KYC</h1>
            <h6 className="contentText">
              Review and update your KYC details easily.
            </h6>
          </div>


          <div style={{position:"absolute", right:"5%", top:"-9%"}}>
            <img src = {welcome} style={{height:"230px", }}/>
          </div>
                  
        </div>
      </div>
    </div>
  );
};

export default Upper;
