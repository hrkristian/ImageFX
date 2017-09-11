package main;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class Histogram extends Stage {

	private VBox root;
	private Pane canvasPane;
	private ObservableList<String> histogramBands;
	private HashMap<String, HistogramSection> histogramSections;

	final int CANVAS_HEIGHT = 200;
	final int CANVAS_WIDTH = 512; // Only multiples of 256. This is linked to canvas painters!
	final int SCALE_REPRESENTATION_HEIGHT = 20;

	public Histogram() {
		super();

		histogramSections = new HashMap<>();
		canvasPane = new Pane();
		root = new VBox();

		// Combo Box
		histogramBands = FXCollections.observableArrayList();
		histogramBands.add("empty");
		histogramSections.put("empty", new HistogramSection());

		final ComboBox histogramSelector = new ComboBox(histogramBands);
		histogramSelector.valueProperty().setValue(histogramBands.get(0));
		histogramSelector.setStyle("-fx-font-size: 18px;");

		histogramSelector.valueProperty().addListener(new ChangeListener() {
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				if (histogramSections.containsKey(newValue)) {
					if (histogramSections.get(newValue).drawn) {
						canvasPane.getChildren().clear();
						canvasPane.getChildren().add(histogramSections.get(newValue));
					}
				}
			}
		});

		Text header = new Text("Histogram");
		header.setFont(new Font(20));

		root.getChildren().add(header);
		root.getChildren().add(histogramSelector);
		root.getChildren().add(canvasPane);

		root.setPadding(new Insets(10));
		root.setSpacing(10);

		setResizable(false);
		setScene(new Scene(root, 600, 350));
		show();
	}

	public void populateHistogram(byte[] values, String band) {
		if (values == null || band == null)
			throw new IllegalArgumentException("Nigga you null?");

		String stringOne = "Normalised - ".concat(band.substring(0,1).toUpperCase().concat(band.substring(1)));
		String stringTwo = "Cumulative - ".concat(band.substring(0,1).toUpperCase().concat(band.substring(1)));

		if (!addToHistogramSectionMap(stringOne)) {
			return;
		}
		if (!addToHistogramSectionMap(stringTwo)) {
			removeFromHistogramSectionMap(stringOne);
			return;
		}

		HistogramSection normalisedTmp = histogramSections.get(stringOne);
		HistogramSection cumulativeTmp = histogramSections.get(stringTwo);

		int[] normalisedhistogram = ImageUtils.createNormalisedHistogramData(values);
		int[] cumulativeHistogram = ImageUtils.createCumulativeHistogramData(values);

		int highestNormalisedValue = 0;
		int highestCumulativeValue = cumulativeHistogram[255];

		for (int i : normalisedhistogram)
			if (i > highestNormalisedValue)
				highestNormalisedValue = i;


		double columnRatioNormalised = CANVAS_HEIGHT * 0.9 / highestNormalisedValue;
		double columnRatioCumulative = CANVAS_HEIGHT * 0.9 / highestCumulativeValue;

		normalisedTmp.pixelPainter.setLineWidth(2);
		cumulativeTmp.pixelPainter.setLineWidth(2);

		for (int i = 0; i < normalisedhistogram.length; i++)
			normalisedTmp.pixelPainter.strokeLine( i*2, CANVAS_HEIGHT, i*2, (CANVAS_HEIGHT - 1) - (normalisedhistogram[i] * columnRatioNormalised) );

		normalisedTmp.drawn = true;

		for (int i = 0; i < cumulativeHistogram.length; i++)
			cumulativeTmp.pixelPainter.strokeLine( i*2, CANVAS_HEIGHT, i*2, (CANVAS_HEIGHT - 1) - (cumulativeHistogram[i] * columnRatioCumulative) );

		cumulativeTmp.drawn = true;

		minWidthProperty().bind( new SimpleDoubleProperty(widthProperty().getValue()) );
		maxWidthProperty().bind( new SimpleDoubleProperty(widthProperty().getValue()) );

		// Create the band representation


		int bandNumber = 0; // represents grey by default
		switch (band) {
			case "red": bandNumber = 1;
				break;
			case "green": bandNumber = 2;
				break;
			case "blue": bandNumber = 3;
				break;
		}

		cumulativeTmp.createBandScaleRepresentation(bandNumber);
		normalisedTmp.createBandScaleRepresentation(bandNumber);

	}

	private boolean addToHistogramSectionMap(String band) {
		if (histogramSections.containsKey(band))
			return false;

		if (histogramSections.containsKey("empty"))
			histogramSections.remove("empty");
		if (histogramBands.contains("empty"))
			histogramBands.remove("empty");

		histogramSections.put(band, new HistogramSection());
		histogramBands.add(band);

		return true;
	}
	private void removeFromHistogramSectionMap(String band) {
		if (!histogramSections.containsKey(band))
			return;
		histogramSections.remove(band);

		if (!histogramBands.contains(band))
			return;

		histogramBands.remove(band);
	}

	/**
	 *
	 * @param bands Arrays containing the color values
	 * @param description Array containing color band description
	 */
	public void populateHistogram(byte[][] bands, String[] description) {
		if (bands.length != description.length)
			throw new IllegalArgumentException("Value array length and description array length must correspond.");

		for (int i = 0; i < description.length; i++)
			populateHistogram(bands[i], description[i]);
	}


	class HistogramSection extends VBox {

		private Canvas canvas, bandScaleRepresentation;
		private HBox top, middle, bottom;

		private GraphicsContext pixelPainter;

		private boolean drawn = false;

		public HistogramSection() {
			super();

			canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
			middle = new HBox(canvas);
			middle.setBorder(new Border(new BorderStroke(
					null, null, Color.BLACK, Color.BLACK,
					BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
					CornerRadii.EMPTY,
					new BorderWidths(2),
					null))
			);

			bandScaleRepresentation = new Canvas(CANVAS_WIDTH, SCALE_REPRESENTATION_HEIGHT);
			bottom = new HBox(bandScaleRepresentation);
			bottom.setPadding(new Insets(0, 2, 0, 2));

			pixelPainter = canvas.getGraphicsContext2D();
			pixelPainter.setFill(Color.WHITE);
			pixelPainter.fillRect(0,0, canvas.getWidth(), canvas.getHeight());

			setSpacing(10);
			getChildren().addAll(middle, bottom);

		}
		private void createBandScaleRepresentation(int band) {
			GraphicsContext scalePainter = bandScaleRepresentation.getGraphicsContext2D();
			scalePainter.setLineWidth(2);
			for (int i = 0, x = 0; i < 256; i++, x=i*2) {
				switch (band) {
					case 0: scalePainter.setStroke(Color.grayRgb(i));
						break;
					case 1: scalePainter.setStroke(Color.rgb(i, 0 ,0));
						break;
					case 2: scalePainter.setStroke(Color.rgb(0, i, 0));
						break;
					case 3: scalePainter.setStroke(Color.rgb(0, 0, i));
						break;
				}
				scalePainter.strokeLine(x, 0, x, SCALE_REPRESENTATION_HEIGHT);
			}
		}
	}


	/**
	 *
	 * @param band the band for which the scale is made
	 * @param canvas a new instance of Canvas, to be reused for both Histograms of one band
	 * @return a completed scale-representation of the specified band
	 */


}
