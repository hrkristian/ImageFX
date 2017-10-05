package main;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.image.Image;

public interface ImageStatusObserver {

    void setImageStatusPropertyListener(ReadOnlyObjectProperty<ImageProcessor.ImageStatus> observedStatusProperty);

}
