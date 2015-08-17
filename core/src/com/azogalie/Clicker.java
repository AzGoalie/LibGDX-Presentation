package com.azogalie;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Clicker extends ApplicationAdapter {
    final int SCREEN_WIDTH = 720;
    final int SCREEN_HEIGHT = 1024;

    final float PPM = 100f;
    final int GROUND_HEIGHT = 125;
    final int COIN_ACCEL = 500;

    SpriteBatch batch;
    TextureAtlas atlas;
    OrthographicCamera camera;

    Sprite background;
    Sprite ground;
    Sprite grass;
    Sprite coinbox;
    Sprite coin;

    Sound sound;

    Array<Body> coins;

    World world;

    @Override
    public void create() {
        batch = new SpriteBatch();
        atlas = new TextureAtlas("game.atlas");
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

        Gdx.input.setInputProcessor(new InputHandler());


        // Sprites
        background = atlas.createSprite("background");
        ground = atlas.createSprite("ground");
        grass = atlas.createSprite("grass");
        coinbox = atlas.createSprite("coinbox");
        coin = atlas.createSprite("coin");

        coinbox.setPosition(SCREEN_WIDTH / 2 - coinbox.getWidth() / 2,
                SCREEN_HEIGHT / 2 - coinbox.getHeight() / 2);

        // Sounds
        sound = Gdx.audio.newSound(Gdx.files.internal("coin.wav"));

        coins = new Array<Body>();

        Box2D.init();
        world = new World(new Vector2(0, -10), true);

        createWall(0, 0, SCREEN_WIDTH, GROUND_HEIGHT);
        createWall(0, 0, 1, SCREEN_HEIGHT);
        createWall(SCREEN_WIDTH, 0, 1, SCREEN_HEIGHT);
        createWall(0, SCREEN_HEIGHT, SCREEN_WIDTH, 1);
    }

    @Override
    public void dispose() {
        super.dispose();
        atlas.dispose();
        batch.dispose();
        sound.dispose();
        world.dispose();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        background.draw(batch);
        grass.draw(batch);
        coinbox.draw(batch);

        for (Body c : coins) {
            coin.setPosition((c.getPosition().x * PPM) - coin.getWidth() / 2,
                    (c.getPosition().y * PPM) - coin.getHeight() / 2);
            coin.setRotation(c.getAngle() * MathUtils.radiansToDegrees);
            coin.draw(batch);
        }

        batch.end();

        world.step(1f / 60f, 6, 2);
    }

    private void createWall(float x, float y, float w, float h) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set((x + w / 2) / PPM, (y + h / 2) / PPM);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w / 2 / PPM, h / 2 / PPM);

        body.createFixture(shape, 0.0f);
        shape.dispose();
    }

    public void spawnCoin() {
        sound.play();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((SCREEN_WIDTH/2) / PPM,
                (SCREEN_HEIGHT/2) / PPM);

        Body body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(coin.getWidth() / 2 / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f;

        body.createFixture(fixtureDef);
        shape.dispose();

        float radians = MathUtils.random(10, 170) * MathUtils.degreesToRadians;
        body.applyForceToCenter(MathUtils.cos(radians) * COIN_ACCEL,
                MathUtils.sin(radians) * COIN_ACCEL, true);
        coins.add(body);
    }

    private class InputHandler extends InputAdapter {
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            Vector3 pos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(pos);
            if (coinbox.getBoundingRectangle().contains(pos.x, pos.y)) {
                spawnCoin();
            }
            return super.touchUp(screenX, screenY, pointer, button);
        }
    }
}
