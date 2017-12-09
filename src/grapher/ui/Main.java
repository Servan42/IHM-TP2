package grapher.ui;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
	public void start(Stage stage) {
		BorderPane root = new BorderPane();

		/*
		 * https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/table-view.htm
		 */
		
		TableView<ExprCol> table = new TableView();
		table.setEditable(true);
		TableColumn expressionCol = new TableColumn("Expression");
		TableColumn colorCol = new TableColumn("Couleur");
		expressionCol.setEditable(true);
		colorCol.setEditable(true);
		expressionCol.setCellFactory(TextFieldTableCell.forTableColumn());
		table.getColumns().addAll(expressionCol, colorCol);
		
		expressionCol.setCellValueFactory(
                new PropertyValueFactory<>("expression"));
		colorCol.setCellValueFactory(
                new PropertyValueFactory<>("color"));
		
		ObservableList<ExprCol> data = FXCollections.observableArrayList(
	            new ExprCol("ccdcd","csdv"),
	            new ExprCol("lih","lihg")
			);
		
		table.setItems(data);

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

		MenuItem MenuItemAdd = new MenuItem("Ajouter...");
		MenuItemAdd.setOnAction(eventAdd);
		MenuItemAdd.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		MenuItem MenuItemDel = new MenuItem("Supprimer");
		MenuItemDel.setOnAction(eventDel);
		MenuItemDel.setAccelerator(new KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.CONTROL_DOWN));

		Menu menu1 = new Menu("Expression");
		menu1.getItems().add(MenuItemAdd);
		menu1.getItems().add(MenuItemDel);

		MenuBar menubar = new MenuBar(menu1);

		BorderPane listSide = new BorderPane();
		// listSide.setCenter(funList);
		listSide.setCenter(table);
		listSide.setBottom(boutons);

		SplitPane fenetre = new SplitPane();
		fenetre.getItems().addAll(listSide, root);

		BorderPane global = new BorderPane();
		global.setCenter(fenetre);
		global.setTop(menubar);

		stage.setTitle("Grapher");
		stage.setScene(new Scene(global));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	public static class ExprCol{
		private SimpleStringProperty expression;
		private SimpleStringProperty color;
		
		private ExprCol(String expression, String color){
			this.expression = new SimpleStringProperty(expression);
			this.color = new SimpleStringProperty(color);
		}
		
		public SimpleStringProperty getExpression() {
			return expression;
		}
		public void setExpression(String expression) {
			this.expression.set(expression);
		}
		public SimpleStringProperty getColor() {
			return color;
		}
		public void setColor(String color) {
			this.color.set(color);
		}
		
		
	}
}