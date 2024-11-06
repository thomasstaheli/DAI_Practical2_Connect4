package ch.heigvd.dai;

public class GameBoard {
    int height;
    int width;
    int winLength = 4; //TODO add it as input
    enum Slot{
        RED, YELLOW, EMPTY
    }
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

    void addSlot(Slot slot,int column){
        for (int i = height-1; i >= 0; i--) {
                if (board[i][column] == Slot.EMPTY){
                    board[i][column] = slot;
                    return;
            }
        }
    }

    boolean isFull(){
        //TODO implémenter ça
        return (height == width);
    }
    boolean Aligned(int column, int row, Slot turn){
        return board[row][column] == turn;
    }

    boolean checkDirection(int column, int row, int directionX, int directionY){
        for (int i = column; i <width; i+=directionX) {

        }
    }
}
