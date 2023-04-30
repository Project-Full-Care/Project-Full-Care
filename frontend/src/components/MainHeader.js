import { Link } from 'react-router-dom';
import { useState } from 'react';

import Login from './Login';
import Button from './Button';

const MainHeader = () => {
    const [isLoginModalVisible, setIsLoginModalVisible] = useState(false);

    const handleLogin = () => {
        setIsLoginModalVisible(true);
    }

    return (
        <>
            <header className='main-header'>
                <div className='main-header-left-col'>
                    <figure className='main-header-logo-img' />
                </div>
                <div className='main-header-link'>
                    <Link to={'./management'}>프로젝트 관리</Link>
                    <Link to={'./recruitment'}>인원 모집</Link>
                </div>
                <div className='main-header-right-col'>
                    <Button 
                        text={'로그인'}
                        onClick={handleLogin}
                    />
                    <Link className='main-header-user-profile-img' to={'./profile'} />
                </div>
            </header>
            {isLoginModalVisible && <Login />}
        </>
    )
}

export default MainHeader;