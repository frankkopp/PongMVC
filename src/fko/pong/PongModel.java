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
package fko.pong;

import fko.pong.PongSounds.Clips;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;

/**
 * PongModel
 * 31.12.2017
 * @author Frank Kopp
 */
public class PongModel {

	private static final double 	INITIAL_PLAYFIELD_HEIGHT = 400.0;
	private static final double 	INITIAL_PLAYFIELD_WIDTH = 600.0; 

	private static final double 	PADDLE_MOVE_STEPS = 2.0;
	private static final double 	BALL_MOVE_INCREMENTS = 2.0;

	private static final double 	INITIAL_BALL_SIZE = 5.0;
	private static final double 	INITIAL_PADDLE_LENGTH = 60.0;
	private static final double 	INITIAL_PADDLE_WIDTH = 10.0;
	private static final double 	INITIAL_PADDLE_X = 10.0;

	private static final double 	INITIAL_BALL_SPEED = 60.0;
	private static final double 	INITIAL_PADDLE_SPEED = 60.0;
	private static final double 	ACCELARATION = 1.05; // factor

	// sounds
	private PongSounds sounds = new PongSounds();

	// configuration of game objects
	private DoubleProperty playfieldWidth = new SimpleDoubleProperty(INITIAL_PLAYFIELD_WIDTH);
	private DoubleProperty playfieldHeight = new SimpleDoubleProperty(INITIAL_PLAYFIELD_HEIGHT);

	private DoubleProperty ballSize = new SimpleDoubleProperty(INITIAL_BALL_SIZE);
	private DoubleProperty ballSpeed = new SimpleDoubleProperty(INITIAL_BALL_SPEED);

	private DoubleProperty speedX = new SimpleDoubleProperty(BALL_MOVE_INCREMENTS);
	private DoubleProperty speedY = new SimpleDoubleProperty(BALL_MOVE_INCREMENTS);

	private DoubleProperty paddleSpeed = new SimpleDoubleProperty(INITIAL_PADDLE_SPEED);

	// The center points of the moving ball
	private DoubleProperty ballCenterX = new SimpleDoubleProperty();
	private DoubleProperty ballCenterY = new SimpleDoubleProperty();

	// The position of the paddles
	private DoubleProperty leftPaddleLength = new SimpleDoubleProperty(INITIAL_PADDLE_LENGTH);
	private DoubleProperty leftPaddleX = new SimpleDoubleProperty();
	private DoubleProperty leftPaddleY = new SimpleDoubleProperty();
	private DoubleProperty rightPaddleLength = new SimpleDoubleProperty(INITIAL_PADDLE_LENGTH);
	private DoubleProperty rightPaddleX = new SimpleDoubleProperty();
	private DoubleProperty rightPaddleY = new SimpleDoubleProperty();

	// points per player
	private Player playerLeft = new Player("Left");
	private Player playerRight = new Player("Right");

	private BooleanProperty leftPaddleUp = new SimpleBooleanProperty(false);
	private BooleanProperty leftPaddleDown = new SimpleBooleanProperty(false);
	private BooleanProperty rightPaddleUp = new SimpleBooleanProperty(false);
	private BooleanProperty rightPaddleDown = new SimpleBooleanProperty(false);

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
	 * Holds all relevant information and does all relevant calculations for a Pong game.
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
		anglePaddleOption.set(false);
		
		// start the paddle movements
		paddleMovementTimeline.setCycleCount(Timeline.INDEFINITE);
		KeyFrame movePaddle = 
				new KeyFrame(Duration.seconds(1/paddleSpeed.get()), e -> { movePaddles();	});
		paddleMovementTimeline.getKeyFrames().add(movePaddle);
		paddleMovementTimeline.play();
		
		// prepare ball movements
		ballMovementTimeline.setCycleCount(Timeline.INDEFINITE);
		KeyFrame moveBall = 
				new KeyFrame(Duration.seconds(1/ballSpeed.get()), e -> {	moveBall();	});
		ballMovementTimeline.getKeyFrames().add(moveBall);
		
		// new players
		playerLeft = new Player("Left");
		playerRight = new Player("Right");

	}

	/**
	 * Starts the game with the ball from either of the two sides.
	 * The side and start position is chosen randomly.
	 */
	public void startGame() {

		// if game is running do nothing
		if (gameRunning.get()) return;

		// new players
		playerLeft.points.set(0);
		playerRight.points.set(0);

		// start from either side of the board
		if (Math.random() < 0.5) {
			ballCenterX.setValue(0.0+ballSize.get());	
			speedX.set(BALL_MOVE_INCREMENTS);
		} else {
			ballCenterX.setValue(playfieldWidth.get()-ballSize.get());
			speedX.set(-BALL_MOVE_INCREMENTS);
		}
		// random y
		ballCenterY.setValue(Math.random() * playfieldHeight.get());
		// random direction
		speedY.set(BALL_MOVE_INCREMENTS * (Math.random() < 0.5 ? 1 : -1));

		// start the ball movements
		ballMovementTimeline.play();

		gamePaused.set(false);
		gameRunning.set(true);

	}

	/**
	 * Stops the game. Ignored if game not running.
	 */
	public void stopGame() {
		ballMovementTimeline.stop();
		gamePaused.set(false);
		gameRunning.set(false);
	}

	/**
	 * Pause the game. Ignored if game not running or already paused.
	 */
	public void pauseGame() {
		if (gameRunning.get() && gamePaused.get()) return;
		gamePaused.set(true);
		ballMovementTimeline.stop();
	}

	/**
	 * Resume a paused game. Ignored if game not running or game not paused.
	 */
	public void resumeGame() {
		if (gameRunning.get() && !gamePaused.get()) return;
		gamePaused.set(false);
		ballMovementTimeline.play();
	}

	/**
	 * Called by the Timeline animation event to move the paddles.
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
	 * Called by the Timeline animation event to move the ball.
	 */
	private void moveBall() {
		ballCenterX.setValue(ballCenterX.getValue() + speedX.get());
		ballCenterY.setValue(ballCenterY.getValue() + speedY.get());
		checkCollision();
	}

	/**
	 * Checks if the ball has hit a wall, a paddle or has left through left or right wall.<br>
	 * If left through left or right wall we have a goal and the score is increased and the ball resetted on the
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
				&& (ballCenterX.get()-ballSize.get()) == (leftPaddleX.get()+INITIAL_PADDLE_WIDTH) 
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
				&& (ballCenterX.get()+ballSize.get()) == (rightPaddleX.get()) 
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
	 * 
	 */
	public void updateBallSpeedAfterPaddleHit() {
		ballSpeed.set(ballSpeed.get() * ACCELARATION);
		paddleSpeed.set(paddleSpeed.get() * ACCELARATION);
		ballMovementTimeline.setRate(ballMovementTimeline.getRate()*ACCELARATION);
		paddleMovementTimeline.setRate(paddleMovementTimeline.getRate()*ACCELARATION);
	}

	/**
	 * @param paddle
	 */
	public void newVector(DoubleProperty paddle) {

		System.out.println("Old SpeedY: "+speedY.toString());
		System.out.println("Old SpeedX: "+speedX.toString());

		double paddleLength;
		if (paddle.equals(leftPaddleY)) {
			paddleLength = leftPaddleLength.get();
		} else {
			paddleLength = rightPaddleLength.get();
		}

		// calculate where the ball hit the paddle
		// center = 0.0, top=-1-0, bottom=+1.0
		double hitPos = (ballCenterY.doubleValue() - paddle.doubleValue()) / paddleLength;
		hitPos = (hitPos-0.5) * 2 * Math.signum(speedY.get());
		System.out.println("HitPos: "+hitPos);

		/*
		 * This leads to either convergence to zero or convergence to bigger angles depending on 
		 * the influence of the hitPos. 
		 */

		// determine new vector (angle and speed)
		double speed = Math.sqrt(speedX.get()*speedX.get()+speedY.get()*speedY.get()); // Pythagoras c=speed
		System.out.println("Old Speed: "+speed);
		double angle = Math.atan(speedY.get()/Math.abs(speedX.get())); // current angle in RAD
		System.out.println(String.format("Old Angle: %.2f ",Math.toDegrees(angle)));
		double newAngle = angle * (1+(hitPos)); // influence of the hit position
		System.out.println(String.format("New Angle: %.2f",Math.toDegrees(newAngle)));

		// adapt speeds for constant total speed
		speedY.set(speed * Math.sin(newAngle));
		System.out.println("New SpeedY: "+speedY.toString());
		speedX.set(Math.signum(speedX.get()) * speed * Math.cos(newAngle));
		speedX.set(speedX.get() * -1);// turn direction
		System.out.println("New SpeedX: "+speedX.toString());
		System.out.println();
	}

	/**
	 * Increases score for the player who scored and resets the ball to the scorer's side. 
	 * @param playerScored
	 */
	private void goal(Player playerScored) {
		// hide ball
		ballMovementTimeline.pause();

		// reset speed
		ballSpeed.set(ballSpeed.get() * INITIAL_BALL_SPEED);
		paddleSpeed.set(paddleSpeed.get() * INITIAL_PADDLE_SPEED);
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
	 * ************************************************************/

	/**
	 * @return the playfield width property
	 */
	public DoubleProperty getPlayfieldWidthProperty() {
		return playfieldWidth;
	}

	/**
	 * @return the playfield
	 */
	public double getPlayfieldWidth() {
		return playfieldWidth.get();
	}

	/**
	 * @param playfield the playfield to set
	 */
	public void setPlayfieldWidth(double value) {
		this.playfieldWidth.set(value);
	}

	/**
	 * @return the playfield width property
	 */
	public DoubleProperty getPlayfieldHeightProperty() {
		return playfieldHeight;
	}

	/**
	 * @return the playfield
	 */
	public double getPlayfieldHeight() {
		return playfieldHeight.get();
	}

	/**
	 * @param playfield the playfield to set
	 */
	public void setPlayfieldHeight(double value) {
		this.playfieldHeight.set(value);
	}

	/**
	 * @return the ballSpeed property
	 */
	public DoubleProperty getBallSpeedProperty() {
		return ballSpeed;
	}

	/**
	 * @return the ballSpeed
	 */
	public double getBallSpeed() {
		return ballSpeed.get();
	}

	/**
	 * @param ballSpeed the ballSpeed to set
	 */
	public void setBallSpeed(double ballSpeed) {
		this.ballSpeed.set(ballSpeed);
	}

	/**
	 * @return the ballSpeed property
	 */
	public DoubleProperty getBallSizeProperty() {
		return ballSize;
	}

	/**
	 * @return the ballSpeed
	 */
	public double getBallSize() {
		return ballSize.get();
	}

	/**
	 * @param ballSpeed the ballSpeed to set
	 */
	public void setBallSize(double ballSize) {
		this.ballSize.set(ballSize);
	}

	/**
	 * @return paddle speed property
	 */
	public DoubleProperty getPaddleSpeedProperty() {
		return paddleSpeed;
	}

	/**
	 * @return the paddleSpeed
	 */
	public double getPaddleSpeed() {
		return paddleSpeed.get();
	}

	/**
	 * @param paddleSpeed the paddleSpeed to set
	 */
	public void setPaddleSpeed(double paddleSpeed) {
		this.paddleSpeed.set(paddleSpeed);
	}

	/**
	 * @return paddle speed Y property
	 */
	public DoubleProperty getSpeedXProperty() {
		return speedX;
	}

	/**
	 * @return the speedX
	 */
	public double getSpeedX() {
		return speedX.get();
	}

	/**
	 * @param speedX the speedX to set
	 */
	public void setSpeedX(double speedX) {
		this.speedX.set(speedX);
	}

	/**
	 * @return paddle speed Y property
	 */
	public DoubleProperty getSpeedYProperty() {
		return speedY;
	}

	/**
	 * @return the speedY
	 */
	public double getSpeedY() {
		return speedY.get();
	}

	/**
	 * @param speedY the speedY to set
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
	 * @param left paddle length
	 */
	public void setLeftPaddleLength(double value) {
		this.leftPaddleLength.set(value);
	}

	/**
	 * @return paddle size property
	 */
	public DoubleProperty getRightPaddleLengthProperty() {
		return rightPaddleLength;
	}

	/**
	 * @return the paddleSize
	 */
	public double getRightPaddleLength() {
		return rightPaddleLength.get();
	}

	/**
	 * @param paddleSize the paddleSize to set
	 */
	public void setRightPaddleLength(double paddleSize) {
		this.rightPaddleLength.set(paddleSize);
	}

	/**
	 * @return leftPaddleUpProperty
	 */
	public BooleanProperty isLeftPaddleUpProperty() {
		return leftPaddleUp;
	}

	/**
	 * @return the leftPaddleUp
	 */
	public boolean isLeftPaddleUp() {
		return leftPaddleUp.get();
	}

	/**
	 * @param leftPaddleUp the leftPaddleUp to set
	 */
	public void setLeftPaddleUp(boolean leftPaddleUp) {
		this.leftPaddleUp.set(leftPaddleUp);
	}

	/**
	 * @return leftPaddleDownProperty
	 */
	public BooleanProperty isLeftPaddleDownProperty() {
		return leftPaddleDown;
	}

	/**
	 * @return the leftPaddleDown
	 */
	public boolean isLeftPaddleDown() {
		return leftPaddleDown.get();
	}

	/**
	 * @param leftPaddleDown the leftPaddleDown to set
	 */
	public void setLeftPaddleDown(boolean leftPaddleDown) {
		this.leftPaddleDown.set(leftPaddleDown);
	}

	/**
	 * @return rightPaddleUpProperty
	 */
	public BooleanProperty isRightPaddleUpProperty() {
		return rightPaddleUp;
	}

	/**
	 * @return the rightPaddleUp
	 */
	public boolean isRightPaddleUp() {
		return rightPaddleUp.get();
	}

	/**
	 * @param rightPaddleUp the rightPaddleUp to set
	 */
	public void setRightPaddleUp(boolean rightPaddleUp) {
		this.rightPaddleUp.set(rightPaddleUp);
	}

	/**
	 * @return rightPaddleDownProperty
	 */
	public BooleanProperty isRightPaddleDownProperty() {
		return rightPaddleDown;
	}

	/**
	 * @return the rightPaddleDown
	 */
	public boolean isRightPaddleDown() {
		return rightPaddleDown.get();
	}

	/**
	 * @param rightPaddleDown the rightPaddleDown to set
	 */
	public void setRightPaddleDown(boolean rightPaddleDown) {
		this.rightPaddleDown.set(rightPaddleDown);
	}

	/**
	 * @return ball center Y property
	 */
	public DoubleProperty getBallCenterYProperty() {
		return ballCenterY;
	}

	/**
	 * @return the ballCenterY
	 */
	public double getBallCenterY() {
		return ballCenterY.get();
	}

	/**
	 * @param ballCenterY the ballCenterY to set
	 */
	public void setBallCenterY(double ballCenterY) {
		this.ballCenterY.set(ballCenterY);
	}

	/**
	 * @return ball center X property
	 */
	public DoubleProperty getBallCenterXProperty() {
		return ballCenterX;
	}

	/**
	 * @return the ballCenterX
	 */
	public double getBallCenterX() {
		return ballCenterX.get();
	}

	/**
	 * @param ballCenterX the ballCenterx to set
	 */
	public void setBallCenterX(double ballCenterX) {
		this.ballCenterY.set(ballCenterX);
	}

	/**
	 * @return the leftPaddleY property
	 */
	public DoubleProperty getLeftPaddleYProperty() {
		return leftPaddleY;
	}

	/**
	 * @return the leftPaddleY 
	 */
	public double getLeftPaddleY() {
		return leftPaddleY.get();
	}

	/**
	 * @param leftPaddleY the leftPaddleY to set
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
	 * @return the leftPaddleY property
	 */
	public DoubleProperty getLeftPaddleXProperty() {
		return leftPaddleX;
	}

	/**
	 * @return the leftPaddleY 
	 */
	public double getLeftPaddleX() {
		return leftPaddleX.get();
	}

	/**
	 * @param leftPaddleY the leftPaddleY to set
	 */
	public void setLeftPaddleX(double leftPaddleY) {
		this.leftPaddleX.set(leftPaddleY);
	}

	/**
	 * @return the rightPaddleY property
	 */
	public DoubleProperty getRightPaddleYProperty() {
		return rightPaddleY;
	}

	/**
	 * @return the rightPaddleY
	 */
	public double getRightPaddleY() {
		return rightPaddleY.get();
	}

	/**
	 * @param rightPaddleY the rightPaddleY to set
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
	 * @return the rightPaddleY property
	 */
	public DoubleProperty getRightPaddleXProperty() {
		return rightPaddleX;
	}

	/**
	 * @return the rightPaddleY
	 */
	public double getRightPaddleX() {
		return rightPaddleX.get();
	}

	/**
	 * @param rightPaddleY the rightPaddleY to set
	 */
	public void setRightPaddleX(double rightPaddleY) {
		this.rightPaddleX.set(rightPaddleY);
	}

	/**
	 * @return the playerLeft
	 */
	public Player getPlayerLeft() {
		return playerLeft;
	}

	/**
	 * @param playerLeft the playerLeft to set
	 */
	public void setPlayerLeft(Player playerLeft) {
		this.playerLeft = playerLeft;
	}

	/**
	 * @return the playerRight
	 */
	public Player getPlayerRight() {
		return playerRight;
	}

	/**
	 * @param playerRight the playerRight to set
	 */
	public void setPlayerRight(Player playerRight) {
		this.playerRight = playerRight;
	}

	/**
	 * @return the gamePaused property
	 */
	public BooleanProperty isGamePausedProperty() {
		return gamePaused;
	}

	/**
	 * @return the gamePaused
	 */
	public boolean isGamePaused() {
		return gamePaused.get();
	}

	/**
	 * @param gamePaused the gamePaused to set
	 */
	public void setGamePaused(boolean gamePaused) {
		this.gamePaused.set(gamePaused);
	}

	/**
	 * @return the gameRunning property
	 */
	public BooleanProperty isGameRunningProperty() {
		return gameRunning;
	}

	/**
	 * @return the gameRunning
	 */
	public boolean isGameRunning() {
		return gameRunning.get();
	}

	/**
	 * @param gameRunning the gameRunning to set
	 */
	public void setGameRunning(boolean gameRunning) {
		this.gameRunning.set(gameRunning);
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
	 * @return
	 */
	public double getPaddleWidth() {
		return INITIAL_PADDLE_WIDTH;
	}

}
