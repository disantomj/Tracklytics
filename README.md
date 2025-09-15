# 🎵 Tracklytics

A comprehensive music analytics platform that integrates with Spotify to analyze users' listening habits and generate personalized music personality insights. Demonstrates full-stack development with OAuth2 integration, automated data processing, and advanced analytics algorithms.

## Live Application Features

### Music Personality Analysis Engine
**Intelligent Categorization**: Advanced algorithm analyzes listening patterns to determine personality archetypes
- "The Mainstream Explorer" (diverse genres + popular tracks)
- "The Underground Adventurer" (diverse genres + niche discoveries) 
- "The Chart Loyalist" (focused preferences + mainstream hits)
- "The Niche Enthusiast" (specialized taste + underground music)
- "The Balanced Listener" (moderate across all metrics)

**Multi-Dimensional Scoring**: Quantitative analysis across multiple musical dimensions
- Genre Diversity Score (0-100): Measures breadth of musical exploration
- Mainstream Appeal (0-100): Analyzes preference for popular vs. underground music
- Listening Mood Classification: Energy-based categorization of musical preferences
- Artist Loyalty Assessment: Deep-dive vs. variety-seeking behavior analysis
- Track Length Preferences: Preference analysis from quick hits to epic journeys

### Historical Intelligence & Trend Analysis
- **Personality Evolution Tracking**: Weekly automated snapshots capture taste evolution over time
- **Timeline Visualization**: Interactive historical view showing personality shifts and musical development
- **Automated Data Synchronization**: Daily background processing keeps analysis current with latest listening habits

### Modern User Experience
- **Glassmorphism Design Language**: Contemporary UI with blur effects, gradients, and smooth animations
- **Responsive Architecture**: Seamless experience across desktop, tablet, and mobile devices
- **Real-time State Management**: Dynamic loading states and error handling for optimal user experience
- **Interactive Dashboard**: Action card interface for intuitive navigation and feature discovery

## Technical Implementation

### Architecture Highlights
- **Microservice-Ready Backend**: Modular Spring Boot architecture with clear separation of concerns
- **JWT Authentication Pipeline**: Secure token-based authentication with Spotify OAuth2 integration
- **Automated Processing Engine**: Scheduled background services for data synchronization and analysis
- **RESTful API Design**: Clean, documented endpoints following REST principles

### Technology Stack
- **Backend Framework**: Spring Boot 3.5.4 with Java 21
- **Database**: PostgreSQL with JPA/Hibernate ORM
- **Authentication**: Spring Security with OAuth2 client and JWT implementation
- **Frontend**: React 18 with modern hooks and Context API
- **API Integration**: Spotify Web API with comprehensive scope permissions
- **Deployment**: Docker containerization with PostgreSQL service orchestration

### Notable Technical Solutions
- **OAuth2 Flow Management**: Complete Spotify integration with token refresh handling and scope management
- **Scheduled Data Processing**: Automated sync services with error handling and rate limiting
- **Music Analytics Algorithms**: Custom algorithms for genre diversity scoring and personality classification
- **Secure Configuration Management**: Environment-based configuration with secret management
- **Database Relationship Modeling**: Complex many-to-many relationships for user music preferences

## Key Development Challenges

### Spotify API Integration
Implemented comprehensive OAuth2 flow with token management, scope handling, and rate limiting to ensure reliable access to user music data while respecting Spotify's API constraints.

### Music Analysis Algorithm Development
Developed sophisticated algorithms to quantify musical taste across multiple dimensions, including genre diversity calculation, mainstream appeal scoring, and behavioral pattern recognition.

### Real-time Data Synchronization
Built automated background processing system to keep user data current while managing API rate limits and handling authentication token refresh cycles.

### Secure Configuration Architecture
Implemented environment-based configuration system to protect sensitive credentials while maintaining development workflow efficiency and production security.

## Development Methodology
**Test-Driven Development**: Comprehensive testing strategy for business logic and API endpoints
**Security-First Design**: JWT implementation, OAuth2 best practices, and environment variable management
**Component Architecture**: Reusable React components with consistent API design
**Database-First Modeling**: Proper normalization and relationship design for scalable data architecture

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** or higher
- **Node.js 16+** and npm
- **PostgreSQL** (or Docker)
- **Git**
- **Spotify Developer Account** (free)

### 🎵 Spotify App Setup

1. Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Click **"Create App"**
3. Fill in your app details:
   - **App Name**: `Tracklytics Local Dev`
   - **App Description**: `Personal music analytics application`
   - **Redirect URI**: `http://127.0.0.1:8080/login/oauth2/code/spotify`
   - **API Used**: `Web API`
4. Save your **Client ID** and **Client Secret** - you'll need these!

### 📁 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/disantomj/tracklytics.git
   cd tracklytics
   ```

2. **Set up environment variables**
   
   Copy the example environment file:
   ```bash
   cp .env.example .env
   ```
   
   Edit `.env` with your actual Spotify credentials:
   ```bash
   # .env file
   SPOTIFY_CLIENT_ID=your_actual_spotify_client_id
   SPOTIFY_CLIENT_SECRET=your_actual_spotify_client_secret
   DATABASE_URL=jdbc:postgresql://localhost:5332/tracklytics
   DATABASE_USERNAME=postgres
   DATABASE_PASSWORD=password
   ```

3. **Start PostgreSQL Database**
   
   Using Docker (recommended):
   ```bash
   docker-compose up -d
   ```
   
   Or install PostgreSQL locally and create a database named `tracklytics`

### 🖥️ Backend Setup

1. **Navigate to the project root** (if not already there)
   ```bash
   cd tracklytics
   ```

2. **Set environment variables** (choose your platform):
   
   **Windows (Command Prompt):**
   ```cmd
   set SPOTIFY_CLIENT_ID=your_actual_client_id
   set SPOTIFY_CLIENT_SECRET=your_actual_client_secret
   ```
   
   **Windows (PowerShell):**
   ```powershell
   $env:SPOTIFY_CLIENT_ID="your_actual_client_id"
   $env:SPOTIFY_CLIENT_SECRET="your_actual_client_secret"
   ```
   
   **Mac/Linux:**
   ```bash
   export SPOTIFY_CLIENT_ID="your_actual_client_id"
   export SPOTIFY_CLIENT_SECRET="your_actual_client_secret"
   ```

3. **Run the Spring Boot application**
   ```bash
   ./mvnw spring-boot:run
   ```
   
   The backend will start at: `http://localhost:8080`

4. **Verify backend is running**
   
   Open: `http://localhost:8080` - you should see the Tracklytics welcome page

### 🎨 Frontend Setup

1. **Open a new terminal** and navigate to the frontend directory:
   ```bash
   cd frontend-react
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Create frontend environment file**
   ```bash
   # frontend-react/.env
   REACT_APP_API_URL=http://localhost:8080
   ```

4. **Start the React development server**
   ```bash
   npm start
   ```
   
   The frontend will start at: `http://localhost:3000`

## 🔑 Authentication Flow

Tracklytics uses a JWT-based authentication system integrated with Spotify OAuth2:

### Step 1: Get Your JWT Token

1. **Open your browser** and go to: `http://localhost:8080`
2. **Click "Login with Spotify"** to initiate OAuth2 flow
3. **Authorize the application** with your Spotify account
4. **Copy the JWT token** from the authentication success page

### Step 2: Access the Frontend Application

1. **Open the React app**: `http://localhost:3000`
2. **Click "Get Token from Backend"** - opens the Spring Boot authentication flow
3. **After Spotify authentication, copy your JWT token**
4. **Paste the token** into the login form on the React application
5. **Click "Login with Token"** to access the dashboard

### Step 3: Initialize Your Music Data

1. **Click "Sync Data"** to import your Spotify listening history
2. **Wait for synchronization** (typically 10-30 seconds for 50 tracks + 50 artists)
3. **Click "Music Personality"** to generate your initial analysis
4. **Explore additional features** like track/artist browsing and personality history

## 📊 API Architecture

### Authentication Endpoints
- `GET /oauth2/authorization/spotify` - Initiate Spotify OAuth2 flow
- `GET /api/auth/me` - Retrieve authenticated user information
- `GET /test-jwt` - JWT token validation and testing interface

### Data Synchronization
- `POST /api/sync` - Trigger Spotify data synchronization
- `GET /api/tracks` - Retrieve user's top tracks with metadata
- `GET /api/artists` - Retrieve user's top artists with genre information

### Analytics Engine
- `GET /api/personality` - Generate current music personality analysis
- `GET /api/personality/history` - Retrieve personality evolution timeline

## 🗄️ Database Architecture

```sql
-- Core Entities
users (id, spotify_id, display_name, email, access_token, refresh_token, token_expiry, last_sync_time)
tracks (id, spotify_id, name, album_name, duration_ms, popularity)
artists (id, spotify_id, name, popularity, genres)

-- Relationship Tables
user_top_tracks (user_id, track_id)
user_top_artists (user_id, artist_id)

-- Analytics Storage
personality_history (id, user_id, analyzed_at, primary_personality, diversity_score, 
                    mainstream_score, listening_mood, artist_loyalty, track_length_preference)
```

## 🔧 Configuration Management

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPOTIFY_CLIENT_ID` | Spotify application client identifier | - | ✅ |
| `SPOTIFY_CLIENT_SECRET` | Spotify application client secret | - | ✅ |
| `DATABASE_URL` | PostgreSQL connection string | `jdbc:postgresql://localhost:5332/tracklytics` | ❌ |
| `DATABASE_USERNAME` | Database authentication username | `postgres` | ❌ |
| `DATABASE_PASSWORD` | Database authentication password | `password` | ❌ |
| `JWT_SECRET` | JWT token signing secret | Auto-generated secure default | ❌ |
| `JWT_EXPIRATION` | Token expiration time (seconds) | `86400` (24 hours) | ❌ |

## 🚀 Production Deployment

### Docker Containerization

1. **Build the application**
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **Deploy with Docker Compose**
   ```bash
   docker-compose up --build
   ```

### Production Environment Considerations
- Configure secure JWT secrets with sufficient entropy
- Enable HTTPS/TLS encryption for production traffic
- Update Spotify app redirect URIs for production domains
- Implement production-grade database credentials and connection pooling
- Configure appropriate CORS origins for frontend domains

## 🧪 Testing & Validation

**Backend API Testing:**
```bash
# Verify backend service health
curl http://localhost:8080

# Test authenticated endpoints (replace with actual JWT)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8080/api/auth/me
```

**Frontend Development Server:**
```bash
# Run frontend test suite
cd frontend-react && npm test
```

## 🔍 Troubleshooting

### Common Issues & Solutions

**"Spotify authentication failed"**
- Verify Client ID and Client Secret accuracy in environment variables
- Ensure redirect URI exactly matches: `http://127.0.0.1:8080/login/oauth2/code/spotify`
- Confirm Spotify app is not in quota exceeded state

**"Database connection failed"**
- Verify PostgreSQL service status: `docker-compose ps`
- Validate database credentials in `.env` configuration
- Confirm database URL format and port accessibility

**"JWT token invalid"**
- Token may have expired (default 24-hour lifespan)
- Ensure complete token copying without truncation
- Generate fresh token via `/test-jwt` endpoint

**"Frontend API connection failed"**
- Verify backend service is running on port 8080
- Check CORS configuration in Spring Boot application
- Confirm `REACT_APP_API_URL` environment variable accuracy

## Project Scope

This application demonstrates comprehensive full-stack development capabilities including:

- **Advanced Backend Architecture**: Spring Boot microservice design with security, scheduling, and external API integration
- **Complex State Management**: React application with authentication flows, real-time updates, and multi-component coordination  
- **Music Analytics Algorithms**: Custom algorithms for personality classification and trend analysis
- **Production-Ready Security**: OAuth2 implementation, JWT management, and secure configuration practices
- **Automated Data Processing**: Background services for data synchronization and personality analysis
- **Modern DevOps Practices**: Docker containerization, environment management, and deployment automation

**Note**: This application requires active Spotify account authentication and demonstrates real-world integration with third-party APIs for production-ready music analytics.
