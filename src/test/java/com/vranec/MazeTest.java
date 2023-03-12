package com.vranec;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class MazeTest {

    private final Maze maze = new Maze(5);

    @Test
    void mazeIsSurroundedBySolidBlocks() {
        for (int column = 0; column < maze.getSize(); column++) {
            assertThat(maze.getBlockAt(column, 0).isSolidWall()).isTrue();
            assertThat(maze.getBlockAt(column, maze.getSize() + 1).isSolidWall()).isTrue();
        }

        for (int row = 0; row < maze.getSize(); row++) {
            assertThat(maze.getBlockAt(0, row).isSolidWall()).isTrue();
            assertThat(maze.getBlockAt(maze.getSize() + 1, row).isSolidWall()).isTrue();
        }
    }

    @Test
    void playerBlockInUpperLeftCornerIsEmpty() {
        assertThat(maze.getBlockAt(1, 1).isEmpty()).isTrue();
    }

    @Test
    void finishBlockIsInLowerRightCorner() {
        assertThat(maze.getBlockAt(maze.getSize(), maze.getSize()).isFinishBlock()).isTrue();
    }

    @Test
    void thereAreSomeMazeBlocksGenerated() {
        int numberOfEmptyBlocks = 0;
        for (int column = 0; column <= maze.getSize(); column++) {
            for (int row = 0; row <= maze.getSize(); row++) {
                if (maze.getBlockAt(column, row).isEmpty()) {
                    numberOfEmptyBlocks++;
                }
            }
        }
        assertThat(numberOfEmptyBlocks).isGreaterThan(5);
        assertThat(numberOfEmptyBlocks).isLessThan(maze.getSize() * maze.getSize() / 4 * 3);
    }
}
