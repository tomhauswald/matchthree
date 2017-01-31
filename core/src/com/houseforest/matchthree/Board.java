package com.houseforest.matchthree;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.util.Vector;

/**
 * Created by Tom on 13.01.2017.
 */

public class Board extends SceneNode implements Disposable {

    public enum State {
        Idle,
        Swap,
        Refill,
        Check
    }

    public class PieceRelocation {
        public Piece piece;
        public Vector2i targetPosition;

        public PieceRelocation(Piece piece, Vector2i target) {
            this.piece = piece;
            this.targetPosition = target;
        }
    }

    private Texture backgroundTexture;
    private Piece[][] pieces;
    private Vector2i pieceCount;
    private Vector2i sizeInPixels;
    private Vector2i offset;
    private Vector2i margin;
    private Vector2i touchPosition;
    private boolean dragProcessed;

    private Vector<PieceRelocation> pendingRelocations;

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

        margin = new Vector2i(21);

        this.pieces = new Piece[pieceCount.x][pieceCount.y];
        for (int x = 0; x < pieceCount.x; ++x) {
            for (int y = 0; y < pieceCount.y; ++y) {
                setPieceAt(x, y, Piece.random(game, this, x, y));
            }
        }

        this.backgroundTexture = new Texture(Gdx.files.internal("board.png"));

        this.state = State.Check;

        this.refillPieces = new Vector<>();
        this.pendingRelocations = new Vector<>();
    }

    public void addPendingRelocation(Piece piece, Vector2i destination) {
        pendingRelocations.add(new PieceRelocation(piece, destination));
    }

    public Vector2i toBoardSpace(Vector2i screenPoint) {
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
                        initializePieceSwap(touchPosition, dragPosition);
                    }
                }

                // Potential vertical move.
                else if (dragPosition.x == touchPosition.x) {
                    if (dragPosition.y == touchPosition.y - 1 || dragPosition.y == touchPosition.y + 1) {
                        dragProcessed = true;
                        initializePieceSwap(touchPosition, dragPosition);
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

    private void initializePieceRefills(Vector2i[] refillPositions) {
        Util.log("initializePieceRefills({");
        for(Vector2i rp : refillPositions) {
            Util.log("{"+rp.x+", "+rp.y+"}, ");
        }
        Util.log("});");

        for(Vector2i rp : refillPositions) {
            refillPieces.add(Piece.random(getGame(), this, rp.x, 0));
            refillPieces.lastElement().moveToBoardPosition(rp);
        }

        state = State.Refill;
    }

    private void initializePieceSwap(Vector2i firstPosition, Vector2i secondPosition) {
        pieces[firstPosition.x][firstPosition.y].moveToBoardPosition(secondPosition);
        pieces[secondPosition.x][secondPosition.y].moveToBoardPosition(firstPosition);
        state = State.Swap;
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

        Vector2i[] refillPositions = new Vector2i[match.getLength()];

        if(match.isHorizontal()) {
            int y = match.getStart().y;

            for (int x = match.getStart().x; x <= match.getEnd().x; ++x) {
                for (int yy = y; yy > 0; --yy) {
                    // Move pieces down.
                    pieces[x][yy - 1].moveToBoardPosition(new Vector2i(x, yy));
                    // setPieceAt(x, yy, getPieceAt(x, yy - 1));
                }

                // Generate new piece at top.
                refillPositions[x - match.getStart().x] = new Vector2i(x, 0);
                //setPieceAt(x, 0, Piece.random(getGame(), this, x, 0));
            }
        }

        // Vertical match
        else {
            int x = match.getStart().x;

            for (int y = match.getEnd().y; y >= match.getLength(); --y) {
                // Move pieces down.
                // setPieceAt(x, y, getPieceAt(x, y - match.getLength()));
                pieces[x][y - match.getLength()].moveToBoardPosition(new Vector2i(x, y));
            }

            for(int y = 0; y<match.getLength(); ++y) {
                // Generate new pieces above.
                // setPieceAt(x, y, Piece.random(getGame(), this, x, y));
                refillPositions[y] = new Vector2i(x, y);
            }
        }

        initializePieceRefills(refillPositions);
    }

    @Override
    public void update(float dt) {

        // Remove all pending relocations.
        pendingRelocations.clear();

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
            case Swap: updateSwapState(dt); break;
            case Refill: updateRefillState(dt); break;
            case Check: updateCheckState(dt); break;
            default: break;
        }

        // Handle pending piece relocations.
        for(PieceRelocation reloc : pendingRelocations) {
            setPieceAt(reloc.targetPosition.x, reloc.targetPosition.y, reloc.piece);
        }
    }

    private void updateIdleState(float dt) {


    }

    private void updateSwapState(float dt) {

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

    private void updateRefillState(float dt) {

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
            refillPieces.clear();
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
        pieces[x][y].setBoardPosition(new Vector2i(x, y));
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
