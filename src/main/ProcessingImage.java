package main;

import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * TODO- Send relevant image information, like file URL, to this image.
 * TODO- ImageProcessor should handle synchronisity and isProcessing property.
 * TODO- ImageProcessor should pass the ReadOnlyProperty to ImageStages, etc.
 * TODO- Should the Property values be handled as part of an Interface contract? Might be smart...
 *
 * Serves as control for image processing.
 * The WritableImage is only offered for processing to one process/function at a time.
 * Chaining is handled by the ImageProcessor, which will check out the image on behalf of a process/function.
 *
 * GUI updates are handled by means of an Observable, firing an event lets elements fetch the new image for use.
 */
public class ProcessingImage extends WritableImage {

	private ReadOnlyObjectWrapper<ImageStatus> isProcessing;
	private WritableImage image;
	private ImageDetails details;

	public ProcessingImage(Image image) {
		this( image.getPixelReader(), (int)image.getWidth(), (int)image.getHeight() );
	}
	public ProcessingImage(int width, int height) {
		super(width, height);
		sharedConstructorTasks();
	}
	public ProcessingImage(PixelReader reader, int width, int height) {
		super(reader, width, height);
		sharedConstructorTasks();
	}
	private void sharedConstructorTasks() {
		isProcessing = new ReadOnlyObjectWrapper<>(ImageStatus.INITIALISING);
		// TODO: Utilize ImageUtils to judge the content of the image; create details.
		details = new ImageDetails(ImageType.RGB);

		setProcessingProperty(ImageStatus.AVAILABLE);
	}

	/* Exposed methods */

	/**
	 *
	 * @return the read only property of the Image's status
	 */
	public ReadOnlyObjectProperty<ImageStatus> getIsProcessingReadOnlyProperty() {
		return isProcessing.getReadOnlyProperty();
	}

	/**
	 * Only to be used for displaying/updating the image, [b]not processing[/b].
	 * @return a type-forced Image
	 */
	public Image getImage() {
		return image;
	}

	/**
	 *
	 * @return the image for processing, or null if image is being processed
	 */
	synchronized public WritableImage getImageForProcessing() {
		if (isProcessing.getValue() == ImageStatus.AVAILABLE) {
			isProcessing.set(ImageStatus.PROCESSING);
			return image;
		}
		return null;
	}

	synchronized public void setImage(Image newImage) { // TODO- Necessary?
		setImage(new WritableImage(newImage.getPixelReader(), (int)newImage.getWidth(), (int)newImage.getHeight()));
	}
	synchronized public void setImage(WritableImage newImage) {
		image = newImage;
		isProcessing.set(ImageStatus.AVAILABLE);
	}

	private void setProcessingProperty(ImageStatus status) {
		isProcessing.set(status);
	}

	class ImageDetails {
		private String filePath;
		private ImageType type;

		public ImageDetails(String filePath, ImageType type) {
			this.filePath = filePath;
			this.type = type;
		}
		public ImageDetails(ImageType type) {
			this(null, type);
		}
	}

	public enum ImageStatus {
		AVAILABLE,
		PROCESSING,
		INITIALISING,
		CLOSING;
	}
	public enum ImageType {
		GREYSCALE,
		RGB;
	}

}
