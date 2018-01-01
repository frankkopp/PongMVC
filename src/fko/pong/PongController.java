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

import javafx.beans.property.DoubleProperty;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.WindowEvent;

/**
 * PongController
 * 31.12.2017
 * @author Frank Kopp
 */
public class PongController {

	private PongModel model;

	// helper for drag event
	private double initialY;
	private double _initialDragAnchor;

	/**
	 * @param model
	 */
	public PongController(PongModel model) {
		this.model = model;

	}

	/**
	 * 
	 */
	public void startGameAction() {
		model.startGame();
	}

	/**
	 * 
	 */
	public void stopGameAction() {
		model.stopGame();
	}

	/**
	 * 
	 */
	public void pauseGameAction() {
		if (model.isGameRunning() && !model.isGamePaused()) model.pauseGame();
		else if  (model.isGameRunning() && model.isGamePaused()) model.resumeGame();
	}

	/**
	 * 
	 */
	public void soundOnOptionAction() {
		model.setSoundOnOption(!model.getSoundOnOption());
	}

	/**
	 * 
	 */
	public void anglePaddleOptionAction() {
		model.setAnglePaddleOption(!model.getAnglePaddleOption());
	}

	/**
	 * @param b 
	 * 
	 */
	public void onLeftPaddleUpAction(boolean b) {
		if (b) model.setLeftPaddleUp(true);
		else model.setLeftPaddleUp(false);
	}

	/**
	 * @param b 
	 * 
	 */
	public void onLeftPaddleDownAction(boolean b) {
		if (b) model.setLeftPaddleDown(true);
		else model.setLeftPaddleDown(false);
	}

	/**
	 * @param b 
	 * 
	 */
	public void onRightPaddleUpAction(boolean b) {
		if (b) model.setRightPaddleUp(true);
		else model.setRightPaddleUp(false);
	}

	/**
	 * @param b 
	 * 
	 */
	public void onRightPaddleDownAction(boolean b) {
		if (b) model.setRightPaddleDown(true);
		else model.setRightPaddleDown(false);
	}

	/**
	 * Handles keyboard pressed events
	 * @param event
	 */
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

	/**
	 * Handles keyboard released events
	 * @param event
	 */
	public void handleKeyboardReleasedEvents(KeyEvent event) {
		switch (event.getCode()) {
		case Q: 		onLeftPaddleUpAction(false); break;
		case A:		onLeftPaddleDownAction(false); break;
		case UP:	 	onRightPaddleUpAction(false); break;
		case DOWN:  onRightPaddleDownAction(false); break;
		default:
		}
	}

	/**
	 * Handles mouse  events
	 * @param event
	 */
	public void handleMouseEventsLeftPaddle(MouseEvent event) {
		handleMouseEvent(event, model.getLeftPaddleYProperty());
	}

	/**
	 * Handles mouse  events
	 * @param event
	 */
	public void handleMouseEventsRightPaddle(MouseEvent event) {
		handleMouseEvent(event, model.getRightPaddleYProperty());
	}

	/**
	 * @param event
	 * @param paddle 
	 */
	private void handleMouseEvent(MouseEvent event, DoubleProperty paddleYProperty) {
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
			//System.out.println("InitialY: "+initialY+" DRAG: "+dragY);	
			if (paddleYProperty.equals(model.getLeftPaddleYProperty())) {
				model.setLeftPaddleY(initialY + dragY);
			} else {
				model.setRightPaddleY(initialY + dragY);
			}
		} else if (eventType.equals(MouseEvent.MOUSE_RELEASED) ) {
			source.setCursor(Cursor.OPEN_HAND);
		}
	}

	/**
	 * @param event
	 */
	public void close_action(WindowEvent event) {
		Pong.exit();		
	}

}
