// Show the selected tab and hide the others
function showTab(tabId) {
    document.getElementById('generatorTab').style.display = tabId === 'generatorTab' ? 'block' : 'none';
    document.getElementById('savedTab').style.display = tabId === 'savedTab' ? 'block' : 'none';
    document.getElementById('logTab').style.display = tabId === 'logTab' ? 'block' : 'none';

    // Update log content when the log tab is shown
    if (tabId === 'logTab') {
        updateLogContent();
    }
}

// Store saved workouts and logs
const savedWorkouts = [];
const workoutLogs = {};

// Generate workouts (existing function)
function generateWorkout() {
    const muscle = document.getElementById('muscle').value;
    const difficulty = document.getElementById('difficulty').value;
    const equipment = document.getElementById('equipment').value;

    document.getElementById('muscleSummary').innerText = muscle;
    document.getElementById('difficultySummary').innerText = difficulty;
    document.getElementById('equipmentSummary').innerText = equipment;

    fetchWorkoutData(muscle, difficulty, equipment);
}

// Fetch workout data (existing function)
function fetchWorkoutData(muscle, difficulty, equipment) {
    const apiUrl = `https://api.api-ninjas.com/v1/exercises?muscle=${muscle}`;

    fetch(apiUrl, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Api-Key': 'ViDZePNbkVmheKpy62fxYQ==xIjZAovy9dXD0DR5'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            displayWorkoutData(data, difficulty, equipment);
        })
        .catch(error => {
            console.error("Error fetching data:", error);
            document.getElementById('resultContent').innerHTML = 'Failed to load workouts. Please try again later.';
        });
}

// Display workouts and save selected ones (existing function)
function displayWorkoutData(data, difficultyFilter, equipmentFilter) {
    document.getElementById('resultContent').innerHTML = '';
    const filteredData = data.filter(exercise =>
        (!difficultyFilter || exercise.difficulty.toLowerCase() === difficultyFilter.toLowerCase()) &&
        (!equipmentFilter || exercise.equipment.toLowerCase().includes(equipmentFilter.toLowerCase()))
    );

    let workoutContent = '';
    filteredData.forEach((exercise, index) => {
        workoutContent += `
            <div class="exercise-item">
                <input type="checkbox" id="exercise-${index}" value="${index}" class="exercise-checkbox">
                <label for="exercise-${index}">
                    <strong>Exercise:</strong> ${exercise.name}<br>
                    <strong>Type:</strong> ${exercise.type}<br>
                    <strong>Muscle:</strong> ${exercise.muscle}<br>
                    <strong>Equipment:</strong> ${exercise.equipment}<br>
                    <strong>Difficulty:</strong> ${exercise.difficulty}<br>
                </label>
            </div><br>
        `;
    });

    document.getElementById('resultContent').innerHTML = workoutContent;
    window.exercisesData = filteredData;
}

// Save selected workouts to "Saved Workouts" tab (existing function)
function saveSelectedWorkouts() {
    const selectedExercises = [];
    document.querySelectorAll('.exercise-checkbox:checked').forEach(checkbox => {
        const index = parseInt(checkbox.value);
        selectedExercises.push(window.exercisesData[index]);
    });

    if (selectedExercises.length === 0) {
        alert("No workouts selected!");
        return;
    }

    savedWorkouts.push(...selectedExercises);
    updateSavedContent();
    showTab('savedTab');
}

// Update "Saved Workouts" tab content
function updateSavedContent() {
    let savedContent = '';
    savedWorkouts.forEach(exercise => {
        savedContent += `
            <div class="saved-exercise">
                <strong>Exercise:</strong> ${exercise.name}<br>
                <strong>Type:</strong> ${exercise.type}<br>
                <strong>Muscle:</strong> ${exercise.muscle}<br>
                <strong>Equipment:</strong> ${exercise.equipment}<br>
                <strong>Difficulty:</strong> ${exercise.difficulty}<br>
            </div><br>
        `;
    });

    document.getElementById('savedContent').innerHTML = savedContent;
}

// Display workout log with sets, reps, and weight inputs
function updateLogContent() {
    if (savedWorkouts.length === 0) {
        document.getElementById('logContent').innerHTML = 'No workouts added to log yet.';
        return;
    }

    let logContent = '';
    savedWorkouts.forEach((exercise, index) => {
        logContent += `
            <div class="log-entry">
                <strong>${exercise.name}</strong><br>
                <label>Sets:</label>
                <input type="number" id="sets-${index}" placeholder="0">
                <label>Reps:</label>
                <input type="number" id="reps-${index}" placeholder="0">
                <label>Weight:</label>
                <input type="number" id="weight-${index}" placeholder="0 lbs"><br><br>
            </div>
        `;
    });

    logContent += `<button onclick="saveWorkoutLog()">Save Workout Log</button>`;
    document.getElementById('logContent').innerHTML = logContent;
}

// Save workout log entries
function saveWorkoutLog() {
    savedWorkouts.forEach((exercise, index) => {
        const sets = document.getElementById(`sets-${index}`).value;
        const reps = document.getElementById(`reps-${index}`).value;
        const weight = document.getElementById(`weight-${index}`).value;

        workoutLogs[exercise.name] = { sets, reps, weight };
    });

    alert("Workout log saved successfully!");
}
