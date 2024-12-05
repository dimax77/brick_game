// gameBoard.js
import { applyRootStyles } from './src/utils.js';
import { GameBoard } from './src/game-board.js';
import { rootStyles, keyCodes } from './src/config.js';

applyRootStyles(rootStyles);
const gameBoard = new GameBoard(document.querySelector('#game-board'));

const $sidePanel = document.querySelector('#side-panel');

document.addEventListener('keydown', function (event) {
    if (keyCodes.up.includes(event.code)) {
        // gameBoard.enableTile(4, 5);
        sendAction(1, true);

        console.log('up');
    }
    if (keyCodes.right.includes(event.code)) {
        // gameBoard.disableTile(4, 5);
        sendAction(1, true);

        console.log('right');
    }
    if (keyCodes.down.includes(event.code)) {
        sendAction(1, true);

        console.log('down');
    }
    if (keyCodes.left.includes(event.code)) {
        sendAction(1, true);

        console.log('left');
    }
});


document.addEventListener('keydown', (event) => {
    if (event.code === 'Escape') {
        console.log('Game paused');
    }
});

let gameRunning = false;
let gamePaused = false;
let gameInterval;

function startGame() {
    setInterval(fetchGameState, 100);
    // gameInterval = setInterval(() => {
    //     if (!gamePaused) {
    //         console.log('Game is running...');
    //         // Основная игровая логика здесь
    //     }
    // }, 100); // Интервал 100 мс
}

// function stopGame() {
//     clearInterval(gameInterval);
//     console.log('Game has stopped.');
//     // Сброс состояния игрового поля
// }

document.querySelector('#start').addEventListener('click', () => {
    if (!gameRunning) {
        gameRunning = true;
        gamePaused = false;
        sendAction(1, true);
    }
});

document.querySelector('#pause').addEventListener('click', () => {
    if (gameRunning) {
        gamePaused = !gamePaused;
        console.log(gamePaused ? 'Game paused.' : 'Game resumed.');
        sendAction(1, true);


    }
});

document.querySelector('#stop').addEventListener('click', () => {
    if (gameRunning) {
        gameRunning = false;
        sendAction(1, true);

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
    up: 4,
    down: 5,
    left: 6,
    right: 7,
    action: 8
};

const fetchGameState = async () => {
    try {
        const response = await fetch('/api/game-state');
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
    // Сбрасываем игровое поле
    gameBoard.tiles.forEach(tile => tile.classList.remove('active', 'obstacle', 'car'));

    // Устанавливаем активные элементы
    gameState.cars.forEach(car => {
        const tile = gameBoard.getTile(car.x, car.y);
        tile.classList.add('car');
    });

    gameState.obstacles.forEach(obstacle => {
        const tile = gameBoard.getTile(obstacle.x, obstacle.y);
        tile.classList.add('obstacle');
    });
};


