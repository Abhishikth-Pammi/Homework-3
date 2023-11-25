from collections import defaultdict

import requests
import random

# Base URL of the game server
base_url = "http://localhost:9090"

def get_thief_positions():
    """Get the positions of thief characters."""
    response = requests.get(f"{base_url}/move/thief")
    return response.json()

def move_thief(position):
    """Move a thief character to a specified position."""
    response = requests.post(f"{base_url}/move/thief/{position}")
    return response.json()

def restart_game():
    """Restart the game."""
    response = requests.get(f"{base_url}/restartgame")
    return response.text

def game_end_reason():
    """Game end reason."""
    response = requests.get(f"{base_url}/gameendreason")
    return response.text

def get_police_positions():
    """Get the positions of police characters."""
    response = requests.get(f"{base_url}/move/police")
    return response.json()

def move_police(position):
    """Move a police character to a specified position."""
    response = requests.post(f"{base_url}/move/police/{position}")
    return response.json()

def is_game_over(response):
    """Check if the game is over (empty list response)."""
    return isinstance(response, list) and len(response) == 0

def choose_position(strategy, positions, visited):
    """Choose a position based on the specified strategy, avoiding visited nodes."""
    unvisited_positions = [pos for pos in positions if pos not in visited]

    if unvisited_positions:
        if strategy == "first":
            return unvisited_positions[0]
        elif strategy == "random":
            return random.choice(unvisited_positions)
        elif strategy == "last":
            return unvisited_positions[-1]
    return None

def handle_game_round(strategy, thief_path, police_path, visited_thief, visited_police):
    """Handles a round of the game using the specified strategy."""
    # Get positions
    thief_response = get_thief_positions()
    police_response = get_police_positions()

    # Check if the game is over
    if is_game_over(thief_response) or is_game_over(police_response):
        print(game_end_reason())
        return True

    # Move characters based on the strategy and visited positions
    thief_position = choose_position(strategy, thief_response, visited_thief)
    police_position = choose_position(strategy, police_response, visited_police)

    if thief_position is not None:
        move_thief(thief_position)
        thief_path.append(thief_position)
        visited_thief.add(thief_position)

    if police_position is not None:
        move_police(police_position)
        police_path.append(police_position)
        visited_police.add(police_position)

    return False

# Main game loop
def play_game():
    print("Choose a strategy: 'first', 'random', or 'last'")
    strategy = input("Enter strategy: ").strip().lower()
    while strategy not in ['first', 'random', 'last']:
        print("Invalid strategy. Please choose 'first', 'random', or 'last'.")
        strategy = input("Enter strategy: ").strip().lower()

    # Paths and visited positions for thief and police
    thief_path = []
    police_path = []
    visited_thief = set()
    visited_police = set()

    # Counter for maximum number of games
    max_games = 5
    game_count = 0

    while game_count < max_games:
        # Restart game at the beginning or if someone wins
        print(restart_game())

        # Reinitialize paths and visited positions for the new game
        thief_path.clear()
        police_path.clear()
        visited_thief.clear()
        visited_police.clear()

        # Continue playing rounds until the game ends
        while not handle_game_round(strategy, thief_path, police_path, visited_thief, visited_police):
            pass

        # Print paths taken before restarting the game
        print("Thief Path:", thief_path)
        print("Police Path:", police_path)

        # Increment game counter
        game_count += 1

def run_simulations():
    strategies = ['first', 'random', 'last']
    num_simulations = 30
    paths_to_print = 5

    for strategy in strategies:
        print(f"\nRunning simulations for strategy: {strategy}")

        game_end_reasons = defaultdict(int)
        all_game_end_reasons = []  # List to store all game end reasons

        for sim in range(num_simulations):
            thief_path, police_path, visited_thief, visited_police = [], [], set(), set()
            print(restart_game())

            while not handle_game_round(strategy, thief_path, police_path, visited_thief, visited_police):
                pass

            if sim < paths_to_print:
                print(f"Simulation {sim + 1} - Thief Path: {thief_path}")
                print(f"Simulation {sim + 1} - Police Path: {police_path}")

            # Retrieve and store game end reason
            current_game_end_reason = game_end_reason()
            all_game_end_reasons.append(current_game_end_reason)

            # Increment game end reason counter
            game_end_reasons[current_game_end_reason] += 1

        # Print all game end reasons for this strategy
        print(f"All Game End Reasons for '{strategy}': {all_game_end_reasons}")
        print(f"Summary of Game End Reasons for '{strategy}': {dict(game_end_reasons)}")

if __name__ == "__main__":
    run_simulations()

if __name__ == "__main__":
    run_simulations()