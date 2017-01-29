package com.houseforest.matchthree;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import java.util.Vector;

/**
 * Created by Tom on 13.01.2017.
 */

public class Board extends SceneNode implements Disposable {

    public enum State {
        Idle,
        Animating,
        Check
    }

    private Texture backgroundTexture;
    private Piece[][] pieces;
    private Vector2i pieceCount;
    private Vector2i sizeInPixels;
    private Vector2i offset;
    private Vector2i margin;
    private Vector2i touchPosition;
    private boolean dragProcessed;

    private State state;

    // Handle refilling the game board with new pieces.
    private Vector<Piece> refillPieces;

    public Board(Game game) {
        super(game);

        pieceCount = new Vector2i(8);

        int minDim = Math.min(Game.RESOLUTION.x, Game.RESOLUTION.y);
        sizeInPixels = new Vector2i(Math.round(minDim * 0.95f));

        offset = new Vector2i(
                (Game.RESOLUTION.x - sizeInPixels.x) / 2,
                (Game.RESOLUTION.y - sizeInPixels.y) / 2
        );

        margin = new Vector2i(0);

        this.pieces = new Piece[pieceCount.x][pieceCount.y];
        for (int x = 0; x < pieceCount.x; ++x) {
            for (int y = 0; y < pieceCount.y; ++y) {
                setPieceAt(x, y, Piece.random(game, this, x, y));
            }
        }

        this.backgroundTexture = new Texture(Gdx.files.internal("general/grid.png"));

        this.state = State.Check;

        this.refillPieces = new Vector<>();
    }

    public Vector2i toBoardSpace(Vector2i screenPoint) {
        final int areaX = sizeInPixels.x - 2 * margin.x;
        final int areaY = sizeInPixels.y - 2 * margin.y;
        final int boardX = screenPoint.x - offset.x - margin.x;
        final int boardY = screenPoint.y - offset.y - margin.y;
        final int pieceAreaX = areaX / pieceCount.x;
        final int pieceAreaY = areaY / pieceCount.y;

        return new Vector2i(
                boardX / pieceAreaX,
                boardY / pieceAreaY
        );
    }

    public void onDrag(int screenX, int screenY) {

        // Ignore input during animations.
        if(state == State.Idle) {
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
                        swap(touchPosition, dragPosition);
                    }
                }

                // Potential vertical move.
                else if (dragPosition.x == touchPosition.x) {
                    if (dragPosition.y == touchPosition.y - 1 || dragPosition.y == touchPosition.y + 1) {
                        dragProcessed = true;
                        swap(touchPosition, dragPosition);
                    }
                }
            }
        } else {
            dragProcessed = true;
        }
    }

    public void onTouch(int screenX, int screenY) {

        // Ignore input during animations.
        if(state == State.Idle) {
            Vector2i pt = toBoardSpace(new Vector2i(screenX, screenY));
            if (pt != null) {
                touchPosition = pt;
                dragProcessed = false;
            }
        } else {
            touchPosition = null;
            dragProcessed = false;
        }
    }

    private void refill() {

        // Find empty grid cells.
        Vector<Vector2i> positions = new Vector<>();
        for(int x = 0; x < pieceCount.x; ++x){
            for(int y = 0; y < pieceCount.y; ++y){
                if(pieces[x][y] == null) {
                    positions.add(new Vector2i(x, y));
                }
            }
        }

        for(Vector2i position : positions) {
            pieces[position.x][position.y] = Piece.random(
                    getGame(),
                    this,
                    position.x,
                    position.y - pieceCount.y
            );
            pieces[position.x][position.y].moveToBoardPosition(position, Piece.MovementType.Fall);
        }

        state = State.Animating;
    }

    private void swap(Vector2i firstPosition, Vector2i secondPosition) {
        Piece first = pieces[firstPosition.x][firstPosition.y];
        first.moveToBoardPosition(secondPosition, Piece.MovementType.Swap);

        Piece second = pieces[secondPosition.x][secondPosition.y];
        second.moveToBoardPosition(firstPosition, Piece.MovementType.Swap);

        pieces[firstPosition.x][firstPosition.y] = second;
        pieces[secondPosition.x][secondPosition.y] = first;

        state = State.Animating;
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
                    pieces[x][yy] = pieces[x][yy - 1];
                    pieces[x][yy].moveToBoardPosition(new Vector2i(x, yy), Piece.MovementType.Fall);
                }

                // Empty cells atop of match.
                pieces[x][0] = null;
            }
        }

        // Vertical match
        else {

            int x = match.getStart().x;

            for (int y = match.getEnd().y; y >= 0; --y) {
                if(y > match.getLength()) {
                    pieces[x][y] = pieces[x][y - match.getLength()];
                    pieces[x][y].moveToBoardPosition(new Vector2i(x, y), Piece.MovementType.Fall);
                } else {
                    pieces[x][y] = null;
                }
            }
        }

        // Issue refill.
       refill();
    }

    @Override
    public void update(float dt) {

        // Update pieces.
        for (Piece[] row : pieces) {
            for (Piece piece : row) {
                if (piece != null) {
                    piece.update(dt);
                }
            }
        }

        switch(state) {
            case Idle: updateIdleState(dt); break;
            case Animating: udateAnimatingState(dt); break;
            case Check: updateCheckState(dt); break;
            default: break;
        }
    }

    private void updateIdleState(float dt) {
    }

    private void udateAnimatingState(float dt) {

        // Check for any remaining moving board pieces.
        boolean done = true;
        for (Piece[] row : pieces) {
            for(Piece piece : row) {
                if (piece.isMoving()) {
                    done = false;
                    break;
                }
            }
        }

        // Board pieces have settled.
        if(done) {
            state = State.Check;
        }
    }

    private void updateCheckState(float dt) {

        // Check for matches.
        Match match = findMatch();
        if(match == null) {
            // No more matches, so resume to idle state.
            state = State.Idle;
        }

        // We found a match, handle it and check for remaining matches in the next iteration.
        else {
            Util.log("Found match from {" +
                    match.getStart().x + ", " + match.getStart().y + "} to {" +
                    match.getEnd().x + ", " + match.getEnd().y + "}");
            handleMatch(match);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {

        // Draw background.
        batch.draw(backgroundTexture, offset.x, offset.y, sizeInPixels.x, sizeInPixels.y);

        // Draw pieces.
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
