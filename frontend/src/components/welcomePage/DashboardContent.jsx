import "./WelcomePage";

let DashboardContent = () => {
  return (
    <div className="dashboardContent">
      {/* LEFT SECTION */}

      <div className="leftDashboard">
        {/* Welcome Section */}

        <div className="welcomeSection">
          <div>
            <h1 className="welcomeHeading">Good morning, Aditya</h1>

            <div className="sessionInfo">
              <span className="secureSession">
                <i className="fa-regular fa-circle-check"></i>
                Secure Session
              </span>

              <span className="lastLogin">• Last login: 24 Oct, 08:15 AM</span>
            </div>
          </div>

          {/* Timeout Box */}

          <div className="timeoutBox">
            <i className="fa-regular fa-clock"></i>
            Timeout in 09:58
          </div>
        </div>

        {/* Quick Actions */}

        <div className="section">
          <h2 className="sectionHeading">Quick Actions</h2>

          <div className="quickActions">
            <div className="quickCard">
              <i className="fa-solid fa-money-bill-transfer"></i>
              <p>Transfer Money</p>
            </div>

            <div className="quickCard">
              <i className="fa-regular fa-clipboard"></i>
              <p>Pay Bills</p>
            </div>

            <div className="quickCard">
              <i className="fa-solid fa-user-plus"></i>
              <p>Add Payee</p>
            </div>

            <div className="quickCard">
              <i className="fa-regular fa-file-lines"></i>
              <p>Request Statement</p>
            </div>
          </div>
        </div>

        {/* Accounts Overview */}

        <div className="section">
          <h2 className="sectionHeading">Accounts Overview</h2>

          <div className="accountsContainer">
            {/* Savings Card */}

            <div className="savingsCard">
              <div className="accountTop">
                <div>
                  <p className="accountType">SAVINGS ACCOUNT</p>
                  <h4>XXXX 8821</h4>
                </div>

                <i className="fa-regular fa-credit-card"></i>
              </div>

              <div className="accountBottom">
                <h1>₹ 4,82,901.50</h1>
                <p>Available Balance</p>
              </div>
            </div>

            {/* Credit Card */}

            <div className="creditCard">
              <div className="accountTop">
                <div>
                  <p className="accountType">MILLENNIA CREDIT CARD</p>
                  <h4>XXXX 4002</h4>
                </div>

                <i
                  className="fa-solid fa-credit-card"
                  style={{ color: "red" }}
                ></i>
              </div>

              <div className="creditBottom">
                <div>
                  <h2>₹ 18,420.00</h2>
                  <p>Current Due</p>
                </div>

                <div>
                  <h3>₹ 5,00,000</h3>
                  <p>Limit</p>
                </div>
              </div>
            </div>
          </div>

          {/* Fixed Deposit */}

          <div className="fdCard">
            <div className="fdLeft">
              <div className="fdIcon">
                <i className="fa-solid fa-lock"></i>
              </div>

              <div>
                <h3>Fixed Deposit</h3>
                <p>Matures on 12 Nov, 2025</p>
              </div>
            </div>

            <div className="fdRight">
              <h2>₹ 12,50,000.00</h2>
              <p>Current Value @ 7.25% p.a.</p>
            </div>
          </div>
        </div>

        {/* Recent Transactions */}

        <div className="section">
          <div className="transactionHeader">
            <h2 className="sectionHeading">Recent Transactions</h2>

            <p className="viewAll">View All</p>
          </div>

          <div className="transactionTable">
            {/* Row 1 */}

            <div className="transactionRow">
              <div>
                <p>23 Oct</p>
                <p>2023</p>
              </div>

              <div>
                <h4>Amazon India Checkout</h4>
                <p>Shopping • Debit Card</p>
              </div>

              <div>
                <span className="completedStatus">Completed</span>
              </div>

              <div className="debitAmount">- ₹ 2,499.00</div>
            </div>

            {/* Row 2 */}

            <div className="transactionRow">
              <div>
                <p>22 Oct</p>
                <p>2023</p>
              </div>

              <div>
                <h4>Interest Credit</h4>
                <p>Bank • Savings</p>
              </div>

              <div>
                <span className="completedStatus">Completed</span>
              </div>

              <div className="creditAmount">+ ₹ 1,204.50</div>
            </div>

            {/* Row 3 */}

            <div className="transactionRow">
              <div>
                <p>20 Oct</p>
                <p>2023</p>
              </div>

              <div>
                <h4>Swiggy Ltd</h4>
                <p>Food • UPI</p>
              </div>

              <div>
                <span className="pendingStatus">Pending</span>
              </div>

              <div className="debitAmount">- ₹ 450.00</div>
            </div>
          </div>
        </div>
      </div>

      {/* RIGHT SECTION */}

      <div className="rightDashboard">
        <h2 className="sectionHeading">Exclusive Offers</h2>

        {/* Offer 1 */}

        <div className="offerCard">
          <img
            src="https://images.unsplash.com/photo-1505693416388-ac5ce068fe85"
            alt="offer"
          />

          <div className="offerContent">
            <h3>Home Loans @ 8.4%*</h3>

            <p>Special processing fee waiver for existing customers.</p>

            <button className="offerButton">Apply Now</button>
          </div>
        </div>

        {/* Offer 2 */}

        <div className="offerCard">
          <img
            src="https://images.unsplash.com/photo-1516321318423-f06f85e504b3"
            alt="offer"
          />

          <div className="offerContent">
            <h3>Cyber Insurance</h3>

            <p>Protect your digital transactions with zero liability.</p>

            <button className="offerButton secondaryButton">Learn More</button>
          </div>
        </div>

        {/* Reward Card */}

        <div className="rewardCard">
          <p className="rewardHeading">LOYALTY REWARDS</p>

          <h3>You have 2,450 Reward Points expiring this month.</h3>

          <p className="redeemText">Redeem Now</p>
        </div>
      </div>
    </div>
  );
};

export default DashboardContent;
