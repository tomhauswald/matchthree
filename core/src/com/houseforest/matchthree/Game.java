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

    public static final Vector2i RESOLUTION = new Vector2i(1080, 1920);

    public enum TextureAtlasName {
        Characters,
        UI,
        Numbers
    }

    public enum State {
        Active,
        Menu
    }

	private SpriteBatch batch;
	private Board board;
    private HashMap<TextureAtlasName, TextureAtlas> textureAtlases;
    private OrthographicCamera camera;
    private State state;

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

        state = State.Active;
	}

    @Override
    public void resize(int width, int height) {

    }

    private void update(float dt) {
        camera.update();
        board.update(dt);
    }

    @Override
	public void render () {
        update(Gdx.graphics.getDeltaTime());

        Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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

        // Only allow player moves in active state.
        if(state == State.Active) {
            if (pointer == 0) {
                board.onTouch(screenX, screenY);
                return true;
            } else {
                return false;
            }
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

        // Only allow player moves in active state.
        if(state == State.Active) {
            if (pointer == 0) {
                board.onDrag(screenX, screenY);
                return true;
            } else {
                return false;
            }
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
