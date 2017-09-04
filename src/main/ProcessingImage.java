package main;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ProcessingImage extends Image {

	private ReadOnlyBooleanWrapper isProcessing;

	public ProcessingImage(FileInputStream inputStream) {
		super(inputStream);
		isProcessing= new ReadOnlyBooleanWrapper(false);
	}
	public ProcessingImage(String url) throws FileNotFoundException {
		this( new FileInputStream(url) );
	}

	public ReadOnlyBooleanProperty getIsProcessingProperty() {
		return isProcessing.getReadOnlyProperty();
	}
}
