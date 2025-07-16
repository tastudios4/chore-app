// script.js
const API_BASE_URL = 'http://localhost:8080/api'; // Your Spring Boot backend URL

// --- Navigation ---
function navigateTo(page) {
    window.location.href = page;
}

// Helper function to display messages
function showMessage(elementId, message, type) {
    const element = document.getElementById(elementId);
    element.textContent = message;
    element.className = `message ${type}`;
    element.classList.remove('hidden');
}

// Helper function to hide messages
function hideMessage(elementId) {
    document.getElementById(elementId).classList.add('hidden');
}

// --- User Management (Login/Register) ---

async function registerUser() {
    const username = document.getElementById('registerUsername').value;
    const password = document.getElementById('registerPassword').value;
    hideMessage('registerUserMessage');

    if (!username || !password) {
        showMessage('registerUserMessage', 'Please enter both username and password.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/users/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            showMessage('registerUserMessage', `User registered! ID: ${data.id}, Username: ${data.username}. You can now log in.`, 'success');
            document.getElementById('registerUsername').value = '';
            document.getElementById('registerPassword').value = '';
            // Optionally pre-fill login fields or redirect
            document.getElementById('loginUsername').value = username;
            document.getElementById('loginPassword').value = password;
        } else {
            showMessage('registerUserMessage', `Error: ${data.message || response.statusText}`, 'error');
        }
    } catch (error) {
        console.error('Error registering user:', error);
        showMessage('registerUserMessage', 'Network error or server unreachable.', 'error');
    }
}

async function loginUser() {
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    hideMessage('loginUserMessage');

    if (!username || !password) {
        showMessage('loginUserMessage', 'Please enter both username and password.', 'error');
        return;
    }

    // For now, we'll simulate login by just checking if user exists.
    // A real login would involve authentication (e.g., Spring Security, JWT).
    try {
        const response = await fetch(`${API_BASE_URL}/users/by-username/${username}`);
        const data = await response.json();

        if (response.ok && data.password === password) { // Basic password check (NOT SECURE FOR PROD)
            showMessage('loginUserMessage', `Logged in as ${data.username}! User ID: ${data.id}`, 'success');
            // Store user ID for later use (e.g., in tribe management, chore completion)
            localStorage.setItem('loggedInUserId', data.id);
            localStorage.setItem('loggedInUsername', data.username);
            localStorage.setItem('loggedInUserTribeId', data.tribe ? data.tribe.id : null); // Store tribe ID if user is in a tribe
            // Redirect to tribe management page after successful login
            navigateTo('tribe_management.html');
        } else {
            showMessage('loginUserMessage', `Login failed: Invalid username or password.`, 'error');
        }
    } catch (error) {
        console.error('Error logging in:', error);
        showMessage('loginUserMessage', 'Network error or server unreachable or user not found.', 'error');
    }
}

// --- Tribe Management ---

async function createTribe() {
    const tribeName = document.getElementById('createTribeName').value;
    hideMessage('createTribeMessage');

    if (!tribeName) {
        showMessage('createTribeMessage', 'Please enter a tribe name.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/tribes`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: tribeName })
        });

        const data = await response.json();

        if (response.ok) {
            showMessage('createTribeMessage', `Tribe created! ID: ${data.id}, Name: ${data.name}, Join Code: ${data.joinCode}`, 'success');
            document.getElementById('createTribeName').value = '';
        } else {
            showMessage('createTribeMessage', `Error: ${data.message || response.statusText}`, 'error');
        }
    } catch (error) {
        console.error('Error creating tribe:', error);
        showMessage('createTribeMessage', 'Network error or server unreachable.', 'error');
    }
}

async function joinTribe() {
    const userId = document.getElementById('joinUserId').value;
    const joinCode = document.getElementById('joinTribeCode').value;
    hideMessage('joinTribeMessage');

    if (!userId || !joinCode) {
        showMessage('joinTribeMessage', 'Please enter both User ID and Join Code.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/users/${userId}/join-tribe/${joinCode}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' }
        });

        const data = await response.json();

        if (response.ok) {
            showMessage('joinTribeMessage', `User ${data.username} joined tribe ${data.tribe.name}!`, 'success');
            document.getElementById('joinUserId').value = '';
            document.getElementById('joinTribeCode').value = '';
            // Update stored user info if this was the logged-in user
            if (localStorage.getItem('loggedInUserId') === userId) {
                localStorage.setItem('loggedInUserTribeId', data.tribe.id);
            }
        } else {
            showMessage('joinTribeMessage', `Error: ${data.message || response.statusText}`, 'error');
        }
    } catch (error) {
        console.error('Error joining tribe:', error);
        showMessage('joinTribeMessage', 'Network error or server unreachable.', 'error');
    }
}

async function leaveTribe() {
    const userId = document.getElementById('leaveUserId').value;
    hideMessage('leaveTribeMessage');

    if (!userId) {
        showMessage('leaveTribeMessage', 'Please enter your User ID.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/users/${userId}/leave-tribe`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' }
        });

        const data = await response.json();

        if (response.ok) {
            showMessage('leaveTribeMessage', `User ${data.username} successfully left their tribe.`, 'success');
            document.getElementById('leaveUserId').value = '';
            // Clear stored tribe info if this was the logged-in user
            if (localStorage.getItem('loggedInUserId') === userId) {
                localStorage.removeItem('loggedInUserTribeId');
            }
        } else {
            showMessage('leaveTribeMessage', `Error: ${data.message || response.statusText}`, 'error');
        }
    } catch (error) {
        console.error('Error leaving tribe:', error);
        showMessage('leaveTribeMessage', 'Network error or server unreachable.', 'error');
    }
}

// --- Leaderboard Page Specific Logic ---
async function loadLeaderboardPageData() {
    const userId = localStorage.getItem('loggedInUserId');
    const username = localStorage.getItem('loggedInUsername');
    const currentUserPointsElement = document.getElementById('currentUserPoints');
    const currentUsernameElement = document.getElementById('currentUsername');

    if (username) {
        currentUsernameElement.textContent = username;
    } else {
        currentUsernameElement.textContent = 'Not logged in';
    }

    if (userId) {
        try {
            const response = await fetch(`${API_BASE_URL}/users/${userId}`);
            const data = await response.json();

            if (response.ok) {
                currentUserPointsElement.textContent = data.points;
            } else {
                currentUserPointsElement.textContent = 'N/A';
                console.error('Error fetching user points for leaderboard:', data.message || response.statusText);
            }
        } catch (error) {
            currentUserPointsElement.textContent = 'N/A';
            console.error('Network error fetching user points for leaderboard:', error);
        }
    } else {
        currentUserPointsElement.textContent = 'N/A (Log in)';
    }
}

// --- Initial Setup / Default Values ---
document.addEventListener('DOMContentLoaded', () => {
    // Set default values for convenience on relevant pages
    if (document.getElementById('registerUsername')) { // Only on login.html
        document.getElementById('registerUsername').value = 'testUser';
        document.getElementById('registerPassword').value = 'testPassword';
        document.getElementById('loginUsername').value = 'testUser';
        document.getElementById('loginPassword').value = 'testPassword';
    }

    if (document.getElementById('joinUserId')) { // Only on tribe_management.html
        const loggedInUserId = localStorage.getItem('loggedInUserId');
        if (loggedInUserId) {
            document.getElementById('joinUserId').value = loggedInUserId;
            document.getElementById('leaveUserId').value = loggedInUserId;
        } else {
            // Fallback if not logged in, or for initial setup
            document.getElementById('joinUserId').value = '1';
            document.getElementById('leaveUserId').value = '1';
        }
        document.getElementById('createTribeName').value = 'My New Tribe';
        // joinTribeCode needs to be manually copied from backend response
    }

    if (document.getElementById('currentUserPoints')) { // Only on leaderboard.html
        loadLeaderboardPageData();
    }
});
