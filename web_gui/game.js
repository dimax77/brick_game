// gameBoard.js
import { applyRootStyles } from './src/utils.js';
import { GameBoard } from './src/game-board.js';
import { rootStyles, keyCodes } from './src/config.js';
import { GAME_BOARD_WIDTH, GAME_BOARD_HEIGHT } from './src/config.js';


applyRootStyles(rootStyles);
const gameBoard = new GameBoard(document.querySelector('#game-board'));
const $sidePanel = document.querySelector('#side-panel');
const $gameMessage = document.querySelector('#game-message');

let gameRunning = false;
let gamePaused = false;
let gameInterval;

document.addEventListener('keydown', function (event) {
    if (keyCodes.up.includes(event.code)) {
        // gameBoard.enableTile(4, 5);
        sendAction(6, event.ctrlKey);

        console.log('up');
    }
    if (keyCodes.right.includes(event.code)) {
        // gameBoard.disableTile(4, 5);
        sendAction(5, true);

        console.log('right');
    }
    if (keyCodes.down.includes(event.code)) {
        sendAction(7, true);

        console.log('down');
    }
    if (keyCodes.left.includes(event.code)) {
        sendAction(4, true);

        console.log('left');
    }
});

document.addEventListener('keydown', (event) => {
    if (event.code === 'Escape') {
        if (!gamePaused) {
            // sendAction(2, false)
            clearInterval(gameInterval);
            gamePaused = true
            console.log('Game paused');
        } else {
            gamePaused = false
            startGame()
        }
    }
});

function startGame() {
    // setInterval(fetchGameState, 100);
    gameInterval = setInterval(() => {
        if (!gamePaused) {
            console.log('Game is running...');
            fetchGameState()
            // Основная игровая логика здесь
        }
    }, 16); // Интервал 100 мс
}

// Логика для окончания игры
function stopGame() {
    clearInterval(gameInterval);
    gameRunning = false;
    showMessage('game-over'); // Показать GAME OVER
    console.log('Game has stopped.');
}

// Логика для старта игры
document.querySelector('#start').addEventListener('click', () => {
    if (!gameRunning) {
        hideMessage(); // Убираем сообщение
        gameRunning = true;
        gamePaused = false;
        sendAction(1, true);
        startGame();
    }
});

// Логика для паузы
document.querySelector('#pause').addEventListener('click', () => {
    if (gameRunning) {
        gamePaused = !gamePaused;
        if (gamePaused) {
            showMessage('paused');
        } else {
            hideMessage(); // Убираем сообщение
        }
        sendAction(2, true);
    }
});

document.querySelector('#stop').addEventListener('click', () => {
    if (gameRunning) {
        gameRunning = false;
        sendAction(3, true);
        stopGame();
    }
});


const sendAction = async (actionId, hold) => {
    try {
        const response = await fetch('/api/actions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                "Accept": "application/json"
            },
            body: JSON.stringify({
                actionId: actionId,
                hold: hold
            })
        });

        if (!response.ok) {
            const error = await response.json();
            console.error('Error:', error.message);
        } else {
            const result = await response.text();
            console.log('Server response:', result);
        }
    } catch (error) {
        console.error('Fetch error:', error);
    }
};

const actions = {
    start: 1,
    pause: 2,
    terminate: 3,
    left: 4,
    right: 5,
    up: 6,
    down: 7,
    action: 8
};

const fetchGameState = async () => {
    console.log("Trying get game state");
    try {
        const response = await fetch('/api/state', { method: 'GET' });
        if (!response.ok) {
            console.error('Failed to fetch game state');
            return;
        }
        const gameState = await response.json();
        updateGameBoard(gameState);
    } catch (error) {
        console.error('Error fetching game state:', error);
    }
};

const updateGameBoard = (gameState) => {
    if (gameState.speed == 0) {
        stopGame()
    } else {
        console.log("Trying update playing feild");

        gameBoard.tiles.forEach(tile => tile.classList.remove('active'));
        console.log(gameState)
        console.log(gameState.field[18][5])


        const score = document.getElementById("score-value")
        score.innerHTML = gameState.score
        const level = document.getElementById("level-value")
        level.innerHTML = gameState.level
        const speed = document.getElementById("speed-value")
        speed.innerHTML = gameState.speed
        const high = document.getElementById("high-value")
        high.innerHTML = gameState.highScore

        for (let i = 0; i < GAME_BOARD_HEIGHT; ++i) {
            for (let j = 0; j < GAME_BOARD_WIDTH; ++j) {

                if (gameState.field[i][j]) {
                    gameBoard.enableTile(i, j);
                } else {
                    console.log("Tile is empty");
                }
            }
        }
    }
};


// Функция для отображения сообщения
function showMessage(state) {
    $gameMessage.classList.remove('hidden', 'ready', 'paused', 'game-over');
    switch (state) {
        case 'ready':
            $gameMessage.textContent = 'READY';
            $gameMessage.classList.add('ready');
            break;
        case 'paused':
            $gameMessage.textContent = 'PAUSED';
            $gameMessage.classList.add('paused');
            break;
        case 'game-over':
            $gameMessage.textContent = 'GAME OVER';
            $gameMessage.classList.add('game-over');
            break;
    }
}

// Функция для скрытия сообщения
function hideMessage() {
    $gameMessage.classList.add('hidden');
}

// Отображение READY при загрузке
showMessage('ready');

