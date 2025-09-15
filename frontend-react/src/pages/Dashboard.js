import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import api from '../services/api';

const Dashboard = () => {
    const { user, logout } = useAuth();
    const [personalityData, setPersonalityData] = useState(null);
    const [tracksData, setTracksData] = useState(null);
    const [artistsData, setArtistsData] = useState(null);
    const [historyData, setHistoryData] = useState(null);
    const [syncStatus, setSyncStatus] = useState('');
    const [loading, setLoading] = useState({
        personality: false,
        sync: false,
        tracks: false,
        artists: false,
        history: false
    });
    const [error, setError] = useState('');

    // Auto-load user data on component mount
    useEffect(() => {
        if (user) {
            getPersonality();
        }
    }, [user]);

    const getPersonality = async () => {
        setLoading(prev => ({ ...prev, personality: true }));
        setError('');
        try {
            const response = await api.get('/api/personality');
            setPersonalityData(response.data);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to get personality analysis');
        } finally {
            setLoading(prev => ({ ...prev, personality: false }));
        }
    };

    const syncData = async () => {
        setLoading(prev => ({ ...prev, sync: true }));
        setError('');
        setSyncStatus('');
        try {
            const response = await api.post('/api/sync');
            setSyncStatus(`Synced ${response.data.tracksCount} tracks and ${response.data.artistsCount} artists`);
            // Refresh personality after sync
            setTimeout(() => getPersonality(), 1000);
        } catch (err) {
            setError(err.response?.data?.message || 'Sync failed');
        } finally {
            setLoading(prev => ({ ...prev, sync: false }));
        }
    };

    const getTracks = async () => {
        setLoading(prev => ({ ...prev, tracks: true }));
        setError('');
        try {
            const response = await api.get('/api/tracks');
            setTracksData(response.data);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to get tracks');
        } finally {
            setLoading(prev => ({ ...prev, tracks: false }));
        }
    };

    const getArtists = async () => {
        setLoading(prev => ({ ...prev, artists: true }));
        setError('');
        try {
            const response = await api.get('/api/artists');
            setArtistsData(response.data);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to get artists');
        } finally {
            setLoading(prev => ({ ...prev, artists: false }));
        }
    };

    const getHistory = async () => {
        setLoading(prev => ({ ...prev, history: true }));
        setError('');
        try {
            const response = await api.get('/api/personality/history');
            setHistoryData(response.data);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to get history');
        } finally {
            setLoading(prev => ({ ...prev, history: false }));
        }
    };

    const ActionCard = ({ title, description, onClick, loading, color, icon }) => (
        <div style={{
            background: 'rgba(255, 255, 255, 0.08)',
            borderRadius: '16px',
            padding: '2rem',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            transition: 'all 0.3s ease',
            cursor: 'pointer',
            position: 'relative',
            overflow: 'hidden'
        }}
             onClick={onClick}
             onMouseEnter={(e) => {
                 e.currentTarget.style.transform = 'translateY(-4px)';
                 e.currentTarget.style.borderColor = color;
                 e.currentTarget.style.boxShadow = `0 8px 32px ${color}20`;
             }}
             onMouseLeave={(e) => {
                 e.currentTarget.style.transform = 'translateY(0)';
                 e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.1)';
                 e.currentTarget.style.boxShadow = 'none';
             }}>
            <div style={{
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                height: '4px',
                background: `linear-gradient(90deg, ${color}, ${color}80)`
            }} />
            <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>{icon}</div>
            <h3 style={{ color: color, margin: '0 0 0.5rem 0', fontSize: '1.2rem' }}>{title}</h3>
            <p style={{ color: '#ccc', margin: 0, fontSize: '0.9rem', lineHeight: '1.4' }}>{description}</p>
            {loading && (
                <div style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    background: 'rgba(0, 0, 0, 0.7)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    borderRadius: '16px'
                }}>
                    <div style={{ color: 'white', fontSize: '0.9rem' }}>Loading...</div>
                </div>
            )}
        </div>
    );

    return (
        <div style={{
            minHeight: '100vh',
            background: 'linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%)',
            color: 'white',
            fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
        }}>
            {/* Header */}
            <header style={{
                padding: '2rem 3rem',
                background: 'rgba(0, 0, 0, 0.3)',
                backdropFilter: 'blur(20px)',
                borderBottom: '1px solid rgba(255, 255, 255, 0.1)'
            }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <h1 style={{
                            color: '#1db954',
                            margin: 0,
                            fontSize: '2.5rem',
                            fontWeight: '700',
                            background: 'linear-gradient(45deg, #1db954, #1ed760)',
                            WebkitBackgroundClip: 'text',
                            WebkitTextFillColor: 'transparent'
                        }}>
                            Tracklytics
                        </h1>
                        <p style={{ margin: '0.5rem 0 0 0', color: '#888', fontSize: '1.1rem' }}>
                            Discover your music DNA
                        </p>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
                        <div style={{ textAlign: 'right' }}>
                            <div style={{ fontSize: '1.1rem', fontWeight: '500' }}>
                                Welcome back, {user?.displayName || 'User'}
                            </div>
                            <div style={{ fontSize: '0.9rem', color: '#888' }}>
                                {user?.hasData ? 'Data synced' : 'No data yet'}
                            </div>
                        </div>
                        <button
                            onClick={logout}
                            style={{
                                background: 'rgba(255, 68, 68, 0.2)',
                                color: '#ff4444',
                                border: '1px solid #ff4444',
                                padding: '0.75rem 1.5rem',
                                borderRadius: '12px',
                                cursor: 'pointer',
                                fontSize: '0.9rem',
                                fontWeight: '500',
                                transition: 'all 0.3s ease'
                            }}
                            onMouseEnter={(e) => {
                                e.target.style.background = '#ff4444';
                                e.target.style.color = 'white';
                            }}
                            onMouseLeave={(e) => {
                                e.target.style.background = 'rgba(255, 68, 68, 0.2)';
                                e.target.style.color = '#ff4444';
                            }}
                        >
                            Sign Out
                        </button>
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <main style={{ padding: '3rem' }}>
                {/* Status Messages */}
                {error && (
                    <div style={{
                        background: 'rgba(255, 68, 68, 0.15)',
                        border: '1px solid #ff4444',
                        padding: '1rem 1.5rem',
                        borderRadius: '12px',
                        marginBottom: '2rem',
                        color: '#ff6b6b'
                    }}>
                        {error}
                    </div>
                )}

                {syncStatus && (
                    <div style={{
                        background: 'rgba(29, 185, 84, 0.15)',
                        border: '1px solid #1db954',
                        padding: '1rem 1.5rem',
                        borderRadius: '12px',
                        marginBottom: '2rem',
                        color: '#1ed760'
                    }}>
                        ✓ {syncStatus}
                    </div>
                )}

                {/* Action Cards Grid */}
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                    gap: '2rem',
                    marginBottom: '3rem'
                }}>
                    <ActionCard
                        title="Sync Data"
                        description="Update your music data from Spotify"
                        onClick={syncData}
                        loading={loading.sync}
                        color="#ff6b35"
                        icon="🔄"
                    />
                    <ActionCard
                        title="Music Personality"
                        description="Analyze your unique listening patterns"
                        onClick={getPersonality}
                        loading={loading.personality}
                        color="#1db954"
                        icon="🎭"
                    />
                    <ActionCard
                        title="Top Tracks"
                        description="View your most played songs"
                        onClick={getTracks}
                        loading={loading.tracks}
                        color="#764ba2"
                        icon="🎵"
                    />
                    <ActionCard
                        title="Top Artists"
                        description="Discover your favorite musicians"
                        onClick={getArtists}
                        loading={loading.artists}
                        color="#2196F3"
                        icon="🎤"
                    />
                    <ActionCard
                        title="Personality History"
                        description="Track how your taste evolves"
                        onClick={getHistory}
                        loading={loading.history}
                        color="#9C27B0"
                        icon="📊"
                    />
                </div>

                {/* Data Display Sections */}
                {personalityData && (
                    <div style={{
                        background: 'rgba(255, 255, 255, 0.05)',
                        padding: '3rem',
                        borderRadius: '20px',
                        marginBottom: '3rem',
                        border: '1px solid rgba(29, 185, 84, 0.3)'
                    }}>
                        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
                            <h2 style={{
                                color: '#1db954',
                                fontSize: '2.5rem',
                                margin: '0 0 1rem 0',
                                fontWeight: '600'
                            }}>
                                {personalityData.primaryPersonality}
                            </h2>
                            <p style={{
                                fontSize: '1.2rem',
                                color: '#ccc',
                                maxWidth: '600px',
                                margin: '0 auto',
                                lineHeight: '1.6'
                            }}>
                                {personalityData.description}
                            </p>
                        </div>

                        <div style={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
                            gap: '1.5rem'
                        }}>
                            <div style={{
                                textAlign: 'center',
                                background: 'rgba(255,255,255,0.08)',
                                padding: '2rem',
                                borderRadius: '16px',
                                border: '1px solid rgba(255,255,255,0.1)'
                            }}>
                                <div style={{
                                    fontSize: '3rem',
                                    fontWeight: 'bold',
                                    background: 'linear-gradient(45deg, #1db954, #1ed760)',
                                    WebkitBackgroundClip: 'text',
                                    WebkitTextFillColor: 'transparent',
                                    marginBottom: '0.5rem'
                                }}>
                                    {personalityData.diversityScore}
                                </div>
                                <div style={{ color: '#888', fontSize: '0.9rem' }}>Genre Diversity</div>
                            </div>
                            <div style={{
                                textAlign: 'center',
                                background: 'rgba(255,255,255,0.08)',
                                padding: '2rem',
                                borderRadius: '16px',
                                border: '1px solid rgba(255,255,255,0.1)'
                            }}>
                                <div style={{
                                    fontSize: '3rem',
                                    fontWeight: 'bold',
                                    background: 'linear-gradient(45deg, #ff6b35, #f9ca24)',
                                    WebkitBackgroundClip: 'text',
                                    WebkitTextFillColor: 'transparent',
                                    marginBottom: '0.5rem'
                                }}>
                                    {personalityData.mainstreamScore}
                                </div>
                                <div style={{ color: '#888', fontSize: '0.9rem' }}>Mainstream Appeal</div>
                            </div>
                            <div style={{
                                textAlign: 'center',
                                background: 'rgba(255,255,255,0.08)',
                                padding: '2rem',
                                borderRadius: '16px',
                                border: '1px solid rgba(255,255,255,0.1)'
                            }}>
                                <div style={{
                                    fontSize: '1.2rem',
                                    fontWeight: 'bold',
                                    color: '#2196F3',
                                    marginBottom: '0.5rem'
                                }}>
                                    {personalityData.listeningMood}
                                </div>
                                <div style={{ color: '#888', fontSize: '0.9rem' }}>Listening Mood</div>
                            </div>
                        </div>
                    </div>
                )}

                {tracksData && (
                    <div style={{
                        background: 'rgba(255, 255, 255, 0.05)',
                        padding: '2rem',
                        borderRadius: '20px',
                        marginBottom: '2rem',
                        border: '1px solid rgba(118, 75, 162, 0.3)'
                    }}>
                        <h3 style={{
                            color: '#764ba2',
                            marginBottom: '2rem',
                            fontSize: '1.8rem',
                            fontWeight: '600'
                        }}>
                            Your Top Tracks ({tracksData.totalTracks})
                        </h3>
                        <div style={{
                            display: 'grid',
                            gap: '1rem',
                            maxHeight: '400px',
                            overflowY: 'auto',
                            paddingRight: '1rem'
                        }}>
                            {tracksData.tracks.slice(0, 10).map((track, index) => (
                                <div key={track.id} style={{
                                    background: 'rgba(255, 255, 255, 0.05)',
                                    padding: '1.5rem',
                                    borderRadius: '12px',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '1.5rem',
                                    border: '1px solid rgba(255, 255, 255, 0.08)',
                                    transition: 'all 0.3s ease'
                                }}
                                     onMouseEnter={(e) => {
                                         e.currentTarget.style.background = 'rgba(118, 75, 162, 0.1)';
                                         e.currentTarget.style.borderColor = '#764ba2';
                                     }}
                                     onMouseLeave={(e) => {
                                         e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)';
                                         e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.08)';
                                     }}>
                                    <div style={{
                                        background: 'linear-gradient(45deg, #764ba2, #667eea)',
                                        color: 'white',
                                        width: '40px',
                                        height: '40px',
                                        borderRadius: '12px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        fontSize: '1rem',
                                        fontWeight: 'bold'
                                    }}>
                                        {index + 1}
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ fontWeight: '600', marginBottom: '0.3rem', fontSize: '1.1rem' }}>
                                            {track.name}
                                        </div>
                                        <div style={{ fontSize: '0.9rem', color: '#888' }}>
                                            {track.albumName}
                                        </div>
                                    </div>
                                    <div style={{ textAlign: 'right', fontSize: '0.9rem' }}>
                                        <div style={{ marginBottom: '0.3rem', fontWeight: '500' }}>
                                            {track.durationMs ? `${Math.floor(track.durationMs / 60000)}:${String(Math.floor((track.durationMs % 60000) / 1000)).padStart(2, '0')}` : 'N/A'}
                                        </div>
                                        <div style={{ color: '#888' }}>
                                            ♪ {track.popularity || 'N/A'}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {artistsData && (
                    <div style={{
                        background: 'rgba(255, 255, 255, 0.05)',
                        padding: '2rem',
                        borderRadius: '20px',
                        marginBottom: '2rem',
                        border: '1px solid rgba(33, 150, 243, 0.3)'
                    }}>
                        <h3 style={{
                            color: '#2196F3',
                            marginBottom: '2rem',
                            fontSize: '1.8rem',
                            fontWeight: '600'
                        }}>
                            Your Top Artists ({artistsData.totalArtists})
                        </h3>
                        <div style={{
                            display: 'grid',
                            gap: '1rem',
                            maxHeight: '400px',
                            overflowY: 'auto',
                            paddingRight: '1rem'
                        }}>
                            {artistsData.artists.slice(0, 10).map((artist, index) => (
                                <div key={artist.id} style={{
                                    background: 'rgba(255, 255, 255, 0.05)',
                                    padding: '1.5rem',
                                    borderRadius: '12px',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '1.5rem',
                                    border: '1px solid rgba(255, 255, 255, 0.08)',
                                    transition: 'all 0.3s ease'
                                }}
                                     onMouseEnter={(e) => {
                                         e.currentTarget.style.background = 'rgba(33, 150, 243, 0.1)';
                                         e.currentTarget.style.borderColor = '#2196F3';
                                     }}
                                     onMouseLeave={(e) => {
                                         e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)';
                                         e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.08)';
                                     }}>
                                    <div style={{
                                        background: 'linear-gradient(45deg, #2196F3, #21CBF3)',
                                        color: 'white',
                                        width: '40px',
                                        height: '40px',
                                        borderRadius: '12px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        fontSize: '1rem',
                                        fontWeight: 'bold'
                                    }}>
                                        {index + 1}
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ fontWeight: '600', marginBottom: '0.3rem', fontSize: '1.1rem' }}>
                                            {artist.name}
                                        </div>
                                        <div style={{ fontSize: '0.9rem', color: '#888' }}>
                                            {artist.genres ?
                                                artist.genres.split(',').slice(0, 3).map(genre => genre.trim()).join(', ')
                                                : 'No genres available'
                                            }
                                        </div>
                                    </div>
                                    <div style={{
                                        background: artist.popularity >= 70 ? 'linear-gradient(45deg, #1db954, #1ed760)' :
                                            artist.popularity >= 50 ? 'linear-gradient(45deg, #ff6b35, #f9ca24)' :
                                                'linear-gradient(45deg, #666, #888)',
                                        color: 'white',
                                        padding: '0.5rem 1rem',
                                        borderRadius: '20px',
                                        fontSize: '0.9rem',
                                        fontWeight: 'bold'
                                    }}>
                                        {artist.popularity || 'N/A'}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {historyData && (
                    <div style={{
                        background: 'rgba(255, 255, 255, 0.05)',
                        padding: '2rem',
                        borderRadius: '20px',
                        marginBottom: '2rem',
                        border: '1px solid rgba(156, 39, 176, 0.3)'
                    }}>
                        <h3 style={{
                            color: '#9C27B0',
                            marginBottom: '2rem',
                            fontSize: '1.8rem',
                            fontWeight: '600'
                        }}>
                            Personality Timeline ({historyData.totalEntries} entries)
                        </h3>
                        {historyData.history.length === 0 ? (
                            <div style={{
                                textAlign: 'center',
                                padding: '3rem',
                                color: '#888'
                            }}>
                                <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📊</div>
                                <p>No personality history yet. Your weekly snapshots will appear here!</p>
                            </div>
                        ) : (
                            <div style={{
                                display: 'flex',
                                flexDirection: 'column',
                                gap: '1.5rem',
                                maxHeight: '500px',
                                overflowY: 'auto',
                                paddingRight: '1rem'
                            }}>
                                {historyData.history.map((entry, index) => (
                                    <div key={entry.id} style={{
                                        background: entry.isLatest ? 'rgba(156, 39, 176, 0.15)' : 'rgba(255, 255, 255, 0.05)',
                                        border: entry.isLatest ? '2px solid #9C27B0' : '1px solid rgba(255, 255, 255, 0.1)',
                                        padding: '2rem',
                                        borderRadius: '16px',
                                        position: 'relative'
                                    }}>
                                        {entry.isLatest && (
                                            <div style={{
                                                position: 'absolute',
                                                top: '-12px',
                                                left: '24px',
                                                background: 'linear-gradient(45deg, #9C27B0, #E91E63)',
                                                color: 'white',
                                                padding: '0.4rem 1rem',
                                                borderRadius: '20px',
                                                fontSize: '0.8rem',
                                                fontWeight: 'bold',
                                                letterSpacing: '0.5px'
                                            }}>
                                                CURRENT
                                            </div>
                                        )}

                                        <div style={{
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            alignItems: 'center',
                                            marginBottom: '1.5rem'
                                        }}>
                                            <h4 style={{
                                                color: entry.isLatest ? '#9C27B0' : '#ccc',
                                                margin: 0,
                                                fontSize: '1.4rem',
                                                fontWeight: '600'
                                            }}>
                                                {entry.primaryPersonality}
                                            </h4>
                                            <span style={{
                                                color: '#888',
                                                fontSize: '0.9rem'
                                            }}>
                                                {new Date(entry.analyzedAt).toLocaleDateString()} {new Date(entry.analyzedAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                                            </span>
                                        </div>

                                        <div style={{
                                            display: 'grid',
                                            gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))',
                                            gap: '1rem'
                                        }}>
                                            <div style={{
                                                background: 'rgba(255, 255, 255, 0.08)',
                                                padding: '1rem',
                                                borderRadius: '12px',
                                                textAlign: 'center',
                                                border: '1px solid rgba(255, 255, 255, 0.1)'
                                            }}>
                                                <div style={{ fontSize: '0.8rem', marginBottom: '0.5rem', color: '#888' }}>Diversity</div>
                                                <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1db954' }}>{entry.diversityScore}</div>
                                            </div>
                                            <div style={{
                                                background: 'rgba(255, 255, 255, 0.08)',
                                                padding: '1rem',
                                                borderRadius: '12px',
                                                textAlign: 'center',
                                                border: '1px solid rgba(255, 255, 255, 0.1)'
                                            }}>
                                                <div style={{ fontSize: '0.8rem', marginBottom: '0.5rem', color: '#888' }}>Mainstream</div>
                                                <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1ed760' }}>{entry.mainstreamScore}</div>
                                            </div>
                                            <div style={{
                                                background: 'rgba(255, 255, 255, 0.08)',
                                                padding: '1rem',
                                                borderRadius: '12px',
                                                textAlign: 'center',
                                                border: '1px solid rgba(255, 255, 255, 0.1)'
                                            }}>
                                                <div style={{ fontSize: '0.7rem', marginBottom: '0.3rem', color: '#888' }}>Mood</div>
                                                <div style={{ fontSize: '0.9rem', fontWeight: 'bold', color: '#1db954' }}>{entry.listeningMood}</div>
                                            </div>
                                            <div style={{
                                                background: 'rgba(255, 255, 255, 0.08)',
                                                padding: '1rem',
                                                borderRadius: '12px',
                                                textAlign: 'center',
                                                border: '1px solid rgba(255, 255, 255, 0.1)'
                                            }}>
                                                <div style={{ fontSize: '0.7rem', marginBottom: '0.3rem', color: '#888' }}>Loyalty</div>
                                                <div style={{ fontSize: '0.9rem', fontWeight: 'bold', color: '#1ed760' }}>{entry.artistLoyalty}</div>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </main>
        </div>
    );
};

export default Dashboard;