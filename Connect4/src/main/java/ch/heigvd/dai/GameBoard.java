package ch.heigvd.dai;

import ch.heigvd.dai.Direction;
import ch.heigvd.dai.userIO;

public class GameBoard {
    int height;
    int width;
    int winLength = 4; //TODO add it as input
    enum Slot{
        RED, YELLOW, EMPTY
    }
    enum GameEnding{
        RED_WINS, YELLOW_WINS, DRAW
    }
    Slot beginningTurn = Slot.RED;
    Slot[][] board;

    GameBoard(int height, int width){
        this.height = height;
        this.width = width;
        this.board = new Slot[height][width];
        this.fillGameBoard(Slot.EMPTY);
    }

    void showGameBoard(){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println("\n");
        }
    }

    void fillGameBoard(Slot slot){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board[i][j] = slot;
            }
        }
    }

    int addSlot(Slot slot,int column){ //returns a true if there was an empty slot available
        for (int i = height-1; i >= 0; i--) {
                if (board[i][column] == Slot.EMPTY){
                    board[i][column] = slot;
                    return i;
            }
        }
        return -1;
    }

    boolean Aligned(int column, int row, Slot turn){
        return board[row][column] == turn;
    }

    boolean checkDirection(int column, int row, Direction direction, Slot turn){
        for (int i = column; i <winLength; i++) {

            int newRow = row + (i * direction.getYIncrement());
            int newColumn = column + (i * direction.getXIncrement());

            if (newRow < 0 || newRow >= height || newColumn < 0 || newColumn >= width || //checking for board bounds
                    board[newRow][newColumn] != turn) //checking for the right color
            {
                return false;
            }
        }
        return true;
    }

    boolean checkAllDirections(int column, int row, Slot turn){
        for (Direction direction :Direction.values()){
            if(checkDirection(column, row, direction, turn)){
                return true;
            }
        }
        return false;
    }

    GameEnding GameLoop(){
        Slot currentTurn = beginningTurn;
        int turnsPlayed = 0;
        int maxTurns = height * width;
        do{
            if(turnsPlayed >= maxTurns) //game board is full
            {
                return GameEnding.DRAW;
            }
            int chosenColumn=userIO.getIntInput(0,width-1);

            int chosenRow=addSlot(currentTurn,chosenColumn);
            while (chosenRow==-1)//get another column if full
            {
                System.out.println("Cette colomne est complète, veuillez en choisir une autre.");
                chosenColumn=userIO.getIntInput(0,width-1);
                chosenRow=addSlot(currentTurn,chosenColumn);
            }


            if(checkAllDirections(chosenColumn,chosenRow,currentTurn)){

                if(currentTurn == Slot.RED){
                    return GameEnding.RED_WINS;
                }

                if(currentTurn == Slot.YELLOW){
                    return GameEnding.YELLOW_WINS;
                }
            }
            // Changing the turn
            if(currentTurn == Slot.RED)
            {
                currentTurn = Slot.YELLOW;
            }
            else if(currentTurn == Slot.YELLOW)
            {
                currentTurn = Slot.RED;
            }
            showGameBoard();

        }
        while (true);
    }
}
