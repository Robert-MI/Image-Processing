import ij.*;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import ij.process.*;
import java.awt.*;
import java.io.*;

public class ClashPlg implements PlugIn {
    private static final String crsFilePath = "src/main/res/rye-s-93.crs";
    private static final String stuFilePath = "src/main/res/rye-s-93.stu";

    public static void main(String[] args) {
        ClashPlg clashMarkerPlugin = new ClashPlg();
        clashMarkerPlugin.run("");
    }

    @Override
    public void run(final String s) {
        int courseCount = countCoursesFromCRSFile();
        ImageProcessor binProc = createBinaryImage(courseCount);
        markClashingCourses(binProc);
        saveBinaryImage(binProc, "src/main/res/custom-result.png");
    }

    private ImageProcessor createBinaryImage(int size) {
        ImageProcessor binProc = new BinaryProcessor(new ByteProcessor(size, size));
        binProc.setColor(Color.WHITE);
        binProc.fill();
        return binProc;
    }

    private void markClashingCourses(ImageProcessor binProc) {
        try (BufferedReader reader = new BufferedReader(new FileReader(stuFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                for (int i = 0; i < tokens.length; i++) {
                    for (int j = i + 1; j < tokens.length; j++) {
                        int course1 = Integer.parseInt(tokens[i]);
                        int course2 = Integer.parseInt(tokens[j]);
                        binProc.putPixel(course1, course2, 0);
                        binProc.putPixel(course2, course1, 0);
                    }
                }
            }
        } catch (IOException e) {
            logAndPrintError("Fail to read .stu file: " + e.getMessage());
        }
    }

    private int countCoursesFromCRSFile() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(crsFilePath))) {
            int courseCount = 0;
            String line;
            while ((line = bufferedReader.readLine()) != null)
                courseCount++;
            return courseCount;
        } catch (IOException e) {
            logAndPrintError(e.getMessage());
        }
        return 0;
    }

    private void saveBinaryImage(ImageProcessor binProc, String filePath) {
        ImagePlus binaryImage = new ImagePlus("Courses", binProc);
        binaryImage.show();
        FileSaver FS = new FileSaver(binaryImage);
        FS.saveAsPng(filePath);
    }

    private void logAndPrintError(String message) {
        IJ.log(message);
        System.err.println(message);
    }
}
