package main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Histogram extends Stage {

	Canvas canvas;
	VBox root;
	HBox top, middle, bottom;

	GraphicsContext pixelPainter;

	final int CANVAS_HEIGHT = 200;

	public Histogram() {
		super();
		setResizable(false);

		Text header = new Text("Histogram");
		header.setFont(new Font(24));
		top = new HBox(header);
		top.setPadding(new Insets(0, 0, 10, 0));

		canvas = new Canvas(512, CANVAS_HEIGHT);
		middle = new HBox(canvas);
		middle.setBorder(new Border(new BorderStroke(
				null, null, Color.BLACK, Color.BLACK,
				BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
				CornerRadii.EMPTY,
				new BorderWidths(2),
				null))
		);

		bottom = new HBox(new Text("0"), new Text("127"), new Text("255"));
		bottom.setAlignment(Pos.CENTER);
		bottom.setSpacing(200);

		root = new VBox(top, middle, bottom);
		root.setPadding(new Insets(10));
		setScene(new Scene(root));

		pixelPainter = canvas.getGraphicsContext2D();
		pixelPainter.setFill(Color.WHITE);
		pixelPainter.fillRect(0,0, canvas.getWidth(), canvas.getHeight());

		show();
	}

	public void populateGreyscaleHistogram(byte[] values) {
		int[] histogram = ImageUtils.createHistogrammaticalData(ImageUtils.convertFromRbgaToGreyscale(values));

		int highestValue = 0;
		for (int i : histogram)
			if (i > highestValue)
				highestValue = i;

		double columnRatio = CANVAS_HEIGHT * 1.0 / highestValue;

		pixelPainter.setLineWidth(2);
		for (int i = 0; i < histogram.length; i++) {
			pixelPainter.strokeLine( i*2, 200, i*2, 199 - (histogram[i] * columnRatio) );
		}
	}
	public void populateHistogram(byte[] values, String band) {
		int[] histogram = new int[values.length];
		for (int i = 0; i < values.length; i++)
			histogram[ Byte.toUnsignedInt(values[i]) ]++;

		int highestValue = 0;
		for (int i : histogram)
			if (i > highestValue)
				highestValue = i;

		double columnRatio = CANVAS_HEIGHT * 0.9 / highestValue;

		pixelPainter.setLineWidth(2);

		for (int i = 0; i < histogram.length; i++)
			pixelPainter.strokeLine( i*2, CANVAS_HEIGHT, i*2, (CANVAS_HEIGHT - 1) - (histogram[i] * columnRatio) );

	}

	/**
	 *
	 * @param bands Arrays containing the color values
	 * @param description Array containing color band description
	 */
	public void populateHistogram(byte[][] bands, String[] description) {

	}

}
