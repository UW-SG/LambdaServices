package com.uw.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

@Path("/imageManipulation")
public class ImageManipulationService
{
  @GET
  @Produces({"text/plain"})
  public String home()
  {
    return "hello";
  }
  
  @Path("/{bucket}/{fileName}")
  @GET
  @Produces({"text/plain"})
  public String imageManipulation(@PathParam("fileName") String fileName,@PathParam("bucket") String bucketname)
  {
	  long start=System.currentTimeMillis();

	  
	  StringBuilder str= new StringBuilder();
	  str.append("\nGrayscale :");
	  str.append(grayScaleImage(fileName,bucketname));
	  
	  str.append("\nResize :");
	  str.append(resizeImage(fileName,bucketname));
	  
	  str.append("\nRotate :");
	  str.append(rotateImage(fileName,bucketname));
	  
	  long end=System.currentTimeMillis();
	  str.append("\nTotal time to exeucte all 3 services (milliseconds):"+(end-start));
	  System.out.println("Total time to exeucte all 3 services (milliseconds):"+(end-start));
	 
	  return str.toString();
  }
  
  
  @Path("/grayscale/{bucket}/{fileName}")
  @GET
  @Produces({"text/plain"})
  public String grayScaleImage(@PathParam("fileName") String fileName,@PathParam("bucket") String bucketName)
  {

	  System.out.println("\nGRAYSCALE IMAGE");
	  long start=System.currentTimeMillis();
	 
 // String bucketName = "uw.services.imagemanipulation";
  String imageName = fileName;
  String processedImageName = "grayscale-"+fileName;
	  
    String result = "failed";
    try
    {
      BufferedImage image = readImageS3(bucketName, imageName);
      
      int width = image.getWidth();
      int height = image.getHeight();
      for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++)
        {
          Color c = new Color(image.getRGB(j, i));
          int red = (int)(c.getRed() * 0.299D);
          int green = (int)(c.getGreen() * 0.587D);
          int blue = (int)(c.getBlue() * 0.114D);
          Color newColor = new Color(red + green + blue, red + green + blue, red + green + blue);
          
          image.setRGB(j, i, newColor.getRGB());
        }
      }
      writeImageS3(image, bucketName, processedImageName);
      
      result = "Success";
    }
    catch (Exception e)
    {
      System.out.println(e.getMessage());
      e.printStackTrace();
      result = "Failed";
    }
    
	  long end=System.currentTimeMillis();
	  System.out.println("Time to execute grayscale service (milliseconds):"+(end-start));
	 
    return result;
  }
  
  
  @Path("/resize/{bucket}/{fileName}")
  @GET
  @Produces({"text/plain"})
  public String resizeImage(@PathParam("fileName") String fileName,@PathParam("bucket") String bucketName){
    
	  System.out.println("\nRESIZE MAGE");
	  long start=System.currentTimeMillis();
	 
	//  String bucketName = "uw.services.imagemanipulation";
	  String imageName = fileName;
	  String processedImageName = "resize-"+fileName;
	  
	  String result = "Failure";
	  
	  BufferedImage  image;
      int width;
      int height;
        
      try {       	
        	//Reading image from S3
        	image = readImageS3(bucketName, imageName);
  		  	  		  	
            width = image.getWidth();
            height = image.getHeight();
            
            int type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();

    		BufferedImage resizedImage = new BufferedImage(width/4, height/4, type);            
    		Graphics2D g = resizedImage.createGraphics();
    		g.drawImage(image, 0, 0, width/4, height/4, null);
    		g.dispose();
    		
            //writing image to S3
            writeImageS3(resizedImage, bucketName, processedImageName);
            //writeImage(image, processedImageName);
            result = "Success";
      } 
      catch (Exception e) {
        	System.out.println(e.getMessage());
        	e.printStackTrace();
        	result = "Failure";
      }
        
      long end=System.currentTimeMillis();
	  System.out.println("Time to execute resizing service (milliseconds):"+(end-start));
	
      return result;
  
  }
  
  /*
   * Rotate image 90Degreess clockwise
   * http://forum.codecall.net/topic/69182-java-image-rotation/
   */

  @Path("/rotate/{bucket}/{fileName}")
  @GET
  @Produces({"text/plain"})
  public String rotateImage(@PathParam("fileName") String fileName,@PathParam("bucket") String bucketName){
	  
	  System.out.println("\nROTATE MAGE");
	  long start=System.currentTimeMillis();
		
	 // String bucketName = "uw.services.imagemanipulation";
	  String imageName = fileName;
	  String processedImageName = "rotate-"+fileName;
	  
	  String result = "Failure";
	  
	  BufferedImage  image;
     	        
      try { 
    	  
    	  	//Reading image from S3
        	image = readImageS3(bucketName, imageName);
  		  	
        	// Rotation information

        	int width=image.getWidth();
            int height=image.getHeight();
        	BufferedImage returnImage = new BufferedImage(height,width , image.getType()  );
        	for( int x = 0; x < width; x++ ) {
        		for( int y = 0; y < height; y++ ) {
        			returnImage.setRGB( height -y -1, x, image.getRGB( x, y  )  );
        //Again check the Picture for better understanding
        		}
        		}
        	
            //writing image to S3
            writeImageS3(returnImage, bucketName, processedImageName);
            //writeImage(image, processedImageName);
            result = "Success";
      } 
      catch (Exception e) {
        	System.out.println(e.getMessage());
        	e.printStackTrace();
        	result = "Failure";
      }
       
      long end=System.currentTimeMillis();
	  System.out.println("Time to execute image rotation service (milliseconds):"+(end-start));
	
      return result;
  
  }
  
  
  
  
  public BufferedImage readImageS3(String bucketName, String imageName)
    throws IOException
  {
    AmazonS3 s3Client = new AmazonS3Client();
    BufferedImage image = null;
    try
    {
      long start=System.currentTimeMillis();
          	
      S3Object xFile = s3Client.getObject(bucketName, imageName);
      InputStream contents = xFile.getObjectContent();
      ImageInputStream iin = ImageIO.createImageInputStream(xFile.getObjectContent());
      image = ImageIO.read(iin);
      
      long end=System.currentTimeMillis();
	  System.out.println("Time to read from S3(milliseconds):"+(end-start));
	
    }
    catch (AmazonServiceException ase)
    {
      System.out.println("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.");
      
      System.out.println("Error Message:    " + ase.getMessage());
      System.out.println("HTTP Status Code: " + ase.getStatusCode());
      System.out.println("AWS Error Code:   " + ase.getErrorCode());
      System.out.println("Error Type:       " + ase.getErrorType());
      System.out.println("Request ID:       " + ase.getRequestId());
    }
    catch (AmazonClientException ace)
    {
      System.out.println("Caught an AmazonClientException, which means the client encountered an internal error while trying to communicate with S3, such as not being able to access the network.");
      
      System.out.println("Error Message: " + ace.getMessage());
    }
    return image;
  }
  
  public void writeImageS3(BufferedImage convertedImage, String bucketName, String processedImageName)
    throws IOException
  {
    AmazonS3 s3client = new AmazonS3Client();
    try
    {
      System.out.println("Uploading a new object to S3 from a file");
      

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(convertedImage, "jpg", os);
      byte[] buffer = os.toByteArray();
      InputStream is = new ByteArrayInputStream(buffer);
      ObjectMetadata meta = new ObjectMetadata();
      meta.setContentLength(buffer.length);
      meta.setContentType("image/jpg");
      
      long start=System.currentTimeMillis();
	  
      
      s3client.putObject(new PutObjectRequest(bucketName, processedImageName, is, meta));
      
      long end=System.currentTimeMillis();
	  System.out.println("Time to write from S3(milliseconds):"+(end-start));
    }
    catch (AmazonServiceException ase)
    {
      System.out.println("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.");
      
      System.out.println("Error Message:    " + ase.getMessage());
      System.out.println("HTTP Status Code: " + ase.getStatusCode());
      System.out.println("AWS Error Code:   " + ase.getErrorCode());
      System.out.println("Error Type:       " + ase.getErrorType());
      System.out.println("Request ID:       " + ase.getRequestId());
    }
    catch (AmazonClientException ace)
    {
      System.out.println("Caught an AmazonClientException, which means the client encountered an internal error while trying to communicate with S3, such as not being able to access the network.");
      
      System.out.println("Error Message: " + ace.getMessage());
    }
  }
  
  public void writeImage(BufferedImage convertedImage, String processedImageName)
    throws IOException
  {
    File ouptut = new File(processedImageName);
    ImageIO.write(convertedImage, "jpg", ouptut);
  }
}
