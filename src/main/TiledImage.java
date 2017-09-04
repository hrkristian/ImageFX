package main;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.*;

public class TiledImage {

	private ImageTile[] imageTiles;
	private SimpleBooleanProperty processing;

	public TiledImage(Image originImage) {

		imageTiles = new ImageTile[4];

		PixelReader originReader =  originImage.getPixelReader();
		int originWidth = (int)originImage.getWidth();
		int originHeight = (int)originImage.getHeight();

		int verticalSplit = originWidth / 2;
		int horizontalSplit = originHeight / 2;

		int isWidthOdd = originWidth % 2;
		int isHeightOdd = originHeight % 2;

		imageTiles[0] = new ImageTile( originReader,
				0, 0,
				verticalSplit, horizontalSplit
		); // Top Left
		imageTiles[1] = new ImageTile( originReader,
				verticalSplit+1, 0,
				verticalSplit+isWidthOdd, horizontalSplit+isHeightOdd
		); // Top Right
		imageTiles[2] = new ImageTile( originReader,
				verticalSplit+1, horizontalSplit+1,
				verticalSplit+isWidthOdd-1, horizontalSplit+isHeightOdd-1
		); // Bottom Right
		imageTiles[3] = new ImageTile( originReader,
				0, horizontalSplit+1,
				verticalSplit+isWidthOdd, horizontalSplit+isHeightOdd-1
		); // Bottom Left

	}

	public Image getMergedImage() {
		int mergedWidth = (int)(imageTiles[0].getWidth() + imageTiles[1].getWidth());
		int mergedHeight = (int)(imageTiles[0].getHeight() + imageTiles[3].getHeight());

		Image mergedImage = new WritableImage(mergedWidth, mergedHeight);

		PixelWriter writer = ((WritableImage)mergedImage).getPixelWriter();

		for (ImageTile img : imageTiles) {
			PixelReader reader = img.getPixelReader();

			for (int i = 0; i < img.width; i++) {
				for (int j = 0; j < img.height; j++) {
					writer.setColor(i + img.x, j + img.y, reader.getColor(i, j));
				}
			}
		}

		return mergedImage;
	}
	public ImageTile[] getImageTiles() {
		return imageTiles;
	}
	public Image getImageTile(Pos position) {
		return imageTiles[position.index];
	}

	class ImageTile extends WritableImage {

		final private int x, y, width, height;

		public ImageTile(PixelReader originReader, int x, int y, int width, int height) {
			super(originReader, x, y, width, height);

			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}

	public enum Pos {
		TOPLEFT (0),
		TOPRIGHT (1),
		BOTTOMRIGHT (2),
		BOTTOMLEFT (3);

		private int index;

		private Pos(int pos) {
			this.index = pos;
		}
	}
}
