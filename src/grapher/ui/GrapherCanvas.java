package grapher.ui;

import static java.lang.Math.*;

import java.util.Optional;
import java.util.Vector;

import javafx.util.converter.DoubleStringConverter;

import javafx.application.Application.Parameters;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;

import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;

import grapher.fc.*;

public class GrapherCanvas extends Canvas {
	static final double MARGIN = 40;
	static final double STEP = 5;

	static final double WIDTH = 400;
	static final double HEIGHT = 300;

	private static DoubleStringConverter d2s = new DoubleStringConverter();

	protected double W = WIDTH;
	protected double H = HEIGHT;

	protected double xmin, xmax;
	protected double ymin, ymax;

	protected Point2D rbd = new Point2D(0, 0); // point haut gauche du rectangle
												// de zoom
	protected Point2D rhg = new Point2D(0, 0); // point bas droite du rectangle
												// de zoom

	protected Vector<Function> functions = new Vector<Function>();

	protected ListView<String> funList;

	public GrapherCanvas(Parameters params) {
		super(WIDTH, HEIGHT);
		xmin = -PI / 2.;
		xmax = 3 * PI / 2;
		ymin = -1.5;
		ymax = 1.5;

		addEventHandler(MouseEvent.ANY, new Handler());
		addEventHandler(ScrollEvent.ANY, new SHandler());

		for (String param : params.getRaw()) {
			functions.add(FunctionFactory.createFunction(param));
		}
	}

	public GrapherCanvas(Parameters params, ListView<String> funList) {
		super(WIDTH, HEIGHT);
		xmin = -PI / 2.;
		xmax = 3 * PI / 2;
		ymin = -1.5;
		ymax = 1.5;

		addEventHandler(MouseEvent.ANY, new Handler());
		addEventHandler(ScrollEvent.ANY, new SHandler());

		for (String param : params.getRaw()) {
			functions.add(FunctionFactory.createFunction(param));
		}
		this.funList = funList;
		funList.getSelectionModel().getSelectedItems().addListener(new listListen());
	}

	public double minHeight(double width) {
		return HEIGHT;
	}

	public double maxHeight(double width) {
		return Double.MAX_VALUE;
	}

	public double minWidth(double height) {
		return WIDTH;
	}

	public double maxWidth(double height) {
		return Double.MAX_VALUE;
	}

	public boolean isResizable() {
		return true;
	}

	public void resize(double width, double height) {
		super.setWidth(width);
		super.setHeight(height);
		redraw();
	}

	private void redraw() {
		GraphicsContext gc = getGraphicsContext2D();
		W = getWidth();
		H = getHeight();

		// background
		gc.clearRect(0, 0, W, H);

		gc.setFill(Color.BLACK);
		gc.setStroke(Color.BLACK);

		// box
		gc.save();
		gc.translate(MARGIN, MARGIN);
		W -= 2 * MARGIN;
		H -= 2 * MARGIN;
		if (W < 0 || H < 0) {
			return;
		}

		gc.strokeRect(0, 0, W, H);

		gc.fillText("x", W, H + 10);
		gc.fillText("y", -10, 0);

		gc.beginPath();
		gc.rect(0, 0, W, H);
		gc.closePath();
		gc.clip();

		// plot
		gc.translate(-MARGIN, -MARGIN);

		// tracé du rectangle de zoom éventuel
		gc.setLineDashes(5);
		gc.strokeRect(rhg.getX(), rhg.getY(), max(rbd.getX(), rhg.getX()) - min(rhg.getX(), rbd.getX()),
				max(rhg.getY(), rbd.getY()) - min(rbd.getY(), rhg.getY()));
		gc.setLineDashes(0);

		// x values
		final int N = (int) (W / STEP + 1);
		final double dx = dx(STEP);
		double xs[] = new double[N];
		double Xs[] = new double[N];
		for (int i = 0; i < N; i++) {
			double x = xmin + i * dx;
			xs[i] = x;
			Xs[i] = X(x);
		}

		for (Function f : functions) {
			// y values
			double Ys[] = new double[N];
			for (int i = 0; i < N; i++) {
				Ys[i] = Y(f.y(xs[i]));
			}

			if (!funList.getSelectionModel().isEmpty()
					&& funList.getSelectionModel().selectedItemProperty().getValue().toString().equals(f.toString()))
				gc.setLineWidth(2);
			else
				gc.setLineWidth(1);

			gc.strokePolyline(Xs, Ys, N);

		}

		gc.restore(); // restoring no clipping

		// axes
		drawXTick(gc, 0);
		drawYTick(gc, 0);

		double xstep = unit((xmax - xmin) / 10);
		double ystep = unit((ymax - ymin) / 10);

		gc.setLineDashes(new double[] { 4.f, 4.f });
		for (double x = xstep; x < xmax; x += xstep) {
			drawXTick(gc, x);
		}
		for (double x = -xstep; x > xmin; x -= xstep) {
			drawXTick(gc, x);
		}
		for (double y = ystep; y < ymax; y += ystep) {
			drawYTick(gc, y);
		}
		for (double y = -ystep; y > ymin; y -= ystep) {
			drawYTick(gc, y);
		}

		gc.setLineDashes(null);
	}

	protected double dx(double dX) {
		return (double) ((xmax - xmin) * dX / W);
	}

	protected double dy(double dY) {
		return -(double) ((ymax - ymin) * dY / H);
	}

	protected double x(double X) {
		return xmin + dx(X - MARGIN);
	}

	protected double y(double Y) {
		return ymin + dy((Y - MARGIN) - H);
	}

	protected double X(double x) {
		double Xs = (x - xmin) / (xmax - xmin) * W;
		return Xs + MARGIN;
	}

	protected double Y(double y) {
		double Ys = (y - ymin) / (ymax - ymin) * H;
		return (H - Ys) + MARGIN;
	}

	protected void drawXTick(GraphicsContext gc, double x) {
		if (x > xmin && x < xmax) {
			final double X0 = X(x);
			gc.strokeLine(X0, MARGIN, X0, H + MARGIN);
			gc.fillText(d2s.toString(x), X0, H + MARGIN + 15);
		}
	}

	protected void drawYTick(GraphicsContext gc, double y) {
		if (y > ymin && y < ymax) {
			final double Y0 = Y(y);
			gc.strokeLine(0 + MARGIN, Y0, W + MARGIN, Y0);
			gc.fillText(d2s.toString(y), 5, Y0);
		}
	}

	protected static double unit(double w) {
		double scale = pow(10, floor(log10(w)));
		w /= scale;
		if (w < 2) {
			w = 2;
		} else if (w < 5) {
			w = 5;
		} else {
			w = 10;
		}
		return w * scale;
	}

	protected void translate(double dX, double dY) {
		double dx = dx(dX);
		double dy = dy(dY);
		xmin -= dx;
		xmax -= dx;
		ymin -= dy;
		ymax -= dy;
		redraw();
	}

	protected void zoom(Point2D center, double dz) {
		double x = x(center.getX());
		double y = y(center.getY());
		double ds = exp(dz * .01);
		xmin = x + (xmin - x) / ds;
		xmax = x + (xmax - x) / ds;
		ymin = y + (ymin - y) / ds;
		ymax = y + (ymax - y) / ds;
		redraw();
	}

	protected void zoom(Point2D p0, Point2D p1) {
		double x0 = x(p0.getX());
		double y0 = y(p0.getY());
		double x1 = x(p1.getX());
		double y1 = y(p1.getY());
		xmin = min(x0, x1);
		xmax = max(x0, x1);
		ymin = min(y0, y1);
		ymax = max(y0, y1);
		redraw();
	}

	public void add(){
		Optional<String> result;
		TextInputDialog box = new TextInputDialog("Votre fonction");
		box.setHeaderText("Expression");
		box.setContentText("Nouvelle expression");
		box.setTitle("Expression");
		
		result = box.showAndWait();
		
		if(result.isPresent()) {
			try {
				functions.add(FunctionFactory.createFunction(result.get()));
				funList.getItems().add(result.get());
				redraw();
			} catch(Exception e) {
				new Alert(Alert.AlertType.ERROR, "Fonction incorrecte", ButtonType.CLOSE).showAndWait();
			}
		}
	}

	public void remove(){
		int i;
		if(!funList.getSelectionModel().isEmpty()) {
			for(i=0; i<functions.size() && !funList.getSelectionModel().selectedItemProperty().getValue().toString().equals(functions.get(i).toString()); i++) ;
			functions.remove(functions.get(i));
			funList.getItems().remove(funList.getSelectionModel().getSelectedIndex());
		}
	}

	class Handler implements EventHandler<MouseEvent> {
		Point2D p;
		Point2D p2;
		int button = 0;
		State state = State.IDLE;

		public void handle(MouseEvent e) {

			switch (state) {
			case IDLE:
				switch (e.getEventType().getName()) {
				case "MOUSE_PRESSED":
					p = new Point2D(e.getX(), e.getY());
					p2 = new Point2D(e.getX(), e.getY());
					if (e.isPrimaryButtonDown())
						button = 1;
					if (e.isSecondaryButtonDown())
						button = 2;
					state = State.PRESSED;
					break;
				default:
					break;
				}
				break;
			case PRESSED:
				switch (e.getEventType().getName()) {
				case "MOUSE_RELEASED":
					if (button == 2) {
						zoom(new Point2D(e.getX(), e.getY()), -15);
					} else if (button == 1) {
						zoom(new Point2D(e.getX(), e.getY()), 15);
					}
					state = State.IDLE;
					setCursor(Cursor.DEFAULT);
					break;
				case "MOUSE_DRAGGED":
					state = State.DRAGGED;
					if (button == 1)
						setCursor(Cursor.CLOSED_HAND);
					else if (button == 2)
						setCursor(Cursor.CROSSHAIR);
					break;
				default:
					break;
				}
				break;
			case DRAGGED:
				switch (e.getEventType().getName()) {
				case "MOUSE_RELEASED":
					if (button == 2) {
						p2 = new Point2D(e.getX(), e.getY());
						rbd = new Point2D(0, 0);
						rhg = new Point2D(0, 0);
						zoom(p, p2);
					}
					state = State.IDLE;
					setCursor(Cursor.DEFAULT);
					break;
				case "MOUSE_DRAGGED":
					if (button == 1) {
						translate(e.getX() - p.getX(), e.getY() - p.getY());
						p = new Point2D(e.getX(), e.getY());
					}
					if (button == 2) {
						rhg = new Point2D(min(p.getX(), p2.getX()), min(p.getY(), p2.getY()));
						rbd = new Point2D(max(p.getX(), p2.getX()), max(p.getY(), p2.getY()));
						redraw();
						p2 = new Point2D(e.getX(), e.getY());
					}
					state = State.DRAGGED;
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}

		}
	}

	class SHandler implements EventHandler<ScrollEvent> {
		public void handle(ScrollEvent e) {
			if (e.getEventType().getName() == "SCROLL") {
				if (e.getDeltaY() > 0) {
					zoom(new Point2D(e.getX(), e.getY()), 15);
				} else {
					zoom(new Point2D(e.getX(), e.getY()), -15);
				}
			}
		}
	}

	class listListen implements ListChangeListener<String> {

		public void onChanged(Change arg0) {
			redraw();
		}

	}
}
