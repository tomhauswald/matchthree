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
        Swapping,
        Falling,
        Refilling,
        Exploding,
        Checking
    }

    private Texture backgroundTexture;
    private Piece[][] pieces;
    private Vector2i pieceCount;
    private Vector2i sizeInPixels;
    private Vector2i offset;
    private Vector2i margin;
    private Vector2i touchPosition;
    private boolean dragProcessed;
    private Vector<Vector2i> explosionCoordinates;

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

        this.state = State.Checking;

        this.refillPieces = new Vector<>();
        this.explosionCoordinates = new Vector<>();
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
                if (dragPosition == null || touchPosition == null) {
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
        state = State.Refilling;

        // Find empty grid cells.
        Vector2i bpos = new Vector2i(0);
        for(int x = 0; x < pieceCount.x; ++x){
            for(int y = 0; y < pieceCount.y; ++y){
                if(pieces[x][y] == null) {
                    bpos.x = x;
                    bpos.y = y;
                    pieces[x][y] = Piece.random(
                            getGame(),
                            this,
                            x,
                            y - pieceCount.y
                    );
                    pieces[x][y].moveToBoardPosition(bpos, Piece.MovementType.Fall);
                }
            }
        }
    }

    private void swap(Vector2i firstPosition, Vector2i secondPosition) {
        state = State.Swapping;

        Piece first = pieces[firstPosition.x][firstPosition.y];
        first.moveToBoardPosition(secondPosition, Piece.MovementType.Swap);

        Piece second = pieces[secondPosition.x][secondPosition.y];
        second.moveToBoardPosition(firstPosition, Piece.MovementType.Swap);

        pieces[firstPosition.x][firstPosition.y] = second;
        pieces[secondPosition.x][secondPosition.y] = first;
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

    private void updateFallingState(float dt) {
        boolean falling = false;
        Vector2i moveTarget = new Vector2i(0);
        for(int y = pieceCount.y - 1; y > 0; --y) {
            for(int x = 0; x < pieceCount.x; ++x) {
                if(pieces[x][y] == null) {
                    // If empty, replace by piece above.
                    moveTarget.x = x;
                    moveTarget.y = y;
                    pieces[x][y] = pieces[x][y-1];
                    pieces[x][y-1] = null;
                    if(pieces[x][y] != null) {
                        pieces[x][y].moveToBoardPosition(moveTarget, Piece.MovementType.Fall);
                        falling = true;
                    }
                }
            }
        }

        if(!falling && !piecesMoving()) {
            refill();
        }
    }

    private void handleMatch(Match match) {
        for(int y = match.getStart().y; y <= match.getEnd().y; ++y) {
            for(int x = match.getStart().x; x <= match.getEnd().x; ++x) {
                explosionCoordinates.add(new Vector2i(x, y));
                pieces[x][y].explode();
            }
        }
        state = State.Exploding;
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
            case Idle:
                updateIdleState(dt);
            break;

            case Falling:
                updateFallingState(dt);
            break;

            case Swapping:
                if(!piecesMoving()) {
                    state = State.Checking;
                }
            break;

            case Refilling:
                if(!piecesMoving()) {
                    state = State.Checking;
                }
            break;

            case Exploding:
                updateExplodeState(dt);
            break;

            case Checking:
                updateCheckState(dt);
            break;
            default: break;
        }
    }

    private void updateIdleState(float dt) {
    }

    private boolean piecesMoving() {
        for (Piece[] row : pieces) {
            for(Piece piece : row) {
                if (piece != null && !piece.isIdle()) {
                    return true;
                }
            }
        }
        return false;
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
            /* Util.log("Found match from {" +
                    match.getStart().x + ", " + match.getStart().y + "} to {" +
                    match.getEnd().x + ", " + match.getEnd().y + "}"); */
            handleMatch(match);
        }
    }

    private void updateExplodeState(float dt) {
        boolean done = true;
        for(Vector2i pos : explosionCoordinates) {
            if (pieces[pos.x][pos.y] != null && !pieces[pos.x][pos.y].hasAnimationFinished()) {
                done = false;
            }
        }

        if(done) {
            for (Vector2i pos : explosionCoordinates) {
                pieces[pos.x][pos.y] = null;
            }
            explosionCoordinates.clear();
            state = State.Falling;
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
