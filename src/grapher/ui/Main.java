package grapher.ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
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
		
		EventHandler<ActionEvent> eventAdd = new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent e) {
		        canvas.add();
		    }
		};
		
		EventHandler<ActionEvent> eventDel = new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent e) {
		        canvas.remove();
		    }
		};

		Button buttonP = new Button("+");
		buttonP.setOnAction(eventAdd);
		Button buttonM = new Button("-");
		buttonM.setOnAction(eventDel);
		ToolBar boutons = new ToolBar(buttonP, buttonM);

		// HBox boutons = new HBox(new Button("+"),new Button("-"));

		MenuItem MenuItemAdd = new MenuItem("Ajouter...");
		MenuItemAdd.setOnAction(eventAdd);
		MenuItem MenuItemDel = new MenuItem("Supprimer");
		MenuItemDel.setOnAction(eventDel);
		
		Menu menu1 = new Menu("Expression");
		menu1.getItems().add(MenuItemAdd);
		menu1.getItems().add(MenuItemDel);
		
		Menu menu2 = new Menu("Couleur");
		
		MenuBar menubar = new MenuBar(menu1,menu2);
		
		BorderPane listSide = new BorderPane();
		listSide.setCenter(funList);
		listSide.setBottom(boutons);
		listSide.setTop(menubar);

		SplitPane fenetre = new SplitPane();
		fenetre.getItems().addAll(listSide, root);

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