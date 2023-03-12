package com.vranec;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EqualsAndHashCode(of = {"x", "y"})
@AllArgsConstructor
public class MazeBlock {

    private static final Random RANDOM = new Random();

    @Getter
    private final int x;
    @Getter
    private final int y;
    private MazeBlockType type;
    private final List<MazeBlock> neighbours = new ArrayList<>();

    public boolean isSolidWall() {
        return type == MazeBlockType.SOLID_WALL;
    }

    public boolean isEmpty() {
        return type == MazeBlockType.EMPTY;
    }

    public boolean isFinishBlock() {
        return type == MazeBlockType.FINISH_BLOCK;
    }

    public void changeTypeTo(MazeBlockType type) {
        this.type = type;
    }

    public void addNeighbour(MazeBlock mazeBlock) {
        if (mazeBlock != null) {
            neighbours.add(mazeBlock);
        }
    }

    public MazeBlock getRandomNeighbour() {
        return neighbours.get(RANDOM.nextInt(neighbours.size()));
    }
}
