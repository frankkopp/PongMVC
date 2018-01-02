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

import javafx.beans.property.DoubleProperty;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.WindowEvent;

/**
 * PongController
 * 
 * <p>
 * A MVC controller for the Pong game handling all user input and forwarding it to the 
 * MVC model. As per clean MVC the controller does not know the View. It is only called by View elements 
 * when a user interacts with the View (Mouse, Keyboard, etc.). It the calls the model to let the model 
 * change itself accordingly.   
 * 
 * 31.12.2017
 * @author Frank Kopp
 */
public class PongController {

	private PongModel model;

	// helper for drag event
	private double initialY;
	private double _initialDragAnchor;

	public PongController(PongModel model) {
		this.model = model;
	}

	public void startGameAction() {
		model.startGame();
	}

	public void stopGameAction() {
		model.stopGame();
	}

	public void pauseGameAction() {
		if (model.isGameRunning() && !model.isGamePaused()) model.pauseGame();
		else if  (model.isGameRunning() && model.isGamePaused()) model.resumeGame();
	}

	public void soundOnOptionAction() {
		model.setSoundOnOption(!model.getSoundOnOption());
	}

	public void anglePaddleOptionAction() {
		model.setAnglePaddleOption(!model.getAnglePaddleOption());
	}

	public void onLeftPaddleUpAction(boolean b) {
		if (b) model.setLeftPaddleUp(true);
		else model.setLeftPaddleUp(false);
	}

	public void onLeftPaddleDownAction(boolean b) {
		if (b) model.setLeftPaddleDown(true);
		else model.setLeftPaddleDown(false);
	}

	public void onRightPaddleUpAction(boolean b) {
		if (b) model.setRightPaddleUp(true);
		else model.setRightPaddleUp(false);
	}

	public void onRightPaddleDownAction(boolean b) {
		if (b) model.setRightPaddleDown(true);
		else model.setRightPaddleDown(false);
	}

	public void handleKeyboardPressedEvents(KeyEvent event) {
		switch (event.getCode()) {
		// game control
		case SPACE: 	startGameAction(); break;
		case ESCAPE:	stopGameAction(); break;
		case P: 		pauseGameAction(); break;
		// options control
		case DIGIT1: soundOnOptionAction(); break;
		case DIGIT2: anglePaddleOptionAction(); break;
		// paddle control
		case Q: 		onLeftPaddleUpAction(true); break;
		case A:		onLeftPaddleDownAction(true); break;
		case UP:	 	onRightPaddleUpAction(true); break;
		case DOWN:  onRightPaddleDownAction(true); break;
		default:
		}
	}

	public void handleKeyboardReleasedEvents(KeyEvent event) {
		switch (event.getCode()) {
		case Q: 		onLeftPaddleUpAction(false); break;
		case A:		onLeftPaddleDownAction(false); break;
		case UP:	 	onRightPaddleUpAction(false); break;
		case DOWN:  onRightPaddleDownAction(false); break;
		default:
		}
	}

	public void handleMouseEventsLeftPaddle(MouseEvent event) {
		handlePaddleMouseEvent(event, model.getLeftPaddleYProperty());
	}

	public void handleMouseEventsRightPaddle(MouseEvent event) {
		handlePaddleMouseEvent(event, model.getRightPaddleYProperty());
	}

	private void handlePaddleMouseEvent(MouseEvent event, DoubleProperty paddleYProperty) {
		final Rectangle source = (Rectangle) event.getSource();
		final EventType<? extends MouseEvent> eventType = event.getEventType();

		// handle the three different mouse events
		if (eventType.equals(MouseEvent.MOUSE_PRESSED) ) {
			source.setCursor(Cursor.CLOSED_HAND);
			initialY = source.getY();
			_initialDragAnchor = event.getSceneY();

		} else if (eventType.equals(MouseEvent.MOUSE_DRAGGED) ) {
			double dragY = event.getSceneY() - _initialDragAnchor;
			// don't leave area
			if (paddleYProperty.equals(model.getLeftPaddleYProperty())) {
				model.setLeftPaddleY(initialY + dragY);
			} else {
				model.setRightPaddleY(initialY + dragY);
			}
		} else if (eventType.equals(MouseEvent.MOUSE_RELEASED) ) {
			source.setCursor(Cursor.OPEN_HAND);
		}
	}

	public void close_action(WindowEvent event) {
		Pong.exit();		
	}

}
