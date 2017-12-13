package grapher.ui;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Main extends Application {
	public void start(Stage stage) {
		BorderPane root = new BorderPane();

		/*
		 * https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/table-view.
		 * htm
		 */

		TableView<ExprCol> table = new TableView<ExprCol>();
		TableColumn<ExprCol, String> expressionCol = new <ExprCol, String>TableColumn("Expression");
		TableColumn<ExprCol, Color> colorCol = new <ExprCol, Color>TableColumn("Couleur");

		expressionCol.setEditable(true);
		colorCol.setEditable(true);

		table.setEditable(true);

		table.getColumns().addAll(expressionCol, colorCol);

		expressionCol.setCellValueFactory(new PropertyValueFactory<>("expression"));
//		colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));
		colorCol.setCellValueFactory(d -> d.getValue().color);
		
		ObservableList<ExprCol> data = FXCollections.observableArrayList();
		for (String f : getParameters().getRaw())
			data.add(new ExprCol(f, Color.BLACK));

		table.setItems(data);

		ListView<String> funList = new ListView<String>();
		funList.getItems().addAll(getParameters().getRaw());

		GrapherCanvas canvas = new GrapherCanvas(getParameters(), table, data);

		expressionCol.setCellFactory(TextFieldTableCell.<ExprCol>forTableColumn());
		expressionCol.setOnEditCommit((TableColumn.CellEditEvent<Main.ExprCol,String> t) -> {
			canvas.updateFun(((CellEditEvent<ExprCol, String>) t).getNewValue(),
					((CellEditEvent<ExprCol, String>) t).getOldValue(),
					((CellEditEvent<ExprCol, String>) t).getTablePosition().getRow());
		});

		colorCol.setCellFactory((TableColumn<ExprCol, Color> col) -> new TableCell<ExprCol, Color>(){
			private final ColorPicker cp = new ColorPicker() {{
				setOnShowing (e -> table.edit(getTableRow().getIndex(), col)); 
				valueProperty().addListener((value, oldVal, newVal) -> {if(isEditing()) { commitEdit(newVal); canvas.updateCol();}}); 
			}};
			
			public void updateItem(Color item, boolean empty) {
				super.updateItem(item, empty);
				cp.setValue(item);
				if (empty || item == null) {
			         setGraphic(null);
			     } else {
			         setGraphic(cp);
			     }
			}
		});
		
//		colorCol.setCellFactory(ColorPickerTableCell.<ExprCol, Color>forTableColumn());
//		colorCol.setOnEditCommit((TableColumn.CellEditEvent<Main.ExprCol, Color> t) -> {
//			table.getItems().get(((CellEditEvent<ExprCol, Color>) t).getTablePosition().getRow()).setColor(((CellEditEvent<ExprCol, Color>) t).getNewValue());
//			canvas.updateCol();
//		});
		
//		colorCol.setOnEditStart((TableColumn.CellEditEvent<Main.ExprCol, Color> t) -> {
//			new ColorPicker();
//		});
		
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

	public static class ExprCol {
		private SimpleStringProperty expression;
		private ObjectProperty<Color> color;

		public ExprCol(String expression, Color color) {
			this.expression = new SimpleStringProperty(expression);
			this.color = new SimpleObjectProperty<Color>(color);
		}

		public String getExpression() {
			return expression.get();
		}

		public void setExpression(String expression) {
			this.expression.set(expression);
		}

		public Color getColor() {
			return color.get();
		}

		public void setColor(Color color) {
			this.color.set(color);
		}

	}

	public static class ColorPickerTableCell extends ComboBoxTableCell<ExprCol, Color> {
		final ColorPicker cp = new ColorPicker();

		public ColorPickerTableCell() {
			super();
			this.getItems().add(new Color(0, 1, 0, 1));
			this.getItems().add(new Color(0, 1, 0, 1));
			this.getItems().add(new Color(0, 1, 0, 1));
			this.getItems().add(new Color(0, 1, 0, 1));
			this.getItems().add(new Color(0, 1, 0, 1));
			cp.setOnAction(new EventHandler() {
				public void handle(Event t) {
					Color c = cp.getValue();
					System.out.println("New Color's RGB = " + c.getRed() + " " + c.getGreen() + " " + c.getBlue());
				}
			});
			cp.getStyleClass().add("button");
		}

		/** {@inheritDoc} */
		@Override
		public void updateItem(Color item, boolean empty) {
			// cp.show();

			// super.updateItem(item, empty);
			// this.getItems().add(new Color(0,1,0,1));
			// this.getItems().add(new Color(0,1,0,1));
			// this.getItems().add(new Color(0,1,0,1));
			// this.getItems().add(new Color(0,1,0,1));
			// this.getItems().add(new Color(0,1,0,1));
			// cp.setVisible(true);

			if (empty || item == null) {
				setText(null);
				setGraphic(null);
			} else {
				setGraphic(cp);
			}
		}
	}
}