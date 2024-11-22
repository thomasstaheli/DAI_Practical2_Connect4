package ch.heigvd.dai;



public class GameBoard {

  private final int height;
  private final int width;
  private final int winLength;

  public enum Slot {
    RED, BLUE, EMPTY
  }

  public enum GameStatus {
    RED_WINS, BLUE_WINS, DRAW, GAME_CONTINUE
  }

  private Slot playerTurn;
  private final Slot[][] board;
  private final int maxTurns;
  private final int countTurnPlayed;

  GameBoard(int height, int width, int winLength){
    this.height = height;
    this.width = width;
    this.board = new Slot[height][width];

    this.winLength = winLength;
    this.playerTurn = Slot.RED;
    this.countTurnPlayed = 0;
    this.maxTurns = this.height * this.width;

    this.fillGameBoard(Slot.EMPTY);
  }

  void fillGameBoard(Slot slot) {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        board[i][j] = slot;
      }
    }
  }

  int addSlot(int column) {
    //returns a true if there was an empty slot available
    for (int i = height - 1; i >= 0; i--) {
      if (board[i][column] == Slot.EMPTY){
        board[i][column] = this.playerTurn;
        return i;
      }
    }
    return -1;
  }

  boolean checkDirection(int column, int row, Direction direction){
    // alignedSlots start with 1 because we have to count the actual dot
    int alignedSlots = 1;

    for (int i = 1; i < winLength; i++) {

      int newRow = row + (i * direction.getYIncrement());
      int newColumn = column + (i * direction.getXIncrement());

      //checking for board bounds
      if (newRow < 0 || newRow >= height || newColumn < 0 || newColumn >= width ||
        board[newRow][newColumn] != this.playerTurn) { //checking for the right color
        return false;
      }
      else {
        ++alignedSlots;
      }
    }
    // >= because at we can have a line of 5 connected but only 4 tokens are needed
    return alignedSlots >= winLength;
  }

  boolean checkAllDirections(int column, int row) {

    for (Direction direction : Direction.values()) {
      if(checkDirection(column, row, direction)) {
        return true;
      }
    }
    return false;

  }

  public void invertPlayerTurn() {
    this.playerTurn = this.playerTurn == Slot.RED ? Slot.BLUE : Slot.RED;
  }

  public GameStatus checkWinCondition(int chosenColumn, int chosenRow) {

    if(this.checkAllDirections(chosenColumn, chosenRow)) {
      return playerTurn == Slot.RED ? GameStatus.RED_WINS : GameStatus.BLUE_WINS;
    }

    return GameStatus.GAME_CONTINUE;
  }

  public boolean checkDrawCondition() {
    return this.countTurnPlayed >= this.maxTurns;
  }

  public Slot getBoardSlot(int row, int column) {
    return board[row][column];
  }

  public int getHeight() {
    return this.height;
  }

  public int getWidth() {
    return this.width;
  }

}


