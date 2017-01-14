package com.houseforest.matchthree;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;

public class Game implements ApplicationListener, InputProcessor {

    public static final Vector2i RESOLUTION = new Vector2i(1920, 1080);

    public enum TextureAtlasName {
        Characters,
        UI,
        Numbers
    }

	private SpriteBatch batch;
	private Board board;
    private HashMap<TextureAtlasName, TextureAtlas> textureAtlases;
    private OrthographicCamera camera;

	@Override
	public void create () {
        textureAtlases = new HashMap<TextureAtlasName, TextureAtlas>(TextureAtlasName.values().length);
        textureAtlases.put(TextureAtlasName.Characters, new TextureAtlas("characters/characters.pack"));
        textureAtlases.put(TextureAtlasName.UI, new TextureAtlas("ui/ui.pack"));
        textureAtlases.put(TextureAtlasName.Numbers, new TextureAtlas("numbers/numbers.pack"));

        // Accomodate for upside-down coordinate system.
        for(TextureAtlas atlas : textureAtlases.values()){
            for(TextureRegion region : atlas.getRegions()) {
                region.flip(false, true);
            }
        }

		batch = new SpriteBatch();
        board = new Board(this);

        Gdx.input.setInputProcessor(this);

        camera = new OrthographicCamera(RESOLUTION.x, RESOLUTION.y);
        camera.setToOrtho(true, RESOLUTION.x, RESOLUTION.y);
	}

    @Override
    public void resize(int width, int height) {

    }

    @Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
		batch.begin();

        board.draw(batch);

		batch.end();
	}

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
	public void dispose () {
		batch.dispose();

        for(TextureAtlas atlas : textureAtlases.values()) {
            atlas.dispose();
        }
        textureAtlases.clear();

        board.dispose();
	}

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) {
            board.onTouch(screenX, screenY);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer == 0) {
            board.onDrag(screenX, screenY);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public TextureAtlas getTextureAtlas(TextureAtlasName name) {
        return textureAtlases.get(name);
    }

    public static final void log(String message) {
        Gdx.app.log("matchthree", message);
    }

    public static <T> T randomArrayElement(T[] array) {
        return array[MathUtils.random(array.length - 1)];
    }
}
