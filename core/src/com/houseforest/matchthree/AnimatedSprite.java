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

    AnimatedSprite(Game game) {
        super(game);
        setAnimation(null);
    }

    AnimatedSprite(Game game, TextureAtlas atlas, String[] keyFrameNames, float keyFrameDuration, Animation.PlayMode mode) {
        super(game);

        Array<TextureRegion> keyFrames = new Array<>(keyFrameNames.length);
        for (int i = 0; i < keyFrameNames.length; ++i) {
            keyFrames.add(atlas.findRegion(keyFrameNames[i]));
        }

        setAnimation(new Animation<>(keyFrameDuration, keyFrames, mode));
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
        if(animation != null && currentFrame != null) {
            batch.draw(currentFrame, getX(), getY(), width, height);
        }
    }

    public void reset() {
        elapsed = 0.0f;
        if(animation != null) {
            currentFrame = animation.getKeyFrame(0.0f);
        } else {
            currentFrame = null;
        }
    }

    public void update(float dt) {
        if(animation != null) {
            elapsed += dt;
            currentFrame = animation.getKeyFrame(elapsed);
        } else {
            currentFrame = null;
        }
    }

    public Animation<TextureRegion> getAnimation() {
        return animation;
    }

    public void setAnimation(Animation<TextureRegion> animation){
        this.animation = animation;
        if(animation != null) {
            setWidth(animation.getKeyFrame(0.0f).getRegionWidth());
            setHeight(animation.getKeyFrame(0.0f).getRegionHeight());
        }
        reset();
    }

    public boolean hasAnimationFinished() {
        assert animation != null;
        return animation.isAnimationFinished(elapsed);
    }
}
