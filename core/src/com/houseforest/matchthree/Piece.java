package com.houseforest.matchthree;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

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

    public enum MovementType {
        Swap,
        Fall
    }

    private Variant variant;
    private float nextBlinkTime;

    private static final Vector2i padding = new Vector2i(4);
    private static Vector2i pieceSize;
    private static Board board;

    private boolean moving;
    private float moveDistance;
    private float moveProgress;
    private MovementType movementType;
    private Direction moveDirection;
    private Vector2i moveTargetBoardPosition;
    private static final HashMap<MovementType, Float> movementSpeeds = new HashMap<>();

    public Piece(Game game, Board board, int x, int y, Variant variant) {
        super(
                game,
                game.getTextureAtlas(Game.TextureAtlasName.Characters),
                new String[]{
                        "BlinkBright_Line" + (1 + variant.ordinal()),
                        "CharactersBright_Line" + (1 + variant.ordinal())
                },
                0.2f,
                Animation.PlayMode.NORMAL
        );

        this.variant = variant;

        if (Piece.board == null) {
            Piece.board = board;
        }

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

        float scaleFactor = pieceSize.x / Math.max(getWidth(), getHeight());
        setWidth(getWidth() * scaleFactor);
        setHeight(getHeight() * scaleFactor);
        blink();

        this.moving = false;
        this.moveProgress = 0.0f;
        this.moveDirection = null;
        this.moveDistance = 0.0f;
        this.moveTargetBoardPosition = null;

        setBoardPosition(new Vector2i(x, y));
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
            blink();
        }

        if(moving){
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
            if(moveProgress >= moveDistance) {
                setBoardPosition(moveTargetBoardPosition);
                moving = false;
                moveProgress = 0.0f;
                moveDirection = null;
                moveDistance = 0.0f;
            }
        }

        super.update(dt);
    }

    private void blink() {
        reset();
        nextBlinkTime = MathUtils.random(6.0f, 30.0f);
    }

    public void moveToBoardPosition(Vector2i target, MovementType movementType) {

        Vector2i bpos = getBoardPosition();
        Util.log("Moving from {" + bpos.x + ", " + bpos.y + "} to {" + target.x + ", " + target.y + "}");

        moving = true;
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
            Util.log("Moving to current location. D'oh!");
            moving = false;
        }

        if(moving && (moveDirection == Direction.Left || moveDirection == Direction.Right)) {
            moveDistance = (pieceSize.x + 2 * padding.x) * Math.abs(target.x - bpos.x);
            Util.log("moveDistance = " + moveDistance);
        } else {
            moveDistance = (pieceSize.y + 2 * padding.y) * Math.abs(target.y - bpos.y);
            Util.log("moveDistance = " + moveDistance);
        }
    }

    public boolean isMoving() {
        return moving;
    }
}
