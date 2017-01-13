package com.houseforest.matchthree;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by Tom on 13.01.2017.
 */

public class SceneNode {

    private Game game;

    public SceneNode(Game game) {
        this.game = game;
    }

    public void update(float dt) {

    }

    public void draw(SpriteBatch batch) {

    }

    protected Game getGame() {
        return this.game;
    }
}
