package com.houseforest.matchthree;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Tom on 14.01.2017.
 */

public class AnimatedSprite extends SceneNode {

    private Animation<TextureRegion> animation;
    private TextureRegion currentFrame;
    private float elapsed;
    private float width;
    private float height;

    AnimatedSprite(Game game, TextureAtlas atlas, String[] keyFrameNames, float keyFrameDuration, Animation.PlayMode mode) {
        super(game);

        Array<TextureRegion> keyFrames = new Array<>(keyFrameNames.length);
        for (int i = 0; i < keyFrameNames.length; ++i) {
            keyFrames.add(atlas.findRegion(keyFrameNames[i]));
        }

        this.animation = new Animation<>(keyFrameDuration, keyFrames, mode);
        this.width = this.animation.getKeyFrame(0.0f).getRegionWidth();
        this.height = this.animation.getKeyFrame(0.0f).getRegionHeight();
        reset();
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height){
        this.height = height;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(currentFrame, getX(), getY(), width, height);
    }

    public void reset() {
        elapsed = 0.0f;
        currentFrame = animation.getKeyFrame(elapsed);
    }

    public void update(float dt) {
        elapsed += dt;
        currentFrame = animation.getKeyFrame(elapsed);
    }

    public Animation<TextureRegion> getAnimation() {
        return animation;
    }

    public void setAnimation(Animation<TextureRegion> animation){
        this.animation = animation;
        reset();
    }
}
