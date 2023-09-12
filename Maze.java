package Finished;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;
import java.util.function.Predicate;

//import Finished.ANode;
////import Finished.Deque;
////import Finished.Node;
////import Finished.Sentinel;


// represents a cell of the grid of a maze
class Cell {
  boolean top = false;
  boolean bottom = false;
  boolean right = false;
  boolean left = false;

  Cell topCell = null;
  Cell bottomCell = null;
  Cell rightCell = null;
  Cell leftCell = null;

  Posn pos;
  ArrayList<Edge> outEdges;
  Color color = Color.gray;
  int cellSize;

  Cell(int x, int y, int cellSize) {
    this.pos = new Posn(x, y);
    this.outEdges = new ArrayList<Edge>();
    this.cellSize = cellSize;
  }

  // draws the cell
  WorldImage drawCell() {
    WorldImage cell = new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, this.color);
    if (!this.top) {
      cell = new AboveImage(new LineImage(new Posn(cellSize, 0), Color.black), cell);
    }
    if (!this.bottom) {
      cell = new AboveImage(cell, new LineImage(new Posn(cellSize, 0), Color.black));
    }
    if (!this.right) {
      cell = new BesideImage(cell, new LineImage(new Posn(0, cellSize), Color.black));
    }
    if (!this.left) {
      cell = new BesideImage(new LineImage(new Posn(0, cellSize), Color.black), cell);
    }
    return cell;
  }

  public String toString() {
    return "Cell: (" + this.pos.x + " " + this.pos.y + ")";
  }
}

// represents the connection between two cells
class Edge {
  boolean horizontal;
  Cell from;
  Cell to;
  int weight;

  Edge() { }

  Edge(boolean horizontal, Cell from, Cell to, int weight) {
    this.horizontal = horizontal;
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  public String toString() {
    return "(" + this.from + " and " + this.to + ")";
  }
}

// class for comparing two Edges
class EdgeComparator extends Edge implements Comparator<Edge> {
  @Override
  public int compare(Edge o1, Edge o2) {
    if (o1.weight > o2.weight) {
      return 1;
    }
    else if (o1.weight < o2.weight) {
      return -1;
    }
    return 0;
  }
}

// represents a maze
class Maze extends World {

  int width;
  int height;
  ArrayList<Edge> edges;
  ArrayList<Cell> cells;
  ArrayList<Edge> minSpanTree;
  Random rand;
  HashMap<Posn, Posn> representatives;
  Cell selectedCell;
  boolean solveMaze;
  ArrayList<Cell> answer;
  int currIndex;

  Maze(int width, int height) {
    this.width = width;
    this.height = height;
    this.edges = new ArrayList<Edge>();
    this.cells = new ArrayList<Cell>();
    this.rand = new Random();
    this.representatives = new HashMap<Posn, Posn>();
    this.solveMaze = false;
    this.currIndex = 0;
    this.initCells();
    this.initEdges();
    this.initReps();
    this.kruskal();
    this.updatePaths();
  }

  Maze(int width, int height, Random rand) {
    this.width = width;
    this.height = height;
    this.edges = new ArrayList<Edge>();
    this.cells = new ArrayList<Cell>();
    this.rand = rand;
    this.representatives = new HashMap<Posn, Posn>();
    this.solveMaze = false;
    this.currIndex = 0;
    this.initCells();
    this.initEdges();
    this.initReps();
    this.kruskal();
    this.updatePaths();
  }

  // initializes all of the cells of the maze
  void initCells() {
    for (int i = 0; i < this.height; i++) {
      for (int j = 0; j < this.width; j++) {
        this.cells.add(new Cell(j, i, 750 / Math.max(this.width, this.height)));
      }
    }
    this.selectedCell = this.cells.get(0);
    this.selectedCell.color = Color.green;
    this.cells.get(this.cells.size() - 1).color = Color.pink;
  }

  // initializes all of the edges of the maze
  void initEdges() {
    Edge currEdge = new Edge();

    for (int i = 0; i < this.height * this.width; i++) {
      // initialize horizontal edges
      if ((i % this.width + 1) < this.width) {
        currEdge = new Edge(true, this.cells.get(i), this.cells.get(i + 1), this.rand.nextInt());
        this.edges.add(currEdge);
        this.cells.get(i).rightCell = this.cells.get(i + 1);
        this.cells.get(i + 1).leftCell = this.cells.get(i);
      }
      // initialize vertical edges
      if ((i + this.width) < this.width * this.height) {
        currEdge = new Edge(false, this.cells.get(i), this.cells.get(i + this.width),
            this.rand.nextInt());
        this.edges.add(currEdge);
        this.cells.get(i).bottomCell = this.cells.get(i + this.width);
        this.cells.get(i + this.width).topCell = this.cells.get(i);
      }
    }
  }

  // adds the two posns to the hashmap field of the maze
  void union(Posn p1, Posn p2) {
    this.representatives.put(this.find(p1), this.find(p2));
  }

  // finds the given posn in the hashmap
  Posn find(Posn p1) {
    if (p1.equals(this.representatives.get(p1))) {
      return p1;
    } else {
      return find(this.representatives.get(p1));
    }
  }

  // initializes the hashmap field of maze
  void initReps() {
    for (int i = 0; i < this.cells.size(); i++) {
      this.representatives.put(this.cells.get(i).pos, this.cells.get(i).pos);
    }
  }

  // creates the minimum spanning tree of all the edges of the maze
  void kruskal() {
    int i = 0;
    this.edges.sort(new EdgeComparator());
    this.minSpanTree = new ArrayList<Edge>();
    Edge curredge;
    int totalNodes = this.height * this.width;
    while (this.minSpanTree.size() < totalNodes && i < this.edges.size()) {
      curredge = this.edges.get(i);
      if (curredge.to != null && curredge.from != null
          && !this.find(curredge.from.pos).equals(this.find(curredge.to.pos))) {
        this.minSpanTree.add(curredge);
        this.union(this.find(curredge.from.pos), this.find(curredge.to.pos));
      }
      i++;
    }
  }

  // updates the path booleans of the cells after the minimum spanning tree
  // is created
  void updatePaths() {
    for (int i = 0; i < this.minSpanTree.size(); i++) {
      Cell from = this.minSpanTree.get(i).from;
      Cell to = this.minSpanTree.get(i).to;

      if (from.bottomCell != null && from.bottomCell.equals(to)) {
        from.bottom = true;
        to.top = true;
      } else if (from.topCell != null && from.topCell.equals(to)) {
        from.top = true;
        to.bottom = true;
      } else if (from.rightCell != null && from.rightCell.equals(to)) {
        from.right = true;
        to.left = true;
      } else if (from.leftCell != null && from.leftCell.equals(to)) {
        from.left = true;
        to.right = true;
      }
      from.outEdges.add(this.minSpanTree.get(i));
      to.outEdges.add(this.minSpanTree.get(i));
    }
  }

  // draws the maze
  public WorldScene makeScene() {
    WorldScene grid = new WorldScene(750, 750);

    int cellSize = this.cells.get(0).cellSize;

    for (int i = 0; i < this.cells.size(); i++) {
      grid.placeImageXY(this.cells.get(i).drawCell(),
          this.cells.get(i).pos.x * cellSize + (cellSize / 2),
          this.cells.get(i).pos.y * cellSize + (cellSize / 2));
    }
    return grid;
  }

  boolean hasWon() {
    return this.selectedCell == this.cells.get(this.cells.size() - 1);
  }

  public void onKeyEvent(String key) {
    if (key.equals("up") && this.selectedCell.top) {
      this.selectedCell = this.selectedCell.topCell;
      this.selectedCell.color = Color.blue;
      this.selectedCell.bottomCell.color = Color.cyan;
    } else if (key.equals("down") && this.selectedCell.bottom) {
      this.selectedCell = this.selectedCell.bottomCell;
      this.selectedCell.color = Color.blue;
      this.selectedCell.topCell.color = Color.cyan;
    } else if (key.equals("right") && this.selectedCell.right) {
      this.selectedCell = this.selectedCell.rightCell;
      this.selectedCell.color = Color.blue;
      this.selectedCell.leftCell.color = Color.cyan;
    } else if (key.equals("left") && this.selectedCell.left) {
      this.selectedCell = this.selectedCell.leftCell;
      this.selectedCell.color = Color.blue;
      this.selectedCell.rightCell.color = Color.cyan;
    } else if (key.equals("b")) {
      this.answer = bfs(this.cells.get(0), this.cells.get(this.cells.size() - 1));
      this.solveMaze = true;
      this.selectedCell.color = Color.blue;
    } else if (key.equals("d")) {
      this.answer = dfs(this.cells.get(0), this.cells.get(this.cells.size() - 1));
      this.solveMaze = true;
      this.selectedCell.color = Color.blue;
    } else if (key.equals("r")) {
      this.edges = new ArrayList<Edge>();
      this.cells = new ArrayList<Cell>();
      this.rand = new Random();
      this.representatives = new HashMap<Posn, Posn>();
      this.solveMaze = false;
      this.currIndex = 0;
      this.initCells();
      this.initEdges();
      this.initReps();
      this.kruskal();
      this.updatePaths();
    }
  }

  ArrayList<Cell> bfs(Cell from, Cell to) {
    return searchHelp(from, to, new Queue<Cell>());
  }

  ArrayList<Cell> dfs(Cell from, Cell to) {
    return searchHelp(from, to, new Stack<Cell>());
  }

  ArrayList<Cell> searchHelp(Cell from, Cell to, ICollection<Cell> worklist) {
    Deque<Cell> alreadySeen = new Deque<Cell>();
    ArrayList<Cell> answer = new ArrayList<Cell>();
    // Initialize the worklist with the from Cell
    worklist.add(from);
    answer.add(from);
    // As long as the worklist isn't empty...
    while (!worklist.isEmpty()) {
      Cell next = worklist.remove();
      if (next.equals(to)) {
        return answer; // Success!
      } else if (alreadySeen.contains(next)) {
        // do nothing: we've already seen this one
      }
      else{
          // add all the neighbors of next to the worklist for further processing
          for (Edge e : next.outEdges) {
            worklist.add(e.to);
            worklist.add(e.from);
            answer.add(e.to);
            answer.add(e.from);
          }
          // add next to alreadySeen, since we're done with it
          alreadySeen.addAtHead(next);
        }
      }
      // We haven't found the to Cell, and there are no more to try
      return answer;
    }

    public void onTick () {
      if (this.solveMaze && !this.hasWon()) {
        this.selectedCell.color = Color.cyan;
        this.selectedCell = this.answer.get(this.currIndex++);
        this.selectedCell.color = Color.blue;
      }
    }
  }

  //Represents a mutable collection of items
  interface ICollection<T> {

    // Is this collection empty?
    boolean isEmpty();

    // EFFECT: adds the item to the collection
    void add(T item);

    // Returns the first item of the collection
    // EFFECT: removes that first item
    T remove();
  }

  class Stack<T> implements ICollection<T> {

    Deque<T> contents;

    Stack() {
      this.contents = new Deque<T>();
    }

    public boolean isEmpty() {
      return this.contents.size() == 0;
    }

    public T remove() {
      return this.contents.removeFromHead();
    }

    public void add(T item) {
      this.contents.addAtHead(item);
    }
  }

  class Queue<T> implements ICollection<T> {

    Deque<T> contents;

    Queue() {
      this.contents = new Deque<T>();
    }

    public boolean isEmpty() {
      return this.contents.size() == 0;
    }

    public T remove() {
      return this.contents.removeFromHead();
    }

    public void add(T item) {
      this.contents.addAtTail(item); // NOTE: Different from Finished.Stack!
    }
  }

  class Deque<T> {

    Sentinel<T> header;

    // default constructor
    Deque() {
      this.header = new Sentinel<T>();
    }

    // constructor that initializes header
    Deque(Sentinel<T> header) {
      this.header = header;
    }

    // counts the number of nodes in this deque
    int size() {
      return this.header.sizeHelper();
    }

    // add a node in the head of this deque
    void addAtHead(T data) {
      this.header.addAtHeadHelper(data);
    }

    // add a node in the tail of this deque
    void addAtTail(T data) {
      this.header.addAtTailHelper(data);
    }

    // remove the first node of this deque
    T removeFromHead() {
      return this.header.removeFromHeadHelper().getData();
    }

    // remove the LAST node of this deque
    T removeFromTail() {
      return this.header.removeFromTailHelper().getData();
    }

    // returns the find Finished.ANode in the deque satisfying the predicate
    ANode<T> find(Predicate<T> pred) {
      return this.header.findHelper(pred);
    }

    // remove the given node from the deque
    void removeNode(ANode<T> target) {
      this.header.next.removeNodeHelper(target);
    }

    boolean contains(T data) {
      return this.header.containsHelper(data);
    }

  }

  //abstract class with next and prev nodes
  abstract class ANode<T> {

    ANode<T> next;
    ANode<T> prev;

    // counts the number of nodes
    abstract int sizeHelper();

    // helper that counts the number of nodes, return 0 in Finished.Sentinel
    abstract int sizeHelper2();

    // returns the first Finished.ANode that satisfies the predicate
    abstract ANode<T> findHelper(Predicate<T> pred);

    // helper that returns the current Finished.ANode or goes to the next one
    abstract ANode<T> findHelper2(Predicate<T> pred);

    // abstraction of remove method
    abstract ANode<T> removeAbstract();

    // removes the given node from the deque
    abstract void removeNodeHelper(ANode<T> target);

    // returns the data of an Finished.ANode
    abstract T getData();

    abstract boolean containsHelper2(T data);
  }

  //Finished.Sentinel class as the header of nodes
  class Sentinel<T> extends ANode<T> {

    // initialize next and prev nodes to itself
    Sentinel() {
      this.next = this;
      this.prev = this;
    }

    // counts the number of nodes
    int sizeHelper() {
      return this.next.sizeHelper2();
    }

    // return 0 in sentinel class
    int sizeHelper2() {
      return 0;
    }

    // add a node in the head of this deque
    void addAtHeadHelper(T data) {
      this.next = addAbstract(data, this.next, this);
    }

    // add a node in the tail of this deque
    void addAtTailHelper(T data) {
      this.prev = addAbstract(data, this, this.prev);
    }

    // remove the first node of this deque
    ANode<T> removeFromHeadHelper() {
      return this.next.removeAbstract();
    }

    // remove the last node of this deque
    ANode<T> removeFromTailHelper() {
      return this.prev.removeAbstract();
    }

    // throws a Runtime Exception since it's an empty list
    ANode<T> removeAbstract() {
      throw new RuntimeException("No node from Empty List!");
    }

    // calls the helper on the next Finished.ANode the sentinel points to
    ANode<T> findHelper(Predicate<T> pred) {
      return this.next.findHelper2(pred);
    }

    // returns the sentinel since no other Finished.ANode satisfied the predicate
    ANode<T> findHelper2(Predicate<T> pred) {
      return this;
    }

    // does nothing since there is no node to remove
    void removeNodeHelper(ANode<T> target) {
      return;
    }

    // throws a runtime exception since sentinel's have no data
    T getData() {
      throw new RuntimeException("No node from Empty List!");
    }

    // abstraction for adding data to the deque
    ANode<T> addAbstract(T data, ANode<T> next, ANode<T> prev) {
      return new Node<T>(data, next, prev);
    }

    public boolean containsHelper(T data) {
      return this.next.containsHelper2(data);
    }

    @Override
    boolean containsHelper2(T data) {
      return false;
    }
  }

  //Finished.Node class that holds the value of node and next & prev nodes
  class Node<T> extends ANode<T> {

    T data;

    // constructor that initialize data and set next and prev to null
    Node(T data) {
      this.data = data;
      this.next = null;
      this.prev = null;
    }

    // constructor that initialize data next and prev
    Node(T data, ANode<T> next, ANode<T> prev) {
      if (next == null || prev == null) {
        throw new IllegalArgumentException("Parameter node can't be null");
      }
      this.data = data;
      this.next = next;
      this.prev = prev;
      this.prev.next = this;
      this.next.prev = this;
    }

    // counts the number of nodes
    int sizeHelper() {
      return this.sizeHelper2();
    }

    // counts the number of nodes
    int sizeHelper2() {
      return 1 + this.next.sizeHelper2();
    }

    // removes this node by linking the next and prev
    ANode<T> removeAbstract() {
      ANode<T> temp = this;
      this.next.prev = this.prev;
      this.prev.next = this.next;
      return temp;
    }

    // calls the helper on the next Finished.ANode in the deque
    ANode<T> findHelper(Predicate<T> pred) {
      return this.findHelper2(pred);
    }

    // returns this if it satisfies the predicate, else checks the next
    ANode<T> findHelper2(Predicate<T> pred) {
      if (pred.test(this.data)) {
        return this;
      } else {
        return this.next.findHelper2(pred);
      }
    }

    // removes this node if it is the target, else moves to the next
    void removeNodeHelper(ANode<T> target) {
      if (this == target) {
        this.removeAbstract();
      } else {
        this.next.removeNodeHelper(target);
      }
    }

    // returns the data of this node
    T getData() {
      return this.data;
    }

    @Override
    boolean containsHelper2(T data) {
      return (this.data == data || this.next.containsHelper2(data));
    }
  }

  class ExamplesMaze {

    Cell cell1;
    Cell cell2;
    Cell cell3;
    Cell cell4;

    Maze maze1;
    Maze maze2;
    Maze maze3;
    Maze maze4;
    Maze maze5;
    Maze maze6;
    Maze maze7;

    EdgeComparator ec;

    void initData() {
      cell1 = new Cell(2, 2, 50);
      cell2 = new Cell(3, 4, 50);
      this.cell2.top = true;
      cell3 = new Cell(2, 1, 50);
      this.cell3.right = true;
      this.cell3.bottom = true;
      this.cell3.top = true;
      this.maze1 = new Maze(10, 10);
      this.maze2 = new Maze(2, 2, new Random(2));
      this.maze3 = new Maze(2, 3, new Random(999));
      this.maze4 = new Maze(1, 2, new Random(27));
      this.maze5 = new Maze(1, 1);
      this.maze6 = new Maze(2, 1, new Random(27));
      this.maze7 = new Maze(50, 30);
      ec = new EdgeComparator();
    }

    boolean testInitCell(Tester t) {
      this.initData();
      return t.checkExpect(this.maze1.cells.size(), 100)
          && t.checkExpect(this.maze2.cells.size(), 4)
          && t.checkExpect(this.maze3.cells.size(), 6);
    }

    boolean testDrawCell(Tester t) {
      this.initData();
      WorldImage cell1Image = new RectangleImage(cell1.cellSize, cell1.cellSize,
          OutlineMode.SOLID, this.cell1.color);
      cell1Image = new AboveImage(new LineImage(new Posn(cell1.cellSize, 0), Color.black),
          cell1Image);
      cell1Image = new AboveImage(cell1Image, new LineImage(new Posn(cell1.cellSize, 0),
          Color.black));
      cell1Image = new BesideImage(cell1Image, new LineImage(new Posn(0, cell1.cellSize),
          Color.black));
      cell1Image = new BesideImage(new LineImage(new Posn(0, cell1.cellSize), Color.black),
          cell1Image);

      WorldImage cell2Image = new RectangleImage(cell2.cellSize, cell2.cellSize,
          OutlineMode.SOLID, this.cell2.color);
      cell2Image = new AboveImage(cell2Image, new LineImage(new Posn(cell2.cellSize, 0),
          Color.black));
      cell2Image = new BesideImage(cell2Image, new LineImage(new Posn(0, cell2.cellSize),
          Color.black));
      cell2Image = new BesideImage(new LineImage(new Posn(0, cell2.cellSize), Color.black),
          cell2Image);

      WorldImage cell3Image = new RectangleImage(cell3.cellSize, cell3.cellSize,
          OutlineMode.SOLID, this.cell3.color);
      cell3Image = new BesideImage(new LineImage(new Posn(0, cell3.cellSize), Color.black),
          cell3Image);
      return t.checkExpect(this.cell3.drawCell(), cell3Image);
    }

    boolean testInitEdges(Tester t) {
      this.initData();
      this.maze2.edges = new ArrayList<Edge>();
      this.maze2.initEdges();
      this.maze3.edges = new ArrayList<Edge>();
      this.maze3.initEdges();
      return t.checkExpect(this.maze2.edges.get(0).from, this.maze2.cells.get(0))
          && t.checkExpect(this.maze2.edges.get(0).to, this.maze2.cells.get(1))
          && t.checkExpect(this.maze3.edges.get(3).from, this.maze3.cells.get(2))
          && t.checkExpect(this.maze3.edges.get(4).from, this.maze3.cells.get(2))
          && t.checkExpect(this.maze3.edges.get(4).to, this.maze3.cells.get(4))
          && t.checkExpect(this.maze3.edges.get(3).to, this.maze3.cells.get(3))
          && t.checkExpect(this.maze3.edges.get(5).from, this.maze3.cells.get(3))
          && t.checkExpect(this.maze3.edges.get(5).to, this.maze3.cells.get(5));
    }

    boolean testEdgeComparator(Tester t) {
      this.initData();
      return t.checkExpect(this.ec.compare(this.maze2.edges.get(0),
          this.maze2.edges.get(1)), -1)
          && t.checkExpect(this.ec.compare(this.maze3.edges.get(2),
          this.maze3.edges.get(1)), 1)
          && t.checkExpect(this.ec.compare(this.maze3.edges.get(3),
          this.maze3.edges.get(4)), -1);
    }

    boolean testUnion(Tester t) {
      this.initData();
      this.maze2.initReps();
      HashMap<Posn, Posn> representative2 = new HashMap<Posn, Posn>(this.maze2.representatives);
      representative2.remove(this.maze2.cells.get(0).pos);
      this.maze2.union(this.maze2.cells.get(0).pos, this.maze2.cells.get(1).pos);
      representative2.put(this.maze2.cells.get(0).pos, this.maze2.cells.get(1).pos);

      HashMap<Posn, Posn> representative3 = new HashMap<Posn, Posn>(this.maze3.representatives);
      representative3.remove(this.maze3.cells.get(3).pos);
      this.maze3.union(this.maze3.cells.get(3).pos, this.maze3.cells.get(5).pos);
      representative3.put(this.maze3.cells.get(3).pos, this.maze3.cells.get(2).pos);
      return t.checkExpect(this.maze2.representatives, representative2)
          && t.checkExpect(this.maze3.representatives, representative3);
    }

    boolean testInitReps(Tester t) {
      this.initData();
      this.maze1.initReps();
      this.maze2.initReps();
      this.maze3.initReps();
      return
          t.checkExpect(this.maze1.find(this.maze1.cells.get(0).pos), this.maze1.cells.get(0).pos)
              && t.checkExpect(this.maze2.find(this.maze2.cells.get(3).pos),
              this.maze2.cells.get(3).pos)
              && t.checkExpect(this.maze3.find(this.maze3.cells.get(5).pos),
              this.maze3.cells.get(5).pos);
    }

    boolean testFind(Tester t) {
      this.initData();
      return
          t.checkExpect(this.maze2.find(this.maze2.cells.get(1).pos), this.maze2.cells.get(3).pos)
              && t.checkExpect(this.maze3.find(this.maze3.cells.get(4).pos),
              this.maze3.cells.get(5).pos)
              && t.checkExpect(this.maze3.find(this.maze3.cells.get(5).pos),
              this.maze3.cells.get(5).pos);
    }

    boolean testKruskal(Tester t) {
      this.initData();
      ArrayList<Edge> maze4mst = new ArrayList<Edge>();
      Cell maze4mstCell1 = new Cell(0, 0, 375);
      maze4mstCell1.bottom = true;
      Cell maze4mstCell2 = new Cell(0, 1, 375);
      maze4mstCell2.top = true;
      maze4mstCell1.bottomCell = maze4mstCell2;
      maze4mstCell2.topCell = maze4mstCell1;
      Edge maze4mstEdge = new Edge(false, maze4mstCell1, maze4mstCell2, -1152021836);
      maze4mstCell1.outEdges.add(maze4mstEdge);
      maze4mstCell2.outEdges.add(maze4mstEdge);
      maze4mst.add(maze4mstEdge);

      ArrayList<Edge> maze6mst = new ArrayList<Edge>();
      Cell maze6mstCell1 = new Cell(0, 0, 375);
      maze6mstCell1.right = true;
      Cell maze6mstCell2 = new Cell(1, 0, 375);
      maze6mstCell2.left = true;
      maze6mstCell1.rightCell = maze6mstCell2;
      maze6mstCell2.leftCell = maze6mstCell1;
      Edge maze6mstEdge = new Edge(true, maze6mstCell1, maze6mstCell2, -1152021836);
      maze6mstCell1.outEdges.add(maze6mstEdge);
      maze6mstCell2.outEdges.add(maze6mstEdge);
      maze6mst.add(maze6mstEdge);
      return t.checkExpect(this.maze5.minSpanTree, new ArrayList<Edge>())
          && t.checkExpect(this.maze4.minSpanTree, maze4mst)
          && t.checkExpect(this.maze6.minSpanTree, maze6mst);
    }

    boolean testUpdatePaths(Tester t) {
      this.initData();
      return t.checkExpect(this.maze2.minSpanTree.get(0).from.bottom, false)
          && t.checkExpect(this.maze2.minSpanTree.get(1).to.left, true)
          && t.checkExpect(this.maze2.minSpanTree.get(2).from.right, true)
          && t.checkExpect(this.maze3.minSpanTree.get(3).from.bottom, true)
          && t.checkExpect(this.maze3.minSpanTree.get(3).to.top, true)
          && t.checkExpect(this.maze3.minSpanTree.get(4).from.left, false);
    }

    boolean testMakeScene(Tester t) {
      this.initData();
      WorldScene maze4image = new WorldScene(750, 750);
      this.maze4.cells.get(0).color = Color.green;
      maze4image.placeImageXY(this.maze4.cells.get(0).drawCell(), 187, 187);
      this.maze4.cells.get(1).color = Color.pink;
      maze4image.placeImageXY(this.maze4.cells.get(1).drawCell(), 187, 562);
      WorldScene maze5image = new WorldScene(750, 750);
      this.maze5.cells.get(0).color = Color.pink;
      maze5image.placeImageXY(this.maze5.cells.get(0).drawCell(), 375, 375);
      WorldScene maze6image = new WorldScene(750, 750);
      this.maze6.cells.get(0).color = Color.green;
      maze6image.placeImageXY(this.maze6.cells.get(0).drawCell(), 187, 187);
      this.maze6.cells.get(1).color = Color.pink;
      maze6image.placeImageXY(this.maze6.cells.get(1).drawCell(), 562, 187);
      return t.checkExpect(this.maze4.makeScene(), maze4image)
          && t.checkExpect(this.maze5.makeScene(), maze5image)
          && t.checkExpect(this.maze6.makeScene(), maze6image);
    }

    void testBigBang(Tester t) {
      initData();
      this.maze7.bigBang(750, 750, 0.025);
    }
}