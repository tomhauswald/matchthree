package com.houseforest.matchthree;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

/**
 * Created by tom on 30.01.17.
 */

public class AnimationManager<E> {

    private HashMap<E, Animation<TextureRegion>> animations;

    public AnimationManager() {
        animations = new HashMap<>();
    }

    public void add(E key, Animation<TextureRegion> animation) {
        animations.put(key, animation);
        Util.log("Registered animation: '" + key.toString() + "' " + animation.toString());
    }

    public Animation<TextureRegion> get(E key) {
        return animations.get(key);
    }

    public boolean exists(E key) {
        return animations.containsKey(key);
    }
}
