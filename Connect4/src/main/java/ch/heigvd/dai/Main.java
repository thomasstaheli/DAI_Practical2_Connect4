package ch.heigvd.dai;
import ch.heigvd.dai.GameBoard;
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world ! \uD83D\uDCA6");
        GameBoard gameBoard= new GameBoard(5,5);
        gameBoard.showGameBoard();
        GameBoard.GameEnding ending = gameBoard.GameLoop();
    }
}