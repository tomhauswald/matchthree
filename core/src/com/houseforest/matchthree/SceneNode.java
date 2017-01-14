package com.houseforest.matchthree;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Tom on 13.01.2017.
 */

public class SceneNode {

    private Game game;
    private Vector2 position;

    public SceneNode(Game game) {
        this.game = game;
        this.position = new Vector2();
    }

    public void update(float dt) {
    }

    public void draw(SpriteBatch batch) {
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getX() {
        return position.x;
    }

    public float getY(){
        return position.y;
    }

    public void setPosition(Vector2 position){
        this.position = position;
    }

    public void setX(float x){
        this.position.x = x;
    }

    public void setY(float y){
        this.position.y = y;
    }

    protected Game getGame() {
        return this.game;
    }
}
