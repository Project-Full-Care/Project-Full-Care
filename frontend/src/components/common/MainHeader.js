/* eslint-disable react-hooks/exhaustive-deps */
import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useRouter } from "../../hooks/useRouter";

import Button from "./Button";
import ToggleMenuButton from "./ToggleMenuButton";

import { authActions } from "../../redux/authSlice";
import { getProfileImage } from "../../lib/apis/mainHeaderApi";
import { isToken } from "../../utils/localstroageHandler";

import profile_default from "../../assets/profile-default-img.png";
import profile_isProfile from "../../assets/ranking-img.png";
import logo from "../../assets/logo-with-text.svg";
import profileLogo from "../../assets/logo-with-text-profile.png";

const MainHeader = () => {
  const [isToggleMenuOpen, setIsToggleMenuOpen] = useState(false);
  const [isProfilePage, setIsProfilePage] = useState(false);
  const [profileImage, setProfileImage] = useState({ id: "", imageUrl: "" });

  const { replaceTo, currentPath, routeTo } = useRouter();
  const dispatch = useDispatch();
  const authState = useSelector((state) => state.auth.isLoggedIn);

  useEffect(() => {
    window.addEventListener("message", (event) => {
      if (event.data === "login") {
        dispatch(authActions.login());
      }
    });
  }, [currentPath, authState]);

  useEffect(() => {
    if (currentPath.includes("/profile")) setIsProfilePage(true);
    const getProfile = async () => {
      const response = await getProfileImage();
      if (response) {
        setProfileImage(response);
      }
    };
    getProfile();

    return () => {
      setIsProfilePage(false);
      setProfileImage({ id: "", imageUrl: "" });
    };
  }, [currentPath]);

  useEffect(() => {
    if (isToken("access_token") && isToken("refresh_token")) {
      dispatch(authActions.login());
    }
  }, []);

  const handleClickLinkMenu = (link) => {
    if (link === "/management" && !isToken("access_token")) {
      dispatch(authActions.setIsLoginModalVisible(true));
    } else {
      routeTo(link);
    }
  };

  const handleLogout = () => {
    dispatch(authActions.logout());
    replaceTo("/");
  };

  const handleLogin = () => {
    dispatch(authActions.setIsLoginModalVisible(true));

    window.addEventListener("message", (event) => {
      if (event.data === "login") {
        dispatch(authActions.login());
      }
    });
  };

  return (
    <>
      <header
        className={
          isProfilePage ? "main-header header-profile-bg" : "main-header"
        }
      >
        <div className="main-header-left-col">
          <figure
            style={{
              backgroundImage: `url(${isProfilePage ? profileLogo : logo})`,
            }}
            className="main-header-logo-img"
            onClick={() => routeTo("/")}
          />
        </div>
        <div className="main-header-medium-col">
          <ul className="main-header-link">
            {headerMenu.map((menu) => (
              <li key={menu.id}>
                <p
                  className={
                    isProfilePage
                      ? "nav_item  header-profile-menu"
                      : currentPath.includes(menu.link)
                      ? "nav_item main-header-link-on"
                      : "nav_item main-header-link-off"
                  }
                  onClick={() => handleClickLinkMenu(menu.link)}
                >
                  {menu.title}
                </p>
              </li>
            ))}
          </ul>
        </div>
        {authState ? (
          <div className="main-header-right-col main-header-logout-col">
            {isProfilePage ? (
              <Button
                text={"log out"}
                type={"positive"}
                color={"white"}
                onClick={handleLogout}
                isProfile={true}
              />
            ) : (
              <Button
                text={"log out"}
                type={"positive"}
                color={"white"}
                onClick={handleLogout}
              />
            )}
            <Link
              className={
                isProfilePage
                  ? "main-header-user-profile-img header-profile-image-bg"
                  : "main-header-user-profile-img header-image-bg"
              }
              to={`/profile/${profileImage.id}/introduce`}
            >
              <img
                className="main-header_img"
                src={
                  !profileImage.imageUrl
                    ? isProfilePage
                      ? profile_isProfile
                      : profile_default
                    : profileImage.imageUrl
                }
                alt={"유저 프로필"}
              />
            </Link>
            <ToggleMenuButton
              isProfilePage={isProfilePage}
              isToggleMenuOpen={isToggleMenuOpen}
              setIsToggleMenuOpen={setIsToggleMenuOpen}
            />
          </div>
        ) : (
          <div className="main-header-right-col main-header-login-col">
            <Button
              text={"log in"}
              color={"white"}
              type={"positive"}
              onClick={handleLogin}
            />
            <ToggleMenuButton
              isToggleMenuOpen={isToggleMenuOpen}
              setIsToggleMenuOpen={setIsToggleMenuOpen}
            />
          </div>
        )}
      </header>
    </>
  );
};

export default MainHeader;

const headerMenu = [
  { id: 1, link: "/management", title: "프로젝트 관리" },
  { id: 2, link: "/recruitment", title: "인원 모집" },
];
