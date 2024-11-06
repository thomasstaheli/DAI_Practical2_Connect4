package ch.heigvd.dai;
import ch.heigvd.dai.GameBoard;
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        GameBoard gameBoard= new GameBoard(5,5);
        gameBoard.showGameBoard();
        gameBoard.addSlot(GameBoard.Slot.RED,0);
        gameBoard.addSlot(GameBoard.Slot.RED,0);
        gameBoard.showGameBoard();
    }
}