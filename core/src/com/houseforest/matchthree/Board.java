package com.houseforest.matchthree;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.awt.Point;
import java.util.Random;
import java.util.Vector;

import javafx.util.Pair;

/**
 * Created by Tom on 13.01.2017.
 */

public class Board extends SceneNode {

    private Sprite backgroundSprite;
    private Piece[][] pieces;
    private Point pieceCount;
    private Point sizeInPixels;
    private Point offset;
    private Point margin;
    private Point touchPosition;
    private boolean dragProcessed;

    public Board(Game game) {
        super(game);

        pieceCount = new Point(8, 8);

        sizeInPixels = new Point(
                Math.round(Game.RESOLUTION.y * 0.9f),
                Math.round(Game.RESOLUTION.y * 0.9f)
        );

        offset = new Point(
                (Game.RESOLUTION.x - sizeInPixels.x) / 2,
                (Game.RESOLUTION.y - sizeInPixels.y) / 2
        );

        margin = new Point(18, 18);

        Random random = new Random();
        this.pieces = new Piece[pieceCount.x][pieceCount.y];
        for (int x = 0; x < pieceCount.x; ++x) {
            for (int y = 0; y < pieceCount.y; ++y) {
                this.pieces[x][y] = Piece.random(game, this, x, y);
            }
        }

        TextureAtlas uiAtlas = game.getTextureAtlas(Game.TextureAtlasName.UI);
        this.backgroundSprite = uiAtlas.createSprite("BackgroundBox", 1);
        this.backgroundSprite.setPosition(offset.x, offset.y);
        this.backgroundSprite.setSize(sizeInPixels.x, sizeInPixels.y);
    }

    private Point toBoardSpace(Point screenPoint) {
        final int areaX = sizeInPixels.x - 2 * margin.x;
        final int areaY = sizeInPixels.y - 2 * margin.y;
        final int boardX = screenPoint.x - offset.x - margin.x;
        final int boardY = screenPoint.y - offset.y - margin.y;
        final int pieceAreaX = areaX / pieceCount.x;
        final int pieceAreaY = areaY / pieceCount.y;

        if (boardX >= 0 && boardX < areaX && boardY >= 0 && boardY < areaY) {
            return new Point(
                    boardX / pieceAreaX,
                    boardY / pieceAreaY
            );
        } else {
            return null;
        }
    }

    public void onDrag(int screenX, int screenY) {
        if (!dragProcessed) {
            Point dragPosition = toBoardSpace(new Point(screenX, screenY));
            if (dragPosition == null) {
                // Dragged off the board.
                dragProcessed = true;
            }

            // Potential horizontal move.
            else if (dragPosition.y == touchPosition.y) {
                if (dragPosition.x == touchPosition.x - 1 || dragPosition.x == touchPosition.x + 1) {
                    dragProcessed = true;
                    swapPieces(touchPosition, dragPosition);
                }
            }

            // Potential vertical move.
            else if (dragPosition.x == touchPosition.x) {
                if (dragPosition.y == touchPosition.y - 1 || dragPosition.y == touchPosition.y + 1) {
                    dragProcessed = true;
                    swapPieces(touchPosition, dragPosition);
                }
            }
        }
    }

    public void onTouch(int screenX, int screenY) {
        Point pt = toBoardSpace(new Point(screenX, screenY));
        if (pt != null) {
            touchPosition = pt;
            dragProcessed = false;
        }
    }

    private void swapPieces(Point firstPosition, Point secondPosition) {
        Piece first = pieces[firstPosition.x][firstPosition.y];
        pieces[firstPosition.x][firstPosition.y] = pieces[secondPosition.x][secondPosition.y];
        pieces[secondPosition.x][secondPosition.y] = first;

        pieces[firstPosition.x][firstPosition.y].updatePosition(firstPosition);
        pieces[secondPosition.x][secondPosition.y].updatePosition(secondPosition);

        for(Pair<Point, Point> match : checkForMatches()) {
            handleMatch(match.getKey(), match.getValue());
        }
    }

    private Vector<Pair<Point, Point>> checkForMatches() {

        Vector<Pair<Point, Point>> matches = new Vector<Pair<Point, Point>>();

        // Check for horizontal matches.
        for (int y = 0; y < pieceCount.y; ++y) {
            for (int x0 = 0; x0 < pieceCount.x - 2; ++x0) {
                Piece.Variant v = getPieceAt(x0, y).getVariant();

                int x1 = x0 + 1;
                for (; x1 < pieceCount.x; ++x1) {
                    if (getPieceAt(x1, y).getVariant() != v) {
                        break;
                    }
                }

                // Matching [x0, x1).
                int matchedCount = x1 - x0;
                if (matchedCount >= 3) {
                    matches.add(new Pair<Point, Point>(new Point(x0, y), new Point(x1 - 1, y)));
                }

                // Skip behind the matched segment.
                x0 += matchedCount - 1;
            }
        }

        // Check for vertical matches.
        for (int x = 0; x < pieceCount.x; ++x) {
            for (int y0 = 0; y0 < pieceCount.y - 2; ++y0) {
                Piece.Variant v = getPieceAt(x, y0).getVariant();

                int y1 = y0 + 1;
                for (; y1 < pieceCount.y; ++y1) {
                    if (getPieceAt(x, y1).getVariant() != v) {
                        break;
                    }
                }

                // Matching [y0, y1).
                int matchedCount = y1 - y0;
                if (matchedCount >= 3) {
                    matches.add(new Pair<Point, Point>(new Point(x, y0), new Point(x, y1 - 1)));
                }

                // Skip below the matched segment.
                y0 += matchedCount - 1;
            }
        }

        return matches;
    }

    private void handleMatch(Point start, Point end) {
        for (int y = start.y; y <= end.y; ++y) {
            for (int x = start.x; x <= end.x; ++x) {
                setPieceAt(x, y, Piece.random(getGame(), this, x, y));
            }
        }
    }

    @Override
    public void update(float dt) {
        for (Piece[] row : pieces) {
            for (Piece piece : row) {
                if (piece != null) {
                    piece.update(dt);
                }
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        backgroundSprite.draw(batch);
        for (Piece[] row : pieces) {
            for (Piece piece : row) {
                if (piece != null) {
                    piece.draw(batch);
                }
            }
        }
    }

    public Point getPieceCount() {
        return pieceCount;
    }

    public Piece getPieceAt(int x, int y) {
        return pieces[x][y];
    }

    public void setPieceAt(int x, int y, Piece piece) {
        pieces[x][y] = piece;
    }

    public Point getOffset() {
        return offset;
    }

    public Point getMargin() {
        return margin;
    }

    public Point getSizeInPixels() {
        return sizeInPixels;
    }
}
