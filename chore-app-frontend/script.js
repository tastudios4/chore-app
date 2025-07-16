// script.js
const API_BASE_URL = 'http://localhost:8080/api'; // Your Spring Boot backend URL

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

// --- User Management ---

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
            showMessage('registerUserMessage', `User registered! ID: ${data.id}, Username: ${data.username}`, 'success');
            document.getElementById('registerUsername').value = '';
            document.getElementById('registerPassword').value = '';
        } else {
            showMessage('registerUserMessage', `Error: ${data.message || response.statusText}`, 'error');
        }
    } catch (error) {
        console.error('Error registering user:', error);
        showMessage('registerUserMessage', 'Network error or server unreachable.', 'error');
    }
}

async function fetchUserPoints() {
    const userId = document.getElementById('userIdPoints').value;
    hideMessage('userPointsDisplay');

    if (!userId) {
        showMessage('userPointsDisplay', 'Please enter a User ID.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/users/${userId}`);
        const data = await response.json();

        if (response.ok) {
            showMessage('userPointsDisplay', `User: ${data.username}, Points: ${data.points}`, 'success');
        } else {
            showMessage('userPointsDisplay', `Error: ${data.message || response.statusText}`, 'error');
        }
    } catch (error) {
        console.error('Error fetching user points:', error);
        showMessage('userPointsDisplay', 'Network error or server unreachable.', 'error');
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
        } else {
            showMessage('joinTribeMessage', `Error: ${data.message || response.statusText}`, 'error');
        }
    } catch (error) {
        console.error('Error joining tribe:', error);
        showMessage('joinTribeMessage', 'Network error or server unreachable.', 'error');
    }
}

// --- Chore Management ---

async function createChore() {
    const tribeId = document.getElementById('createChoreTribeId').value;
    const name = document.getElementById('choreName').value;
    const description = document.getElementById('choreDescription').value;
    const pointsValue = document.getElementById('chorePoints').value;
    const dueDate = document.getElementById('choreDueDate').value; // YYYY-MM-DD format
    const isRecurring = document.getElementById('isRecurringChore').checked;
    const recurrencePattern = document.getElementById('recurrencePattern').value;
    hideMessage('createChoreMessage');

    if (!tribeId || !name || !pointsValue) {
        showMessage('createChoreMessage', 'Please fill in Chore Name, Points Value, and Tribe ID.', 'error');
        return;
    }
    if (isRecurring && !recurrencePattern) {
        showMessage('createChoreMessage', 'Please provide a recurrence pattern for recurring chores.', 'error');
        return;
    }

    const choreData = {
        name,
        description,
        pointsValue: parseInt(pointsValue),
        dueDate: dueDate || null, // Send null if empty
        isRecurring,
        recurrencePattern: isRecurring ? recurrencePattern : null,
        isActive: true
    };

    try {
        const response = await fetch(`${API_BASE_URL}/chores/tribe/${tribeId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(choreData)
        });

        const data = await response.json();

        if (response.ok) {
            showMessage('createChoreMessage', `Chore created! ID: ${data.id}, Name: ${data.name}, Recurring: ${data.isRecurring}`, 'success');
            // Clear form fields
            document.getElementById('choreName').value = '';
            document.getElementById('choreDescription').value = '';
            document.getElementById('chorePoints').value = '10';
            document.getElementById('choreDueDate').value = '';
            document.getElementById('isRecurringChore').checked = false;
            document.getElementById('recurrencePattern').value = '';
            // Optionally refresh active chores list if tribe ID matches
            if (document.getElementById('activeChoresTribeId').value === tribeId) {
                fetchActiveChores();
            }
        } else {
            showMessage('createChoreMessage', `Error: ${data.message || response.statusText}`, 'error');
        }
    } catch (error) {
        console.error('Error creating chore:', error);
        showMessage('createChoreMessage', 'Network error or server unreachable.', 'error');
    }
}

async function fetchActiveChores() {
    const tribeId = document.getElementById('activeChoresTribeId').value;
    const choresListDiv = document.getElementById('activeChoresList');
    hideMessage('activeChoresMessage');
    choresListDiv.innerHTML = '<p class="text-gray-500">Loading chores...</p>';

    if (!tribeId) {
        showMessage('activeChoresMessage', 'Please enter a Tribe ID.', 'error');
        choresListDiv.innerHTML = '<p class="text-gray-500">No active chores loaded yet.</p>';
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/chores/tribe/${tribeId}/active`);
        const data = await response.json();

        if (response.ok) {
            choresListDiv.innerHTML = ''; // Clear previous list
            if (data.length === 0) {
                choresListDiv.innerHTML = '<p class="text-gray-500">No active chores found for this tribe.</p>';
            } else {
                data.forEach(chore => {
                    const choreItem = document.createElement('div');
                    choreItem.className = 'list-item';
                    choreItem.innerHTML = `
                        <span>
                            <strong>ID: ${chore.id}</strong> - ${chore.name} (${chore.pointsValue} pts)
                            ${chore.dueDate ? ` - Due: ${chore.dueDate}` : ''}
                            ${chore.isRecurring ? ` (Recurring: ${chore.recurrencePattern})` : ''}
                        </span>
                        <button onclick="prefillCompleteChore(${chore.id})" class="btn btn-secondary text-sm px-3 py-1">Complete</button>
                    `;
                    choresListDiv.appendChild(choreItem);
                });
            }
        } else {
            showMessage('activeChoresMessage', `Error: ${data.message || response.statusText}`, 'error');
            choresListDiv.innerHTML = '<p class="text-gray-500">Failed to load chores.</p>';
        }
    } catch (error) {
        console.error('Error fetching active chores:', error);
        showMessage('activeChoresMessage', 'Network error or server unreachable.', 'error');
        choresListDiv.innerHTML = '<p class="text-gray-500">Failed to load chores.</p>';
    }
}

function prefillCompleteChore(choreId) {
    document.getElementById('completeChoreId').value = choreId;
    // Optionally scroll to the complete chore section
    document.getElementById('completeChoreId').scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// --- Chore Completion ---

async function completeChore() {
    const choreId = document.getElementById('completeChoreId').value;
    const userId = document.getElementById('completingUserId').value;
    hideMessage('completeChoreMessage');

    if (!choreId || !userId) {
        showMessage('completeChoreMessage', 'Please enter both Chore ID and your User ID.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/chore-completions/${choreId}/complete-by/${userId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        const data = await response.json();

        if (response.ok) {
            showMessage('completeChoreMessage', `Chore ID ${data.chore.id} completed by User ID ${data.completedBy.id}! Points Awarded: ${data.pointsAwarded}. User now has ${data.completedBy.points} points.`, 'success');
            document.getElementById('completeChoreId').value = '';
            // Optionally refresh user points and active chores list
            fetchUserPoints();
            fetchActiveChores();
            fetchCompletionHistory();
        } else {
            showMessage('completeChoreMessage', `Error: ${data.message || response.statusText}`, 'error');
        }
    } catch (error) {
        console.error('Error completing chore:', error);
        showMessage('completeChoreMessage', 'Network error or server unreachable.', 'error');
    }
}

async function fetchCompletionHistory() {
    const userId = document.getElementById('historyUserId').value;
    const historyListDiv = document.getElementById('completionHistoryList');
    hideMessage('historyMessage');
    historyListDiv.innerHTML = '<p class="text-gray-500">Loading history...</p>';

    if (!userId) {
        showMessage('historyMessage', 'Please enter a User ID.', 'error');
        historyListDiv.innerHTML = '<p class="text-gray-500">No history loaded yet.</p>';
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/chore-completions/user/${userId}`);
        const data = await response.json();

        if (response.ok) {
            historyListDiv.innerHTML = ''; // Clear previous list
            if (data.length === 0) {
                historyListDiv.innerHTML = '<p class="text-gray-500">No completion history found for this user.</p>';
            } else {
                data.forEach(completion => {
                    const historyItem = document.createElement('div');
                    historyItem.className = 'list-item';
                    historyItem.innerHTML = `
                        <span>
                            <strong>Chore: ${completion.chore.name}</strong> (ID: ${completion.chore.id}) - Completed: ${new Date(completion.completionDate).toLocaleString()} - Awarded: ${completion.pointsAwarded} pts
                        </span>
                    `;
                    historyListDiv.appendChild(historyItem);
                });
            }
        } else {
            showMessage('historyMessage', `Error: ${data.message || response.statusText}`, 'error');
            historyListDiv.innerHTML = '<p class="text-gray-500">Failed to load history.</p>';
        }
    } catch (error) {
        console.error('Error fetching completion history:', error);
        showMessage('historyMessage', 'Network error or server unreachable.', 'error');
        historyListDiv.innerHTML = '<p class="text-gray-500">Failed to load history.</p>';
    }
}

// Set default values for convenience
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('registerUsername').value = '';
    document.getElementById('registerPassword').value = '';
    document.getElementById('userIdPoints').value = '';
    document.getElementById('createTribeName').value = '';
    document.getElementById('joinUserId').value = '';
    // joinTribeCode needs to be manually copied from backend response
    document.getElementById('createChoreTribeId').value = '';
    document.getElementById('activeChoresTribeId').value = '';
    document.getElementById('completingUserId').value = '';
    document.getElementById('historyUserId').value = '';
});
