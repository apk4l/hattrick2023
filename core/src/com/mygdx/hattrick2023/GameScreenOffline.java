package com.mygdx.hattrick2023;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 15-Nov-16.
 */
public class GameScreenOffline implements Screen, GameListener {
    private DeviceAPI mController;

    public void MyGdxGame(DeviceAPI mController) {
        this.mController = mController;
    }

    private Game game;
    private GameClientInterface gameClient;

    private OrthographicCamera guiCam;
    private SpriteBatch batch;
    private Vector3 touchPoint;
    private Texture img;
    private TextureRegion background;
    private Sprite backgroundSprite;

    private Puck puck;
    private Player myPlayer;
    private otherPlayer otherPlayer;
    private Player[] players;
    private otherPlayer[] player;
    private int SCREEN_WIDTH, SCREEN_HEIGHT;

    private float offset = 70;
    private float scoreVerticalOffset = 40, scoreHorizontalOffset = 15;

    private Map<Integer, Sprite> spriteMap;

    private boolean yDown;

    public GameScreenOffline(Game game, DeviceAPI mController) {
        this.game = game;
        this.mController = mController;
        createGame();
    }

    public void createGame() {
        Gdx.input.setCatchBackKey(true);
        guiCam = new OrthographicCamera();
        Viewport gamePort = new StretchViewport(GAME_WIDTH, GAME_HEIGHT, guiCam);
        yDown = true;
        guiCam.setToOrtho(yDown);

        guiCam.position.set(0, 0, 0);

        batch = new SpriteBatch();
        batch.setProjectionMatrix(guiCam.combined);

        SCREEN_WIDTH = Gdx.graphics.getWidth();
        SCREEN_HEIGHT = Gdx.graphics.getHeight();

        spriteMap = new HashMap<Integer, Sprite>();
        for (int i = 0; i < 10; i++) {
            Sprite sprite = new Sprite(new Texture(i + ".png"));
            sprite.setSize(sprite.getWidth(), sprite.getHeight());
            spriteMap.put(i, sprite);
        }

        img = new Texture("playingfieldtest.png");
        background = new TextureRegion(img, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        backgroundSprite = new Sprite(img);
        backgroundSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        puck = new Puck();
        myPlayer = new Player();
        otherPlayer = new otherPlayer();

        offset = offset * SCREEN_HEIGHT / GAME_HEIGHT;
        scoreVerticalOffset = scoreVerticalOffset * SCREEN_HEIGHT / GAME_HEIGHT;
        scoreHorizontalOffset = scoreHorizontalOffset * SCREEN_WIDTH / GAME_WIDTH;

        if (!yDown) {
            myPlayer.setPosition(SCREEN_WIDTH / 2, offset);
            otherPlayer.setPosition(SCREEN_WIDTH / 2, SCREEN_HEIGHT - offset);
        } else {
            myPlayer.setPosition(SCREEN_WIDTH / 2, SCREEN_HEIGHT - offset);
            otherPlayer.setPosition(SCREEN_WIDTH / 2, offset);
            scoreVerticalOffset = -scoreVerticalOffset;
        }

        touchPoint = new Vector3();
        Gdx.input.setCatchBackKey(false);
    }

    //  public void setGameClient(GameClientInterface gameClient) {
    // this.gameClient = gameClient;
    //  this.gameClient.setListener(this);
    // }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update();

        batch.begin();
        draw();
        drawScores();
        drawPaddle1();
        drawPaddle2();
        drawPuck();
        batch.end();

    }

    public void draw() {
        backgroundSprite.draw(batch);
    }

    public void drawScores() {
        otherPlayer.score1.setPosition(scoreHorizontalOffset,
                SCREEN_HEIGHT / 2 + scoreVerticalOffset - otherPlayer.score1.getHeight());
        otherPlayer.score1.draw(batch);

        otherPlayer.score2.setPosition(scoreHorizontalOffset * 1.1f + otherPlayer.score1.getWidth(),
                SCREEN_HEIGHT / 2 + scoreVerticalOffset - otherPlayer.score2.getHeight());
        otherPlayer.score2.draw(batch);

        myPlayer.score1.setPosition(scoreHorizontalOffset,
                SCREEN_HEIGHT / 2 - scoreVerticalOffset);
        myPlayer.score1.draw(batch);

        myPlayer.score2.setPosition(scoreHorizontalOffset * 1.1f + otherPlayer.score1.getWidth(),
                SCREEN_HEIGHT / 2 - scoreVerticalOffset);
        myPlayer.score2.draw(batch);
    }

    public void drawPaddle1() {
        myPlayer.draw();
    }

    public void drawPaddle2() {
        otherPlayer.draw();
    }

    public void drawPuck() {
        puck.draw();
    }

    private Player draggingPlayer = null;

    private otherPlayer draggingPlayer2 = null;
    private Vector2 dragOffset2 = new Vector2();
    private Vector2 dragOffset1 = new Vector2();

    public void update() {
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            Gdx.app.exit();
        }

        puck.update();

        if (Gdx.input.isTouched()) {
            guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (draggingPlayer == null && myPlayer.getBounds().contains(touchPoint.x, touchPoint.y) && draggingPlayer2 == null) {
                draggingPlayer = myPlayer;
                dragOffset1.set((float) (touchPoint.x - myPlayer.x), (float) (touchPoint.y - myPlayer.y));
            } else if (draggingPlayer2 == null && otherPlayer.getBounds().contains(touchPoint.x, touchPoint.y) && draggingPlayer == null) {
                draggingPlayer2 = otherPlayer;
                dragOffset2.set((float) (touchPoint.x - otherPlayer.x), (float) (touchPoint.y - otherPlayer.y));
            } else if (!myPlayer.getBounds().contains(touchPoint.x, touchPoint.y) && !otherPlayer.getBounds().contains(touchPoint.x, touchPoint.y)) {
                draggingPlayer = null;
                draggingPlayer2 = null;
            }

            if (draggingPlayer != null && Gdx.input.isTouched()) {
                guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));


                // calculate new position of player paddle based on how much touch point has moved
                float newX = touchPoint.x - dragOffset1.x;
                float newY = touchPoint.y - dragOffset1.y;

                // update player paddle position
                myPlayer.update(newX, newY);
            }

            if (draggingPlayer2 != null && Gdx.input.isTouched()) {
                guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));



                // calculate new position of player paddle based on how much touch point has moved
                float otherX = touchPoint.x - dragOffset2.x;
                float otherY = touchPoint.y - dragOffset2.y;

                // update player paddle position
                otherPlayer.update(otherX, otherY);
            }
        }
        checkCollision(myPlayer, puck);
        checkCollision2(otherPlayer, puck);
    }
        public void checkCollision(Player player, Puck puck) {
        puck.checkCollision(player);

        double distance = Math.sqrt(Math.pow(puck.x-player.x, 2)+Math.pow(puck.y-player.y, 2));
        if(distance<Math.sqrt(Math.pow(puck.radius+player.radius, 2)))
        {
            puck.x = (puck.x-player.x)*(puck.radius+player.radius)/distance+player.x;
            puck.y = (puck.y-player.y)*(puck.radius+player.radius)/distance+player.y;
        }
    }
    public void checkCollision2(otherPlayer otherPlayer, Puck puck) {
       puck.checkCollision2(otherPlayer);

        double distance = Math.sqrt(Math.pow(puck.x-otherPlayer.x, 2)+Math.pow(puck.y-otherPlayer.y, 2));
        if(distance<Math.sqrt(Math.pow(puck.radius+otherPlayer.radius, 2)))
        {
            puck.x = (puck.x-otherPlayer.x)*(puck.radius+otherPlayer.radius)/distance+otherPlayer.x;
            puck.y = (puck.y-otherPlayer.y)*(puck.radius+otherPlayer.radius)/distance+otherPlayer.y;
        }
    }
    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {
        gameClient.disconnect();
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                game.setScreen(new MainMenuScreen(game, mController));
                mController.showNotification("Disconnected from other player");
            }
        }, 0.2f);
    }

    @Override
    public void onConnectionFailed() {}

    @Override
    public void onMessageReceived(byte[] data, int length) {

    }

    //  @Override
  //  public void onMessageReceived(String message) {
  //      String[] coords = message.split(",");
//
   //     otherPlayer.update(SCREEN_WIDTH - Float.parseFloat(coords[0]) * SCREEN_WIDTH / GAME_WIDTH,
 //               Float.parseFloat(coords[1]) * SCREEN_HEIGHT / GAME_HEIGHT);

   //     if (gameClient.getPlayerNumber() == PLAYER2) {
  //          puck.update(
   //                SCREEN_WIDTH - Double.parseDouble(coords[2]) * SCREEN_WIDTH / GAME_WIDTH,
    //                Double.parseDouble(coords[3]) * SCREEN_HEIGHT / GAME_HEIGHT,
   //                 Double.parseDouble(coords[4]),
  //                  Double.parseDouble(coords[5])
   //         );
  //      }
  //  }

    @Override
    public DeviceAPI getDeviceAPI() {
        return mController;
    }

    class Puck {
        public Vector2d velocity = new Vector2d(0, 0);
        public double x, y;
        public int radius = 30 / 2;

        int leftBound = 100;
        int rightBound = 220;

        Sprite puckSprite;
        Sound edgeHitSound, playerHitSound, goalSound;

        public Puck() {
            radius = radius * SCREEN_WIDTH / GAME_WIDTH;
            x = SCREEN_WIDTH / 2;
            y = SCREEN_HEIGHT / 2;
            Vector2d velocity = new Vector2d(0, 0);
            puckSprite = new Sprite(new Texture("puckGrey.png"));
            puckSprite.setSize(radius * 2, radius * 2);

            velocity.i = 0;
            velocity.j = 0;

            leftBound = leftBound * SCREEN_WIDTH / GAME_WIDTH;
            rightBound = rightBound * SCREEN_WIDTH / GAME_WIDTH;

            edgeHitSound = Gdx.audio.newSound(Gdx.files.internal("EdgeHit.ogg"));
            playerHitSound = Gdx.audio.newSound(Gdx.files.internal("PlayerHit.ogg"));
            goalSound = Gdx.audio.newSound(Gdx.files.internal("Goal.ogg"));

        }

        public float getWidth() {
            return puckSprite.getWidth();
        }

        public float getHeight() {
            return puckSprite.getHeight();
        }

        public void setPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void draw() {
            puckSprite.setPosition((float) x - getWidth() / 2, (float) y - getHeight() / 2);
            puckSprite.draw(batch);
        }

        public void update() {
            double minVelocity = 4.5; // adjust as necessary
            x += velocity.i *= .98;
            y += velocity.j *= .98;
            // check if velocity falls below minimum threshold
            if (Math.hypot(velocity.i, velocity.j) < minVelocity) {
                // increase velocity to minimum threshold
                double angle = Math.atan2(velocity.j, velocity.i);
                velocity.i = minVelocity * Math.cos(angle);
                velocity.j = minVelocity * Math.sin(angle);
            }
            // bounce off left wall
            if (x <= radius + 70) {
                velocity.i = Math.abs(velocity.i); //bounce
                x = radius + 70;
                edgeHitSound.play();
            }


            // bounce off right wall
            if (x >= SCREEN_WIDTH - (radius + 70)) {
                velocity.i = -Math.abs(velocity.i);
                x = SCREEN_WIDTH - (radius + 70);
                edgeHitSound.play();
            }

            // bounce off bottom
            if (y <= (radius + 100)) {

                // goal
                if (x >= leftBound && x <= rightBound) {
                    goalSound.play();
                    reset();

                    if (!yDown) {
                        otherPlayer.score++;
                        otherPlayer.updateScore();
                    } else {
                        myPlayer.score++;
                        myPlayer.updateScore();
                    }
//                    setPlayer2ScorePosition();
                } else {
                    velocity.j = Math.abs(velocity.j);
                    y = radius + 160;
                    edgeHitSound.play();
                }
            }

            // bounce off top for Player 1 or bottom for Player 2
            if (y >= SCREEN_HEIGHT - radius - 160) {

                //goal
                if (x >= leftBound && x <= rightBound) {
                    goalSound.play();
                    reset();

                    if (!yDown) {
                        myPlayer.score++;
                        myPlayer.updateScore();
                    } else {
                        otherPlayer.score++;
                        otherPlayer.updateScore();
                    }
//                    setPlayer1ScorePosition();
                } else {
                    velocity.j = -Math.abs(velocity.j);
                    y = SCREEN_HEIGHT - radius - 160;
                    edgeHitSound.play();
                }
            }
        }

        void update(double newX, double newY, double i, double j) {
            velocity.i = i;
            velocity.j = j;

            x = newX;
            y = newY;
        }

        void reset() {
            velocity.i = 0;
            velocity.j = 0;

            x = SCREEN_WIDTH / 2;
            y = SCREEN_HEIGHT / 2;
        }

        private long lastCollisionTime = 0;
        private static final long COLLISION_COOLDOWN = 500; // adjust as necessary

        public boolean checkCollision(Player p){
            if(Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2) <= Math.pow(p.radius + radius, 2)){
                playerHitSound.play();

                Vector2d collisionDirection = new Vector2d(x-p.x, y-p.y);
                velocity = p.velocity.proj(collisionDirection).plus(velocity.proj(collisionDirection).times(-1)
                        .plus(velocity.proj(new Vector2d(collisionDirection.j, -collisionDirection.i)))).times(0.9);
                return true;
            }
            return false;
        }


        public boolean checkCollision2(otherPlayer p) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastCollisionTime > COLLISION_COOLDOWN &&
                    Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2) <= Math.pow(p.radius + radius, 2)) {
                playerHitSound.play();

                Vector2d collisionDirection = new Vector2d(x - p.x, y - p.y);
                velocity = p.velocity.proj(collisionDirection).plus(velocity.proj(collisionDirection).times(-1)
                        .plus(velocity.proj(new Vector2d(collisionDirection.j, -collisionDirection.i)))).times(0.9);

                lastCollisionTime = currentTime;
                return true;
            }
            return false;
        }

        /**
         * Reflects the puck's velocity off a surface with the given normal vector.
         *
         * @param normal the normal vector of the surface to reflect off
         */
        private void reflectVelocity(Vector2d normal) {
            // compute dot product of velocity and normal
            double dot = velocity.dot(normal);

            // reflect velocity off the surface
            Vector2d reflection = velocity.times(-1).plus(normal.times(2 * dot));
            velocity.set(reflection.i, reflection.j);
        }
    }
    class Player {
        public int score = 0;

        Sprite score1, score2;

        public Vector2d velocity = new Vector2d(0,0);
        int radius = 70/2;
        public double x = 0;
        public double y = 0;

        Texture playerImg;
        Texture otherPlayerImg;
        Sprite playerSprite;
        Sprite otherPlayerSprite;
        public Player() {
            otherPlayerImg = new Texture("Player2.png");
            playerImg = new Texture("Paddle.png");
            playerSprite = new Sprite(playerImg);
            otherPlayerSprite = new Sprite(otherPlayerImg);
            radius = radius * SCREEN_WIDTH / GAME_WIDTH;

            playerSprite.setSize((float) (radius * 1.5),
                    (float) (radius * 1.5));
            otherPlayerSprite.setSize((float) (radius * 1.5),
                    (float) (radius * 1.5));


            updateScore();
        }
        public void limitMovement() {
            if (y < SCREEN_HEIGHT / 2 - radius) {
                y = SCREEN_HEIGHT / 2 - radius;
            } else if (y > SCREEN_HEIGHT / 2 + radius) {
                y = SCREEN_HEIGHT / 2 + radius;
            }
        }
        public Rectangle getBounds() {
            return new Rectangle((float) x - radius, (float) y - radius, radius * 2, radius * 2);
        }
        public float getWidth() {return playerSprite.getWidth();}
        public float getHeight() {return playerSprite.getHeight();}

        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void draw() {
            playerSprite.setPosition((float)x - getWidth() / 2, (float)y - getHeight() / 2);
            playerSprite.draw(batch);
        }

        public void update(float x, float y) {

            velocity.i = x - this.x;
            velocity.j = y - this.y;
            this.x = x;
            this.y = y;
            // limit the paddle's movement
            if (yDown) {
                if (y < SCREEN_HEIGHT / 2) {
                    this.y = SCREEN_HEIGHT / 2;
                } else if (y < SCREEN_HEIGHT / 2) {
                    this.y = SCREEN_HEIGHT / 2;
                }
            }
        }

        public void updateScore() {
            score1 = spriteMap.get(score / 10);
            score2 = spriteMap.get(score % 10);
        }
    }


    private class otherPlayer {
        public int score = 0;

        Sprite score1, score2;

        public Vector2d velocity = new Vector2d(0,0);
        int radius = 70/2;
        public double x = 0;
        public double y = 0;

        Texture playerImg;
        Texture otherPlayerImg;
        Sprite playerSprite;
        Sprite otherPlayerSprite;
        public otherPlayer() {
            otherPlayerImg = new Texture("Player2.png");
            otherPlayerSprite = new Sprite(otherPlayerImg);
            radius = radius * SCREEN_WIDTH / GAME_WIDTH;

            otherPlayerSprite.setSize((float) (radius * 1.5),
                    (float) (radius * 1.5));


            updateScore();
        }
        public Rectangle getBounds() {
            return new Rectangle((float) x - radius, (float) y - radius, radius * 2, radius * 2);
        }
        public float getWidth() {return otherPlayerSprite.getWidth();}
        public float getHeight() {return otherPlayerSprite.getHeight();}


        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void draw() {
            otherPlayerSprite.setPosition((float)x - getWidth() / 2, (float)y - getHeight() / 2);
            otherPlayerSprite.draw(batch);
        }

        public void update(float x, float y) {
            velocity.i = x - this.x;
            velocity.j = y - this.y;
            this.x = x;
            this.y = y;
            // limit the paddle's movement
            if (y > SCREEN_HEIGHT / 2) {
                this.y = SCREEN_HEIGHT / 2;
            } else if (y > SCREEN_HEIGHT / 2) {
                this.y = SCREEN_HEIGHT / 2 ;
            }
        }

        public void updateScore() {
            score1 = spriteMap.get(score / 10);
            score2 = spriteMap.get(score % 10);
        }
    }
    private void drawTriangle(ShapeRenderer shapeRenderer, Color color, Vector2 p1, Vector2 p2, Vector2 p3) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.triangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
        shapeRenderer.end();
    }
    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}


    private double sign(double x1, double y1, double x2, double y2, double x3, double y3) {
        return (x1 - x3) * (y2 - y3) - (x2 - x3) * (y1 - y3);
    }

}