/**
MIT License

Copyright (c) 2017 Frank Kopp

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package fko.pong_mvc;

import fko.pong_mvc.PongSounds.Clips;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;

/**
 * PongModel - represents a pong game holding all necessary information for any state of the game.
 * 
 * <p>
 * A Pong game has two paddles and a ball. It has two walls (upwards and downwards) and two sides 
 * (left and right). When started the ball moves from one side to the other while bouncing of the walls. 
 * When a ball reaches one of the sides the other side will receive a score. To avoid this the side 
 * the balls flies towards will try to move the paddle in front of the ball so the ball bounces of the 
 * paddle.<br>
 * The balls is accelerated every time it hits a paddle to make the game faster until the next goal. Then 
 * the speed is reset.<br>
 * The ball bouncing of paddles may use different mechanics. The most simple one being just to change the 
 * horizontal direction. Advanced mechanics allow the paddle to influence the direction of the ball as it
 * bounces off.<br>
 * 
 * TODO: constant speed of ball after paddle bouncing
 * 
 * 31.12.2017
 * @author Frank Kopp
 */
public class PongModel {

	private static final double 	INITIAL_PLAYFIELD_HEIGHT = 400.0;
	private static final double 	INITIAL_PLAYFIELD_WIDTH = 600.0; 

	private static final double 	PADDLE_MOVE_STEPS = 2.0;
	private static final double 	BALL_MOVE_INCREMENTS = 2.0;
	private static final double 	BALL_SPEED = Math.sqrt(2 * BALL_MOVE_INCREMENTS * BALL_MOVE_INCREMENTS);
	private static final double 	MAX_ANGLE_DEGREE = 60.0;

	private static final double 	INITIAL_BALL_SIZE = 5.0;
	private static final double 	INITIAL_PADDLE_LENGTH = 60.0;
	private static final double 	INITIAL_PADDLE_WIDTH = 10.0;
	private static final double 	INITIAL_PADDLE_X = 10.0;

	private static final double 	INITIAL_BALL_SPEED = 60.0;
	private static final double 	INITIAL_PADDLE_SPEED = 60.0;
	private static final double 	ACCELARATION = 1.1; // factor

	// sounds
	private PongSounds sounds = new PongSounds();

	// configuration of game objects
	private DoubleProperty playfieldWidth = new SimpleDoubleProperty(INITIAL_PLAYFIELD_WIDTH);
	private DoubleProperty playfieldHeight = new SimpleDoubleProperty(INITIAL_PLAYFIELD_HEIGHT);

	// speed of animations and stepping for each frame
	private DoubleProperty speedX = new SimpleDoubleProperty(BALL_MOVE_INCREMENTS);
	private DoubleProperty speedY = new SimpleDoubleProperty(BALL_MOVE_INCREMENTS);

	// The center points and size of the moving ball
	private DoubleProperty ballCenterX = new SimpleDoubleProperty();
	private DoubleProperty ballCenterY = new SimpleDoubleProperty();
	private DoubleProperty ballSize = new SimpleDoubleProperty(INITIAL_BALL_SIZE);

	// The position of the paddles
	private DoubleProperty leftPaddleLength = new SimpleDoubleProperty(INITIAL_PADDLE_LENGTH);
	private DoubleProperty leftPaddleX = new SimpleDoubleProperty();
	private DoubleProperty leftPaddleY = new SimpleDoubleProperty();
	private DoubleProperty rightPaddleLength = new SimpleDoubleProperty(INITIAL_PADDLE_LENGTH);
	private DoubleProperty rightPaddleX = new SimpleDoubleProperty();
	private DoubleProperty rightPaddleY = new SimpleDoubleProperty();

	// the current actions applied to move paddles - is used in the paddleMovementTimeline
	private BooleanProperty leftPaddleUp = new SimpleBooleanProperty(false);
	private BooleanProperty leftPaddleDown = new SimpleBooleanProperty(false);
	private BooleanProperty rightPaddleUp = new SimpleBooleanProperty(false);
	private BooleanProperty rightPaddleDown = new SimpleBooleanProperty(false);
	
	// points per player
	private Player playerLeft = new Player("Left");
	private Player playerRight = new Player("Right");

	// status of game
	private BooleanProperty gamePaused = new SimpleBooleanProperty(false);
	private BooleanProperty gameRunning = new SimpleBooleanProperty(false);

	// Options
	private BooleanProperty soundOnOption 	 = new SimpleBooleanProperty(true);
	private BooleanProperty anglePaddleOption = new SimpleBooleanProperty(true);

	// animations
	private Timeline ballMovementTimeline = new Timeline();;
	private Timeline paddleMovementTimeline = new Timeline();

	/**
	 * Holds all relevant information for a pong game and does all relevant calculations.
	 */
	public PongModel() {
		
		// initial paddle positions
		leftPaddleX.set(INITIAL_PADDLE_X);
		rightPaddleX.bind(playfieldWidth.subtract(INITIAL_PADDLE_X).subtract(INITIAL_PADDLE_WIDTH));
		leftPaddleY.set(playfieldHeight.get()/2 - leftPaddleLength.get()/2);
		rightPaddleY.set(playfieldHeight.get()/2 - rightPaddleLength.get()/2);
		
		// initial ball position
		ballCenterX.set(playfieldWidth.get()/2);
		ballCenterY.set(playfieldHeight.get()/2);

		// set sound option listener
		soundOnOption.addListener((obs, oldX, newX) -> {
			if (soundOnOption.get()) sounds.soundOn();
			else sounds.soundOff();
		});
		
		// initial options
		soundOnOption.set(false);
		anglePaddleOption.set(true);
		
		// start the paddle movements
		paddleMovementTimeline.setCycleCount(Timeline.INDEFINITE);
		KeyFrame movePaddle = 
				new KeyFrame(Duration.seconds(1/INITIAL_PADDLE_SPEED), e -> { movePaddles();	});
		paddleMovementTimeline.getKeyFrames().add(movePaddle);
		paddleMovementTimeline.play();
		
		// prepare ball movements (will be start in startGame())
		ballMovementTimeline.setCycleCount(Timeline.INDEFINITE);
		KeyFrame moveBall = 
				new KeyFrame(Duration.seconds(1/INITIAL_BALL_SPEED), e -> {	moveBall();	});
		ballMovementTimeline.getKeyFrames().add(moveBall);
		
		// new players
		playerLeft = new Player("Left");
		playerRight = new Player("Right");

	}

	/**
	 * Starts the game with the ball from either of the two sides.
	 * The side and start position of the ball is chosen randomly.
	 */
	public void startGame() {

		// if game is running do nothing
		if (gameRunning.get()) return;

		// reset players / creating new players cuts the bindings from the view
		// might need to re-think this
		playerLeft.points.set(0);
		playerRight.points.set(0);

		// choose randomly from which side to start
		if (Math.random() < 0.5) {
			ballCenterX.setValue(0.0+ballSize.get());	
			speedX.set(BALL_MOVE_INCREMENTS);
		} else {
			ballCenterX.setValue(playfieldWidth.get()-ballSize.get());
			speedX.set(-BALL_MOVE_INCREMENTS);
		}
		
		// random height (y) to start from
		ballCenterY.setValue(Math.random() * playfieldHeight.get());
		
		// random direction to shoot the ball at the start
		speedY.set(BALL_MOVE_INCREMENTS * (Math.random() < 0.5 ? 1 : -1));

		// start the ball movements
		ballMovementTimeline.play();

		// game is now running
		gamePaused.set(false);
		gameRunning.set(true);

	}

	/**
	 * Stops the game. Ignored if game not running.
	 */
	public void stopGame() {
		ballMovementTimeline.stop(); // stops ball movements
		// game stopped
		gamePaused.set(false);
		gameRunning.set(false);
	}

	/**
	 * Pause the game. Ignored if game not running or already paused.
	 */
	public void pauseGame() {
		if (gameRunning.get() && gamePaused.get()) return;
		gamePaused.set(true);
		ballMovementTimeline.stop(); // stops ball movements
	}

	/**
	 * Resume a paused game. Ignored if game not running or game not paused.
	 */
	public void resumeGame() {
		if (gameRunning.get() && !gamePaused.get()) return;
		gamePaused.set(false);
		ballMovementTimeline.play(); // (re-)starts ball movements
	}

	/**
	 * Called by the <code>paddleMovementTimeline<code> animation event to move the paddles.
	 */
	private void movePaddles() {
		if (leftPaddleUp.get() 
				&& leftPaddleY.get() > 0.0) {
			leftPaddleY.setValue(leftPaddleY.getValue() - PADDLE_MOVE_STEPS);
		}
		if (leftPaddleDown.get()  
				&& leftPaddleY.get() + leftPaddleLength.get() < playfieldHeight.get()) {
			leftPaddleY.set(leftPaddleY.get() + PADDLE_MOVE_STEPS);
		}
		if (rightPaddleUp.get()
				&& rightPaddleY.get() > 0.0) {
			rightPaddleY.set(rightPaddleY.get() - PADDLE_MOVE_STEPS);
		}
		if (rightPaddleDown.get()
				&& rightPaddleY.get() + rightPaddleLength.get() < playfieldHeight.get()) {
			rightPaddleY.set(rightPaddleY.get() + PADDLE_MOVE_STEPS);
		}
	}

	/**
	 * Called by the <code>ballMovementTimeline</code> animation event to move the ball.
	 */
	private void moveBall() {
		ballCenterX.setValue(ballCenterX.get() + speedX.get());
		ballCenterY.setValue(ballCenterY.get() + speedY.get());
		checkCollision();
	}

	/**
	 * Checks if the ball has hit a wall, a paddle or has left through left or right wall.<br>
	 * If left through left or right wall we have a goal and the score is increased and the ball reseted on the
	 * scorer's side.   
	 */
	private void checkCollision() {
		double xMin = ballCenterX.get() - ballSize.get();
		double xMax = ballCenterX.get() + ballSize.get();
		double yMin = ballCenterY.get() - ballSize.get();
		double yMax = ballCenterY.get() + ballSize.get();

		// hit top or bottom wall
		if (yMin < 0 || yMax > playfieldHeight.get()) {
			sounds.playClip(Clips.WALL);
			speedY.set(speedY.get() * -1);
		}
		
		// hit left or right wall
		if (xMax < 0 || xMin > playfieldWidth.get()) {
			sounds.playClip(Clips.GOAL);
			//speedX.set(speedX.get() * -1);
			goal(xMin < 0 ? playerRight : playerLeft);
		}

		// hit on a paddle - left
		if (speedX.get() < 0 // moving left
				&& (ballCenterX.get()-ballSize.get()) <= (leftPaddleX.get()+INITIAL_PADDLE_WIDTH) 
				&& (ballCenterY.get()+ballSize.get() > leftPaddleY.get())
				&& (ballCenterY.get()-ballSize.get() < leftPaddleY.get()+leftPaddleLength.get())) {

			sounds.playClip(Clips.LEFT);
			
			updateBallSpeedAfterPaddleHit();

			// new direction
			if (anglePaddleOption.get()) {
				newVector(leftPaddleY);
			} else {
				// just changed direction - angle is always constant
				speedX.set(speedX.get() * -1);
			}
		} // hit on a paddle - right
		else if (speedX.get() > 0 // moving right
				&& (ballCenterX.get()+ballSize.get()) >= (rightPaddleX.get()) 
				&& (ballCenterY.get()+ballSize.get() > rightPaddleY.get())
				&& (ballCenterY.get()-ballSize.get() < rightPaddleY.get()+leftPaddleLength.get())) {

			sounds.playClip(Clips.RIGHT);
			
			updateBallSpeedAfterPaddleHit();

			// new direction
			if (anglePaddleOption.get()) {
				newVector(rightPaddleY);
			} else {
				// just changed direction - angle is always constant
				speedX.set(speedX.get() * -1);
			}
		} 
	}

	/**
	 * Accelerate ball and paddles after each hit on paddle
	 */
	private void updateBallSpeedAfterPaddleHit() {
		ballMovementTimeline.setRate(ballMovementTimeline.getRate()*ACCELARATION);
		paddleMovementTimeline.setRate(paddleMovementTimeline.getRate()*ACCELARATION);
	}

	/**
	 * Calculate a new outgoing direction for a ball after hitting a paddle.<br>
	 * Is only used when option Angling Paddle is ON.
	 * @param paddle
	 */
	private void newVector(DoubleProperty paddle) {

		double hitPos = calculateHitPos(paddle);
		double newAngleRAD = Math.toRadians(MAX_ANGLE_DEGREE * hitPos); // influence of the hit position

		// adapt speeds for constant total speed
		speedY.set(BALL_SPEED * Math.sin(newAngleRAD)); // new Y speed
		speedX.set(-Math.signum(speedX.get()) // turn direction
				* Math.abs(BALL_SPEED * Math.cos(newAngleRAD))); // new X speed
		
	}

	/**
	 * @param paddleY
	 * @return
	 */
	private double calculateHitPos(DoubleProperty paddleY) {
		
		double paddleLength;
		if (paddleY.equals(leftPaddleY)) {
			paddleLength = leftPaddleLength.get();
		} else {
			paddleLength = rightPaddleLength.get();
		}
		
		// calculate where the ball hit the paddle
		// center = 0.0, top=-1-0, bottom=+1.0
		double hitPos = 2.0 * (((ballCenterY.doubleValue() - paddleY.doubleValue()) / paddleLength) - 0.5);
		
		return hitPos;
	}

	/**
	 * Increases score for the player who scored and resets the ball to the scorer's side. 
	 * @param playerScored
	 */
	private void goal(Player playerScored) {
		// hide ball
		ballMovementTimeline.pause();

		// reset speed
		ballMovementTimeline.setRate(1.0);
		paddleMovementTimeline.setRate(1.0);

		// start from either side of the board
		if (playerScored.equals(playerLeft)) {
			ballCenterX.setValue(0.0+ballSize.get());	
			speedX.set(BALL_MOVE_INCREMENTS);
			playerLeft.points.set(playerLeft.points.get()+1);
		} else {
			ballCenterX.setValue(playfieldWidth.get()-ballSize.get());
			speedX.set(-BALL_MOVE_INCREMENTS);
			playerRight.points.set(playerRight.points.get()+1);
		}
		
		// random y
		ballCenterY.setValue(Math.random() * playfieldHeight.get());
		
		// random direction
		speedY.set(BALL_MOVE_INCREMENTS * (Math.random() < 0.5 ? 1 : -1));

		// short break
		try { Thread.sleep(500);
		} catch (InterruptedException e) {}

		// move again
		ballMovementTimeline.play();
	}

	/* ************************************************************
	 * GETTER / SETTER
	 * 
	 * This is really ugly in Java - but to correctly isolate 
	 * inner workings of classes from other classes necessary.  
	 * ************************************************************/

	/**
	 * @return the playfield width property
	 */
	public DoubleProperty getPlayfieldWidthProperty() {
		return playfieldWidth;
	}

	/**
	 * @return the playfield width
	 */
	public double getPlayfieldWidth() {
		return playfieldWidth.get();
	}

	/**
	 * @param value the playfield width to set
	 */
	public void setPlayfieldWidth(double value) {
		this.playfieldWidth.set(value);
	}

	/**
	 * @return the playfield height property
	 */
	public DoubleProperty getPlayfieldHeightProperty() {
		return playfieldHeight;
	}

	/**
	 * @return the playfield height
	 */
	public double getPlayfieldHeight() {
		return playfieldHeight.get();
	}

	/**
	 * @param value the playfield height to set
	 */
	public void setPlayfieldHeight(double value) {
		this.playfieldHeight.set(value);
	}

	/**
	 * @return the ball size property
	 */
	public DoubleProperty getBallSizeProperty() {
		return ballSize;
	}

	/**
	 * @return the ball size
	 */
	public double getBallSize() {
		return ballSize.get();
	}

	/**
	 * @param ballSize the ball size to set
	 */
	public void setBallSize(double ballSize) {
		this.ballSize.set(ballSize);
	}

	/**
	 * @return the ball's speed in horizontal direction property
	 */
	public DoubleProperty getSpeedXProperty() {
		return speedX;
	}

	/**
	 * @return the ball's speed in horizontal direction
	 */
	public double getSpeedX() {
		return speedX.get();
	}

	/**
	 * @param speedX the ball's speed in horizontal direction to set
	 */
	public void setSpeedX(double speedX) {
		this.speedX.set(speedX);
	}

	/**
	 * @return the ball's speed in vertical direction property
	 */
	public DoubleProperty getSpeedYProperty() {
		return speedY;
	}

	/**
	 * @return the ball's speed in vertical direction
	 */
	public double getSpeedY() {
		return speedY.get();
	}

	/**
	 * @param speedY the ball's speed in vertical direction to set
	 */
	public void setSpeedY(double speedY) {
		this.speedY.set(speedY);
	}

	/**
	 * @return left paddle length property
	 */
	public DoubleProperty getLeftPaddleLengthProperty() {
		return leftPaddleLength;
	}

	/**
	 * @return the left paddle length
	 */
	public double getLeftPaddleLength() {
		return leftPaddleLength.get();
	}

	/**
	 * @param value left paddle length
	 */
	public void setLeftPaddleLength(double value) {
		this.leftPaddleLength.set(value);
	}

	/**
	 * @return right paddle length property
	 */
	public DoubleProperty getRightPaddleLengthProperty() {
		return rightPaddleLength;
	}

	/**
	 * @return right paddle length
	 */
	public double getRightPaddleLength() {
		return rightPaddleLength.get();
	}

	/**
	 * @param value left paddle length to set
	 */
	public void setRightPaddleLength(double value) {
		this.rightPaddleLength.set(value);
	}

	/**
	 * @return property for when movement upwards is currently triggered by user input
	 */
	public BooleanProperty isLeftPaddleUpProperty() {
		return leftPaddleUp;
	}

	/**
	 * @return true if movement upwards is currently triggered by user input
	 */
	public boolean isLeftPaddleUp() {
		return leftPaddleUp.get();
	}

	/**
	 * @param leftPaddleUp set true when upwards movement is 
	 * currently triggered by user input - false otherwise
	 */
	public void setLeftPaddleUp(boolean leftPaddleUp) {
		this.leftPaddleUp.set(leftPaddleUp);
	}

	/**
	 * @return property for when movement downwards is currently triggered by user input
	 */
	public BooleanProperty isLeftPaddleDownProperty() {
		return leftPaddleDown;
	}

	/**
	 * @return true if movement downwards is currently triggered by user input
	 */
	public boolean isLeftPaddleDown() {
		return leftPaddleDown.get();
	}

	/**
	 * @param leftPaddleDown set true when downwards movement is 
	 * currently triggered by user input - false otherwise
	 */
	public void setLeftPaddleDown(boolean leftPaddleDown) {
		this.leftPaddleDown.set(leftPaddleDown);
	}

	/**
	 * @return property for when movement upwards is currently triggered by user input
	 */
	public BooleanProperty isRightPaddleUpProperty() {
		return rightPaddleUp;
	}

	/**
	 * @return true if movement upwards is currently triggered by user input
	 */
	public boolean isRightPaddleUp() {
		return rightPaddleUp.get();
	}

	/**
	 * @param @param rightPaddleUp set true when upwards movement is 
	 * currently triggered by user input - false otherwise
	 */
	public void setRightPaddleUp(boolean rightPaddleUp) {
		this.rightPaddleUp.set(rightPaddleUp);
	}

	/**
	 * @return property for when movement downwards is currently triggered by user input
	 */
	public BooleanProperty isRightPaddleDownProperty() {
		return rightPaddleDown;
	}

	/**
	 * @return true if movement downwards is currently triggered by user input
	 */
	public boolean isRightPaddleDown() {
		return rightPaddleDown.get();
	}

	/**
	 * @param rightPaddleDown set true when downwards movement is 
	 * currently triggered by user input - false otherwise
	 */
	public void setRightPaddleDown(boolean rightPaddleDown) {
		this.rightPaddleDown.set(rightPaddleDown);
	}

	/**
	 * @return ball's center vertical position property
	 */
	public DoubleProperty getBallCenterYProperty() {
		return ballCenterY;
	}

	/**
	 * @return the ball's center vertical position
	 */
	public double getBallCenterY() {
		return ballCenterY.get();
	}

	/**
	 * @param ballCenterY the ball's center vertical position to set
	 */
	public void setBallCenterY(double ballCenterY) {
		this.ballCenterY.set(ballCenterY);
	}

	/**
	 * @return ball's center horizontal position property
	 */
	public DoubleProperty getBallCenterXProperty() {
		return ballCenterX;
	}

	/**
	 * @return ball's center horizontal position 
	 */
	public double getBallCenterX() {
		return ballCenterX.get();
	}

	/**
	 * @param ballCenterX ball's center horizontal position to set
	 */
	public void setBallCenterX(double ballCenterX) {
		this.ballCenterY.set(ballCenterX);
	}

	/**
	 * @return the left paddle's vertical position (left upper corner) property
	 */
	public DoubleProperty getLeftPaddleYProperty() {
		return leftPaddleY;
	}

	/**
	 * @return the left paddle's vertical position (left upper corner) 
	 */
	public double getLeftPaddleY() {
		return leftPaddleY.get();
	}

	/**
	 * @param leftPaddleY the left paddle's vertical position (left upper corner) to set
	 */
	public void setLeftPaddleY(double leftPaddleY) {
		if (leftPaddleY < 0) {
			leftPaddleY = 0;
		} else if (leftPaddleY + leftPaddleLength.get() > playfieldHeight.get()) { 
			leftPaddleY = playfieldHeight.get() - leftPaddleLength.get();
		}
		this.leftPaddleY.set(leftPaddleY);
	}

	/**
	 * @return the left paddle's horizontal position (left upper corner) property
	 */
	public DoubleProperty getLeftPaddleXProperty() {
		return leftPaddleX;
	}

	/**
	 * @return the left paddle's horizontal position (left upper corner)
	 */
	public double getLeftPaddleX() {
		return leftPaddleX.get();
	}

	/**
	 * @param leftPaddleY the left paddle's horizontal position (left upper corner) to set
	 */
	public void setLeftPaddleX(double leftPaddleY) {
		this.leftPaddleX.set(leftPaddleY);
	}

	/**
	 * @return the right paddle's vertical position (left upper corner) property
	 */
	public DoubleProperty getRightPaddleYProperty() {
		return rightPaddleY;
	}

	/**
	 * @return the right paddle's vertical position (left upper corner)
	 */
	public double getRightPaddleY() {
		return rightPaddleY.get();
	}

	/**
	 * @param rightPaddleY the right paddle's vertical position (left upper corner) to set
	 */
	public void setRightPaddleY(double rightPaddleY) {
		if (rightPaddleY < 0) {
			rightPaddleY = 0;
		} else if (rightPaddleY + rightPaddleLength.get() > playfieldHeight.get()) { 
			rightPaddleY = playfieldHeight.get() - leftPaddleLength.get();
		}
		this.rightPaddleY.set(rightPaddleY);
	}

	/**
	 * @return the right paddle's horizontal position (left upper corner) property
	 */
	public DoubleProperty getRightPaddleXProperty() {
		return rightPaddleX;
	}

	/**
	 * @return the right paddle's horizontal position (left upper corner)
	 */
	public double getRightPaddleX() {
		return rightPaddleX.get();
	}

	/**
	 * @param rightPaddleY the right paddle's horizontal position (left upper corner) to set
	 */
	public void setRightPaddleX(double rightPaddleY) {
		this.rightPaddleX.set(rightPaddleY);
	}

	/**
	 * @return the left player
	 */
	public Player getPlayerLeft() {
		return playerLeft;
	}

	/**
	 * @param playerLeft the left player to set
	 */
	public void setPlayerLeft(Player playerLeft) {
		this.playerLeft = playerLeft;
	}

	/**
	 * @return the right player
	 */
	public Player getPlayerRight() {
		return playerRight;
	}

	/**
	 * @param playerRight the playeright player to set
	 */
	public void setPlayerRight(Player playerRight) {
		this.playerRight = playerRight;
	}

	/**
	 * @return the game paused property
	 */
	public BooleanProperty isGamePausedProperty() {
		return gamePaused;
	}

	/**
	 * @return true if game is running and is paused
	 */
	public boolean isGamePaused() {
		return gamePaused.get();
	}

	/**
	 * @return the game running property
	 */
	public BooleanProperty isGameRunningProperty() {
		return gameRunning;
	}

	/**
	 * @return true if game is running
	 */
	public boolean isGameRunning() {
		return gameRunning.get();
	}

	/**
	 * @return the soundOnOption property
	 */
	public BooleanProperty getSoundOnOptionProperty() {
		return soundOnOption;
	}

	/**
	 * @return the soundOnOption 
	 */
	public boolean getSoundOnOption() {
		return soundOnOption.get();
	}

	/**
	 * @param soundOnOption the soundOnOption to set
	 */
	public void setSoundOnOption(boolean soundOnOption) {
		this.soundOnOption.set(soundOnOption);
	}

	/**
	 * @return the anglePaddleOption property
	 */
	public BooleanProperty getAnglePaddleOptionProperty() {
		return anglePaddleOption;
	}

	/**
	 * @return the anglePaddleOption
	 */
	public boolean getAnglePaddleOption() {
		return anglePaddleOption.get();
	}

	/**
	 * @param anglePaddleOption the anglePaddleOption to set
	 */
	public void setAnglePaddleOption(boolean anglePaddleOption) {
		this.anglePaddleOption.set(anglePaddleOption);
	}

	/**
	 * @return the width of both of the paddles
	 */
	public double getPaddleWidth() {
		return INITIAL_PADDLE_WIDTH;
	}

}
