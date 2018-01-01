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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * The Pong view.
 * @author Frank Kopp
 */
public class PongView {

	private final PongModel model;
	private final PongController controller;
	private final BorderPane view;
	
	protected Text optionsText =  new Text("Options");

	/**
	 * @param model
	 * @param controller
	 */
	public PongView(PongModel model, PongController controller) {

		this.model = model;
		this.controller = controller;

		// setup main view
		view = new BorderPane();
		view.setBackground(new Background(
				new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

		// pong pane
		PongPane pongpane = new PongPane(this.model , this.controller, this);
		view.setCenter(pongpane);

		// add a two line hbox for how-to and options
		VBox vBox = new VBox();
		vBox.setAlignment(Pos.CENTER);

		// add game how-to
		Text howtoText = new Text("SPACE=Start ESC=Stop P=Pause Q=left up A=left down UP=right up DOWN=right down");
		vBox.getChildren().add(howtoText);

		vBox.getChildren().add(optionsText);

		view.setBottom(vBox);
		BorderPane.setAlignment(vBox, Pos.CENTER);
	}

	/**
	 * @param controller
	 */
	public void addInputHandler() {
		// set key event to control game and move flags
		view.getScene().setOnKeyPressed(e -> controller.handleKeyboardPressedEvents(e));
		view.getScene().setOnKeyReleased(e -> controller.handleKeyboardReleasedEvents(e));
	}

	/**
	 * Returns the main view panel
	 * @return main view panel
	 */
	public Parent asParent() {
		return view ;
	}

}
