package com.houseforest.matchthree;

import java.awt.Point;

/**
 * Created by Tom on 13.01.2017.
 */

public class Match {

    private boolean horizontal;
    private int length;
    private Vector2i start;
    private Vector2i end;

    public Match(Vector2i start, Vector2i end) {
        this.start = start;
        this.end = end;
        this.horizontal = end.x != start.x;
        this.length = horizontal ? (end.x - start.x + 1) : (end.y - start.y + 1);
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public int getLength() {
        return length;
    }

    public Vector2i getStart() {
         return start;
    }

    public Vector2i getEnd() {
        return end;
    }
}
