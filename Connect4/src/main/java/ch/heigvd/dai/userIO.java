package ch.heigvd.dai;

import java.util.Scanner;

public class userIO {

   static public int getIntInput(int minValue, int maxValue)
   {
       int input=0; //must initialize to avoid error (var might not have been initialized)
       Scanner scanner = new Scanner(System.in); //input code source : w3s Java input
       boolean validInput = false;
       do{
           System.out.println("Veuillez choisir un numéro de colonne entre "+minValue+" et "+maxValue);
           if(scanner.hasNextInt()) {
               input = scanner.nextInt();
               validInput = input >= minValue && input <= maxValue;
           }


       }
       while(!validInput); //had to create a var because I can't put the hasNextInt here,
       // it cannot scan for the first time
       return input;
   }

}
