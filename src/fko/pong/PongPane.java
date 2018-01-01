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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * The PongPane view.
 * @author Frank Kopp
 */
public class PongPane extends Pane {

	private final PongModel model;
	private final PongController controller;
	private final PongView view;

	private Rectangle _leftPaddle;
	private Rectangle _rightPaddle;
	private Circle _ball;

	// helper for dragging of paddles
	protected double _initialTranslateY;
	protected double _initialDragAnchor;

	// text to display options which can be turned on and off
	private StringProperty _optionsTextString = new SimpleStringProperty("Options: ");

	/**
	 * The pane where the playing takes place.
	 * @param controller 
	 * @param model 
	 * @param view 
	 */
	public PongPane(PongModel model, PongController controller, PongView view) {
		super();

		this.model = model;
		this.controller = controller;
		this.view = view;

		this.setBackground(new Background(
				new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

		this.prefHeightProperty().bind(model.getPlayfieldWidthProperty());
		this.prefWidthProperty().bind(model.getPlayfieldHeightProperty());
		
		// observe model values
		model.getSoundOnOptionProperty().addListener((obs, oldX, newX) -> updateOptions());
		model.getAnglePaddleOptionProperty().addListener((obs, oldX, newX) -> updateOptions());
		this.view.optionsText.textProperty().bind(_optionsTextString);

		addBall();
		addPaddles();
		addScore();

		// initial update to options 
		updateOptions();
	}

	/**
	 * Updates the text showing which options are active
	 */
	private void updateOptions() {
		StringBuilder sb = new StringBuilder("Options: ");
		sb.append("Sound (1) ").append(model.getSoundOnOption() ? "ON" : "OFF").append("  ");
		sb.append("Angling Paddle (2) ").append(model.getAnglePaddleOption() ? "ON" : "OFF").append("  ");
		_optionsTextString.set(sb.toString());
	}

	/**
	 * Adds a ball to the screen. Not visible when game not running. 
	 */
	private void addBall() {
		_ball = new Circle(model.getBallSize(),  Color.WHITE);
		_ball.radiusProperty().bind(model.getBallSizeProperty());
		_ball.centerXProperty().bind(model.getBallCenterXProperty());
		_ball.centerYProperty().bind(model.getBallCenterYProperty());
		this.getChildren().add(_ball);
	}

	/**
	 * Adding the two player paddles to the screen.<br>
	 * Also adding a mouse handler to the paddles.
	 */
	private void addPaddles() {

		_leftPaddle = new Rectangle(model.getPaddleWidth(), model.getLeftPaddleLength(), Color.WHITE);
		_leftPaddle.heightProperty().bind(model.getLeftPaddleLengthProperty());
		_leftPaddle.xProperty().bind(model.getLeftPaddleXProperty());
		_leftPaddle.yProperty().bind(model.getLeftPaddleYProperty());
		_leftPaddle.setCursor(Cursor.OPEN_HAND);
		_leftPaddle.setOnMousePressed(event -> controller.handleMouseEventsLeftPaddle(event)); 
		_leftPaddle.setOnMouseDragged(event -> controller.handleMouseEventsLeftPaddle(event)); 
		_leftPaddle.setOnMouseReleased(event -> controller.handleMouseEventsLeftPaddle(event));
		this.getChildren().add(_leftPaddle);

		_rightPaddle = new Rectangle(model.getPaddleWidth(), model.getRightPaddleLength(), Color.WHITE);
		_rightPaddle.heightProperty().bind(model.getRightPaddleLengthProperty());
		_rightPaddle.xProperty().bind(model.getRightPaddleXProperty());
		_rightPaddle.yProperty().bind(model.getRightPaddleYProperty());
		_rightPaddle.setCursor(Cursor.OPEN_HAND);
		_rightPaddle.setOnMousePressed(event -> controller.handleMouseEventsRightPaddle(event));
		_rightPaddle.setOnMouseDragged(event -> controller.handleMouseEventsRightPaddle(event));
		_rightPaddle.setOnMouseReleased(event -> controller.handleMouseEventsRightPaddle(event));
		this.getChildren().add(_rightPaddle);
	}

	/**
	 * Adds the display of the score for each player.
	 */
	private void addScore() {
		// create Text for each score 
		Text leftScore = new Text();
		Text rightScore = new Text();
		
		// add them to pane
		this.getChildren().add(leftScore);
		this.getChildren().add(rightScore);

		// positioning helpers
		final double middle = model.getPlayfieldWidth() / 2;
		final int offsetFromMiddle = 150;

		// layout helpers
		final Font font = Font.font("OCR A Std", FontWeight.BOLD, FontPosture.REGULAR, 40.0);
		final int locationY = 50;
		final Color color = Color.WHITE;

		// left
		leftScore.setFont(font);
		leftScore.setY(locationY);
		leftScore.setFill(color);
		// right
		rightScore.setFont(font);
		rightScore.setY(locationY);
		rightScore.setFill(color);

		// position score text
		leftScore.setX(middle - offsetFromMiddle - 15);
		rightScore.setX(middle + offsetFromMiddle);

		// bind text to score property
		leftScore.textProperty().bind(model.getPlayerLeft().points.asString());
		rightScore.textProperty().bind(model.getPlayerRight().points.asString());
	}


}