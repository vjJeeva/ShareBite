import React from "react";
import "./TermsModal.css";

const TermsModal = ({ onAgree, onClose, type }) => {
  return (
    <div className="modal-overlay">
      <div className="modal-box">
        <h2>Terms & Conditions</h2>

        <div className="terms-content">
          <p>
            By choosing to <b>{type}</b>, you agree that:
          </p>

          <ul>
            <li>Food must be safe and hygienic</li>
            <li>No expired or spoiled food</li>
            <li>Respectful communication is required</li>
            <li>Platform is not responsible for misuse</li>
          </ul>
        </div>

        <div className="modal-actions">
          <button className="agree-btn" onClick={onAgree}>
            Agree & Continue
          </button>

          <button className="cancel-btn" onClick={onClose}>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default TermsModal;