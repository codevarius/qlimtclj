package org.qwep.core.impl;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Deprecated
public class QRman {

    @Deprecated
    public static void createQrImage(String data, String path, String charset, int width, int height)
            throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(
                new String(data.getBytes(charset), charset),
                BarcodeFormat.QR_CODE, width, height);
        MatrixToImageWriter.writeToPath(
                matrix,
                path.substring(path.lastIndexOf('.') + 1),
                new File(path).toPath());
    }

    @Deprecated
    public static String readQR(String path) throws IOException, NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(path)))));
        Result result = new MultiFormatReader().decode(binaryBitmap);
        return result.getText();
    }

    @Deprecated
    public static String readQR(BufferedImage img) throws NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(img)));
        Result result;
        try {
            result = new MultiFormatReader().decode(binaryBitmap);
        } catch (Exception exception) {
            System.out.println(exception.getLocalizedMessage());
            return "unknown";
        }
        return result.getText();
    }

    @Deprecated
    public static int[] flatten(int[][] arr) {
        int[] result = new int[arr.length * arr[0].length];
        int index = 0;
        for (int[] ints : arr) {
            for (int j = 0; j < arr.length; j++) {
                result[index++] = ints[j];
            }
        }
        return result;
    }

    @Deprecated
    public static int[] createQrVec(String data, String charset, int height, int width)
            throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(
                new String(data.getBytes(charset), charset),
                BarcodeFormat.QR_CODE, width, height);
        BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix);
        int[][] out = new int[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                out[i][j] = img.getRGB(i, j) < -1 ? 1 : 0;
        return flatten(out);
    }

    @Deprecated
    public static BufferedImage drawImgFromVec(int[] vec, int width, int height) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int i = 0;
        for (int rc = 0; rc < height; rc++) {
            for (int cc = 0; cc < width; cc++) {
                img.setRGB(cc, rc, vec[i++] == 1 ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        ImageIO.write(img, "jpg", new File("restored.jpg"));
        return img;
    }

}
