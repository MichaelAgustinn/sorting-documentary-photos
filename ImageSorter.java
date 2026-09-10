import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class ImageSorter {
  private JFrame frame;
  private JLabel imageLabel;
  private File[] images;
  private int currentIndex = 0;
  private File fixDir, dupDir;

  public ImageSorter(String sourceDir, String fixDirName, String dupDirName) {
    File source = new File(sourceDir);
    images = source.listFiles((dir, name) -> {
      String lower = name.toLowerCase();
      return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    });

    if (images == null || images.length == 0) {
      JOptionPane.showMessageDialog(null, "There's no image file in " + sourceDir);
      System.exit(0);
    }

    fixDir = new File(fixDirName);
    dupDir = new File(dupDirName);
    fixDir.mkdirs();
    dupDir.mkdirs();

    frame = new JFrame("Photo Sorter");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());

    frame.setResizable(false);

    imageLabel = new JLabel("", SwingConstants.CENTER);
    frame.add(imageLabel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    JButton fixButton = new JButton("Fix");
    JButton dupButton = new JButton("Duplicate");

    fixButton.addActionListener(e -> moveImage(fixDir));
    dupButton.addActionListener(e -> moveImage(dupDir));

    buttonPanel.add(fixButton);
    buttonPanel.add(dupButton);

    frame.add(buttonPanel, BorderLayout.SOUTH);
    frame.setSize(800, 600);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);

    loadImage();
  }

  private void loadImage() {
    if (currentIndex >= images.length) {
      JOptionPane.showMessageDialog(frame, "all images have been processed!");
      System.exit(0);
    }

    ImageIcon icon = new ImageIcon(images[currentIndex].getAbsolutePath());
    Image img = icon.getImage();

    int originalWidth = img.getWidth(null);
    int originalHeight = img.getHeight(null);

    int targetWidth = imageLabel.getWidth();
    int targetHeight = imageLabel.getHeight();

    if (targetWidth == 0)
      targetWidth = 800;
    if (targetHeight == 0)
      targetHeight = 500;

    double widthRatio = (double) targetWidth / originalWidth;
    double heightRatio = (double) targetHeight / originalHeight;

    double scale = Math.min(widthRatio, heightRatio);

    scale = Math.min(1.0, scale);

    int newWidth = (int) (originalWidth * scale);
    int newHeight = (int) (originalHeight * scale);

    Image scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    imageLabel.setIcon(new ImageIcon(scaled));
  }

  private void moveImage(File targetDir) {
    try {
      Files.move(images[currentIndex].toPath(),
          new File(targetDir, images[currentIndex].getName()).toPath(),
          StandardCopyOption.REPLACE_EXISTING);
      currentIndex++;
      loadImage();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(frame, "failed: " + e.getMessage());
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new ImageSorter("./source", "./fix", "./duplicate"));
  }
}
