package ch.heigvd.dai;

import ch.heigvd.dai.GameBoard;

public class Main {
  public static void main(String[] args) {

    final int heightBoard = 5;
    final int widthBoard  = 5;
    final int winLength   = 4;

    GameBoard gameBoard = new GameBoard(heightBoard,widthBoard, winLength);
    Display display     = new Display(gameBoard);
    // TODO: Improve
    // We can return a void and treat the victory in the class, so we have nothing to return
    GameBoard.GameStatus gameStatus = display.GameLoop();
  }
}