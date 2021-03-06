package main;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
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
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class Histogram extends ImageFXStage implements ImageObserver {

	private ImageProcessor ip;

//	private VBox rootPane;
	private Pane canvasPane;
	private ObservableList<String> histogramBands;
	private HashMap<String, HistogramSection> histogramSections;

	private ComboBox histogramSelector;

	private String previouslySelectedBand;

	private final int CANVAS_HEIGHT = 200;
	private final int CANVAS_WIDTH = 512; // Only multiples of 256. This is linked (but not referenced) to canvas painters!
	private final int SCALE_REPRESENTATION_HEIGHT = 20;

	public Histogram(ImageProcessor ip, Color stageColor) {
		super(stageColor, 600, 350);

		this.ip = ip;
		previouslySelectedBand = "empty";

		histogramSections = new HashMap<>();
		canvasPane = new Pane();
//		rootPane = new VBox();

		// Combo Box
		histogramBands = FXCollections.observableArrayList();
		histogramBands.add(previouslySelectedBand);
		histogramSections.put(previouslySelectedBand, new HistogramSection());

		histogramSelector = new ComboBox(histogramBands);
		histogramSelector.valueProperty().setValue(histogramBands.get(0));
		histogramSelector.setStyle("-fx-font-size: 18px;");

		histogramSelector.valueProperty().addListener(new ChangeListener() {
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				if (histogramSections.containsKey(newValue)) {
					if (histogramSections.get(newValue).drawn) {
						canvasPane.getChildren().clear();
						canvasPane.getChildren().add(histogramSections.get(newValue));

						previouslySelectedBand = (String)newValue;
					}
				}
			}
		});

		Text header = new Text("Histogram");
		header.setFont(new Font(20));

		HBox headerPane = new HBox(header), selectorPane = new HBox(histogramSelector);
		headerPane.setBackground(new Background(new BackgroundFill(stageColor, null, null)));
		headerPane.setPadding(new Insets(10, 10, 10 ,10));
		selectorPane.setPadding(new Insets(0, 10, 0, 10));
		canvasPane.setPadding(new Insets(0, 10, 10, 10));

		rootPane.getChildren().add(headerPane);
		rootPane.getChildren().add(selectorPane);
		rootPane.getChildren().add(canvasPane);

//		rootPane.setPadding(new Insets(10));
		rootPane.setSpacing(10);
		rootPane.requestLayout();

		setResizable(false);

//		setScene(new Scene(rootPane, 600, 350));
		show();
	}

	public void populateHistogram(byte[] values, String band) {
		if (values == null || band == null)
			throw new IllegalArgumentException("Nigga you null?");

		String stringOne = "Original - ".concat(band.substring(0,1).toUpperCase().concat(band.substring(1)));
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

		int[] originalHistogram = ImageUtils.createHistogramData(values);
		int[] cumulativeHistogram = ImageUtils.createCumulativeHistogramData(values);

		int highestNormalisedValue = 0;
		int highestCumulativeValue = cumulativeHistogram[255];

		for (int i : originalHistogram)
			if (i > highestNormalisedValue)
				highestNormalisedValue = i;


		double columnRatioNormalised = CANVAS_HEIGHT * 1.0 / highestNormalisedValue;
		double columnRatioCumulative = CANVAS_HEIGHT * 1.0 / highestCumulativeValue;

		normalisedTmp.pixelPainter.setLineWidth(2);
		normalisedTmp.pixelPainter.setStroke(Color.DARKGRAY);
		cumulativeTmp.pixelPainter.setLineWidth(2);
		cumulativeTmp.pixelPainter.setStroke(Color.DARKGRAY);

		for (int i = 0; i < originalHistogram.length; i++)
			normalisedTmp.pixelPainter.strokeLine( i*2, CANVAS_HEIGHT, i*2, (CANVAS_HEIGHT - 1) - (originalHistogram[i] * columnRatioNormalised) );

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

		histogramSelector.valueProperty().setValue(histogramBands.get(0));
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

	@Override
	public void setImagePropertyListener(ReadOnlyObjectProperty<Image> observedImageProperty) {
		observedImageProperty.addListener(listener -> {
			new Thread(() -> {

				/* Create new workable data
				* Frees the main thread from doing background calculations */
				byte[] imageByteData = ImageUtils.getImageAsByteArray( observedImageProperty.getValue() );
				byte[] newGreyscaleBand = ImageUtils.convertFromBgraToAveragedGreyscale(imageByteData);
				byte[][] newRgbBands = ImageUtils.splitRbgaToIndividualRbg( imageByteData );
				String[] bands = {"Red", "Green", "Blue"};

				Platform.runLater(() -> {
					/* Clear all old references */
					histogramSections.clear();
					histogramBands.clear();
					canvasPane.getChildren().clear();

					populateHistogram(newGreyscaleBand, "Greyscale");
					populateHistogram(newRgbBands, bands);

					if (!previouslySelectedBand.equals(histogramSelector.valueProperty().getName()))
						if (histogramBands.contains(previouslySelectedBand))
							histogramSelector.valueProperty().setValue(previouslySelectedBand);
				});


			} ).start();
		});
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
