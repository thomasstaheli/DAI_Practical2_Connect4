package ch.heigvd.dai.util;

import java.util.Scanner;

/**
 * The {@code UserIO} class provides methods for handling user input and validating it.
 * It facilitates retrieving user input from the console, checking if the input is valid,
 * and storing the user's chosen column and row for a game.
 */
public class UserIO {

  private int choosenColum;
  private int choosenRow;
  private static final int DEFAULT_VALUE = -1;

  /**
   * Constructs a {@code UserIO} object with default values for the chosen column and row.
   */
  public UserIO() {
    this.choosenColum = DEFAULT_VALUE;
    this.choosenRow   = DEFAULT_VALUE;
  }

  /**
   * Validates whether the given input is within the specified range.
   *
   * @param input The value to check.
   * @param min The minimum allowable value.
   * @param max The maximum allowable value.
   * @return {@code true} if the input is within the range, otherwise {@code false}.
   */
  static public boolean isInputValid(int input, int min, int max) {
    return input >= min && input <= max;
  }

  /**
   * Reads a line of text input from the user.
   *
   * @return The user input as a {@code String}.
   */
  static public String getUserInput() {
    Scanner sc = new Scanner(System.in);
    return sc.nextLine();
  }

  /**
   * Reads an integer input from the user, ensuring it is within the specified range.
   * If the input is invalid, it prompts the user until a valid input is given.
   *
   * @param minValue The minimum valid value.
   * @param maxValue The maximum valid value.
   * @return The valid integer input from the user.
   */
  static public int getIntInput(int minValue, int maxValue) {
    // must initialize to avoid error (var might not have been initialized)
    int input = 0;
    Scanner scanner = new Scanner(System.in);
    boolean validInput = false;

    do {
      System.out.println("Please choose a column number between " + minValue + " and " + maxValue);

      if(scanner.hasNextInt()) {
        input = scanner.nextInt();
        validInput = input >= minValue && input <= maxValue;
      }
    } while(!validInput);

    return input;
  }

  /**
   * Sets the column chosen by the user.
   *
   * @param value The column number chosen by the user.
   */
  public void setChosenColum(int value) {
    this.choosenColum = value;
  }

  /**
   * Sets the row chosen by the user.
   *
   * @param value The row number chosen by the user.
   */
  public void setChosenRow(int value) {
    this.choosenRow = value;
  }

  /**
   * Gets the column chosen by the user.
   *
   * @return The column number chosen by the user.
   */
  public int getChosenColum() {
    return this.choosenColum;
  }

  /**
   * Gets the row chosen by the user.
   *
   * @return The row number chosen by the user.
   */
  public int getChosenRow() {
    return this.choosenRow;
  }

}
