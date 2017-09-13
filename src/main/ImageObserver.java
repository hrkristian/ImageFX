package main;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.image.Image;

/**
 *
 */
public interface ImageObserver {

	void setImagePropertyListener(ReadOnlyObjectProperty<Image> observedImageProperty);

}
