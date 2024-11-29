package ch.heigvd.dai;

import ch.heigvd.dai.GameBoard;
import ch.heigvd.dai.commands.Root;
import picocli.CommandLine;

public class Main {

  public static void main(String[] args) {

    final int heightBoard = 5;
    final int widthBoard  = 5;
    final int winLength   = 4;

    /*
    GameBoard gameBoard = new GameBoard(heightBoard, widthBoard, winLength);
    Display display     = new Display(gameBoard);
    // TODO: Improve
    // We can return a void and treat the victory in the class, so we have nothing to return
    GameBoard.GameStatus gameStatus = display.GameLoop();
    */

    Root root = new Root();

    // Calculate execution time for root command and its subcommands
    Long start = System.nanoTime();
    int exitCode =
            new CommandLine(root)
                    .setCaseInsensitiveEnumValuesAllowed(true)
                    .execute(args);
    Long end = System.nanoTime();

    if (exitCode == 0) {
      System.out.println("Execution time in ms: " + (end - start) / (1000 * 1000));
    }

    System.exit(exitCode);
  }

}