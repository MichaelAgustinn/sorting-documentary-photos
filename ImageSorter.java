import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class ImageSorter {
  private JFrame frame;
  private JLabel currentImageLabel;
  private JLabel prevImageLabel;
  private JLabel nextImageLabel;
  private JLabel prevStatusLabel;

  private File[] images;
  private int currentIndex = 0;
  private File fixDir, dupDir;

  private File lastMovedFile = null;
  private String lastMovedDirName = "";

  public ImageSorter(String sourceDir, String fixDirName, String dupDirName) {
    File source = new File(sourceDir);
    images = source.listFiles((dir, name) -> {
      String lower = name.toLowerCase();
      return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    });

    if (images == null || images.length == 0) {
      JOptionPane.showMessageDialog(null, "There's no image file in" + sourceDir);
      System.exit(0);
    }

    fixDir = new File(fixDirName);
    dupDir = new File(dupDirName);
    fixDir.mkdirs();
    dupDir.mkdirs();

    frame = new JFrame("Photo Sorter");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout(10, 10));

    // left panel
    JPanel prevPanel = new JPanel(new BorderLayout(5, 5));
    prevPanel.setBorder(BorderFactory.createTitledBorder("sortered image"));
    prevPanel.setPreferredSize(new Dimension(220, 0));

    prevImageLabel = new JLabel("empty", SwingConstants.CENTER);
    prevStatusLabel = new JLabel("<html><center>no images sortered</center></html>",
        SwingConstants.CENTER);

    prevPanel.add(prevImageLabel, BorderLayout.CENTER);
    prevPanel.add(prevStatusLabel, BorderLayout.SOUTH);

    // middle panel
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setBorder(BorderFactory.createTitledBorder("current image"));

    currentImageLabel = new JLabel("", SwingConstants.CENTER);
    centerPanel.add(currentImageLabel, BorderLayout.CENTER);

    // right panel
    JPanel nextPanel = new JPanel(new BorderLayout(5, 5));
    nextPanel.setBorder(BorderFactory.createTitledBorder("next image"));
    nextPanel.setPreferredSize(new Dimension(220, 0));

    nextImageLabel = new JLabel("empty", SwingConstants.CENTER);
    nextPanel.add(nextImageLabel, BorderLayout.CENTER);

    // add all panel to main frame
    frame.add(prevPanel, BorderLayout.WEST);
    frame.add(centerPanel, BorderLayout.CENTER);
    frame.add(nextPanel, BorderLayout.EAST);

    // button panel
    JPanel buttonPanel = new JPanel();
    JButton fixButton = new JButton("Fix");
    JButton dupButton = new JButton("Duplicate");

    fixButton.setPreferredSize(new Dimension(120, 40));
    dupButton.setPreferredSize(new Dimension(120, 40));

    fixButton.addActionListener(e -> moveImage(fixDir));
    dupButton.addActionListener(e -> moveImage(dupDir));

    buttonPanel.add(fixButton);
    buttonPanel.add(dupButton);

    frame.add(buttonPanel, BorderLayout.SOUTH);

    frame.setSize(1100, 650);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);

    // responsive view
    currentImageLabel.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        updateViews();
      }
    });

    updateViews();
  }

  private void updateViews() {
    if (currentIndex >= images.length) {
      JOptionPane.showMessageDialog(frame, "all image have been processed!");
      System.exit(0);
      return;
    }

    // current image (middle panel)
    File currentFile = images[currentIndex];
    int mainWidth = currentImageLabel.getWidth();
    int mainHeight = currentImageLabel.getHeight();
    if (mainWidth <= 0)
      mainWidth = 600;
    if (mainHeight <= 0)
      mainHeight = 450;

    currentImageLabel.setIcon(getScaledImageIcon(currentFile, mainWidth, mainHeight));

    // next image (right panel)
    if (currentIndex + 1 < images.length) {
      File nextFile = images[currentIndex + 1];
      nextImageLabel.setIcon(getScaledImageIcon(nextFile, 200, 300));
      nextImageLabel.setText("");
    } else {
      nextImageLabel.setIcon(null);
      nextImageLabel.setText("last image");
    }

    // sortered image (left panel)
    if (lastMovedFile != null && lastMovedFile.exists()) {
      prevImageLabel.setIcon(getScaledImageIcon(lastMovedFile, 200, 300));
      prevImageLabel.setText("");
      prevStatusLabel.setText("<html><center><b>moved to:</b><br><font color='blue'>"
          + lastMovedDirName + "</font><br><small>" + lastMovedFile.getName() + "</small></center></html>");
    } else {
      prevImageLabel.setIcon(null);
      prevImageLabel.setText("empty");
    }
  }

  private ImageIcon getScaledImageIcon(File imageFile, int maxWidth, int maxHeight) {
    if (imageFile == null || !imageFile.exists())
      return null;

    ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
    Image img = icon.getImage();

    int originalWidth = img.getWidth(null);
    int originalHeight = img.getHeight(null);

    if (originalWidth <= 0 || originalHeight <= 0)
      return null;

    double widthRatio = (double) maxWidth / originalWidth;
    double heightRatio = (double) maxHeight / originalHeight;

    double scale = Math.min(1.0, Math.min(widthRatio, heightRatio));

    int newWidth = Math.max(1, (int) (originalWidth * scale));
    int newHeight = Math.max(1, (int) (originalHeight * scale));

    Image scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    return new ImageIcon(scaled);
  }

  private void moveImage(File targetDir) {
    try {
      File currentFile = images[currentIndex];
      File targetFile = new File(targetDir, currentFile.getName());

      Files.move(currentFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

      lastMovedFile = targetFile;
      lastMovedDirName = targetDir.getName();

      currentIndex++;
      updateViews();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(frame, "failed:" + e.getMessage());
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new ImageSorter("./source", "./fix", "./duplicate"));
  }
}
