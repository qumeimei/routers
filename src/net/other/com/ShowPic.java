package net.other.com;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
 
public class ShowPic{
 
	public static void main(String[] args) {
 
		try {
			/*
 
			byte[] imageInByte;
			BufferedImage originalImage = ImageIO.read(new File(
					"ReceivedFile/ouput"));
 
			// convert BufferedImage to byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(originalImage, "jpg", baos);
			baos.flush();
			imageInByte = baos.toByteArray();
			baos.close();
 **/
			// convert byte array back to BufferedImage
			String soureceFilePath="ReceivedFile\\output";
			File file = new File(soureceFilePath);
			DataInputStream diStream = new DataInputStream(new FileInputStream(file));
            long len = (int) file.length();
            byte[] fileBytes = new byte[(int) len];
            
            
            
			InputStream in = new ByteArrayInputStream(fileBytes);
			BufferedImage bImageFromConvert = ImageIO.read(in);
 
			ImageIO.write(bImageFromConvert, "jpg", new File(
					"ReceivedFile/ouput.jpg"));
 
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
