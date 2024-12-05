package ch.heigvd.dai;

import ch.heigvd.dai.util.UserIO;

/**
 * The {@code Display} class is responsible for displaying the game board and managing
 * the flow of the game by interacting with the user for input and showing the updated game state.
 */
public class Display {

  private GameBoard game;
  private final int displayWidth;
  private final int displayHeight;

  /**
   * Constructs a {@code Display} object, initializing the game board and setting
   * the width and height of the display based on the game board dimensions.
   *
   * @param game The {@code GameBoard} object that contains the current game state.
   */
  public Display(GameBoard game) {
    this.game = game;
    this.displayWidth  = game.getWidth();
    this.displayHeight = game.getHeight();
    this.showGameBoard();
  }

  /**
   * Displays the current state of the game board in the console.
   * It prints the game board, including the players' pieces (Red and Blue) and
   * the empty slots, formatted with borders.
   */
  public void showGameBoard() {

    System.out.print("+----".repeat(displayWidth));
    System.out.println("+");

    for (int i = 0; i < displayHeight; i++) {
      // Print row contents
      System.out.print("|");
      for (int j = 0; j < displayWidth; j++) {
        String cellContent = "";
        switch (game.getBoardSlot(i, j)) {
          case RED:
            // RED DOT 🔴
            cellContent = "\uD83D\uDD34";
            break;
          case BLUE:
            // BLUE DOT 🔵
            cellContent = "\uD83D\uDD35";
            break;
          case EMPTY:
            cellContent = " ";
            break;
          default:
            break;
        }
        // Ensure each cell is 3 characters wide (including space padding)
        System.out.print(" " + String.format("%-3s", cellContent) + "|");
      }
      System.out.println();

      // Print row separator
      System.out.print("+----".repeat(displayWidth));
      System.out.println("+");
    }
  }

  /** WARNING : This method is not used in this Practical work !
   * Manages the game loop, asking the user to make a move and updating the game state
   * after each move. It checks for draw or win conditions after each move.
   * The loop continues until there is a winner or a draw.
   *
   * @return The current game status, which can be either {@link GameBoard.GameStatus#DRAW},
   *         {@link GameBoard.GameStatus#BLUE_WINS}, or {@link GameBoard.GameStatus#RED_WINS}.
   */
  public GameBoard.GameStatus GameLoop() {

    int chosenColumn;
    int chosenRow;

    do {
      // Check if the game is a draw
      if (game.checkDrawCondition()) {
        return GameBoard.GameStatus.DRAW;
      }

      // Ask the user for a valid column input
      chosenColumn = UserIO.getIntInput(0, displayWidth - 1);
      chosenRow    = game.addSlot(chosenColumn);

      // Ensure the column is not full
      while (chosenRow == -1) {
        System.out.println("This column is full, please choose another one.");
        chosenColumn = UserIO.getIntInput(0, displayWidth - 1);
        chosenRow    = game.addSlot(chosenColumn);
      }

      // Check for a win condition after the move
      GameBoard.GameStatus gameStatus = game.checkWinCondition(chosenColumn, chosenRow);
      if (gameStatus != GameBoard.GameStatus.GAME_CONTINUE) {
        // Display the final game state and the winner
        this.showGameBoard();
        System.out.print(gameStatus == GameBoard.GameStatus.BLUE_WINS ? "Blue " : "Red ");
        System.out.println("player WIN!");
        return gameStatus;
      }

      // Change the turn after each move
      game.invertPlayerTurn();

      // Display the updated board after the turn
      this.showGameBoard();

    } while (true);
  }
}
