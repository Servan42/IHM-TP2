package grapher.ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
	public void start(Stage stage) {
		BorderPane root = new BorderPane();
		ListView<String> funList = new ListView<String>();
		funList.getItems().addAll(getParameters().getRaw());

		GrapherCanvas canvas = new GrapherCanvas(getParameters(), funList);
		
		root.setCenter(canvas);

		Button buttonP = new Button("+");
		buttonP.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent arg0) {
				canvas.add();
			}
		});
		Button buttonM = new Button("-");
		buttonM.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent arg0) {
				canvas.remove();
			}
		});
		ToolBar boutons = new ToolBar(buttonP, buttonM);

		// HBox boutons = new HBox(new Button("+"),new Button("-"));

		BorderPane listAndButtons = new BorderPane();
		listAndButtons.setCenter(funList);
		listAndButtons.setBottom(boutons);

		SplitPane fenetre = new SplitPane();
		fenetre.getItems().addAll(listAndButtons, root);

		BorderPane global = new BorderPane();
		global.setCenter(fenetre);

		stage.setTitle("Grapher");
		stage.setScene(new Scene(global));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}