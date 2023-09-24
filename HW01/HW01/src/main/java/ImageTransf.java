import ij.*;
import ij.io.FileSaver;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class ImageTransf implements PlugInFilter {
  @Override
  public int setup(String args, ImagePlus imagePlus) {
    return DOES_ALL;
  }

  @Override
  public void run(ImageProcessor imageProcessor) {
    transformImage(imageProcessor);
  }

  public void transformImage(ImageProcessor imageProcessor) {
    int width = imageProcessor.getWidth();
    int height = imageProcessor.getHeight();
    int leftWidth = width / 2;
    int rightWidth = width - leftWidth;
    int topHeight = height / 2;
    int bottomHeight = height - topHeight;

    swapLeftAndRight(imageProcessor, leftWidth, rightWidth, height);
    swapTopAndBottom(imageProcessor, width, topHeight, bottomHeight);

    showAndSaveResultImage(imageProcessor);
  }

  private void swapLeftAndRight(ImageProcessor imageProcessor, int leftWidth, int rightWidth, int height) {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < leftWidth; x++) {
        int leftPixel = imageProcessor.getPixel(x, y);
        int rightPixel = imageProcessor.getPixel(x + rightWidth, y);
        imageProcessor.putPixel(x, y, rightPixel);
        imageProcessor.putPixel(x + rightWidth, y, leftPixel);
      }
    }
  }

  private void swapTopAndBottom(ImageProcessor imageProcessor, int width, int topHeight, int bottomHeight) {
    for (int y = 0; y < topHeight; y++) {
      for (int x = 0; x < width; x++) {
        int topPixel = imageProcessor.getPixel(x, y);
        int bottomPixel = imageProcessor.getPixel(x, y + bottomHeight);
        imageProcessor.putPixel(x, y, bottomPixel);
        imageProcessor.putPixel(x, y + bottomHeight, topPixel);
      }
    }
  }

  private void showAndSaveResultImage(ImageProcessor imageProcessor) {
    ImagePlus imagePlus = new ImagePlus("Transf", imageProcessor);
    imagePlus.show();

    FileSaver fileSaver = new FileSaver(imagePlus);
    boolean saved = fileSaver.saveAsPng("src/main/res/res-edit.png");
    if (saved) {
      IJ.log("Result image saved successfully.");
    } else {
      IJ.log("Failed to save the result image.");
    }
  }

  public static void main(String[] args) {
    ImagePlus ip = IJ.openImage("src/main/res/custom-result.png");
    if (ip == null) {
      IJ.log("Failed to open the input image.");
      return;
    }

    ImageTransf imageTransformationFilter = new ImageTransf();
    imageTransformationFilter.setup("", ip);
    imageTransformationFilter.run(ip.getProcessor());
  }
}
