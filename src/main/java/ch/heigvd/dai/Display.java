package ch.heigvd.dai;

public class Display {

  GameBoard game;
  private final int displayWidth;
  private final int displayHeight;

  Display(GameBoard game) {
    this.game = game;
    this.displayWidth  = game.getWidth();
    this.displayHeight = game.getHeight();
    this.showGameBoard();
  }

  void showGameBoard() {

    // TODO: REMOVE because not working !!!
    /*
    try {
      Process process = Runtime.getRuntime().exec("clear");
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (Exception e) {
      System.out.println(e);
    }
    */

    System.out.print("+----".repeat(displayWidth));
    System.out.println("+");

    for (int i = 0; i < displayHeight; i++) {
      // Row content
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
        // Make sure each cell is 3 characters wide (including space padding)
        System.out.print(" " + String.format("%-3s", cellContent) + "|");
      }
      System.out.println();

      // Row separator
      System.out.print("+----".repeat(displayWidth));
      System.out.println("+");
    }
  }

  public GameBoard.GameStatus GameLoop() {

    int chosenColumn;
    int chosenRow;

    do{

      if(game.checkDrawCondition()) {
        return GameBoard.GameStatus.DRAW;
      }

      chosenColumn = userIO.getIntInput(0, displayWidth - 1);
      chosenRow    = game.addSlot(chosenColumn);

      while (chosenRow == -1) {
        //get another column if full
        System.out.println("Cette colomne est complète, veuillez en choisir une autre.");
        chosenColumn = userIO.getIntInput(0, displayWidth - 1);
        chosenRow    = game.addSlot(chosenColumn);
      }

      // TODO improve
      GameBoard.GameStatus gameStatus = game.checkWinCondition(chosenRow, chosenColumn);
      if(gameStatus == GameBoard.GameStatus.BLUE_WINS || gameStatus == GameBoard.GameStatus.RED_WINS) {
        return gameStatus;
      }
      // Changing the turn
      game.invertPlayerTurn();

      this.showGameBoard();

    } while (true);
  }

}
