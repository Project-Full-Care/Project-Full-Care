import { Link } from "react-router-dom";

import { googleAuthUrl, kakaoAuthUrl, naverAuthUrl } from "../../utils/auth";
import { useRef } from "react";
import { useOutsideClick } from "../../hooks/useOutsideClick";

const Login = ({ closeModal }) => {
  const modalOutsideRef = useRef(null);

  const left = (window.innerWidth - 472) / 2;

  useOutsideClick(modalOutsideRef, () => closeModal());

  return (
    <div className="login-modal-wrapper" ref={modalOutsideRef}>
      <div className="login-modal">
        <div className="login-logo-img"></div>
        <div className="login-heading">
          <h1>Log in</h1>
        </div>
        <div className="login-text">
          <p>
            플케어에서 팀원을 만나고 <br />
            프로젝트를 경험하세요!
          </p>
        </div>
        <div className="login-button-wrapper">
          <Link
            className="login-button login-button-google"
            onClick={() => {
              window.open(
                googleAuthUrl,
                "_blank",
                `width=450, height=500, top=55px, left=${left}`
              );
            }}
          />
          <Link
            className="login-button login-button-kakao"
            onClick={() => {
              window.open(
                kakaoAuthUrl,
                "_blank",
                `width=450, height=500, top=55px, left=${left}`
              );
            }}
          />
          <Link
            className="login-button login-button-naver"
            onClick={() => {
              window.open(
                naverAuthUrl,
                "_blank",
                `width=450, height=500, top=55px, left=${left}`
              );
            }}
          />
        </div>
      </div>
    </div>
  );
};

export default Login;
