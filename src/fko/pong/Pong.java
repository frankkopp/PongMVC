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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 
 * @author Frank Kopp
 */
public class Pong extends Application {

	// VERSION
	public static final String VERSION = "1.1"; 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		PongModel model = new PongModel();
		PongController controller = new PongController(model);
		PongView view = new PongView(model, controller);

		// FIX: how to set correct height so that middle pane gets it preferred size
		Scene scene = new Scene(view.asParent(),600,432); 

		// setup primary stage
		primaryStage.setTitle("Pong by Frank Kopp (c)");
		primaryStage.setResizable(false);
//		primaryStage.setMinWidth(600);
//		primaryStage.setMinHeight(454);
//		primaryStage.setMaxWidth(600);
//		primaryStage.setMaxHeight(454);

		primaryStage.setScene(scene);
		
		// closeAction - close through close action
		scene.getWindow().setOnCloseRequest(event -> {
			controller.close_action(event);
			event.consume();
		});
		
		// let the view register the keyboard handler
		view.addInputHandler();
		
		primaryStage.show();
	}

	/**
	 * Clean up and exit the application
	 */
	public static void exit() {
		exit(0);
	}

	/**
	 * Clean up and exit the application
	 */
	private static void exit(int returnCode) {
		// nothing to clean up yet
		System.exit(returnCode);
	}

	/**
	 * Called when there is an unexpected unrecoverable error.<br/>
	 * Prints a stack trace together with a provided message.<br/>
	 * Terminates with <tt>exit(1)</tt>.
	 * @param message to be displayed with the exception message
	 */
	public static void fatalError(String message) {
		Exception e = new Exception(message);
		e.printStackTrace();
		exit(1);
	}

	/**
	 * Called when there is an unexpected but recoverable error.<br/>
	 * Prints a stack trace together with a provided message.<br/>
	 * @param message to be displayed with the exception message
	 */
	public static void criticalError(String message) {
		Exception e = new Exception(message);
		e.printStackTrace();
	}

	/**
	 * Called when there is an unexpected minor error.<br/>
	 * Prints a provided message.<br/>
	 * @param message to be displayed
	 */
	public static void minorError(String message) {
		System.err.println(message);
	}

}
