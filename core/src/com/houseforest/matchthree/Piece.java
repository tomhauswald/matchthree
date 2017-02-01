package com.houseforest.matchthree;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

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

    private Variant variant;
    private float nextBlinkTime;

    private static final Vector2i padding = new Vector2i(8);
    private static Vector2i pieceSize;
    private static Board board;

    private boolean moving;
    private float moveDistance;
    private float moveProgress;
    private Direction moveDirection;
    private Vector2i moveTargetBoardPosition;
    private static final float moveSpeed = 2.0f;

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
        if (pieceSize == null) {
            pieceSize = new Vector2i(
                    Math.round((board.getSizeInPixels().x - 2 * board.getMargin().x) / board.getPieceCount().x),
                    Math.round((board.getSizeInPixels().y - 2 * board.getMargin().y) / board.getPieceCount().y)
            );
            pieceSize.x -= 2 * padding.x;
            pieceSize.y -= 2 * padding.y;
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

    public static Piece randomFloating(Game game, Board board, int x) {
        return new Piece(game, board, x, -2, Util.randomArrayElement(Variant.values()));
    }

    @Override
    public void update(float dt) {
        if((nextBlinkTime -= dt) <= 0.0f) {
            blink();
        }

        if(moving){
            float delta = dt * moveSpeed * (pieceSize.x + 2 * padding.x);

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
        nextBlinkTime = MathUtils.random(1.0f, 10.0f);
    }

    public void moveToBoardPosition(Vector2i target) {

        Vector2i bpos = getBoardPosition();
        Util.log("Moving from {" + bpos.x + ", " + bpos.y + "} to {" + target.x + ", " + target.y + "}");

        moving = true;
        moveDistance = pieceSize.x * Math.abs(target.x - bpos.x)
                     + pieceSize.y * Math.abs(target.y - bpos.y);
        moveProgress = 0.0f;
        moveTargetBoardPosition = target;

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
    }

    public boolean isMoving() {
        return moving;
    }
}
