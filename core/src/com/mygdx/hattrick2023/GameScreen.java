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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 15-Nov-16.
 */
public class GameScreen implements Screen, GameListener {

    private int myPlayerNumber; // added field
    private DeviceAPI mController;

    private GameClientInterface gameClient;
    public void MyGdxGame(DeviceAPI mController) {
        this.mController = mController;
    }

    private Game game;

    private MyServer myServer;
    private MyClient2 myClient2;
    private OrthographicCamera guiCam;
    private SpriteBatch batch;
    private Vector3 touchPoint;
    private Texture img;
    private TextureRegion background;
    private Sprite backgroundSprite;

    private Puck puck;
    private Player myPlayer, otherPlayer;
    private Player[] players;

    private int SCREEN_WIDTH, SCREEN_HEIGHT;

    private float offset = 70;
    private float scoreVerticalOffset = 40, scoreHorizontalOffset = 15;

    private Map<Integer, Sprite> spriteMap;

    private boolean yDown;
    private MyClient myClient;
    private Vector2 lastReceivedPuckPosition = new Vector2();
    private Vector2 lastReceivedPuckVelocity = new Vector2();
    private float lastReceivedPuckTime = 0;
    private Vector2 predictedPuckPosition = new Vector2();
    private Vector2 predictedPuckVelocity = new Vector2();
    private float predictedPuckTime = 0;

    MyClient2 client2;
    public GameScreen(Game game, DeviceAPI mController, GameClientInterface gameClient) {
        this.game = game;
        this.mController = mController;;
        this.gameClient = gameClient;
        this.gameClient.setListener(this);


        createGame();
    }


    public void createGame() {
        guiCam = new OrthographicCamera();
        Viewport gamePort = new StretchViewport(GAME_WIDTH, GAME_HEIGHT, guiCam);
        yDown = gameClient.getPlayerNumber() != PLAYER1;
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
        if (gameClient instanceof MyServer) {
            myServer = (MyServer) gameClient;
        }
        img = new Texture("playingfieldtest.png");
        background = new TextureRegion(img, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        backgroundSprite = new Sprite(img);
        backgroundSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        puck = new Puck();
        myPlayer = new Player();
        otherPlayer = new Player();

        offset = offset * SCREEN_HEIGHT / GAME_HEIGHT;
        scoreVerticalOffset = scoreVerticalOffset * SCREEN_HEIGHT / GAME_HEIGHT;
        scoreHorizontalOffset = scoreHorizontalOffset * SCREEN_WIDTH / GAME_WIDTH;

        if(!yDown) {
            myPlayer.setPosition(SCREEN_WIDTH / 2, offset);
            otherPlayer.setPosition(SCREEN_WIDTH / 2, SCREEN_HEIGHT - offset);
        } else {
            myPlayer.setPosition(SCREEN_WIDTH / 2, SCREEN_HEIGHT - offset);
            otherPlayer.setPosition(SCREEN_WIDTH / 2, offset);
            scoreVerticalOffset = -scoreVerticalOffset;
        }
        Gdx.app.log("GameScreen", "Player number: " + gameClient.getPlayerNumber());
        touchPoint = new Vector3();
        Gdx.input.setCatchBackKey(false);
    }
    public void setGameClient(GameClientInterface gameClient) {
        this.gameClient = gameClient;
        this.gameClient.setListener(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(delta);

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

    public void drawPaddle2() { otherPlayer.draw(); }

    public void drawPuck() {
        puck.draw();
    }

    private Player draggingPlayer = null;
    private Vector2 dragOffset2 = new Vector2();
    private Vector2 dragOffset1 = new Vector2();

    public void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            gameClient.disconnect();
            Gdx.app.exit();
        }




        if (Gdx.input.isTouched()) {
            guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));



            if (draggingPlayer == null && myPlayer.getBounds().contains(touchPoint.x, touchPoint.y)) {
                draggingPlayer = myPlayer;
                dragOffset1.set((float) (touchPoint.x - myPlayer.x), (float) (touchPoint.y - myPlayer.y));
            } else if (!myPlayer.getBounds().contains(touchPoint.x, touchPoint.y)) {
                draggingPlayer = null;
            }
            if (draggingPlayer != null && Gdx.input.isTouched()) {
                guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
                float newX = touchPoint.x - dragOffset1.x;
                float newY = touchPoint.y - dragOffset1.y;

                myPlayer.update(newX, newY);
            }
        //    if ((yDown && touchPoint.y <= SCREEN_HEIGHT / 2)/           || (!yDown && touchPoint.y >= SCREEN_HEIGHT / 2)) {
         //       touchPoint.y = SCREEN_HEIGHT / 2;
         //   }

          //  if ((yDown && touchPoint.y <= SCREEN_HEIGHT / 2)
        //           || (!yDown && touchPoint.y >= SCREEN_HEIGHT / 2)) {
       //        touchPoint.y = SCREEN_HEIGHT / 2;
       //    }



            if (gameClient.getPlayerNumber() == PLAYER2) {
                    gameClient.sendMessage2(myPlayer.x * GAME_WIDTH / SCREEN_WIDTH + "," +
                            myPlayer.y * GAME_HEIGHT / SCREEN_HEIGHT);

                }
            }

            String s = touchPoint.x + ", " + touchPoint.y;
       //    gameClient.sendVoiceMessage(s.getBytes());




        if (gameClient.getPlayerNumber() == PLAYER1) {
            checkCollision(myPlayer, puck);
            checkCollision(otherPlayer, puck);
            gameClient.sendMessage(myPlayer.x * GAME_WIDTH / SCREEN_WIDTH
                   + "," + myPlayer.y * GAME_HEIGHT / SCREEN_HEIGHT
                   + "," + puck.x * GAME_WIDTH / SCREEN_WIDTH
                   + "," + puck.y * GAME_HEIGHT / SCREEN_HEIGHT);
        }

        if (gameClient.getPlayerNumber() == PLAYER2) {
            checkCollisionPlayer2(myPlayer, puck);
            // Update the predicted puck position and velocity
            predictedPuckTime += delta;
            predictedPuckPosition.x += predictedPuckVelocity.x + delta;
            predictedPuckPosition.y += predictedPuckVelocity.y * delta;

            // Reconcile the predicted and received puck positions
            float lerpFactor = 0.9f; // Adjust this value to control the smoothness of the reconciliation
            puck.x = predictedPuckPosition.x * (1 - lerpFactor) + lastReceivedPuckPosition.x * lerpFactor;
            puck.y = predictedPuckPosition.y * (1 - lerpFactor) + lastReceivedPuckPosition.y * lerpFactor;
            puck.update2(puck.x, puck.y, lastReceivedPuckVelocity.x, lastReceivedPuckVelocity.y);

        } else {
            puck.update();
        }

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
    public void checkCollisionPlayer2(Player player, Puck puck) {
        puck.checkCollisionPlayer2(player);
        Vector2 newPuck = new Vector2();

        newPuck.x = (float) puck.x;
        newPuck.y = (float) puck.y;
        double distance = Math.sqrt(Math.pow(puck.x-player.x, 2)+Math.pow(puck.y-player.y, 2));
        if(distance<Math.sqrt(Math.pow(puck.radius+player.radius, 2)))
        {
            puck.x = newPuck.x;
            puck.y = newPuck.y;
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
        String message = new String(data, 0, length);
        Gdx.app.log("Received Message", message); // Add this line
        try {
            String[] coords = message.split(",");
            String[] tokens = message.split(",");
            if (Float.parseFloat(coords[0]) == 9999999) {
                //trigger a puck collision
            } else {
                float mirroredX = Float.parseFloat(coords[0]) * SCREEN_WIDTH / GAME_WIDTH;
                float mirroredY = Float.parseFloat(coords[1]) * SCREEN_HEIGHT / GAME_HEIGHT;
                Gdx.app.log("GameScreen", "Parsed coordinates: " + Arrays.toString(coords));
                Gdx.app.log("GameScreen", "Coords array size: " + coords.length);
                Gdx.app.log("GameScreen", "Parsed mirroredX: " + mirroredX);
                Gdx.app.log("GameScreen", "Parsed mirroredY: " + mirroredY);
                if (yDown) {
                    mirroredY = SCREEN_HEIGHT - mirroredY;
                }
                otherPlayer.update(mirroredX, mirroredY);

                if (gameClient.getPlayerNumber() == PLAYER2) {
                    otherPlayer.x = Float.parseFloat(tokens[0]) * SCREEN_WIDTH / GAME_WIDTH;
                    otherPlayer.y = Float.parseFloat(tokens[1]) * SCREEN_HEIGHT / GAME_HEIGHT;
                    lastReceivedPuckPosition.set(Float.parseFloat(tokens[2]) * SCREEN_WIDTH / GAME_WIDTH,
                            Float.parseFloat(tokens[3]) * SCREEN_HEIGHT / GAME_HEIGHT);
                    lastReceivedPuckTime = 0;

                    // Set the predicted puck position and velocity
                    predictedPuckPosition.set(lastReceivedPuckPosition);
                    predictedPuckVelocity.set(lastReceivedPuckVelocity);
                    predictedPuckTime = 0;
                }
                Gdx.app.log("GameScreen", "Updating otherPlayer position to: (" + mirroredX + ", " + mirroredY + ")");
                Gdx.app.log("GameScreen", "Updating lastReceivedPuckPosition to: (" + lastReceivedPuckPosition.x + ", " + lastReceivedPuckPosition.y + ")");
                Gdx.app.log("GameScreen", "Updating predictedPuckPosition to: (" + predictedPuckPosition.x + ", " + predictedPuckPosition.y + ")");
            }
            } catch(Exception e){
                Gdx.app.error("GameScreen", "Exception caught while parsing message", e);
            }

    }


    @Override
    public DeviceAPI getDeviceAPI() {
        return mController;
    }

    private class Puck {
        public Vector2d velocity = new Vector2d(0, 0);
        public double x, y;
        public int radius = 30 / 2;

        int leftBound = 70;
        int rightBound = 250;

        Sprite puckSprite;
        Sound edgeHitSound, playerHitSound, goalSound;

        public Puck() {
            radius = radius * SCREEN_WIDTH / GAME_WIDTH;
            x = SCREEN_WIDTH / 2;
            y = SCREEN_HEIGHT / 2;

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
            double minVelocity = 6; // adjust as necessary
            x += velocity.i *= .999;
            y += velocity.j *= .999;

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
                    y = radius + 100;
                    edgeHitSound.play();
                }
            }
            int bottomBuffer = 0;
            // bounce off top for Player 1 or bottom for Player 2

            if (yDown) {
                bottomBuffer = 160;
            } else {
                bottomBuffer = 160;
            }
            // bounce off top for Player 1 or bottom for Player 2
            if (y >= SCREEN_HEIGHT - (radius + bottomBuffer)) {


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
                    y = SCREEN_HEIGHT - (radius + bottomBuffer);
                    edgeHitSound.play();
                }
            }
        }

        void update2(double newX, double newY, double i, double j) {
            velocity.i = i;
            velocity.j = j;

            x = newX;
            y = newY;

            // bounce off left wall
            if (x <= radius + 70) {
                velocity.i = Math.abs(velocity.i); //bounce
                x = radius + 70;
            //    edgeHitSound.play();
            }


            // bounce off right wall
            if (x >= SCREEN_WIDTH - (radius + 70)) {
                velocity.i = -Math.abs(velocity.i);
                x = SCREEN_WIDTH - (radius + 70);
             //   edgeHitSound.play();
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
                    y = radius;
                 //   edgeHitSound.play();
                }
            }
            int bottomBuffer = 0;
            // bounce off top for Player 1 or bottom for Player 2

            if (yDown) {
                bottomBuffer = 160;
            } else {
                bottomBuffer = 160;
            }
            // bounce off top for Player 1 or bottom for Player 2
            if (y >= SCREEN_HEIGHT - (radius + bottomBuffer)) {


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
                    y = SCREEN_HEIGHT - (radius + bottomBuffer);
                  //  edgeHitSound.play();
                }
            }
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

        public void remoteCollision(float puckX, float puckY, float playerX,  float playerY, Player p) {
            long currentTime = System.currentTimeMillis();
            Vector2d collisionDirection = new Vector2d(puckX - playerX, puckY - playerY);
            if (currentTime - lastCollisionTime > COLLISION_COOLDOWN &&
                    Math.pow(puckX - playerX, 2) + Math.pow(puckY - playerY, 2) <= Math.pow(p.radius + radius, 2)) {

                lastCollisionTime = currentTime;
            }
        }
        public boolean checkCollisionPlayer2(Player p){
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastCollisionTime > COLLISION_COOLDOWN &&
                    Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2) <= Math.pow(p.radius + radius, 2)) {
                playerHitSound.play();

                lastCollisionTime = currentTime;
                return true;
            }
            return false;
        }
    }



    private class Player {
        public int score = 0;

        Sprite score1, score2;

        public Vector2d velocity = new Vector2d(0, 0);
        int radius = 70 / 2;
        public double x = 0;
        public double y = 0;

        Texture img;
        Sprite playerSprite;

        public Player() {
            img = new Texture("Paddle.png");
            playerSprite = new Sprite(img);

            radius = radius * SCREEN_WIDTH / GAME_WIDTH;

            playerSprite.setSize((float) (radius * 1.5),
                    (float) (radius * 1.5));

            updateScore();
        }

        public Rectangle getBounds() {
            return new Rectangle((float) x - radius, (float) y - radius, radius * 2, radius * 2);
        }

        public float getWidth() {
            return playerSprite.getWidth();
        }

        public float getHeight() {
            return playerSprite.getHeight();
        }

        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void draw() {
            playerSprite.setPosition((float) x - getWidth() / 2, (float) y - getHeight() / 2);
            playerSprite.draw(batch);
        }

        public void update(float x, float y) {
            velocity.i = x - this.x;
            velocity.j = y - this.y;
            this.x = x;
            this.y = y;

            // limit the paddle's movement
        //    if (yDown) {
        //        if (y < SCREEN_HEIGHT / 2) {
        //            this.y = SCREEN_HEIGHT / 2;
        //        } else if (y < SCREEN_HEIGHT / 2) {
        //            this.y = SCREEN_HEIGHT / 2;
        //        }
        //    }
        //    if (!yDown) {
                // limit the paddle's movement
         //       if (y > SCREEN_HEIGHT / 2) {
        //           this.y = SCREEN_HEIGHT / 2;
         //       } else if (y > SCREEN_HEIGHT / 2) {
          //          this.y = SCREEN_HEIGHT / 2;
         //       }
       //     }
        }



        public void updateScore() {
            score1 = spriteMap.get(score / 10);
            score2 = spriteMap.get(score % 10);
        }
    }
    public void getOtherPlayer(float x, float y) {
        x = (float) otherPlayer.x;
        y = (float) otherPlayer.y;
    }
    public void getMyPlayer(float x, float y) {
        x = (float) myPlayer.x;
        y = (float) myPlayer.y;
    }
    public void getPuckPosition(float x, float y) {
        x = (float) myPlayer.x;
        y = (float) myPlayer.y;
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}