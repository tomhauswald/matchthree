package com.houseforest.matchthree;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

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

    private Variant variant;
    private Vector2i boardPosition;
    private float nextBlinkTime;

    private static final Vector2i padding = new Vector2i(8);
    private static Vector2i pieceSize;
    private static Board board;

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
        this.boardPosition = new Vector2i(x, y);

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
        updatePosition(this.boardPosition);
        blink();
    }

    public Variant getVariant() {
        return variant;
    }

    public void updatePosition(Vector2i position) {
        this.boardPosition = position;
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
        super.update(dt);
    }

    private void blink() {
        reset();
        nextBlinkTime = MathUtils.random(1.0f, 10.0f);
    }
}
