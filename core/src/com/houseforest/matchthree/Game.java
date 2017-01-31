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
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

public class Game implements ApplicationListener, InputProcessor {

    public static final Vector2i RESOLUTION = new Vector2i(1080, 640);

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

    private Texture backgroundTexture;
    private AnimationManager<String> animationManager;

	@Override
	public void create () {
        loadTextures();

        animationManager = new AnimationManager<>();
        loadAnimations();

		batch = new SpriteBatch();
        board = new Board(this);

        Gdx.input.setInputProcessor(this);

        camera = new OrthographicCamera(RESOLUTION.x, RESOLUTION.y);
        camera.setToOrtho(true, RESOLUTION.x, RESOLUTION.y);

        state = State.Active;
	}

    private void loadTextures() {
        // Texture atlases.
        textureAtlases = new HashMap<>(TextureAtlasName.values().length);
        textureAtlases.put(TextureAtlasName.Characters, new TextureAtlas(Gdx.files.internal("characters/characters.pack"), true));
        textureAtlases.put(TextureAtlasName.UI, new TextureAtlas(Gdx.files.internal("ui/ui.pack"), true));
        textureAtlases.put(TextureAtlasName.Numbers, new TextureAtlas(Gdx.files.internal("numbers/numbers.pack"), true));

        // Individual textures.
        backgroundTexture = new Texture(Gdx.files.internal("general/bg_trail.jpg"));
    }

    private void loadAnimations() {
        TextureAtlas atlas = getTextureAtlas(Game.TextureAtlasName.Characters);
        assert atlas != null;

        // Load board piece animations.
        for(int variantIndex = 1; variantIndex <= Piece.Variant.values().length; ++variantIndex) {

            Array<TextureRegion> keyframes;

            // Idle.
            {
                keyframes = new Array<>(1);
                keyframes.setSize(1);
                keyframes.set(0, atlas.findRegion("CharactersBright_Line" + variantIndex));
                animationManager.add(
                        "piece_idle_" + variantIndex,
                        new Animation<>(1.0f, keyframes, Animation.PlayMode.LOOP)
                );
            }

            // Blink.
            {
                keyframes = new Array<>(1);
                keyframes.setSize(1);
                keyframes.set(0, atlas.findRegion("BlinkBright_Line" + variantIndex));
                animationManager.add(
                        "piece_blink_" + variantIndex,
                        new Animation<>(0.2f, keyframes, Animation.PlayMode.NORMAL)
                );
            }

            // Explode.
            {
                keyframes = new Array<>(3);
                keyframes.setSize(5);
                keyframes.set(0, atlas.findRegion("CharactersGray_Line"  + variantIndex));
                keyframes.set(1, atlas.findRegion("CharactersBlack_Line" + variantIndex));
                keyframes.set(2, atlas.findRegion("CharactersGray_Line"  + variantIndex));
                keyframes.set(3, atlas.findRegion("CharactersBlack_Line" + variantIndex));
                keyframes.set(4, atlas.findRegion("CharactersGray_Line"  + variantIndex));
                animationManager.add(
                        "piece_explode_" + variantIndex,
                        new Animation<>(0.12f, keyframes, Animation.PlayMode.NORMAL)
                );
            }
        }

        Piece.cacheAnimations(this);
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

        Util.drawTextureFlipped(batch, backgroundTexture, 0, 0, RESOLUTION.x, RESOLUTION.y);
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
        disposeTextures();
        board.dispose();
	}

    private void disposeTextures() {
        for(TextureAtlas atlas : textureAtlases.values()) {
            atlas.dispose();
        }
        textureAtlases.clear();
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

    public AnimationManager<String> getAnimationManager() {
        return animationManager;
    }
}
