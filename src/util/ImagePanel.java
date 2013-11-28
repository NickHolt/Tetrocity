package util;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/** An ImagePanel is a simply JPanel component that contains an image and a JLabel only. Both
 * of these components can be set and updated by the appropriate methods.
 * The JLabel is placed under the drawn image.
 * 
 * @author Nick Holt
 */
public class ImagePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    BufferedImage mImage;
    String mText;
    
    /** A new ImagePanel that contains the specified image. The text of this ImagePanel will
     * be the empty string.
     * 
     * @param image The image of this ImagePanel
     */
    public ImagePanel(BufferedImage image) {
        setImage(image);
        setText("");
        
        setSize(imageWidth(), imageHeight());
        setVisible(true);
        repaint();
    }
    
    public int imageHeight() {
        return mImage.getHeight();
    }
    
    public int imageWidth() {
        return mImage.getWidth();
    }

    public void setImage(BufferedImage image) {
        mImage = image;
        repaint();
    }
    
    public void setText(String text) {
        mText = text;
        repaint();
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        
        int[] imageDimensions = new int[]{mImage.getHeight(), mImage.getWidth()};
        
        //Draw image
        g.drawImage(mImage, (int) (getWidth() - imageDimensions[1]) / 2,
                (int) (getHeight() - imageDimensions[0]) / 2, null);
        
        //Draw centered text
        FontMetrics fm   = g.getFontMetrics(g.getFont());
        Rectangle2D rect = fm.getStringBounds(mText, g);

        g.drawString(mText, (int) (getWidth()  - rect.getWidth())  / 2
                , (int) (getHeight() - fm.getAscent()));

        //Draw borders
        g.setColor(Color.BLACK);
        g.drawLine(0, 0, 0, (int) getHeight() -1);
        g.drawLine(0, (int) getHeight() - 1,
                   (int) getWidth() - 1, (int) getHeight() - 1);
    }
}
