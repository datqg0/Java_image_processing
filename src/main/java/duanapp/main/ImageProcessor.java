package duanapp.main;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;

/**
 * Stateless utility class for image processing operations.
 * All methods operate on copies or accept Mat as parameter to avoid side effects.
 */
public final class ImageProcessor {

    private ImageProcessor() {
        // Utility class — no instantiation
    }

    // -------------------------------------------------------------------------
    // Format conversions
    // -------------------------------------------------------------------------

    /**
     * Converts an OpenCV {@link Mat} to a JavaFX {@link WritableImage}.
     * Handles both grayscale (1-channel) and colour (3-channel BGR) mats.
     *
     * @param mat source mat
     * @return writable image, or {@code null} on error
     */
    public static WritableImage matToWritableImage(Mat mat) {
        try {
            int type = BufferedImage.TYPE_BYTE_GRAY;
            Mat display = mat;

            if (mat.channels() > 1) {
                Mat rgb = new Mat();
                Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_BGR2RGB);
                display = rgb;
                type = BufferedImage.TYPE_3BYTE_BGR;
            }

            BufferedImage bufferedImage = new BufferedImage(display.cols(), display.rows(), type);
            byte[] data = new byte[display.rows() * display.cols() * display.channels()];
            display.get(0, 0, data);
            bufferedImage.getRaster().setDataElements(0, 0, display.cols(), display.rows(), data);

            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            System.err.println("ImageProcessor: Error converting Mat to WritableImage — " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts an OpenCV {@link Mat} to a {@link BufferedImage}.
     * Handles both 3-channel BGR and 4-channel BGRA (PNG with alpha) mats safely.
     * If the Mat has 4 channels, the alpha channel is dropped (converted to BGR first).
     *
     * @param mat source mat (3-channel BGR or 4-channel BGRA)
     * @return corresponding 3-channel BufferedImage
     */
    public static BufferedImage matToBufferedImage(Mat mat) {
        // Normalise to a 3-channel BGR mat so the byte count always matches
        Mat bgr = mat;
        if (mat.channels() == 4) {
            bgr = new Mat();
            Imgproc.cvtColor(mat, bgr, Imgproc.COLOR_BGRA2BGR);
        } else if (mat.channels() == 1) {
            bgr = new Mat();
            Imgproc.cvtColor(mat, bgr, Imgproc.COLOR_GRAY2BGR);
        }

        BufferedImage image = new BufferedImage(bgr.width(), bgr.height(), BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = new byte[bgr.rows() * bgr.cols() * bgr.channels()];
        bgr.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, bgr.cols(), bgr.rows(), data);
        return image;
    }

    /**
     * Converts a {@link BufferedImage} back to an OpenCV {@link Mat}.
     * The returned mat has the same dimensions as {@code source}.
     *
     * @param source    BufferedImage to convert
     * @param prototype a Mat of the correct size to clone from (for metadata)
     * @return Mat representation of the image
     */
    public static Mat bufferedImageToMat(BufferedImage source, Mat prototype) {
        int width  = source.getWidth();
        int height = source.getHeight();
        Mat mat = prototype.clone();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb   = source.getRGB(i, j);
                int red   = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8)  & 0xFF;
                int blue  =  rgb        & 0xFF;

                double[] pixel = prototype.get(j, i);
                pixel[0] = blue;
                pixel[1] = green;
                pixel[2] = red;
                mat.put(j, i, pixel);
            }
        }
        return mat;
    }

    // -------------------------------------------------------------------------
    // Pixel-level operations
    // -------------------------------------------------------------------------

    /**
     * Applies a brightness delta to every pixel of {@code source} and returns a
     * new mat. The original mat is never modified.
     *
     * @param source          source mat
     * @param brightnessLevel delta to add to each channel (-255 … 255)
     * @return new mat with adjusted brightness
     */
    public static Mat applyBrightness(Mat source, int brightnessLevel) {
        Mat result = source.clone();
        for (int row = 0; row < result.rows(); row++) {
            for (int col = 0; col < result.cols(); col++) {
                double[] pixel = result.get(row, col);
                for (int ch = 0; ch < pixel.length; ch++) {
                    pixel[ch] = clamp(pixel[ch] + brightnessLevel, 0, 255);
                }
                result.put(row, col, pixel);
            }
        }
        return result;
    }

    /**
     * Inverts all channel values (negative / âm bản) and returns a new mat.
     *
     * @param source source mat (3-channel BGR expected)
     * @return new mat with inverted colours
     */
    public static Mat invertColors(Mat source) {
        Mat result = source.clone();
        for (int row = 0; row < result.rows(); row++) {
            for (int col = 0; col < result.cols(); col++) {
                double[] pixel = result.get(row, col);
                pixel[0] = 255 - pixel[0];
                pixel[1] = 255 - pixel[1];
                pixel[2] = 255 - pixel[2];
                result.put(row, col, pixel);
            }
        }
        return result;
    }

    /**
     * Converts {@code source} to greyscale and returns a new 3-channel BGR mat so
     * it is compatible with the rest of the pipeline.
     *
     * @param source source mat
     * @return greyscale mat (3-channel BGR)
     */
    public static Mat toGrayscale(Mat source) {
        Mat grey = new Mat();
        Imgproc.cvtColor(source, grey, Imgproc.COLOR_BGR2GRAY);
        Mat result = new Mat();
        Imgproc.cvtColor(grey, result, Imgproc.COLOR_GRAY2BGR);
        return result;
    }

    /**
     * Applies a 5×5 median blur and returns a new mat.
     *
     * @param source source mat
     * @return blurred mat
     */
    public static Mat applyMedianBlur(Mat source) {
        Mat result = new Mat();
        Imgproc.medianBlur(source, result, 5);
        return result;
    }

    /**
     * Darkens pixels that are <em>outside</em> the crop rectangle to give the
     * user a visual preview of the selected area.
     *
     * @param source       source mat (not modified)
     * @param startRow     top boundary (inclusive)
     * @param startCol     left boundary (inclusive)
     * @param endRow       bottom boundary (inclusive)
     * @param endCol       right boundary (inclusive)
     * @return preview mat
     */
    public static Mat buildCropPreview(Mat source,
                                       int startRow, int startCol,
                                       int endRow,   int endCol) {
        Mat preview = source.clone();
        for (int row = 0; row < preview.rows(); row++) {
            for (int col = 0; col < preview.cols(); col++) {
                boolean insideSelection = (row >= startRow && row <= endRow
                                        && col >= startCol && col <= endCol);
                if (!insideSelection) {
                    double[] pixel = preview.get(row, col);
                    for (int ch = 0; ch < pixel.length; ch++) {
                        pixel[ch] = clamp(pixel[ch] - 50, 0, 255);
                    }
                    preview.put(row, col, pixel);
                }
            }
        }
        return preview;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Clamps {@code value} to [min, max]. */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
