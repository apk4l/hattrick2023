package com.mygdx.hattrick2023;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Timer;

import java.net.SocketException;
import java.util.List;

/**
 * Created by Administrator on 07-Nov-16.
 */
public class MainMenuScreen implements Screen, GameListener {

    private Game game;

    private Rectangle createBounds, joinBounds, cancelBounds, quitBounds;
    private TextButton createButton, joinButton, cancelButton, quitButton;

    private GameClient gameClient;

    private GameClient gameClient2;
    private GameClient myClient2;
    private OrthographicCamera guiCam;
    private SpriteBatch batch;
    private Vector3 touchpoint;

    private BitmapFont font;
    public static String debugText = "";
    private Skin skin;
    private Stage stage, cancelStage;
    private boolean hideButtons;


    private DeviceAPI mController;

    private int SCREEN_WIDTH, SCREEN_HEIGHT;

    public MainMenuScreen(Game game, DeviceAPI mController) {
        this.game = game;
        batch = new SpriteBatch();

        guiCam = new OrthographicCamera(GAME_WIDTH, GAME_HEIGHT);
        guiCam.setToOrtho(false);
        guiCam.position.set(0, 0, 0);
        batch.setProjectionMatrix(guiCam.combined);

        SCREEN_WIDTH = Gdx.graphics.getWidth();
        SCREEN_HEIGHT = Gdx.graphics.getHeight();

        createBasicSkin();

        float scaleX = 0.5f * SCREEN_WIDTH / GAME_WIDTH;
        float scaleY = 0.5f * SCREEN_HEIGHT / GAME_HEIGHT;

        createButton = new TextButton("Create Game", skin);
        createButton.setPosition(SCREEN_WIDTH / 2 - createButton.getWidth() / 2,
                SCREEN_HEIGHT / 2 - createButton.getHeight());
        createButton.getLabel().setFontScale(scaleX, scaleY);

        joinButton = new TextButton("Join Game", skin);
        joinButton.setPosition(SCREEN_WIDTH / 2 - joinButton.getWidth() / 2,
                SCREEN_HEIGHT / 2 + joinButton.getHeight());
        joinButton.getLabel().setFontScale(scaleX, scaleY);

        quitButton = new TextButton("Quit", skin);
        quitButton.setPosition(SCREEN_WIDTH / 2 - quitButton.getWidth() / 2,
                SCREEN_HEIGHT / 2 - quitButton.getHeight() * 3);
        quitButton.getLabel().setFontScale(scaleX, scaleY);

        createBounds = new Rectangle(SCREEN_WIDTH / 2 - createButton.getWidth() / 2,
                SCREEN_HEIGHT / 2 - createButton.getHeight(),
                createButton.getWidth(), createButton.getHeight());
        joinBounds = new Rectangle(SCREEN_WIDTH / 2 - joinButton.getWidth() / 2,
                SCREEN_HEIGHT / 2 + joinButton.getHeight(),
                joinButton.getWidth(), joinButton.getHeight());
        quitBounds = new Rectangle(SCREEN_WIDTH / 2 - quitButton.getWidth() / 2,
                SCREEN_HEIGHT / 2 - quitButton.getHeight() * 3,
                quitButton.getWidth(), quitButton.getHeight());

        stage = new Stage();
        stage.addActor(createButton);
        stage.addActor(joinButton);
        stage.addActor(quitButton);

        cancelButton = new TextButton("Cancel", skin);
        cancelButton.setPosition(SCREEN_WIDTH / 2 - cancelButton.getWidth() / 2,
                SCREEN_HEIGHT / 2);
        cancelButton.getLabel().setFontScale(scaleX, scaleY);

        cancelBounds = new Rectangle(SCREEN_WIDTH / 2 - cancelButton.getWidth() / 2,
                SCREEN_HEIGHT / 2,
                cancelButton.getWidth(), cancelButton.getHeight());

        cancelStage = new Stage();
        cancelStage.addActor(cancelButton);

        touchpoint = new Vector3();
        font = new BitmapFont();

        hideButtons = false;

        this.mController = mController;
        debugText = mController.getIpAddress();
    }

    private void update() throws SocketException {
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            if (gameClient != null) {
                if (gameClient.isConnected()) {
                    gameClient.disconnect();
                } else {
                    gameClient.cancel();
                }
            }
            Gdx.app.exit();
        }

        if (Gdx.input.justTouched()) {
            guiCam.unproject(touchpoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (!hideButtons) {
                if (createBounds.contains(touchpoint.x, touchpoint.y)) {
                    if (mController.isConnectedToLocalNetwork()) {
                        hideButtons = true;
                        createServer();
                    } else {
                        mController.showNotification("Not connected to a network.");
                        Gdx.app.log("mygdxgame", "Not connected to a network");
                        debugText = "Not connected to a network";
                    }
                }

                if (joinBounds.contains(touchpoint.x, touchpoint.y)) {
                    if (mController.isConnectedToLocalNetwork()) {
                        hideButtons = true;
                        runClient();
                    } else {
                        mController.showNotification("Not connected to a network.");
                        Gdx.app.log("mygdxgame", "Not connected to a network");
                        debugText = "Not connected to a network";
                    }
                }

                if (quitBounds.contains(touchpoint.x, touchpoint.y)) {
                    if (gameClient != null) {
                        if (gameClient.isConnected()) {
                            gameClient.disconnect();
                        } else {
                            gameClient.cancel();
                        }
                    }
                    Gdx.app.exit();
                }
            } else {
                if (cancelBounds.contains(touchpoint.x, touchpoint.y)) {
                    gameClient.cancel();
                    hideButtons = false;
                }
            }
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!hideButtons) {
            stage.act();
            stage.draw();
        } else {
            cancelStage.act();
            cancelStage.draw();
        }

        batch.begin();
        font.draw(batch, debugText, 0, guiCam.viewportHeight - 10);
        batch.end();
    }

    @Override
    public void render(float delta) {
        draw();
        try {
            update();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private void createServer() throws SocketException {
        gameClient = new MyServer(this, mController.getIpAddress(), 45351,  game, mController, 1);
        Thread t = new Thread(gameClient);
        t.start();
    }

    private void runClient() {
        debugText = "Starting up client thread";

        gameClient = new MyServer(this, mController.getIpAddress(), 45351,  game, mController, 2);
        Thread t = new Thread(gameClient);
        t.start();
    }

    private void createBasicSkin(){
        //Create a font
        BitmapFont font = new BitmapFont();
        skin = new Skin();
        skin.add("default", font);

        //Create a texture
        Pixmap pixmap = new Pixmap((int)SCREEN_WIDTH/4,(int)SCREEN_HEIGHT/10, Pixmap.Format.RGB888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("background",new Texture(pixmap));

        //Create a button style
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("background", Color.GRAY);
        textButtonStyle.down = skin.newDrawable("background", Color.DARK_GRAY);
        textButtonStyle.checked = skin.newDrawable("background", Color.DARK_GRAY);
        textButtonStyle.over = skin.newDrawable("background", Color.LIGHT_GRAY);
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);
    }

    @Override
    public void onConnected() {
        debugText = "Connected to socket";
        mController.showNotification("Player found");
        mController.setCallback(gameClient);

        Gdx.app.log(DeviceAPI.TAG, "Setting timer");

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                Gdx.app.log(DeviceAPI.TAG, "Setting screen");
                game.setScreen(new GameScreen(game, mController, gameClient));
                Gdx.app.log(DeviceAPI.TAG, "Screen set");
            }
        }, 0.2f);
        Gdx.app.log(DeviceAPI.TAG, "Timer set");

    }

    @Override
    public void onMessageReceived(byte[] data, int length) {
        String message = new String(data, 0, length);
        // Process the received message as before
    }
    @Override
    public void onDisconnected() {
        debugText = "Disconnected";
        mController.showNotification("Lost connection with other player");

        hideButtons = false;
    }

    @Override
    public void onConnectionFailed() {
        debugText = "Failed to find a connection";
        Gdx.app.log(mController.TAG, "Failed to find a game");

        hideButtons = false;
//        mController.showNotification("Failed to find a game. Try again.");
    }

    public void appendText(String text) { debugText += "\n" + text; }

    public DeviceAPI getDeviceAPI() { return mController; }

    @Override public void show() {}

    @Override public void resize(int width, int height) {}

    @Override public void pause() {
        Gdx.app.log(DeviceAPI.TAG, "Paused");
    }
    @Override public void resume() {
        Gdx.app.log(DeviceAPI.TAG, "Paused");
    }
    @Override public void hide() {}
    @Override public void dispose() {}
}
