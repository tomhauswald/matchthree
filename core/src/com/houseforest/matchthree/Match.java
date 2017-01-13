package com.houseforest.matchthree;

import java.awt.Point;

/**
 * Created by Tom on 13.01.2017.
 */

public class Match {

    private boolean horizontal;
    private int length;
    private Point start;
    private Point end;

    public Match(Point start, Point end) {
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

    public Point getStart() {
         return start;
    }

    public Point getEnd() {
        return end;
    }
}
