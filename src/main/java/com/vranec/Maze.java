package com.vranec;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.vranec.MazeBlockType.EMPTY;
import static com.vranec.MazeBlockType.FINISH_BLOCK;

public class Maze {

    private final List<List<MazeBlock>> blocks = new ArrayList<>();
    @Getter
    private final int size;

    public Maze(int size) {
        this.size = size;

        validateSize();
        fillMazeWithWalls();
        setNeigboursToBlocks();
        generateMazeRoads();
        addFinishBlock();
    }

    private void generateMazeRoads() {
        MazeBlock currentMazeBlock = chooseOneBlockAndAddItToMaze();
        int remainingMazeRoadToGenerate = (size / 2 + 1) * (size / 2 + 1) - 1;

        while (remainingMazeRoadToGenerate > 0) {
            var nextMazeBlock = currentMazeBlock.getRandomNeighbour();
            if (nextMazeBlock.isSolidWall()) {
                nextMazeBlock.changeTypeTo(EMPTY);
                getBlockBetween(currentMazeBlock, nextMazeBlock).changeTypeTo(EMPTY);
                remainingMazeRoadToGenerate--;
            }
            currentMazeBlock = nextMazeBlock;
        }
    }

    private MazeBlock chooseOneBlockAndAddItToMaze() {
        int middle = size / 2;
        if (middle % 2 == 0) {
            middle++;
        }
        var currentMazeBlock = blocks.get(middle).get(middle);
        currentMazeBlock.changeTypeTo(EMPTY);
        return currentMazeBlock;
    }

    private void setNeigboursToBlocks() {
        for (int column = 1; column <= size; column++) {
            for (int row = 1; row <= size; row++) {
                if (column % 2 == 1 && row % 2 == 1) {
                    MazeBlock block = blocks.get(column).get(row);
                    block.addNeighbour(getBlockAt(column - 2, row));
                    block.addNeighbour(getBlockAt(column + 2, row));
                    block.addNeighbour(getBlockAt(column, row - 2));
                    block.addNeighbour(getBlockAt(column, row + 2));
                }
            }
        }
    }

    private void validateSize() {
        if (size % 2 != 1) {
            throw new IllegalArgumentException("Maze size must be odd number");
        }
    }

    private MazeBlock getBlockBetween(MazeBlock a, MazeBlock b) {
        return blocks.get((a.getX() + b.getX()) / 2).get((a.getY() + b.getY()) / 2);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int column = 0; column <= size + 1; column++) {
            for (int row = 0; row <= size + 1; row++) {
                result.append(blocks.get(column).get(row).isSolidWall() ? "X" : " ");
            }
            result.append("\n");
        }
        return result.toString();
    }

    private void fillMazeWithWalls() {
        for (int column = 0; column <= size + 1; column++) {
            ArrayList<MazeBlock> currentColumn = new ArrayList<>();
            blocks.add(currentColumn);

            for (int row = 0; row <= size + 1; row++) {
                currentColumn.add(new MazeBlock(column, row, MazeBlockType.SOLID_WALL));
            }
        }
    }

    public MazeBlock getBlockAt(int column, int row) {
        if (column < 0) {
            return null;
        }
        if (column > size + 1) {
            return null;
        }

        if (row < 0) {
            return null;
        }
        if (row > size + 1) {
            return null;
        }

        return blocks.get(column).get(row);
    }

    private void addFinishBlock() {
        blocks.get(size).get(size).changeTypeTo(FINISH_BLOCK);
    }
}
