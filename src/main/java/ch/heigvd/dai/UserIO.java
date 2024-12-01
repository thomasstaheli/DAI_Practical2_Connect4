package ch.heigvd.dai;

import java.util.Scanner;

public class UserIO {

  private int choosenColum;
  private int choosenRow;
  private static final int DEFAULT_VALUE = -1;

  public UserIO() {
    this.choosenColum = DEFAULT_VALUE;
    this.choosenRow   = DEFAULT_VALUE;
  }

  static public int getIntInput(int minValue, int maxValue) {
    // must initialize to avoid error (var might not have been initialized)
    int input = 0;
    //input code source : w3s Java input
    Scanner scanner = new Scanner(System.in);
    boolean validInput = false;

    do {
      System.out.println("Veuillez choisir un numéro de colonne entre "+minValue+" et "+maxValue);

      if(scanner.hasNextInt()) {
        input = scanner.nextInt();
        validInput = input >= minValue && input <= maxValue;
      }
      // had to create a var because I can't put the hasNextInt here,
      // it cannot scan for the first time
    } while(!validInput);

    return input;
  }

  public void setChoosenColum(int value) {
    this.choosenColum = value;
  }

  public void setChoosenRow(int value) {
    this.choosenRow = value;
  }

  public int getChoosenColum() {
    return this.choosenColum;
  }

  public int getChoosenRow() {
    return this.choosenRow;
  }

}
