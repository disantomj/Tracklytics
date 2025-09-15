import React, { useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

const AuthCallback = () => {
    const { login } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        // Get token from URL parameters
        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');

        if (token) {
            login(token);
            navigate('/dashboard');
        } else {
            navigate('/login');
        }
    }, [login, navigate]);

    return (
        <div style={{
            minHeight: '100vh',
            background: '#1a1a1a',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white'
        }}>
            <div>Processing authentication...</div>
        </div>
    );
};

export default AuthCallback;