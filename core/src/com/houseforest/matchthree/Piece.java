package com.houseforest.matchthree;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import java.util.HashMap;

/**
 * Created by Tom on 13.01.2017.
 */

public class Piece extends AnimatedSprite {

    public enum Variant {
        Lemon,
        Apple,
        Fig,
        Strawberry,
        Carrot
    }

    public enum Direction {
        Up,
        Right,
        Down,
        Left
    }

    private enum State {
        Idle,
        Move,
        Explosion
    }

    public enum MovementType {
        Swap,
        Fall
    }

    // Formatting.
    private static final Vector2i padding = new Vector2i(5);
    private static Vector2i pieceSize;

    private Board board;
    private Variant variant;
    private float nextBlinkTime;
    private State state;

    // Handle movement.
    private float moveDistance;
    private float moveProgress;
    private MovementType movementType;
    private Direction moveDirection;
    private Vector2i moveTargetBoardPosition;
    private static final HashMap<MovementType, Float> movementSpeeds = new HashMap<>();

    // Cache piece animations by variant.
    private static final HashMap<Variant, HashMap<String, Animation<TextureRegion>>> animations = new HashMap<>();

    public Piece(Game game, Board board, int x, int y, Variant variant) {
        super(game);

        this.board = board;
        this.variant = variant;

        // Content area of pieces.
        if (Piece.pieceSize == null) {
            Piece.pieceSize = new Vector2i(
                    Math.round((board.getSizeInPixels().x - 2 * board.getMargin().x) / board.getPieceCount().x),
                    Math.round((board.getSizeInPixels().y - 2 * board.getMargin().y) / board.getPieceCount().y)
            );
            Piece.pieceSize.x -= 2 * padding.x;
            Piece.pieceSize.y -= 2 * padding.y;
        }

        if(Piece.movementSpeeds.size() == 0) {
            Piece.movementSpeeds.put(MovementType.Swap, 2.5f);
            Piece.movementSpeeds.put(MovementType.Fall, 7.5f);
        }

        setAnimation(getGame().getAnimationManager().get("piece_idle_1"));

        float scaleFactor = pieceSize.x / Math.max(getWidth(), getHeight());
        setWidth(getWidth() * scaleFactor);
        setHeight(getHeight() * scaleFactor);

        this.state = State.Idle;
        setBoardPosition(new Vector2i(x, y));
         // Piece.animations.get(variant).get("idle"));
        nextBlinkTime = MathUtils.random(1.0f, 10.0f);
    }

    public static final void cacheAnimations(Game game) {
        AnimationManager animMgr = game.getAnimationManager();
        for(int i = 0; i < Variant.values().length; ++i) {
            Piece.animations.put(Variant.values()[i], new HashMap<String, Animation<TextureRegion>>());
            Piece.animations.get(Variant.values()[i]).put("idle", animMgr.get("piece_idle_" + (i + 1)));
            Piece.animations.get(Variant.values()[i]).put("blink", animMgr.get("piece_blink_" + (i + 1)));
            Piece.animations.get(Variant.values()[i]).put("explode", animMgr.get("piece_explode_" + (i + 1)));
            Util.log("Cached animation: " + Piece.animations.get(Variant.values()[i]).get("idle"));
            Util.log("Cached animation: " + Piece.animations.get(Variant.values()[i]).get("blink"));
            Util.log("Cached animation: " + Piece.animations.get(Variant.values()[i]).get("explode"));
        }
    }

    public Variant getVariant() {
        return variant;
    }

    public Vector2i getBoardPosition() {
        return board.toBoardSpace(new Vector2i((int)getX(), (int)getY()));
    }

    public void setBoardPosition(Vector2i position) {
        setX(board.getOffset().x + board.getMargin().x + padding.x + position.x * (pieceSize.x + 2 * padding.x) + (pieceSize.x - getWidth()) / 2);
        setY(board.getOffset().y + board.getMargin().y + padding.y + position.y * (pieceSize.y + 2 * padding.y) + (pieceSize.y - getHeight()) / 2);
    }

    public static Piece random(Game game, Board board, int x, int y) {
        return new Piece(game, board, x, y, Util.randomArrayElement(Variant.values()));
    }

    @Override
    public void update(float dt) {
        if((nextBlinkTime -= dt) <= 0.0f) {
            //blink();
        }

        switch(state) {
            case Idle:
                updateIdleState(dt);
                break;

            case Move:
                updateMoveState(dt);
                break;

            case Explosion:
                updateExplosionState(dt);
            break;
        }

        super.update(dt);
    }

    private void updateIdleState(float dt) {
        /* if(getAnimation() == animations.get(variant).get("blink") && hasAnimationFinished()) {
            setAnimation(animations.get(variant).get("idle"));
        } */
    }

    private void updateMoveState(float dt) {
        float delta = dt * Piece.movementSpeeds.get(movementType) * (pieceSize.x + 2 * padding.x);

        switch (moveDirection) {
            case Right:
                getPosition().add(delta, 0);
                break;

            case Left:
                getPosition().add(-delta, 0);
                break;

            case Down:
                getPosition().add(0, delta);
                break;

            case Up:
                getPosition().add(0, -delta);
                break;
        }

        moveProgress += delta;
        if (moveProgress >= moveDistance) {
            setBoardPosition(moveTargetBoardPosition);
            moveProgress = 0.0f;
            moveDirection = null;
            moveDistance = 0.0f;
            state = State.Idle;
        }
    }

    private void updateExplosionState(float dt) {
        if(hasAnimationFinished()) {
            state = State.Idle;
            setAnimation(animations.get(variant).get("idle"));
        }
    }

    private void blink() {
        setAnimation(animations.get(variant).get("blink"));
        nextBlinkTime = MathUtils.random(6.0f, 30.0f);
    }

    private void explode() {
        setAnimation(animations.get(variant).get("explode"));
        state = State.Explosion;
    }

    public void moveToBoardPosition(Vector2i target, MovementType movementType) {

        Vector2i bpos = getBoardPosition();
        // Util.log("Moving from {" + bpos.x + ", " + bpos.y + "} to {" + target.x + ", " + target.y + "}");

        state = State.Move;
        moveProgress = 0.0f;
        moveTargetBoardPosition = target;
        this.movementType = movementType;

        if (moveTargetBoardPosition.x > bpos.x) {
            moveDirection = Direction.Right;
        } else if (moveTargetBoardPosition.x < bpos.x) {
            moveDirection = Direction.Left;
        } else if (moveTargetBoardPosition.y > bpos.y) {
            moveDirection = Direction.Down;
        } else if (moveTargetBoardPosition.y < bpos.y) {
            moveDirection = Direction.Up;
        } else {
            // Util.log("Moving to current location. D'oh!");
            state = State.Idle;
        }

        if(state == State.Move) {
            if (moveDirection == Direction.Left || moveDirection == Direction.Right) {
                moveDistance = (pieceSize.x + 2 * padding.x) * Math.abs(target.x - bpos.x);
                // Util.log("moveDistance = " + moveDistance);
            } else {
                moveDistance = (pieceSize.y + 2 * padding.y) * Math.abs(target.y - bpos.y);
                // Util.log("moveDistance = " + moveDistance);
            }
        }
    }

    public boolean isIdle() { return state == State.Idle; }
}
