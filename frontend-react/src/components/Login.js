import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

const Login = () => {
    const [tokenInput, setTokenInput] = useState('');
    const { login, user } = useAuth();
    const navigate = useNavigate();

    // If user is already authenticated, redirect to dashboard
    useEffect(() => {
        if (user) {
            navigate('/dashboard');
        }
    }, [user, navigate]);

    const handleManualLogin = async () => {
        if (tokenInput.trim()) {
            try {
                await login(tokenInput.trim());
                // Navigation will happen automatically via the useEffect above
            } catch (error) {
                console.error('Login failed:', error);
            }
        }
    };

    return (
        <div style={{
            minHeight: '100vh',
            background: 'linear-gradient(135deg, #2c2c2c 0%, #1a1a1a 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white',
            fontFamily: 'Arial, sans-serif'
        }}>
            <div style={{
                background: 'rgba(255, 255, 255, 0.1)',
                padding: '3rem',
                borderRadius: '20px',
                textAlign: 'center',
                maxWidth: '600px',
                backdropFilter: 'blur(10px)'
            }}>
                <h1 style={{ fontSize: '3rem', marginBottom: '1rem' }}>Tracklytics</h1>

                <div style={{ marginBottom: '2rem' }}>
                    <p style={{ marginBottom: '1rem' }}>Step 1: Get your token</p>
                    <a
                        href="http://localhost:8080/test-jwt"
                        target="_blank"
                        rel="noopener noreferrer"
                        style={{
                            background: '#1db954',
                            color: 'white',
                            padding: '10px 20px',
                            textDecoration: 'none',
                            borderRadius: '5px'
                        }}
                    >
                        Get Token from Backend
                    </a>
                </div>

                <div>
                    <p style={{ marginBottom: '1rem' }}>Step 2: Paste your JWT token here</p>
                    <input
                        type="text"
                        value={tokenInput}
                        onChange={(e) => setTokenInput(e.target.value)}
                        placeholder="Paste JWT token here..."
                        style={{
                            width: '100%',
                            padding: '10px',
                            marginBottom: '1rem',
                            borderRadius: '5px',
                            border: 'none'
                        }}
                    />
                    <button
                        onClick={handleManualLogin}
                        disabled={!tokenInput.trim()}
                        style={{
                            background: tokenInput.trim() ? '#1db954' : '#666',
                            color: 'white',
                            border: 'none',
                            padding: '10px 20px',
                            borderRadius: '5px',
                            cursor: tokenInput.trim() ? 'pointer' : 'not-allowed'
                        }}
                    >
                        Login with Token
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Login;