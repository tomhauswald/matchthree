package com.houseforest.matchthree;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;

import java.util.Random;
import java.util.Vector;

import javafx.util.Pair;

/**
 * Created by Tom on 13.01.2017.
 */

public class Board extends SceneNode implements Disposable {

    private Texture backgroundTexture;
    private Piece[][] pieces;
    private Vector2i pieceCount;
    private Vector2i sizeInPixels;
    private Vector2i offset;
    private Vector2i margin;
    private Vector2i touchPosition;
    private boolean dragProcessed;

    public Board(Game game) {
        super(game);

        pieceCount = new Vector2i(8, 8);

        sizeInPixels = new Vector2i(
                Math.round(Game.RESOLUTION.y * 0.9f),
                Math.round(Game.RESOLUTION.y * 0.9f)
        );

        offset = new Vector2i(
                (Game.RESOLUTION.x - sizeInPixels.x) / 2,
                (Game.RESOLUTION.y - sizeInPixels.y) / 2
        );

        margin = new Vector2i(18, 18);

        this.pieces = new Piece[pieceCount.x][pieceCount.y];
        for (int x = 0; x < pieceCount.x; ++x) {
            for (int y = 0; y < pieceCount.y; ++y) {
                this.pieces[x][y] = Piece.random(game, this, x, y);
            }
        }

        this.backgroundTexture = new Texture(Gdx.files.internal("board.png"));
    }

    private Vector2i toBoardSpace(Vector2i screenPoint) {
        final int areaX = sizeInPixels.x - 2 * margin.x;
        final int areaY = sizeInPixels.y - 2 * margin.y;
        final int boardX = screenPoint.x - offset.x - margin.x;
        final int boardY = screenPoint.y - offset.y - margin.y;
        final int pieceAreaX = areaX / pieceCount.x;
        final int pieceAreaY = areaY / pieceCount.y;

        if (boardX >= 0 && boardX < areaX && boardY >= 0 && boardY < areaY) {
            return new Vector2i(
                    boardX / pieceAreaX,
                    boardY / pieceAreaY
            );
        } else {
            return null;
        }
    }

    public void onDrag(int screenX, int screenY) {
        if (!dragProcessed) {
            Vector2i dragPosition = toBoardSpace(new Vector2i(screenX, screenY));
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
        Vector2i pt = toBoardSpace(new Vector2i(screenX, screenY));
        if (pt != null) {
            touchPosition = pt;
            dragProcessed = false;
        }
    }

    private void swapPieces(Vector2i firstPosition, Vector2i secondPosition) {
        Piece first = pieces[firstPosition.x][firstPosition.y];
        pieces[firstPosition.x][firstPosition.y] = pieces[secondPosition.x][secondPosition.y];
        pieces[secondPosition.x][secondPosition.y] = first;

        pieces[firstPosition.x][firstPosition.y].updatePosition(firstPosition);
        pieces[secondPosition.x][secondPosition.y].updatePosition(secondPosition);

        Match match;
        while((match = findMatch()) != null) {
            Game.log("Found match from " + match.getStart().toString() + " to " + match.getEnd().toString());
            handleMatch(match);
        }
    }

    private Match findMatch() {
        // Check for horizontal match.
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
                    return new Match(new Vector2i(x0, y), new Vector2i(x1 - 1, y));
                }
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
                    return new Match(new Vector2i(x, y0), new Vector2i(x, y1 - 1));
                }
            }
        }

        return null;
    }

    private void handleMatch(Match match) {
        if(match.isHorizontal()) {
            int y = match.getStart().y;
            for (int x = match.getStart().x; x <= match.getEnd().x; ++x) {
                for (int yy = y; yy > 0; --yy) {
                    // Move pieces down.
                    setPieceAt(x, yy, getPieceAt(x, yy - 1));
                }

                // Generate new piece at top.
                setPieceAt(x, 0, Piece.random(getGame(), this, x, 0));
            }
        }

        // Vertical match
        else {
            int x = match.getStart().x;
            for (int y = match.getEnd().y; y >= match.getLength(); --y) {
                // Move pieces down.
                setPieceAt(x, y, getPieceAt(x, y - match.getLength()));
            }

            for(int y = 0; y<match.getLength(); ++y) {
                // Generate new pieces above.
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
        batch.draw(backgroundTexture, offset.x, offset.y, sizeInPixels.x, sizeInPixels.y);
        for (Piece[] row : pieces) {
            for (Piece piece : row) {
                if (piece != null) {
                    piece.draw(batch);
                }
            }
        }
    }

    public Vector2i getPieceCount() {
        return pieceCount;
    }

    public Piece getPieceAt(int x, int y) {
        return pieces[x][y];
    }

    public void setPieceAt(int x, int y, Piece piece) {
        pieces[x][y] = piece;
        pieces[x][y].updatePosition(new Vector2i(x, y));
    }

    public Vector2i getOffset() {
        return offset;
    }

    public Vector2i getMargin() {
        return margin;
    }

    public Vector2i getSizeInPixels() {
        return sizeInPixels;
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
    }
}
