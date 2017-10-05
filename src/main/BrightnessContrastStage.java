package main;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class BrightnessContrastStage extends ImageFXStage {

	/* Layout elements */
	private VBox rootPane;
	private TabPane topTabPane;
	private BrightnessTab brightnessTab;
	private ContrastTab contrastTab;

	private Button resetButton, applyButton;

	/* Action interface */
	ImageProcessor ip;

	public BrightnessContrastStage(ImageProcessor ip, Color color) {
		super(color);

		this.ip = ip;

		brightnessTab = new BrightnessTab();
		contrastTab = new ContrastTab();
		topTabPane = new TabPane(brightnessTab, contrastTab);

		resetButton = new Button("Reset image");
		rootPane = new VBox(topTabPane, resetButton);
		rootPane.setAlignment(Pos.TOP_CENTER);

		setScene( new Scene(rootPane) );
		show();
	}

	private class BrightnessTab extends Tab {
		private VBox rootPane;

		private Slider brightnessSlider;

		private BrightnessTab() {
			super("Brightness");

			Text brightnessText = new Text("Brightness");
			brightnessSlider = new Slider(-255, 255, 0);
			brightnessSlider.setBlockIncrement(1);
			brightnessSlider.setShowTickLabels(true);

			setElementActions();

			rootPane = new VBox(
					brightnessText,
					brightnessSlider
			);
			rootPane.setPadding(new Insets(10));
			rootPane.setSpacing(10);
			rootPane.setAlignment(Pos.TOP_CENTER);

			setContent(rootPane);
		}
		private void setElementActions() {
			brightnessSlider.valueProperty().addListener(value_change -> {
				ip.adjustBrightness((int) brightnessSlider.getValue());
			});
		}
	}
	private class ContrastTab extends Tab {
		private VBox rootPane;

		private Button autoContrastButton;
		private Slider manualContrastSliderLow, manualContrastSliderHigh;
		private Slider manualThresholdSliderLow, manualThresholdSliderHigh;
		

		private ContrastTab() {
			super("Contrast");

			/* Autocontrast */
			Text autoContrastText = new Text("Autocontrast:");
			// TODO- Should just be a button
			autoContrastButton = new Button("Apply autocontrast");

			/* Manual contrast */
			Text manualContrastText = new Text("Manual contrast:");
			Text lowContrastText = new Text("Low: ");
			manualContrastSliderLow = new Slider(0, 50, 0);
			manualContrastSliderLow.setBlockIncrement(1);
			manualContrastSliderLow.setShowTickLabels(true);
			Text highContrastText = new Text("High:");
			manualContrastSliderHigh = new Slider(0, 0.5, 0);
			manualContrastSliderHigh.setBlockIncrement(0.05);
			manualContrastSliderHigh.setShowTickLabels(true);

			BorderPane manualContrastPane = new BorderPane();
			manualContrastPane.setTop(manualContrastText);
			manualContrastPane.setLeft(new VBox(lowContrastText, manualContrastSliderLow));
			manualContrastPane.setRight(new VBox(highContrastText, manualContrastSliderHigh));

			/* Otsu thresholding */

			/* Manual thresholding */
			Text manualThresholdText = new Text("Manual thresholding:");
			Text lowThresholdText = new Text("Low: ");
			manualThresholdSliderLow = new Slider(0, 255, 0);
			manualThresholdSliderLow.setBlockIncrement(1);
			manualThresholdSliderLow.setShowTickLabels(true);
			Text highThresholdText = new Text("High:");
			manualThresholdSliderHigh = new Slider(0, 255, 255);
			manualThresholdSliderHigh.setBlockIncrement(1);
			manualThresholdSliderHigh.setShowTickLabels(true);

			BorderPane manualThresholdPane = new BorderPane();
			manualThresholdPane.setTop(manualThresholdText);
			manualThresholdPane.setLeft(new VBox(lowThresholdText, manualThresholdSliderLow));
			manualThresholdPane.setRight(new VBox(highThresholdText, manualThresholdSliderHigh));

			

			rootPane = new VBox(
					autoContrastText,
					autoContrastButton,
					manualContrastPane,
					manualThresholdPane
			);
			rootPane.setPadding(new Insets(10));
			rootPane.setSpacing(10);
			rootPane.setAlignment(Pos.CENTER);

			setContent(rootPane);

			setElementProperties();
			setElementActions();
		}

		private void setElementProperties() {

			// Makes sure the sliders don't go past each other.
			manualThresholdSliderLow.valueProperty().addListener(valueChange -> {
				if (manualThresholdSliderLow.getValue() >= manualThresholdSliderHigh.getValue())
					manualThresholdSliderHigh.setValue(manualThresholdSliderLow.getValue() + 1);
			});
			manualThresholdSliderHigh.valueProperty().addListener(valueChange -> {
				if (manualThresholdSliderHigh.getValue() <= manualThresholdSliderLow.getValue())
					manualThresholdSliderLow.setValue(manualThresholdSliderHigh.getValue() - 1);
			});
		}

		private void setElementActions() {

			// Autocontrast
			autoContrastButton.setOnAction(value_change -> {
				ip.autoContrast();
			});

			// Manual contrast
			manualContrastSliderLow.setOnMouseReleased(shit -> {
				ip.manualAutoContrast((int)manualContrastSliderLow.getValue());
			});

			// Threshold
			manualThresholdSliderLow.setOnMouseReleased(applyThreshold -> {
				ip.manualThreshold(manualThresholdSliderLow.getValue(), manualThresholdSliderHigh.getValue());
			});
			manualThresholdSliderHigh.setOnMouseReleased(applyThreshold -> {
				ip.manualThreshold(manualThresholdSliderLow.getValue(), manualThresholdSliderHigh.getValue());
			});

			// lkhakdgfal

		}
	}

}
