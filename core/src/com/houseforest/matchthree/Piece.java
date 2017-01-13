package com.houseforest.matchthree;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import java.awt.Point;

/**
 * Created by Tom on 13.01.2017.
 */

public class Piece extends SceneNode {

    public enum Variant {
        Lemon,
        Apple,
        Fig,
        Strawberry,
        Carrot
    }

    private Variant variant;
    public Sprite sprite;
    private Point position;

    private static final Point padding = new Point(8, 8);
    private static Point pieceSize;
    private static Board board;

    public Piece(Game game, Board board, int x, int y, Variant variant) {
        super(game);
        this.variant = variant;
        this.position = new Point(x, y);

        if (Piece.board == null) {
            Piece.board = board;
        }

        // Content area of pieces.
        if (pieceSize == null) {
            pieceSize = new Point(
                    Math.round((board.getSizeInPixels().x - 2 * board.getMargin().x) / board.getPieceCount().x),
                    Math.round((board.getSizeInPixels().y - 2 * board.getMargin().y) / board.getPieceCount().y)
            );
            pieceSize.x -= 2 * padding.x;
            pieceSize.y -= 2 * padding.y;
        }

        String textureName = "CharactersFlat_Line" + (variant.ordinal() + 1);
        this.sprite = game.getTextureAtlas(Game.TextureAtlasName.Characters).createSprite(textureName);
        this.sprite.setSize(pieceSize.x, pieceSize.y);
        this.sprite.setOrigin(0.0f, 100);
        updatePosition(this.position);
    }

    @Override
    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public Variant getVariant() {
        return variant;
    }

    public void updatePosition(Point position) {
        this.position = position;
        sprite.setPosition(
                board.getOffset().x + board.getMargin().x + padding.x + position.x * (pieceSize.x + 2 * padding.x),
                board.getOffset().y + board.getMargin().y + padding.y + position.y * (pieceSize.y + 2 * padding.y)
        );
    }

    public static Piece random(Game game, Board board, int x, int y) {
        return new Piece(game, board, x, y, Game.randomArrayElement(Variant.values()));
    }
}
