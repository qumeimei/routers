package net.other.com;


import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
 
public class reImage 
{	
    public static void main( String[] args )
    {
    	String dir="ReceivedFile//output";
    	Image image = null;
        try {
        	image = ImageIO.read(new File(dir));
        } catch (IOException e) {
        	e.printStackTrace();
        }
 
        JFrame frame = new JFrame();
        frame.setSize(300, 300);
        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label);
        frame.setVisible(true);
    }
}