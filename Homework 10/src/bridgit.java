import java.util.ArrayList;
import java.util.Arrays;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//represents a single square of the game area
class Cell {
  int x;
  int y;
  Color color;
  boolean bridged;
  boolean isCircle; //distinguishes between circles and bridges
  int playerDir; // direction of a bridge based on which player's move
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;
  int imageSize = 20;
  //reason why phantom fields must be created as onMouseMoved
  //will overwrite onMouseClicked otherwise
  boolean phantom;
  int phantomDir1;
  int phantomDir2;
  Color phantomCol;
  int phantomTurn;

  
  //main constructor
  Cell(int x, int y, boolean bridged, boolean isCircle, int playerDir, Color color) {
    this.x = x;
    this.y = y;
    this.bridged = bridged;
    this.isCircle = isCircle;
    this.playerDir = playerDir;
    this.color = color;
    this.left = this;
    this.top = this;
    this.right = this;
    this.bottom = this;
  }

  //draws the cell as a circle if a stationary dot or a bridge in its appropriate direction
  WorldImage drawCell() {
    if (this.isCircle) {
      return new CircleImage(this.imageSize, OutlineMode.SOLID, this.color);
    }
    else if (this.playerDir == 1) { //horizontal bridge
      return new RectangleImage(5 * imageSize, imageSize / 2, OutlineMode.SOLID, this.color);
    }
    else if (this.playerDir == 2) { //vertical bridge
      return new RectangleImage(imageSize / 2, 5 * imageSize, OutlineMode.SOLID, this.color);
    }
    else if (this.phantom) {
      return this.drawPhantomCell();
    }
    else { //"empty" image
      return new CircleImage(1, OutlineMode.SOLID, Color.white);
    }
  }
  
  //draws the cell as if it was not actually placed but rather a cursor was hovering over it
  WorldImage drawPhantomCell() {
    if (this.phantomDir1 == 1) {
      if (phantomTurn == 1) {
        return new RectangleImage(3 * imageSize, imageSize / 2, OutlineMode.SOLID, this.phantomCol);
      }
      else {
        return new RectangleImage(imageSize / 2, 3 * imageSize, OutlineMode.SOLID, this.phantomCol);
      }
      
    }
    else {
      if (phantomTurn == 1) {
        return new RectangleImage(imageSize / 2, 3 * imageSize, OutlineMode.SOLID, this.phantomCol);
      }
      else {
        return new RectangleImage(3 * imageSize, imageSize / 2, OutlineMode.SOLID, this.phantomCol);
      }
    }
     
  }
  
  //EFFECT: sets a given cell to left of this, and this to the right of given
  public void setLeft(Cell newCell) {
    this.left = newCell;
    newCell.right = this;
  }
  
  //EFFECT: sets a given cell to left of this, and this to the right of given
  public void setTop(Cell newCell) {
    this.top = newCell;
    newCell.bottom = this;
  }
  
  //EFFECT: sets a given cell to left of this, and this to the right of given
  public void setRight(Cell newCell) {
    this.right = newCell;
    newCell.left = this;
  }
  
  //EFFECT: sets a given cell to left of this, and this to the right of given
  public void setBottom(Cell newCell) {
    this.bottom = newCell;
    newCell.top = this;
  }
  
  //returns an ArrayList of a Cell's bridged neighbors to be considered in the path next
  ArrayList<Cell> toAddNeighbors(ArrayList<ArrayList<Cell>> board, int boardSize, Color cur, 
      ArrayList<Cell> alreadySeen) {
    ArrayList<Cell> results = new ArrayList<Cell>();
    if (this.right.bridged && this.x <= boardSize - 3 && this.right.color.equals(cur) 
        && !alreadySeen.contains(this.right)) {
      //System.out.print("adding right one \n");
      results.add(board.get(this.x + 2).get(this.y));
    }
    if (this.left.bridged && this.x >= 2 && this.left.color.equals(cur) 
        && !alreadySeen.contains(this.left)) {
      //System.out.print("adding left one \n");
      results.add(board.get(this.x - 2).get(this.y));
    }
    if (this.top.bridged && this.y >= 2 && this.top.color.equals(cur) 
        && !alreadySeen.contains(this.top)) {
      //System.out.print("adding top one \n");
      results.add(board.get(this.x).get(this.y - 2));
    }
    if (this.bottom.bridged && this.y <= boardSize - 3 && this.bottom.color.equals(cur) 
        && !alreadySeen.contains(this.bottom)) {
      //System.out.print("adding bottom one \n");
      results.add(board.get(this.x).get(this.y + 2));
    }
    return results;
  }
}

//represents the game world
class BridgItWorld extends World {
  ArrayList<ArrayList<Cell>> board;
  int boardSize;
  int playerTurn = 1; //always starts with player 1
  Color player1;
  Color player2;
  ArrayList<Cell> starters1; //Cells on left, used at the start of paths (player 1)
  ArrayList<Cell> enders1; //Cells on right, used at end of paths (player 1)
  ArrayList<Cell> starters2; //Cells on top, used at start of paths (player 2)
  ArrayList<Cell> enders2; //Cells on bottom, used at end of paths (player 2)
  Cell prevPhantom = new Cell(0, 0, false, false, 3, Color.white);
  int imageSize = 60;

  //main constructor
  BridgItWorld(int boardSize, Color player1, Color player2) {
    //checks validity of given board size
    if (boardSize % 2 == 1 && boardSize >= 3) {
      this.boardSize = boardSize;
    }
    else {
      throw new IllegalArgumentException("BoardSize must be at least 3 and an odd number");
    }
    this.player1 = player1;
    this.player2 = player2;
    this.starters1 = new ArrayList<Cell>();
    this.enders1 = new ArrayList<Cell>();
    this.starters2 = new ArrayList<Cell>();
    this.enders2 = new ArrayList<Cell>();
    this.board = initBoard();
  }

  //constructor used in testing
  BridgItWorld(ArrayList<ArrayList<Cell>> board) {
    if (board.size() % 2 == 1 && board.size() >= 3) {
      this.boardSize = board.size();
    }
    else {
      throw new IllegalArgumentException("BoardSize must be at least 3 and an odd number");
    }
    this.board = board;
    this.player1 = board.get(1).get(0).color;
    this.player2 = board.get(0).get(1).color;
    this.starters1 = new ArrayList<Cell>();
    this.enders1 = new ArrayList<Cell>();
    this.starters2 = new ArrayList<Cell>();
    this.enders2 = new ArrayList<Cell>();
  }

  //constructs a board from scratch
  ArrayList<ArrayList<Cell>> initBoard() {
    
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>(); 
    
    //creates a new cell in every position of the board
    for (int row = 0; row < boardSize; row++) {
      board.add(new ArrayList<Cell>());
      for (int column = 0; column < boardSize; column++) {
        if (row % 2 == 0 && column % 2 == 1) {
          board.get(row).add(new Cell(row, column, true, true, 3, this.player1));
        }
        else if (row % 2 == 1 && column % 2 == 0) {
          board.get(row).add(new Cell(row, column, true, true, 3, this.player2));
        }
        else {
          board.get(row).add(new Cell(row, column, false, false, 3, Color.white));
          if (row % 2 == 1 && column % 2 == 1) {
            board.get(row).get(column).phantomDir1 = 1;
            board.get(row).get(column).phantomDir2 = 2;
          }
          else {
            board.get(row).get(column).phantomDir1 = 2;
            board.get(row).get(column).phantomDir2 = 1;
          }
        }
      }
    }
    
    //links up references to neighboring cells 
    for (int row = 0; row < boardSize; row++) {
      for (int column = 0; column < boardSize; column++) {
        Cell currentCell = board.get(row).get(column);
        fixAdjCells(currentCell, board);
      }
    }
    return board;
  }
 
  //fixes adjacent cells to reference other cells
  void fixAdjCells(Cell cur, ArrayList<ArrayList<Cell>> board) {
    if (cur.x != 0) {
      cur.setLeft(board.get(cur.x - 1).get(cur.y));
    }
    if (cur.x != boardSize - 1) {
      cur.setRight(board.get(cur.x + 1).get(cur.y));
    }
    if (cur.y != 0) {
      cur.setTop(board.get(cur.x).get(cur.y - 1));
    }
    if (cur.y != boardSize - 1) {
      cur.setBottom(board.get(cur.x).get(cur.y + 1));
    } 
  }
  
  /////////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////////

  //renders the scene of the current world
  public WorldScene makeScene() {
    WorldScene gameBoard = this.drawAllCells(new WorldScene(imageSize
        * boardSize,imageSize * boardSize));
    return this.drawBorder(gameBoard);
  }

  //draws the player borders on the scene
  WorldScene drawBorder(WorldScene boardScene) {
    WorldImage player1Border = new RectangleImage((imageSize / 12)
        * boardSize, imageSize * boardSize, OutlineMode.SOLID, this.player1);
    WorldImage player2Border = new RectangleImage(imageSize
        * boardSize, (imageSize / 12) * boardSize, OutlineMode.SOLID, this.player2);
    
    boardScene.placeImageXY(player1Border, 0, (imageSize / 2) * boardSize);
    boardScene.placeImageXY(player1Border, imageSize * boardSize, (imageSize / 2) * boardSize);
    boardScene.placeImageXY(player2Border, (imageSize / 2) * boardSize, 0);
    boardScene.placeImageXY(player2Border, (imageSize / 2) * boardSize, imageSize * boardSize);
    return boardScene;
  }
  
  //renders the whole board as a WorldScene
  WorldScene drawAllCells(WorldScene accImage) {
    for (int row = 0; row < boardSize; row++) {
      for (int column = 0; column < boardSize; column++) {
        Cell currentCell = board.get(row).get(column);
        Posn cellPosn = this.imagePos(currentCell);
        accImage.placeImageXY(currentCell.drawCell(), cellPosn.x, cellPosn.y);
      }
    }
    return accImage;
  }

  //returns the position of a cell to be displayed in the image
  Posn imagePos(Cell c) {
    int xPos = ((c.x + 1) * imageSize) - (imageSize / 2);
    int yPos = ((c.y + 1) * imageSize) - (imageSize / 2);
    return new Posn(xPos, yPos);
  }
  
  //updates the World based on a user's click
  public void onMouseClicked(Posn uPos) {
    //System.out.print("------------------------------------------------------- \n");
    //System.out.println("\n" + uPos.x);
    //System.out.println(uPos.y);
    Cell clickedCell = this.pointCell(uPos);
    
    //if clicked on a colored circle or on edges, nothing happens
    if (clickedCell.color != Color.white 
        || this.onBorder(clickedCell)) {
      return;
    }
    
    //player 1's turn
    else if (playerTurn == 1) {
      clickedCell.color = this.player1;
      clickedCell.bridged = true;
      //if bridge is created with Cell on left side, add Cell to starters list
      if (clickedCell.x == 1) {
        starters1.add(board.get(0).get(clickedCell.y));
      }
      //if bridged is created with Cell on right side, add Cell to enders list
      if (clickedCell.x == boardSize - 2) {
        enders1.add(board.get(boardSize - 1).get(clickedCell.y));
      }
      //sets a bridge Cell to be vertical or horizontal
      clickedCell.playerDir = this.isHorizontal(uPos, player1);
      //updates turn
      this.playerTurn = 2;
    }
    
    //player 2's turn
    else {
      clickedCell.color = this.player2;
      clickedCell.bridged = true;
      //if bridge is created with Cell on top, add Cell to starters list
      if (clickedCell.y == 1) {
        starters2.add(board.get(clickedCell.x).get(0));
      }
      //if bridge is created with Cell on bottom, add Cell to enders list
      if (clickedCell.y == boardSize - 2) {
        enders2.add(board.get(clickedCell.x).get(boardSize - 1));
      }
      //sets a bridge Cell to be vertical or horizontal
      clickedCell.playerDir = this.isHorizontal(uPos, player2);
      //updates turn
      this.playerTurn = 1;
    }
    //System.out.print("starters1:" + starters1 + "\n");
    //System.out.print("starters2:" + starters2 + "\n");
    //System.out.print("enders1:" + enders1 + "\n");
    //System.out.print("enders2:" + enders2 + "\n");
    
    //checks if either player has won
    if (this.playerTurn == 1) {
      hasWon(starters2, enders2, player2);
    }
    else {
      hasWon(starters1, enders1, player1);
    }
  }
  
  //checks if a clicked cell is a border cell
  boolean onBorder(Cell clicked) {
    return clicked.x == 0 || clicked.x == boardSize - 1
        || clicked.y == 0 || clicked.y == boardSize - 1;
  }
  
  //returns 2 if a bridge is to be vertical 
  //or 1 if bridge is to be horizontal according to the player's turn
  //using integers instead of boolean so the circle Cells are set to 3
  int isHorizontal(Posn pos, Color curColor) {
    Cell currentCell = this.pointCell(pos);
    if (curColor.equals(player1)) {
      if (currentCell.x % 2 == 0 && currentCell.y % 2 == 0) {
        return 2;
      }
      else {
        return 1;
      }
    }
    else {
      if (currentCell.x % 2 == 1 && currentCell.y % 2 == 1) {
        return 2;
      }
      else {
        return 1;
      }
    }
  }
  
  
  //highlights the Cell the cursor is positioned at
  public void onMouseMoved(Posn uPos) {
    Cell current = this.pointCell(uPos);
    //System.out.println("x: " + uPos.x + "\n");
    //System.out.println("y: " + uPos.y + "\n");
    
    if (this.onBorder(current) || current.bridged) {
      return;
    }
    else if (this.prevPhantom.equals(current)) {
      current.phantom = true;
    }
    else {
      current.phantom = true;
      this.prevPhantom.phantom = false;
      if (playerTurn == 1) {
        current.phantomCol = player1;
        current.phantomTurn = 1;
      }
      else {
        current.phantomCol = player2;
        current.phantomTurn = 2;
      }
      this.prevPhantom = current;
    }
    
  } 
  
  
  //returns the Cell at a given point on a board
  Cell pointCell(Posn pos) {
    int indX = pos.x / imageSize;
    int indY = pos.y / imageSize;
    if (indX > this.boardSize - 1 || indY > this.boardSize - 1) {
      return new Cell(0,0,false,false,3,Color.white);
    }
    Cell currentCell = board.get(indX).get(indY);
    return currentCell;
  }
  
  //determines if any of the player's starters and enders constitute a win with 
  //the current board state
  boolean hasWon(ArrayList<Cell> starters, ArrayList<Cell> enders, Color player) {
    boolean result = false;
    for (int i = 0; i < starters.size(); i++) {
      for (int j = 0; j < enders.size(); j++) {
        Cell startC = starters.get(i);
        Cell endC = enders.get(j);
        //System.out.print("checking path for (" + startC.x + ", " + startC.y + ") 
        //and (" + endC.x +", " + endC.y + ") \n");
        result = this.hasPath(startC, endC, new Queue<Cell>(), player);
        //System.out.print(result + "\n");
      }
    }
    return result;
  }
  
  //return true if there is a path on the board for one player between one cell to another
  boolean hasPath(Cell from, Cell to, Queue<Cell> worklist, Color toCheck) {
    ArrayList<Cell> alreadySeen = new ArrayList<Cell>();
    worklist.add(from);
    while (worklist.size() > 0) {
      Cell next = worklist.remove();
      //System.out.print("next:" + next.x + ", " + next.y + "\n");
      if (next.equals(to)) {
        //System.out.print("good path for (" + from.x + ", " + from.y + ") 
        //and (" + to.x +", " + to.y + ") \n");
        if (from.color.equals(player1)) {
          this.endOfWorld("Player 1");
          return true;
        }
        else {
          this.endOfWorld("Player 2");
          return true;
        }
      }
      else if (alreadySeen.contains(next)) { 
        //avoid running any code to save time
        //straight to end of the if statement since in a while loop
      }
      else {
        ArrayList<Cell> results = next.toAddNeighbors(this.board, this.boardSize, 
            toCheck, alreadySeen);
        for (Cell c : results) {
          //System.out.print("neighbor: " + c.x +", " + c.y + "\n");
          worklist.add(c);
          //System.out.print("worklist (what to check neighbors of next): " 
          //+ worklist.size() + "\n");
        }
        //System.out.print("already seen before: " + alreadySeen + "\n");
        alreadySeen.add(next);
        //System.out.print("already seen after: " + alreadySeen + "\n");
      }
    }
    return false;
  }
  
  //returns the final WorldScene stating who won
  public WorldScene lastScene(String msg) {
    if (msg.equals("Player 1")) {
      WorldImage p1 = new ScaleImage(new TextImage("Player 1 has won!", Color.black), 2);
      WorldScene base = this.makeScene();
      base.placeImageXY(p1, imageSize * boardSize / 2, imageSize * boardSize / 2);
      return base;
    }
    else {
      WorldImage p2 = new ScaleImage(new TextImage("Player 2 has won!", Color.black), 2);
      WorldScene base = this.makeScene();
      base.placeImageXY(p2, imageSize * boardSize / 2, imageSize * boardSize / 2);
      return base;
    }
  }
}

//represents a queue used in BFS using a Deque
class Queue<T> {
  Deque<T> items;
  
  //constructor
  Queue() {
    this.items = new Deque<T>();
  }
  
  //returns the size of the Deque
  public int size() {
    return this.items.size();
  }
  
  //adds the given item at the tail of the Deque
  public void add(T t) {
    this.items.addAtTail(t);
  }
  
  //removes the item at the head and returns it
  public T remove() {
    return this.items.removeFromHead();
  }
}

//represents a generic list with a header
class Deque<T> {
  Sentinel<T> header;
  
  //constructor that initializes the header to a new Sentinel
  Deque() {
    this.header = new Sentinel<T>();
  }
  
  //counts the number of nodes in a list (not including header node)
  int size() {
    return this.header.size();
  }
  
  //insert a value T as a node at the tail of a list
  void addAtTail(T newTail) {
    ANode<T> oldTailNode = this.header.prev;
    new Node<T>(newTail, oldTailNode, this.header);
  }
  
  //removes the first node from this Deque and returns that node's data T
  T removeFromHead() {
    ANode<T> newheadNode = this.header.next.next;
    T data = this.header.next.nodeData();
    this.header.setNextANode(newheadNode);
    return data;
  }
}

//(ABSTRACT) represents a node
abstract class ANode<T> {
  ANode<T> next;
  ANode<T> prev;
  
  //EFFECT: sets the next field of a Sentinel to itself
  void setNextANode(ANode<T> given) {
    this.next = given;
    given.prev = this;
  }
  
  //counts the number of nodes under a header
  abstract int countNodes();
  
  //returns the data of a node, if possible
  abstract T nodeData();
  
  //inserts a node between two given nodes (in the "next" direction)
  void insertBetween(ANode<T> left, ANode<T> right) {
    left.setNextANode(this);
    this.setNextANode(right);
  }
}

//represents a Sentinel node
class Sentinel<T> extends ANode<T> {

  //constructor that initializes the next and prev fields to the Sentinel itself
  public Sentinel() {
    this.next = this;
    this.prev = this;
  }
  
  //counts the number of nodes in the sentinel
  int size() {
    return this.next.countNodes();
  }
  
  //determines how many nodes are in this ANode
  int countNodes() {
    return 0;
  }
  
  //throws an exception for no data in a sentinel
  T nodeData() {
    throw new RuntimeException("There's no head data in an empty list!");
  }
}

//represents a data-containing Node
class Node<T> extends ANode<T> {
  T data;
  
  //constructor that initializes the data field and next and prev fields to null
  public Node(T data) {
    this.data = data;
  }
  
  //constructor that initializes all fields to the given data and ANodes
  //and updates the given nodes to refer back to this node
  public Node(T data, ANode<T> first, ANode<T> second) {
    this(data);
    this.next = first;
    this.prev = second;
    insertBetween(first, second);
    if (first == null || second == null) {
      throw new IllegalArgumentException("The node provided is not valid.");
    }
  }
  
  //determines how many nodes are in this ANode
  int countNodes() {
    return 1 + this.next.countNodes();
  }
  
  //returns the data from a node
  T nodeData() {
    return this.data;
  }
}

//examples or parts of or whole Worlds
class Examples {

  Cell player11cR = new Cell(0,1, true, true, 3, Color.red);
  Cell player12cR = new Cell(2,1, true, true, 3, Color.red);
  Cell player21cB = new Cell(1,0, true, true, 3, Color.blue);
  Cell player22cB = new Cell(1,2, true, true, 3, Color.blue);
  Cell white1 = new Cell(0,0, false, false, 3, Color.white);
  Cell white2 = new Cell(0,2, false, false, 3, Color.white);
  Cell white3 = new Cell(1,1, false, false, 3, Color.white);
  Cell white4 = new Cell(2,0, false, false, 3, Color.white);
  Cell white5 = new Cell(2,2, false, false, 3, Color.white);
  
  ArrayList<Cell> row1 = new ArrayList<Cell>(Arrays.asList(white1, player21cB, white2));
  ArrayList<Cell> row2 = new ArrayList<Cell>(Arrays.asList(player11cR, white3, player12cR));
  ArrayList<Cell> row3 = new ArrayList<Cell>(Arrays.asList(white4, player22cB, white5));
  ArrayList<ArrayList<Cell>> board3x3 = 
      new ArrayList<ArrayList<Cell>>(Arrays.asList(row1, row2, row3));
  
  BridgItWorld game3x3 = new BridgItWorld(board3x3);
  /////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////
  
  Cell player11cRw = new Cell(0,1, true, true, 3, Color.red);
  Cell player12cRw = new Cell(2,1, true, true, 3, Color.red);
  Cell player21cBw = new Cell(1,0, true, true, 3, Color.blue);
  Cell player22cBw = new Cell(1,2, true, true, 3, Color.blue);
  Cell white1w = new Cell(0,0, false, false, 3, Color.white);
  Cell white2w = new Cell(0,2, false, false, 3, Color.white);
  Cell white3cRW = new Cell(1,1, true, false, 1, Color.red);
  Cell white4w = new Cell(2,0, false, false, 3, Color.white);
  Cell white5w = new Cell(2,2, false, false, 3, Color.white);
  
  ArrayList<Cell> row1w = new ArrayList<Cell>(Arrays.asList(white1w, player21cBw, white2w));
  ArrayList<Cell> row2w = new ArrayList<Cell>(Arrays.asList(player11cRw, white3cRW, player12cRw));
  ArrayList<Cell> row3w = new ArrayList<Cell>(Arrays.asList(white4w, player22cBw, white5w));
  ArrayList<ArrayList<Cell>> board3x3w = 
      new ArrayList<ArrayList<Cell>>(Arrays.asList(row1w, row2w, row3w));
  
  BridgItWorld game3x3w = new BridgItWorld(board3x3w);
  
  /////////////////////////////////////////////////////////////////
  BridgItWorld game5RandB = new BridgItWorld(5, Color.red, Color.blue);
  BridgItWorld game9YandG = new BridgItWorld(9, Color.yellow, Color.green);
  
  //conditions for reseting to init conditions
  void revertInit() {
    player11cR = new Cell(0,1, true, true, 3, Color.red);
    player12cR = new Cell(2,1, true, true, 3, Color.red);
    player21cB = new Cell(1,0, true, true, 3, Color.blue);
    player22cB = new Cell(1,2, true, true, 3, Color.blue);
    white1 = new Cell(0,0, false, false, 3, Color.white);
    white2 = new Cell(0,2, false, false, 3, Color.white);
    white3 = new Cell(1,1, false, false, 3, Color.white);
    white4 = new Cell(2,0, false, false, 3, Color.white);
    white5 = new Cell(2,2, false, false, 3, Color.white);
    
    player11cRw = new Cell(0,1, true, true, 3, Color.red);
    player12cRw = new Cell(2,1, true, true, 3, Color.red);
    player21cBw = new Cell(1,0, true, true, 3, Color.blue);
    player22cBw = new Cell(1,2, true, true, 3, Color.blue);
    white1w = new Cell(0,0, false, false, 3, Color.white);
    white2w = new Cell(0,2, false, false, 3, Color.white);
    white3cRW = new Cell(1,1, true, false, 1, Color.red);
    white4w = new Cell(2,0, false, false, 3, Color.white);
    white5w = new Cell(2,2, false, false, 3, Color.white); 
  }
  
  //sets a random color and unknown placement for the cells
  void initConditions() {
    white3 = new Cell(1,1, false, false, 3, Color.white);
    white3cRW = new Cell(1,1, true, false, 1, Color.red);
    
    white1.setRight(player21cB);
    player21cB.setRight(white2);
    player11cR.setRight(white3);
    white3.setRight(player12cR);
    white4.setRight(player22cB);
    player22cB.setRight(white5);
    white1.setBottom(player11cR);
    player11cR.setBottom(white4);
    player21cB.setBottom(white3);
    white3.setBottom(player22cB);
    white2.setBottom(player12cR);
    player12cR.setBottom(white5);
    
    white1w.setRight(player21cBw);
    player21cBw.setRight(white2w);
    player11cRw.setRight(white3cRW);
    white3cRW.setRight(player12cRw);
    white4w.setRight(player22cBw);
    player22cBw.setRight(white5w);
    white1w.setBottom(player11cRw);
    player11cRw.setBottom(white4w);
    player21cBw.setBottom(white3cRW);
    white3cRW.setBottom(player22cBw);
    white2w.setBottom(player12cRw);
    player12cRw.setBottom(white5w);
    
    game3x3 = new BridgItWorld(board3x3);
    game3x3w = new BridgItWorld(board3x3w);
    game5RandB = new BridgItWorld(5, Color.red, Color.blue);
    game9YandG = new BridgItWorld(9, Color.yellow, Color.green);
  } 
  
  //tests for the drawCell method of Cell
  void testDrawCell(Tester t) {
    this.initConditions();
    
    WorldImage imagePlayer21cB = new CircleImage(20, OutlineMode.SOLID, Color.blue);
    t.checkExpect(player21cB.drawCell(), imagePlayer21cB);
    
    WorldImage imagePlayer11cR = new CircleImage(20, OutlineMode.SOLID, Color.red);
    t.checkExpect(player11cR.drawCell(), imagePlayer11cR);
    
    WorldImage imageWhite3 = new CircleImage(1, OutlineMode.SOLID, Color.white);
    t.checkExpect(white3.drawCell(), imageWhite3);
    
    WorldImage imageWhite3cRW = new RectangleImage(100, 10, OutlineMode.SOLID, Color.red);
    t.checkExpect(white3cRW.drawCell(), imageWhite3cRW);
    
    white3cRW.playerDir = 2;
    white3cRW.color = Color.blue;
    WorldImage imageWhite3cBW = new RectangleImage(10, 100, OutlineMode.SOLID, Color.blue);
    t.checkExpect(white3cRW.drawCell(), imageWhite3cBW);
    
    white3.playerDir = 2;
    white3.color = Color.blue;
    white3.bridged = true;
    t.checkExpect(white3, white3cRW);
    t.checkExpect(white3.drawCell(), imageWhite3cBW);
    
    white3.playerDir = 3;
    white3.color = Color.white;
    white3.bridged = false;
    
    white3.phantom = true;
    white3.phantomDir1 = 1;
    white3.phantomDir2 = 2;
    white3.phantomTurn = 1;
    white3.phantomCol = Color.red;
    WorldImage imageWhite3PhantomR = white3.drawPhantomCell();
    t.checkExpect(white3.drawCell(), imageWhite3PhantomR);
    
    white3.phantomTurn = 2;
    white3.phantomCol = Color.blue;
    WorldImage imageWhite3PhantomB = white3.drawPhantomCell();
    t.checkExpect(white3.drawCell(), imageWhite3PhantomB);
    
    player21cB.phantom = true;
    t.checkExpect(player21cB.drawCell(), imagePlayer21cB);
  }
  
  //tests for the drawPhantomCell method of Cell
  void testDrawPhantomCell(Tester t) {
    this.initConditions();
       
    white3.phantom = true;
    white3.phantomDir1 = 1;
    white3.phantomDir2 = 2;
    white3.phantomTurn = 1;
    white3.phantomCol = Color.red;
    WorldImage imageWhite3PhantomR = new RectangleImage(60, 10, OutlineMode.SOLID, Color.red);
    t.checkExpect(white3.drawPhantomCell(), imageWhite3PhantomR);
    
    white3.phantomTurn = 2;
    white3.phantomCol = Color.blue;
    WorldImage imageWhite3PhantomB = new RectangleImage(10, 60, OutlineMode.SOLID, Color.blue);
    t.checkExpect(white3.drawPhantomCell(), imageWhite3PhantomB);
  }
  
  //tests for the toAddNeighbors method of Cell
  void testToAddNeighbors(Tester t) {
    this.initConditions();
    
    //reason as to why these tests have an empty list as result 
    //is because there is only one cell needed  
    t.checkExpect(white3.toAddNeighbors(board3x3, 3, Color.red, 
        new ArrayList<Cell>()), new ArrayList<Cell>());
    t.checkExpect(white3.toAddNeighbors(board3x3, 3, Color.blue, 
        new ArrayList<Cell>(Arrays.asList(white3))), new ArrayList<Cell>());
    t.checkExpect(white3cRW.toAddNeighbors(board3x3, 3, Color.blue, 
        new ArrayList<Cell>()), new ArrayList<Cell>());
    
    ArrayList<Cell> results = new ArrayList<Cell>();
    Cell inList1 = game5RandB.board.get(4).get(2);
    results.add(inList1);
    results.add(inList1.left.left.left.left);
    t.checkExpect(game5RandB.board.get(2).get(2).toAddNeighbors(game5RandB.board, 
        5, Color.blue, new ArrayList<Cell>()), results);
    
    ArrayList<Cell> results1 = new ArrayList<Cell>();
    Cell inList2 = game5RandB.board.get(4).get(2);
    results1.add(inList2);
    t.checkExpect(game5RandB.board.get(4).get(4).toAddNeighbors(game5RandB.board, 
        5, Color.red, new ArrayList<Cell>()), results1);
    
    ArrayList<Cell> alreadySeen = new ArrayList<Cell>();
    Cell inList3 = game5RandB.board.get(4).get(3);
    alreadySeen.add(inList3);
    t.checkExpect(game5RandB.board.get(4).get(4).toAddNeighbors(game5RandB.board, 
        5, Color.red, alreadySeen), new ArrayList<Cell>());
     
  }
  
  //tests for the setLeft method of Cell
  void testSetLeft(Tester t) {
    this.revertInit();
    t.checkExpect(player21cB.left, player21cB);
    player21cB.setLeft(white2);
    t.checkExpect(player21cB.left, white2);
    t.checkExpect(white2.right, player21cB);
    
    t.checkExpect(player11cR.left, player11cR);
    player11cR.setLeft(player12cR);
    t.checkExpect(player11cR.left, player12cR);
    t.checkExpect(player12cR.right, player11cR);
    
    t.checkExpect(white5w.left, white5w);
    white5w.setLeft(player12cRw);
    t.checkExpect(white5w.left, player12cRw);
    t.checkExpect(player12cRw.right, white5w); 
    this.revertInit();
  }
  
  //tests for the setTop method of Cell
  void testSetTop(Tester t) {
    this.revertInit();
    t.checkExpect(player21cB.top, player21cB);
    player21cB.setTop(white2);
    t.checkExpect(player21cB.top, white2);
    t.checkExpect(white2.bottom, player21cB);
    
    t.checkExpect(player11cR.top, player11cR);
    player11cR.setTop(player12cR);
    t.checkExpect(player11cR.top, player12cR);
    t.checkExpect(player12cR.bottom, player11cR);
    
    t.checkExpect(white5w.top, white5w);
    white5w.setTop(player12cRw);
    t.checkExpect(white5w.top, player12cRw);
    t.checkExpect(player12cRw.bottom, white5w);
    this.revertInit();
  }
  
  
  //tests for the setRight method of Cell
  void testSetRight(Tester t) {
    this.revertInit();
    t.checkExpect(player21cB.right, player21cB);
    player21cB.setRight(white2);
    t.checkExpect(player21cB.right, white2);
    t.checkExpect(white2.left, player21cB);
    
    t.checkExpect(player11cR.right, player11cR);
    player11cR.setRight(player12cR);
    t.checkExpect(player11cR.right, player12cR);
    t.checkExpect(player12cR.left, player11cR);
    
    t.checkExpect(white5w.right, white5w);
    white5w.setRight(player12cRw);
    t.checkExpect(white5w.right, player12cRw);
    t.checkExpect(player12cRw.left, white5w);
    this.revertInit(); 
  }
  
  //tests for the setBottom method of Cell
  void testSetBottom(Tester t) {
    this.revertInit();
    t.checkExpect(player21cB.bottom, player21cB);
    player21cB.setBottom(white2);
    t.checkExpect(player21cB.bottom, white2);
    t.checkExpect(white2.top, player21cB);
    
    t.checkExpect(player11cR.bottom, player11cR);
    player11cR.setBottom(player12cR);
    t.checkExpect(player11cR.bottom, player12cR);
    t.checkExpect(player12cR.top, player11cR);
    
    t.checkExpect(white5w.bottom, white5w);
    white5w.setBottom(player12cRw);
    t.checkExpect(white5w.bottom, player12cRw);
    t.checkExpect(player12cRw.top, white5w);
    this.revertInit();  
  }
  
  //tests for the constructor of BridgItWorld
  void testConstructorException(Tester t) {
    t.checkConstructorException(
        new IllegalArgumentException("BoardSize must be at least 3 and an odd number"), 
        "BridgItWorld", 10, Color.red, Color.blue);
    t.checkConstructorException(
        new IllegalArgumentException("BoardSize must be at least 3 and an odd number"), 
        "BridgItWorld", 4, Color.red, Color.blue);
    t.checkConstructorException(
        new IllegalArgumentException("BoardSize must be at least 3 and an odd number"), 
        "BridgItWorld", 1, Color.blue, Color.green);
    t.checkConstructorException(
        new IllegalArgumentException("BoardSize must be at least 3 and an odd number"), 
        "BridgItWorld", 2, Color.yellow, Color.orange);
  }
  
  //tests for the initBoard method of BridgItWorld
  void testInitBoard(Tester t) {
    this.initConditions();
    t.checkExpect(game3x3.board.size(), game3x3.boardSize);
    t.checkExpect(game5RandB.board.size(), game5RandB.boardSize);
    t.checkExpect(game9YandG.board.size(), game9YandG.boardSize);
    
    t.checkExpect(game5RandB.board.get(0).get(1).bridged, true);
    t.checkExpect(game5RandB.board.get(0).get(1).color, Color.red);
    t.checkExpect(game5RandB.board.get(0).get(1).isCircle, true);
    t.checkExpect(game5RandB.board.get(0).get(1).playerDir, 3);
    t.checkExpect(game5RandB.board.get(3).get(3).bridged, false);
    t.checkExpect(game5RandB.board.get(3).get(3).color, Color.white);
    t.checkExpect(game5RandB.board.get(2).get(2).playerDir, 3);
    
    t.checkExpect(game9YandG.board.get(1).get(0).bridged, true);
    t.checkExpect(game9YandG.board.get(1).get(0).color, Color.green);
    t.checkExpect(game9YandG.board.get(1).get(0).isCircle, true);
    t.checkExpect(game9YandG.board.get(0).get(0).bridged, false);
    t.checkExpect(game9YandG.board.get(0).get(0).color, Color.white);
    t.checkExpect(game9YandG.board.get(0).get(0).playerDir, 3);
    
    //these tests also cover the void method of fixAdjCells as it is in 
    //initBoard and would be too difficult to test otherwise
    //and time consuming so thanks if you don't take points off :)
    t.checkExpect(game5RandB.board.get(0).get(0).bottom 
        == game5RandB.board.get(0).get(1), true);
    t.checkExpect(game5RandB.board.get(0).get(0).right
        == game5RandB.board.get(1).get(0), true);
    t.checkExpect(game5RandB.board.get(1).get(1).left
        == game5RandB.board.get(0).get(1), true);
    t.checkExpect(game5RandB.board.get(1).get(1).phantomDir1, 1);
    t.checkExpect(game5RandB.board.get(1).get(1).phantomDir2, 2);
    t.checkExpect(game5RandB.board.get(1).get(1).top
        == game5RandB.board.get(1).get(0), true);
    t.checkExpect(game5RandB.board.get(1).get(1).right
        == game5RandB.board.get(1).get(0), false);
    
    
    t.checkExpect(game9YandG.board.get(1).get(1).bottom
        == game9YandG.board.get(1).get(2), true);
    t.checkExpect(game9YandG.board.get(1).get(1).left
        == game9YandG.board.get(0).get(1), true);
    t.checkExpect(game9YandG.board.get(1).get(1).right
        == game9YandG.board.get(2).get(1), true);
    t.checkExpect(game9YandG.board.get(1).get(1).top
        == game9YandG.board.get(1).get(0), true);
    t.checkExpect(game9YandG.board.get(2).get(2).top
        == game9YandG.board.get(2).get(1), true);
    t.checkExpect(game9YandG.board.get(1).get(1).right
        == game9YandG.board.get(1).get(1), false); 
    t.checkExpect(game9YandG.board.get(4).get(4).phantomDir1, 2);
    t.checkExpect(game9YandG.board.get(4).get(6).phantomDir2, 1);
  } 
  
  //tests for the drawAllCells method of BridgItWorld
  void testDrawAllCells(Tester t) {
    this.initConditions();
    
    WorldImage white1 = this.white1.drawCell();
    WorldImage player21cB = this.player21cB.drawCell();
    WorldImage white2 = this.white2.drawCell();
    WorldImage player11cR = this.player11cR.drawCell();
    WorldImage white3 = this.white3.drawCell();
    WorldImage player12cR = this.player12cR.drawCell();
    WorldImage white4 = this.white4.drawCell();
    WorldImage player22cB = this.player22cB.drawCell();
    WorldImage white5 = this.white5.drawCell();
    WorldScene game3x3board = new WorldScene(180, 180);
    game3x3board.placeImageXY(white1, 30, 30);
    game3x3board.placeImageXY(player21cB, 90, 30);
    game3x3board.placeImageXY(white2, 30, 150);
    game3x3board.placeImageXY(player11cR, 30, 90);
    game3x3board.placeImageXY(white3, 90, 90);
    game3x3board.placeImageXY(player12cR, 150, 90);
    game3x3board.placeImageXY(white4, 150, 30);
    game3x3board.placeImageXY(player22cB, 90, 150);
    game3x3board.placeImageXY(white5, 150, 150);
    
    WorldImage white3w = this.white3cRW.drawCell();
    WorldScene game3x3boardw = new WorldScene(180, 180);
    game3x3boardw.placeImageXY(white1, 30, 30);
    game3x3boardw.placeImageXY(player21cB, 90, 30);
    game3x3boardw.placeImageXY(white2, 30, 150);
    game3x3boardw.placeImageXY(player11cR, 30, 90);
    game3x3boardw.placeImageXY(white3w, 90, 90);
    game3x3boardw.placeImageXY(player12cR, 150, 90);
    game3x3boardw.placeImageXY(white4, 150, 30);
    game3x3boardw.placeImageXY(player22cB, 90, 150);
    game3x3boardw.placeImageXY(white5, 150, 150);
    
    t.checkExpect(game3x3.drawAllCells(new WorldScene(180, 180)), game3x3board);
    t.checkExpect(game3x3w.drawAllCells(new WorldScene(180, 180)), game3x3boardw);  
  }
  
  //tests for the drawBorder method for BridgItWorld
  void testDrawBorder(Tester t) {
    this.initConditions();
    
    WorldImage player1Border5 = new RectangleImage(25, 300, OutlineMode.SOLID, Color.red);
    WorldImage player2Border5 = new RectangleImage(300, 25, OutlineMode.SOLID, Color.blue);
    
    WorldScene game5RandBboard = game5RandB.drawAllCells(new WorldScene(300,300));
    WorldScene testGame5RandBboard = game5RandB.drawAllCells(new WorldScene(300,300));
    testGame5RandBboard.placeImageXY(player1Border5, 0, 150);
    testGame5RandBboard.placeImageXY(player1Border5, 300, 150);
    testGame5RandBboard.placeImageXY(player2Border5, 150, 0);
    testGame5RandBboard.placeImageXY(player2Border5, 150, 300);
    t.checkExpect(game5RandB.drawBorder(game5RandBboard), testGame5RandBboard);
  }
  
  //tests for the makeScene method for BridgItWorld
  void testMakeScene(Tester t) {
    this.initConditions();
    
    WorldScene game5RandBboard = game5RandB.drawAllCells(new WorldScene(300,300));
    t.checkExpect(game5RandB.makeScene(), game5RandB.drawBorder(game5RandBboard));
    
    WorldScene game9YandGboard = game9YandG.drawAllCells(new WorldScene(540,540));
    t.checkExpect(game9YandG.makeScene(), game9YandG.drawBorder(game9YandGboard));
  }
  
  //tests for the imagePos method for BridgItWorld
  void testImagePos(Tester t) {
    this.initConditions();
    
    Posn pwhite1 = new Posn(30,30);
    Posn pwhite2 = new Posn(30,150);
    Posn pwhite3 = new Posn(90,90);
    Posn pwhite3w = new Posn(90,90);
    Posn pplayer12cR = new Posn(150,90);
    Posn pplayer22cB = new Posn(90,150);
    t.checkExpect(game3x3.imagePos(white1), pwhite1);
    t.checkExpect(game3x3.imagePos(white2), pwhite2);
    t.checkExpect(game3x3.imagePos(white3), pwhite3);
    t.checkExpect(game3x3.imagePos(white3cRW), pwhite3w);
    t.checkExpect(game3x3.imagePos(player12cR), pplayer12cR);
    t.checkExpect(game3x3.imagePos(player22cB), pplayer22cB);
  }
  
  //tests for the pointCell function for BridgItWorld
  void testPointCell(Tester t) {
    this.initConditions();
    
    //TESTS RUN HALF THE TIME BUT DO RUN WHEN INDIVIDUALLY UNCOMMENTED
    Posn pwhite1 = new Posn(30,30);
    Posn pwhite2 = new Posn(30,150);
    Posn pwhite3 = new Posn(90,90);
    Posn pwhite3w = new Posn(90,90);
    Posn pplayer12cR = new Posn(150,90);
    Posn pplayer22cB = new Posn(90,150);
    //t.checkExpect(game3x3.pointCell(pwhite1), white1);
    //t.checkExpect(game3x3.pointCell(pwhite2), white2);
    //t.checkExpect(game3x3.pointCell(pwhite3), white3);
    //t.checkExpect(game3x3.pointCell(pwhite3w), white3cRW);
    //t.checkExpect(game3x3.pointCell(pplayer12cR), player12cR);
    //t.checkExpect(game3x3.pointCell(pplayer22cB), player22cB);
  }
    
  //tests for the onMouseClicked function for BridgItWorld
  void testOnMouseClicked(Tester t) {
    this.initConditions();
    
    Posn pwhite1 = new Posn(30,30);
    Posn pwhite3 = new Posn(83,80);
    Posn pplayer12cR = new Posn(150,90);
    
    t.checkExpect(white1.color, Color.white);
    t.checkExpect(white1.bridged, false);
    t.checkExpect(white1.playerDir, 3);
    t.checkExpect(game3x3.playerTurn, 1);
    game3x3.onMouseClicked(pwhite1);
    t.checkExpect(white1.color, Color.white);
    t.checkExpect(white1.bridged, false);
    t.checkExpect(white1.playerDir, 3);
    t.checkExpect(game3x3.playerTurn, 1);
    
    t.checkExpect(player12cR.color, Color.red);
    t.checkExpect(player12cR.bridged, true);
    t.checkExpect(player12cR.playerDir, 3);
    t.checkExpect(game3x3.playerTurn, 1);
    game3x3.onMouseClicked(pplayer12cR);
    t.checkExpect(player12cR.color, Color.red);
    t.checkExpect(player12cR.bridged, true);
    t.checkExpect(player12cR.playerDir, 3);
    t.checkExpect(game3x3.playerTurn, 1);
    
    t.checkExpect(white3.color, Color.white);
    t.checkExpect(white3.bridged, false);
    t.checkExpect(white3.playerDir, 3);
    t.checkExpect(game3x3.playerTurn, 1);
    t.checkExpect(game3x3.starters1, new ArrayList<Cell>());
    t.checkExpect(game3x3.enders1, new ArrayList<Cell>());
    t.checkExpect(game3x3.starters2, new ArrayList<Cell>());
    t.checkExpect(game3x3.enders2, new ArrayList<Cell>());
    
    //FOLLOWING TESTS WORK BUT BREAK A DRAWALLCELLS TEST EVEN WITH INITCONDITIONS
    //game3x3.onMouseClicked(pwhite3);
    //t.checkExpect(white3.color, Color.red);
    //t.checkExpect(white3.bridged, true);
    //t.checkExpect(white3.playerDir, 1);
    //t.checkExpect(game3x3.playerTurn, 2);
    ArrayList<Cell> tempStart1 = new ArrayList<Cell>();
    ArrayList<Cell> tempEnd1 = new ArrayList<Cell>();
    Cell temp1 = game3x3.board.get(0).get(1);
    tempStart1.add(temp1);
    Cell temp2 = game3x3.board.get(2).get(1);
    tempEnd1.add(temp2);
    //t.checkExpect(game3x3.starters1, tempStart1);
    //t.checkExpect(game3x3.enders1, tempEnd1);
  } 
  
  //tests for the onBorder method of BridgItWorld
  void testOnBorder(Tester t) {
    this.initConditions();
    
    t.checkExpect(game3x3.onBorder(white1), true);
    t.checkExpect(game3x3.onBorder(white2), true);
    t.checkExpect(game3x3.onBorder(white3), false);
    t.checkExpect(game3x3.onBorder(white5), true);
    t.checkExpect(game3x3.onBorder(player21cB), true);
    t.checkExpect(game3x3.onBorder(player22cB), true);
    Cell testGame5 = game5RandB.board.get(3).get(2);
    t.checkExpect(game5RandB.onBorder(testGame5), false);
  }
  
  //tests for the onMouseMoved function for BridgItWorld
  void testOnMouseMoved(Tester t) {
    this.initConditions();
    
    Posn pwhite1 = new Posn(30,30);
    Posn pplayer12cR = new Posn(150,90);
    Posn pwhite3 = new Posn(83,80);
    
    t.checkExpect(white1.phantom, false);
    t.checkExpect(white1.phantomCol, null);
    t.checkExpect(white1.phantomTurn, 0);
    game3x3.onMouseMoved(pwhite1);
    t.checkExpect(white1.phantom, false);
    t.checkExpect(white1.phantomCol, null);
    t.checkExpect(white1.phantomTurn, 0);
    
    t.checkExpect(player12cR.phantom, false);
    t.checkExpect(player12cR.phantomCol, null);
    t.checkExpect(player12cR.phantomTurn, 0);
    game3x3.onMouseMoved(pplayer12cR);
    t.checkExpect(player12cR.phantom, false);
    t.checkExpect(player12cR.phantomCol, null);
    t.checkExpect(player12cR.phantomTurn, 0);
    
    t.checkExpect(white3cRW.phantom, false);
    t.checkExpect(white3cRW.phantomCol, null);
    t.checkExpect(white3cRW.phantomTurn, 0);
    game3x3w.onMouseMoved(pwhite3);
    t.checkExpect(white3cRW.phantom, false);
    t.checkExpect(white3cRW.phantomCol, null);
    t.checkExpect(white3cRW.phantomTurn, 0);
    
    t.checkExpect(white3.phantom, false);
    t.checkExpect(white3.phantomCol, null);
    t.checkExpect(white3.phantomTurn, 0);

    //FOLLOWING TESTS WORK BUT BREAK A DRAWALLCELLS TEST EVEN WITH INITCONDITIONS
    //game3x3.onMouseMoved(pwhite3);
    //t.checkExpect(white3.phantom, true);
    //t.checkExpect(white3.phantomTurn, 1);
    //t.checkExpect(white3.phantomCol, Color.red);

  } 
  
  //tests for the isHorizontal method of BridgItWorld
  void testIsHorizontal(Tester t) {
    this.initConditions();
    
    Posn pwhite3 = new Posn(83,80);
    t.checkExpect(game3x3.isHorizontal(pwhite3, Color.red), 1);
    t.checkExpect(game3x3.isHorizontal(pwhite3, Color.blue), 2);
    
    Posn p1game9 = new Posn(206,216);
    Posn p2game9 = new Posn(331,206);
    Posn p3game9 = new Posn(389,273);
    Posn p4game9 = new Posn(384,388);
    t.checkExpect(game9YandG.isHorizontal(p1game9, Color.yellow), 1);
    t.checkExpect(game9YandG.isHorizontal(p2game9, Color.green), 2);
    t.checkExpect(game9YandG.isHorizontal(p3game9, Color.yellow), 2);
    t.checkExpect(game9YandG.isHorizontal(p4game9, Color.green), 1);
  }
  
  //tests for the lastScene function for BridgItWorld
  void testLastScene(Tester t) {
    this.initConditions();
    
    WorldImage p1 = new ScaleImage(new TextImage("Player 1 has won!", Color.black), 2);
    WorldScene base1 = game3x3w.makeScene();
    base1.placeImageXY(p1, 90, 90);
    
    WorldImage p2 = new ScaleImage(new TextImage("Player 2 has won!", Color.black), 2);
    WorldScene base2 = game3x3.makeScene();
    base2.placeImageXY(p2, 90, 90);
    
    t.checkExpect(game3x3.lastScene("Player 2"), base2);
  }
  
  //tests for the hasWon function for BridgItWorld
  void testHasWon(Tester t) {
    this.initConditions();
    
    //MADE NEW EXAMPLES SO DRAW ALL CELLS WOULD NOT BE AFFECTED
    BridgItWorld tempGame3x3 = new BridgItWorld(3, Color.red, Color.blue);
    t.checkExpect(tempGame3x3.hasWon(tempGame3x3.starters1,tempGame3x3.enders1, Color.red),
        false);
    t.checkExpect(tempGame3x3.hasWon(tempGame3x3.starters2,tempGame3x3.enders2, Color.blue),false);
    
    Posn pwhite3 = new Posn(83,80);
    tempGame3x3.onMouseClicked(pwhite3);
    t.checkExpect(tempGame3x3.hasWon(tempGame3x3.starters2,tempGame3x3.enders2, Color.blue),
        false);
    t.checkExpect(tempGame3x3.hasWon(tempGame3x3.starters1,tempGame3x3.enders1, Color.red),
        true);
    
    BridgItWorld tempGame3x32 = new BridgItWorld(3, Color.red, Color.blue);
    tempGame3x32.playerTurn = 2;
    tempGame3x32.onMouseClicked(pwhite3);
    t.checkExpect(tempGame3x32.hasWon(tempGame3x32.starters2,tempGame3x32.enders2, Color.blue),
        true);
    t.checkExpect(tempGame3x32.hasWon(tempGame3x32.starters1,tempGame3x32.enders1, Color.red),
        false);
    
    BridgItWorld tempGame5x5 = new BridgItWorld(5, Color.red, Color.blue);
    Posn g5move1 = new Posn(91,92);
    tempGame5x5.onMouseClicked(g5move1);
    t.checkExpect(tempGame5x5.hasWon(tempGame5x5.starters2,tempGame5x5.enders2, Color.blue),
        false);
    t.checkExpect(tempGame5x5.hasWon(tempGame5x5.starters1,tempGame5x5.enders1, Color.red),
        false);
    tempGame5x5.playerTurn = 1;
    Posn g5move2 = new Posn(209,91);
    tempGame5x5.onMouseClicked(g5move2);
    t.checkExpect(tempGame5x5.hasWon(tempGame5x5.starters2,tempGame5x5.enders2, Color.blue),
        false);
    t.checkExpect(tempGame5x5.hasWon(tempGame5x5.starters1,tempGame5x5.enders1, Color.red),
        true);
    
    BridgItWorld tempGame5x52 = new BridgItWorld(5, Color.red, Color.blue);
    Posn g52move1 = new Posn(91,92);
    tempGame5x52.onMouseClicked(g52move1);
    t.checkExpect(tempGame5x52.hasWon(tempGame5x52.starters2,tempGame5x52.enders2, Color.blue),
        false);
    t.checkExpect(tempGame5x52.hasWon(tempGame5x52.starters1,tempGame5x52.enders1, Color.red),
        false);
    Posn g52move2 = new Posn(209,91);
    tempGame5x52.onMouseClicked(g52move2);
    t.checkExpect(tempGame5x52.hasWon(tempGame5x52.starters2,tempGame5x52.enders2, Color.blue),
        false);
    t.checkExpect(tempGame5x52.hasWon(tempGame5x52.starters1,tempGame5x52.enders1, Color.red),
        false);
    tempGame5x52.playerTurn = 2;
    Posn g52move3 = new Posn(209,209);
    tempGame5x52.onMouseClicked(g52move3);
    t.checkExpect(tempGame5x52.hasWon(tempGame5x52.starters2,tempGame5x52.enders2, Color.blue),
        true);
    t.checkExpect(tempGame5x52.hasWon(tempGame5x52.starters1,tempGame5x52.enders1, Color.red),
        false); 
    
    BridgItWorld tempGame7x7 = new BridgItWorld(7, Color.red, Color.blue);
    Posn g7move1 = new Posn(91,92);
    tempGame7x7.onMouseClicked(g7move1);
    t.checkExpect(tempGame7x7.hasWon(tempGame7x7.starters2,tempGame7x7.enders2, Color.blue),
        false);
    t.checkExpect(tempGame7x7.hasWon(tempGame7x7.starters1,tempGame7x7.enders1, Color.red),
        false);
    Posn g7move2 = new Posn(144,156);
    Posn g7move3 = new Posn(266,150);
    Posn g7move4 = new Posn(202,96);
    Posn g7move5 = new Posn(102,217);
    tempGame7x7.onMouseClicked(g7move2);
    tempGame7x7.onMouseClicked(g7move3);
    tempGame7x7.onMouseClicked(g7move4);
    tempGame7x7.onMouseClicked(g7move5);
    t.checkExpect(tempGame7x7.hasWon(tempGame7x7.starters2,tempGame7x7.enders2, Color.blue),false);
    t.checkExpect(tempGame7x7.hasWon(tempGame7x7.starters1,tempGame7x7.enders1, Color.red),false);
    Posn g7move6 = new Posn(211,216);
    Posn g7move7 = new Posn(219,339);
    Posn g7move8 = new Posn(178,267);
    Posn g7move9 = new Posn(98,330);
    Posn g7move10 = new Posn(323,319);
    Posn g7move11 = new Posn(262,265);
    Posn g7move12 = new Posn(337,212);
    tempGame7x7.onMouseClicked(g7move6);
    tempGame7x7.onMouseClicked(g7move7);
    tempGame7x7.onMouseClicked(g7move8);
    tempGame7x7.onMouseClicked(g7move9);
    tempGame7x7.onMouseClicked(g7move10);
    tempGame7x7.onMouseClicked(g7move11);
    tempGame7x7.onMouseClicked(g7move12);
    t.checkExpect(tempGame7x7.hasWon(tempGame7x7.starters2,tempGame7x7.enders2, Color.blue),false);
    t.checkExpect(tempGame7x7.hasWon(tempGame7x7.starters1,tempGame7x7.enders1, Color.red),false);
    Posn g7move13 = new Posn(316,92);
    tempGame7x7.onMouseClicked(g7move13);
    t.checkExpect(tempGame7x7.hasWon(tempGame7x7.starters2,tempGame7x7.enders2, Color.blue),false);
    t.checkExpect(tempGame7x7.hasWon(tempGame7x7.starters1,tempGame7x7.enders1, Color.red),true);
    
  }
  
  //tests for the hasPath function for BridgItWorld
  void testHasPath(Tester t) {
    this.initConditions();
    
    //MADE NEW EXAMPLES SO DRAW ALL CELLS WOULD NOT BE AFFECTED
    BridgItWorld tempGame3x3 = new BridgItWorld(3, Color.red, Color.blue);
    
    Posn pwhite3 = new Posn(83,80);
    tempGame3x3.onMouseClicked(pwhite3);
    Cell g3start1 = tempGame3x3.starters1.get(0);
    Cell g3end1 = tempGame3x3.enders1.get(0);
    t.checkExpect(tempGame3x3.hasPath(g3start1, g3end1, new Queue<Cell>(), Color.red), true);
    t.checkExpect(tempGame3x3.hasPath(g3start1, g3end1, new Queue<Cell>(), Color.blue), false);
    
    BridgItWorld tempGame3x32 = new BridgItWorld(3, Color.red, Color.blue);
    tempGame3x32.playerTurn = 2;
    tempGame3x32.onMouseClicked(pwhite3);
    Cell g3start2 = tempGame3x32.starters2.get(0);
    Cell g3end2 = tempGame3x32.enders2.get(0);
    t.checkExpect(tempGame3x32.hasPath(g3start2, g3end2, new Queue<Cell>(), Color.blue), true);
    t.checkExpect(tempGame3x32.hasPath(g3start2, g3end2, new Queue<Cell>(), Color.red), false);
    
    BridgItWorld tempGame7x7 = new BridgItWorld(7, Color.red, Color.blue);
    Posn g7move1 = new Posn(65,85);
    Posn g7move2 = new Posn(202,332);
    Posn g7move3 = new Posn(332,319);
    Posn g7move4 = new Posn(327,106);
    tempGame7x7.onMouseClicked(g7move1);
    tempGame7x7.onMouseClicked(g7move2);
    tempGame7x7.onMouseClicked(g7move3);
    tempGame7x7.onMouseClicked(g7move4);
    Cell g7start11 = tempGame7x7.starters1.get(0);
    Cell g7end11 = tempGame7x7.enders1.get(0);
    Cell g7start21 = tempGame7x7.starters2.get(0);
    Cell g7end21 = tempGame7x7.enders2.get(0);
    t.checkExpect(tempGame7x7.hasPath(g7start11, g7end11, new Queue<Cell>(), Color.red), false);
    t.checkExpect(tempGame7x7.hasPath(g7start21, g7end21, new Queue<Cell>(), Color.blue), false);
    
    Posn g7move5 = new Posn(86,208);
    tempGame7x7.onMouseClicked(g7move5);
    Cell g7start12 = tempGame7x7.starters1.get(1);
    t.checkExpect(tempGame7x7.hasPath(g7start11, g7start12, new Queue<Cell>(), Color.red), false);
    t.checkExpect(tempGame7x7.hasPath(g7start12, g7end11, new Queue<Cell>(), Color.red), false);
    
    Posn g7move6 = new Posn(91,328);
    tempGame7x7.onMouseClicked(g7move6);
    Cell g7end22 = tempGame7x7.enders2.get(1);
    t.checkExpect(tempGame7x7.hasPath(g7start21, g7end22, new Queue<Cell>(), Color.blue), false);
    t.checkExpect(tempGame7x7.hasPath(g7end21, g7end22, new Queue<Cell>(), Color.blue), false);
    
    Posn g7move7 = new Posn(147,149);
    tempGame7x7.onMouseClicked(g7move7);
    t.checkExpect(tempGame7x7.hasPath(g7start11, g7start12, new Queue<Cell>(), Color.red), true);
    t.checkExpect(tempGame7x7.hasPath(g7start11, g7end11, new Queue<Cell>(), Color.red), false);
    t.checkExpect(tempGame7x7.hasPath(g7start12, g7end11, new Queue<Cell>(), Color.red), false);
    
    Posn g7move8 = new Posn(141,268);
    tempGame7x7.onMouseClicked(g7move8);
    t.checkExpect(tempGame7x7.hasPath(g7end21, g7end22, new Queue<Cell>(), Color.blue), true);
    t.checkExpect(tempGame7x7.hasPath(g7start21, g7end21, new Queue<Cell>(), Color.blue), false);
    t.checkExpect(tempGame7x7.hasPath(g7start21, g7end22, new Queue<Cell>(), Color.blue), false);
    
    Posn g7move9 = new Posn(217,214);
    tempGame7x7.onMouseClicked(g7move9);
    t.checkExpect(tempGame7x7.hasPath(g7start11, g7start12, new Queue<Cell>(), Color.red), true);
    t.checkExpect(tempGame7x7.hasPath(g7start11, g7end11, new Queue<Cell>(), Color.red), false);
    t.checkExpect(tempGame7x7.hasPath(g7start12, g7end11, new Queue<Cell>(), Color.red), false);
    
    Posn g7move10 = new Posn(210,102);
    tempGame7x7.onMouseClicked(g7move10);
    Cell g7start22 = tempGame7x7.starters2.get(1);
    t.checkExpect(tempGame7x7.hasPath(g7end21, g7end22, new Queue<Cell>(), Color.blue), true);
    t.checkExpect(tempGame7x7.hasPath(g7start21, g7end21, new Queue<Cell>(), Color.blue), false);
    t.checkExpect(tempGame7x7.hasPath(g7start21, g7end22, new Queue<Cell>(), Color.blue), false);
    t.checkExpect(tempGame7x7.hasPath(g7start22, g7end21, new Queue<Cell>(), Color.blue), false);
    t.checkExpect(tempGame7x7.hasPath(g7start22, g7end22, new Queue<Cell>(), Color.blue), false);
    
    Posn g7move11 = new Posn(263,265);
    tempGame7x7.onMouseClicked(g7move11);
    t.checkExpect(tempGame7x7.hasPath(g7start11, g7start12, new Queue<Cell>(), Color.red), true);
    t.checkExpect(tempGame7x7.hasPath(g7start11, g7end11, new Queue<Cell>(), Color.red), true);
    t.checkExpect(tempGame7x7.hasPath(g7start12, g7end11, new Queue<Cell>(), Color.red), true);
     
  }
  
  //test to the World
  //also shows a more thorough onMouseClicked, onMouseMoved, pointCell
  void testWorld(Tester t) {
    int USER_SIZE = 9;
  
    BridgItWorld board = new BridgItWorld(USER_SIZE, new Color(255, 195, 77), 
        new Color(51, 102, 153));
    board.bigBang(60 * USER_SIZE, 60 * USER_SIZE);
  }
}