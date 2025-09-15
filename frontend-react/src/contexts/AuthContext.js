import React, { createContext, useState, useContext, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext();

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const checkAuth = async () => {
        const token = localStorage.getItem('jwt_token');
        if (!token) {
            setLoading(false);
            return;
        }

        try {
            const response = await api.get('/api/auth/me');
            setUser(response.data);
        } catch (error) {
            console.error('Auth check failed:', error);
            localStorage.removeItem('jwt_token');
        } finally {
            setLoading(false);
        }
    };

    const login = async (token) => {
        localStorage.setItem('jwt_token', token);
        await checkAuth();
    };

    const logout = () => {
        localStorage.removeItem('jwt_token');
        setUser(null);
        window.location.href = '/login';
    };

    useEffect(() => {
        checkAuth();
    }, []);

    const value = {
        user,
        login,
        logout,
        loading
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};