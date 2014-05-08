package net.other.com;


/**
 *
 * @author Khamis
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;


public class LoadAnshowImageFromFile extends JPanel {
          
    BufferedImage image;
    private static JPanel imagePanel;
    private static JFrame frame;
    //You can specify your own image directory
    private String dir = "ReceivedFile//output";


    public LoadAnshowImageFromFile() {
       try {
          /**
           * ImageIO.read() returns a BufferedImage object, decoding the supplied  
           * file with an ImageReader, chosen automatically from registered files 
           * The File is wrapped in an ImageInputStream object, so we don't need
           * one. Null is returned, If no registered ImageReader claims to be able
           * to read the resulting stream.
           */
           image = ImageIO.read(new File(dir));
       } catch (IOException e) {
           //Let us know what happened
           System.out.println("Error reading dir: " + e.getMessage());
       }

    }
    //We set our preferred size if we succeeded in loading image
    public Dimension getPreferredSize() {
        if (image == null) {
             return new Dimension(100,100);
        } else {
           return new Dimension(image.getWidth(null), image.getHeight(null));
       }
    }
    
    //Draw our image on the screen with Graphic's "drawImage()" method
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }

    
    public static void main(String[] args) {

        frame = new JFrame("Loading Image From File Example");  
        imagePanel = new JPanel();
        //Release the resource window handle as we close the frame
        frame.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
        imagePanel.add(new LoadAnshowImageFromFile());
        frame.add(imagePanel);
        frame.pack();
        frame.setVisible(true);
    }
}
