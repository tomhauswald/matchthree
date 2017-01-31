package com.houseforest.matchthree;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

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

    public enum Direction {
        Left,
        Up,
        Right,
        Down
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

    // Handle moving pieces.
    private Piece[] swapPieces;
    private Direction swapDirection;
    private final float swapSpeed = 1.0f;
    private float swapProgress;
    private Vector2i[] swapBoardPositions;

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
                this.pieces[x][y] = Piece.random(game, this, x, y);
            }
        }

        this.backgroundTexture = new Texture(Gdx.files.internal("board.png"));

        this.state = State.Check;
        this.swapPieces = new Piece[2];
        this.swapPieces[0] = this.swapPieces[1] = null;
        this.swapDirection = Direction.Up;
        this.swapProgress = 0.0f;
        this.swapBoardPositions = new Vector2i[2];
        this.swapBoardPositions[0] = this.swapBoardPositions[1] = null;
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

    private void initializePieceSwap(Vector2i firstPosition, Vector2i secondPosition) {
        swapPieces[0] = pieces[firstPosition.x][firstPosition.y];
        swapPieces[1] = pieces[secondPosition.x][secondPosition.y];

        swapBoardPositions[0] = firstPosition;
        swapBoardPositions[1] = secondPosition;

        if(firstPosition.x > secondPosition.x) {
            swapDirection = Direction.Left;
        }
        else if(firstPosition.x < secondPosition.x) {
            swapDirection = Direction.Right;
        }
        else if(firstPosition.y > secondPosition.y) {
            swapDirection = Direction.Up;
        }
        else if(firstPosition.y < secondPosition.y) {
            swapDirection = Direction.Down;
        }
        else {
            Util.log("How would one go about swapping a piece with itself!?....");
            assert false;
        }

        swapProgress = 0.0f;
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
    }

    private void updateIdleState(float dt) {


    }

    private void updateSwapState(float dt) {

        assert swapPieces[0] != null && swapPieces[1] != null;
        float pieceSize = Piece.getPieceSize().x;

        Piece p0 = swapPieces[0];
        Piece p1 = swapPieces[1];

        Util.log("Swapping " + p0 + " and " + p1);

        switch (swapDirection) {
            case Left:
                p0.getPosition().add(-dt * swapSpeed * pieceSize, 0);
                p1.getPosition().add(dt * swapSpeed * pieceSize, 0);
                break;

            case Up:
                p0.getPosition().add(0, -dt * swapSpeed * pieceSize);
                p1.getPosition().add(0, dt * swapSpeed * pieceSize);
                break;

            case Right:
                p0.getPosition().add(dt * swapSpeed * pieceSize, 0);
                p1.getPosition().add(-dt * swapSpeed * pieceSize, 0);
                break;

            case Down:
                p0.getPosition().add(0, dt * swapSpeed * pieceSize);
                p1.getPosition().add(0, -dt * swapSpeed * pieceSize);
                break;
        }

        swapProgress += dt * swapSpeed;

        // Finished swapping pieces, so check for matches next.
        if (swapProgress >= 1.0f) {
            setPieceAt(swapBoardPositions[1].x, swapBoardPositions[1].y, p0);
            setPieceAt(swapBoardPositions[0].x, swapBoardPositions[0].y, p1);

            swapPieces[0] = swapPieces[1] = null;
            swapBoardPositions[0] = swapBoardPositions[1] = null;
            swapProgress = 0.0f;

            state = State.Check;
        }
    }

    private void updateRefillState(float dt) {

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
            Util.log("Found match from " + match.getStart().toString() + " to " + match.getEnd().toString());
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
