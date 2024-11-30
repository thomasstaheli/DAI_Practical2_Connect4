package ch.heigvd.dai;

import ch.heigvd.dai.GameBoard;
import ch.heigvd.dai.commands.Root;
import picocli.CommandLine;

public class Main {

  public static void main(String[] args) {

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