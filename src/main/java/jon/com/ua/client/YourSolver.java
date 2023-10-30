package jon.com.ua.client;

import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import jon.com.ua.view.View;

import java.util.*;

/**
 * User: your name
 */
public class YourSolver implements Solver<Board> {
    private Dice dice;
    private Board board;
    private List<Point> path;

    public YourSolver(Dice dice) {
        this.dice = dice;
        this.path = new ArrayList<>();
    }

    @Override
    public String get(Board board) {
        if (board.isGameOver()) {
            return Direction.UP.toString();
        }
        Vertex[][] verticesMatrix = createMatrix(board, board.getSnake().size() > 49, false);
        Vertex apple = getAppleVertex(verticesMatrix, board);
        Vertex head = getHeadVertex(verticesMatrix, board);
        Vertex stone = getStoneVertex(verticesMatrix, board);
        Point stonePoint = board.getStones().get(0);
        if (head.edges.size() == 1 && head.edges.get(0).target().point.equals(board.getStones().get(0))){
            verticesMatrix = createMatrix(board,true,false);
        }
        if (apple.edges.size() == 1) {
            if (apple.point.getX() == 1 && apple.point.getY() == 1 && (stonePoint.getX() == 1 && stonePoint.getY() == 2) || (stonePoint.getX() == 2 && stonePoint.getY() == 1)) {
                verticesMatrix = createMatrix(board, true, false);
            }
            if (apple.point.getX() == 1 && apple.point.getY() == verticesMatrix.length - 1 && (stonePoint.getX() == 1 && stonePoint.getY() == verticesMatrix.length - 2) || (stonePoint.getX() == 2 && stonePoint.getY() == verticesMatrix.length - 1)) {
                verticesMatrix = createMatrix(board, true, false);
            }
            if (apple.point.getX() == verticesMatrix[0].length - 1 && apple.point.getY() == 1 && (stonePoint.getX() == verticesMatrix.length - 2 && stonePoint.getY() == 1) || (stonePoint.getX() == verticesMatrix.length - 1 && stonePoint.getY() == 2)) {
                verticesMatrix = createMatrix(board, true, false);
            }
            if (apple.point.getX() == verticesMatrix[0].length - 1 && apple.point.getY() == verticesMatrix.length - 1 && (stonePoint.getX() == verticesMatrix.length - 2 && stonePoint.getY() == verticesMatrix[0].length - 1) || (stonePoint.getX() == verticesMatrix.length - 1 && stonePoint.getY() == verticesMatrix[0].length - 1)) {
                verticesMatrix = createMatrix(board, true, false);
            }
        }
        Stack<Vertex> stack = generateShortestPath(apple);
        if (countSizeOfTunnel(apple, board, verticesMatrix) < board.getSnake().size()) {
            verticesMatrix = createMatrix(board,board.getSnake().size() > 49,true);
            if (!isInTunnel(verticesMatrix, board, apple)){
                stack.clear();
            } else {
                if (!stack.isEmpty()){
                    return head.point.direction(stack.pop().point).toString();
                }
            }
        }

        if (board.getSnake().size() > 50
                && countSizeOfTunnel(stone, board, verticesMatrix) > board.getSnake().size()
                && generateShortestPath(getStoneVertex(verticesMatrix, board)).size() > generateShortestPath(apple).size()
        ) {
            stack = generateShortestPath(getStoneVertex(verticesMatrix, board));
            if (!stack.isEmpty()){
                return head.point.direction(stack.pop().point).toString();
            }
        }


        if (stack.isEmpty()) {
            verticesMatrix = createMatrix(board, true, true);
            if (head.edges.isEmpty() && generateShortestPath(verticesMatrix[getTailPoint(board).getX()][getTailPoint(board).getY()]).isEmpty()) {
                return Direction.UP.toString();
            }
            if (!generateShortestPath(verticesMatrix[getTailPoint(board).getX()][getTailPoint(board).getY()]).isEmpty()) {
                stack = generateShortestPath(verticesMatrix[getTailPoint(board).getX()][getTailPoint(board).getY()]);
            } else if (head.edges.size() == 2) {
                int sizeFirst = countSizeOfTunnel(head.edges.get(0).target(), board, verticesMatrix);
                int sizeSecond = countSizeOfTunnel(head.edges.get(1).target(), board, verticesMatrix);
                stack = generateShortestPath(sizeFirst > sizeSecond ? head.edges.get(0).target() : head.edges.get(1).target());
            } else if (head.edges.size() == 3){
                int sizeFirst = countSizeOfTunnel(head.edges.get(0).target(), board, verticesMatrix);
                int sizeSecond = countSizeOfTunnel(head.edges.get(1).target(), board, verticesMatrix);
                int sizeThird = countSizeOfTunnel(head.edges.get(2).target(), board, verticesMatrix);
                stack = generateShortestPath(sizeFirst > sizeSecond ? head.edges.get(0).target() : sizeSecond > sizeThird ? head.edges.get(1).target() : head.edges.get(2).target());
            }
            else {
                stack = generateShortestPath(head.edges.get(0).target());
            }
        }

        return head.point.direction(stack.pop().point).toString();
    }

    public Vertex[][] createMatrix(Board board, boolean withBadApple, boolean withTail) {
        List<Point> emptyPoints = board.get(Elements.NONE);
        Point head = board.getHead();
        emptyPoints.add(head);
        emptyPoints.add(board.getApples().get(0));
        if (withBadApple) {
            emptyPoints.add(board.getStones().get(0));
        }
        if (withTail) {
            emptyPoints.add(getTailPoint(board));
        }
        Vertex[][] verticesMatrix = new Vertex[board.size()][board.size()];
        for (Point point : emptyPoints) {
            verticesMatrix[point.getX()][point.getY()] = new Vertex(point);
        }
        for (int i = 1; i < verticesMatrix.length - 1; i++) {
            for (int j = 1; j < verticesMatrix.length - 1; j++) {
                if (verticesMatrix[i][j] == null) {
                    continue;
                }
                if (verticesMatrix[i][j - 1] != null) {
                    verticesMatrix[i][j].edges.add(new Edge(verticesMatrix[i][j - 1], 14));
                }
                if (verticesMatrix[i][j + 1] != null) {
                    verticesMatrix[i][j].edges.add(new Edge(verticesMatrix[i][j + 1], 14));
                }
                if (verticesMatrix[i - 1][j] != null) {
                    verticesMatrix[i][j].edges.add(new Edge(verticesMatrix[i - 1][j], 10));
                }
                if (verticesMatrix[i + 1][j] != null) {
                    verticesMatrix[i][j].edges.add(new Edge(verticesMatrix[i + 1][j], 10));
                }
            }
        }
        calculateDijkstra(verticesMatrix[head.getX()][head.getY()]);
        return verticesMatrix;
    }

    public void calculateDijkstra(Vertex source) {
        source.minDistance = 0;
        PriorityQueue<Vertex> queue = new PriorityQueue<>();
        queue.offer(source);
        while (!queue.isEmpty()) {
            Vertex current = queue.poll();
            for (Edge edge : current.edges) {
                Vertex neighbor = edge.target();
                double distance = current.minDistance + edge.weight();
                if (distance < neighbor.minDistance) {
                    neighbor.minDistance = distance;
                    neighbor.previous = current;
                    queue.offer(neighbor);
                }
            }
        }
    }


    public Vertex getAppleVertex(Vertex[][] verticesMatrix, Board board) {
        Point apple = board.getApples().get(0);
        return verticesMatrix[apple.getX()][apple.getY()];
    }

    public Vertex getStoneVertex(Vertex[][] verticesMatrix, Board board) {
        Point stone = board.getStones().get(0);
        return verticesMatrix[stone.getX()][stone.getY()];
    }

    public Vertex getHeadVertex(Vertex[][] verticesMatrix, Board board) {
        Point head = board.getHead();
        return verticesMatrix[head.getX()][head.getY()];
    }

    public Point getTailPoint(Board board) {
        List<Point> tail1 = board.get(Elements.TAIL_END_DOWN);
        List<Point> tail2 = board.get(Elements.TAIL_END_LEFT);
        List<Point> tail3 = board.get(Elements.TAIL_END_UP);
        List<Point> tail4 = board.get(Elements.TAIL_END_RIGHT);
        if (!tail1.isEmpty() && tail1.get(0) != null) {
            return tail1.get(0);
        }
        if (!tail2.isEmpty() && tail2.get(0) != null) {
            return tail2.get(0);
        }
        if (!tail3.isEmpty() && tail3.get(0) != null) {
            return tail3.get(0);
        }
        if (!tail4.isEmpty() && tail4.get(0) != null) {
            return tail4.get(0);
        }


        return null;
    }


    public Stack<Vertex> generateShortestPath(Vertex apple) {
        Stack<Vertex> stack = new Stack<>();
        for (Vertex dest = apple; dest.previous != null; dest = dest.previous) {
            stack.push(dest);
        }
        return stack;
    }

    public int countSizeOfTunnel(Vertex entry, Board board, Vertex[][] vertices) {
        setFalseToVisitedForAll(vertices);
        entry = vertices[entry.point.getX()][entry.point.getY()];
        getHeadVertex(vertices, board).visited = true;
        System.out.println("ENTRY " + entry);
        int count = 0;
        Queue<Vertex> queue = new LinkedList<>();
        queue.offer(entry);
        count++;
        while (!queue.isEmpty()) {
            Vertex vertex = queue.poll();
            for (Edge neighbor : vertex.edges) {
                if (neighbor.target().visited) {
                    continue;
                }
                neighbor.target().visited = true;
                queue.offer(neighbor.target());
                count++;
            }
        }
        System.out.println("SIZE OF THE TUNNEL" + count);
        return count;
    }

    public boolean isInTunnel(Vertex[][] verticesMatrix, Board board, Vertex entry) {
        setFalseToVisitedForAll(verticesMatrix);
        entry = verticesMatrix[entry.point.getX()][entry.point.getY()];
        Vertex tail = verticesMatrix[getTailPoint(board).getX()][getTailPoint(board).getY()];
        if (entry.equals(tail)) {
            return true;
        }
        getHeadVertex(verticesMatrix, board).visited = true;
        Queue<Vertex> queue = new LinkedList<>();
        queue.offer(entry);
        while (!queue.isEmpty()) {
            Vertex vertex = queue.poll();
            if (vertex.equals(tail)){
                return true;
            }
            for (Edge neighbor : vertex.edges) {
                if (neighbor.target().visited) {
                    continue;
                }
                neighbor.target().visited = true;
                queue.offer(neighbor.target());
            }
        }
        return false;
    }


    public void setFalseToVisitedForAll(Vertex[][] vertices) {
        for (int i = 0; i < vertices.length; i++) {
            for (int j = 0; j < vertices[0].length; j++) {
                if (vertices[i][j] == null) continue;
                vertices[i][j].visited = false;
            }
        }
    }

    public List<Point> getPath() {
        return path;
    }

    public static void main(String[] args) {
        View.runClient(
                new YourSolver(null),
                new Board());
    }
}
