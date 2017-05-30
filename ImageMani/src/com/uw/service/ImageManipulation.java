package com.uw.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class ImageManipulation implements RequestHandler<Object, Object> {
	
	  @Override
	  public String handleRequest(Object input, Context context) {
		  
		  long start=System.currentTimeMillis();
	        StringBuilder resultString = new StringBuilder("Success");
	        
	        HashMap<String, String> mInput = (HashMap<String, String>) input;
			String fileName = mInput.get("fileName");
			String bucket = mInput.get("bucket");
	        
	        boolean success1 = greyScaleImage(fileName,bucket);
	        boolean success2 = resizeImage(fileName,bucket);
	        boolean success3=rotateImage(fileName,bucket);
	       	        
	        if(!success1 || !success2 || !success3){
	        	resultString = new StringBuilder("Failure");
	        	
	        }
	        /*resultString.append(" GreyScale Image returned :: "+success1);
        	resultString.append(" Resize Image returned :: "+success2);
        	resultString.append(" Rotate Image returned :: "+success3);*/
        	           
	        System.out.println(" GreyScale Image returned :: "+success1);
	        System.out.println(" Resize Image returned :: "+success2);
	        System.out.println(" Rotate Image returned :: "+success3);
          
        	System.out.println(resultString.toString());
        	
        long end=System.currentTimeMillis();
        System.out.println("\n Time to execute resizing service (milliseconds):"+(end-start));
      		
	        return resultString.toString();
	  }
	  
	  public boolean resizeImage(String fileName,String bucketName){

		  String imageName = fileName;
		  String processedImageName = "resize-"+fileName;
		  long start=System.currentTimeMillis();

		  
		  boolean result = false;
		  
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
	            result = true;
	      } 
	      catch (Exception e) {
	        	System.out.println(e.getMessage());
	        	e.printStackTrace();
	        	result = false;
	      }
	      
	      long end=System.currentTimeMillis();
	      System.out.println("\n Time to execute resizing service (milliseconds):"+(end-start));
	      		
	        
	      return result;
	  
	  }
	  
	  /*
	   * Rotate image 90Degreess clockwise
	   * http://forum.codecall.net/topic/69182-java-image-rotation/
	   */

	  public boolean rotateImage(String fileName,String bucketName){

		  
		  String imageName = fileName;
		  String processedImageName = "rotate-"+fileName;
		  long start=System.currentTimeMillis();
	           		
	        
		  boolean result = false;
		  
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
	            result = true;
	      } 
	      catch (Exception e) {
	        	System.out.println(e.getMessage());
	        	e.printStackTrace();
	        	result = false;
	      }
	        
	      long end=System.currentTimeMillis();
	      System.out.println("\n Time to execute resizing service (milliseconds):"+(end-start));
	      		
	        
	      return result;
	  
	  }
	 
	  
	  /*
	   * GreyScale
	   * 
	   */
	  
	  public boolean greyScaleImage(String fileName,String bucketName){
		 
	      long start=System.currentTimeMillis();

		  String imageName = fileName;
		  String processedImageName = "grayscale-"+fileName;
		  
		  boolean result = false;
		  
		  BufferedImage  image;
	      int width;
	      int height;
	        
	      try {       	
	        	//Reading image from S3
	        	image = readImageS3(bucketName, imageName);
	  		  	  		  	
	            width = image.getWidth();
	            height = image.getHeight();
	            
	            for(int i=0; i<height; i++){
	            
	               for(int j=0; j<width; j++){
	               
	                  Color c = new Color(image.getRGB(j, i));
	                  int red = (int)(c.getRed() * 0.299);
	                  int green = (int)(c.getGreen() * 0.587);
	                  int blue = (int)(c.getBlue() *0.114);
	                  Color newColor = new Color(red+green+blue,
	                  
	                  red+green+blue,red+green+blue);
	                  
	                  image.setRGB(j,i,newColor.getRGB());
	               }
	            }	            
	            
	            //writing image to S3
	            writeImageS3(image, bucketName, processedImageName);
	            //writeImage(image, processedImageName);
	            result = true;
	      } 
	      catch (Exception e) {
	        	System.out.println(e.getMessage());
	        	e.printStackTrace();
	        	result = false;
	      }
	        
	      long end=System.currentTimeMillis();
		  System.out.println("Execution time for (milliseconds):"+(end-start));
		  
	      return result;
	  }
	  
	  public static BufferedImage readImageS3(String bucketName, String imageName) throws IOException{
		  
		  
		  AmazonS3 s3Client = new AmazonS3Client();
		  BufferedImage image = null;
		  try {
			
			  long start=System.currentTimeMillis();
			  
			  S3Object xFile = s3Client.getObject(bucketName, imageName);
			  InputStream contents = xFile.getObjectContent();
			  ImageInputStream iin = ImageIO.createImageInputStream(xFile.getObjectContent());
			  image = ImageIO.read(iin);
			  
			  long end=System.currentTimeMillis();
			  System.out.println("Read time from S3(milliseconds):"+(end-start));
		  }
		  catch (AmazonServiceException ase) {
		      
			  System.out.println("Caught an AmazonServiceException, which " +
		            		"means your request made it " +
		                    "to Amazon S3, but was rejected with an error response" +
		                    " for some reason.");
		      
			  System.out.println("Error Message:    " + ase.getMessage());
		      System.out.println("HTTP Status Code: " + ase.getStatusCode());
		      System.out.println("AWS Error Code:   " + ase.getErrorCode());
		      System.out.println("Error Type:       " + ase.getErrorType());
		      System.out.println("Request ID:       " + ase.getRequestId());
		  } 
		  catch (AmazonClientException ace) {
		      
			  System.out.println("Caught an AmazonClientException, which " +
		            		"means the client encountered " +
		                    "an internal error while trying to " +
		                    "communicate with S3, " +
		                    "such as not being able to access the network.");
		      
			  System.out.println("Error Message: " + ace.getMessage());
		  }	  
		  
		  return image;
	  }
	  
	  public void writeImageS3(BufferedImage convertedImage, String bucketName, String processedImageName) throws IOException{
		  		
		  //AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
		  
		  AmazonS3 s3client = new AmazonS3Client();
		  try {
			  
			  System.out.println("Uploading a new object to S3 from a file\n");
		      
			  /*File file = new File(processedImageName);
		      ImageIO.write(convertedImage, "jpg", file);
		      s3client.putObject(new PutObjectRequest(bucketName, processedImageName, file));
		      */
			  
			  long start=System.currentTimeMillis();

			  ByteArrayOutputStream os = new ByteArrayOutputStream();
		      ImageIO.write(convertedImage, "jpg", os);
		      byte[] buffer = os.toByteArray();
		      InputStream is = new ByteArrayInputStream(buffer);
		      ObjectMetadata meta = new ObjectMetadata();
		      meta.setContentLength(buffer.length);
		      meta.setContentType("image/jpg");
		      
		      s3client.putObject(new PutObjectRequest(bucketName, processedImageName, is, meta));

			  long end=System.currentTimeMillis();
			  System.out.println("Write time to S3(milliseconds):"+(end-start));


		      //TransferManager tm = new TransferManager(s3client);
		      //Upload upload = tm.upload(new PutObjectRequest(bucketName, processedImageName, file));
		  } 
		  catch (AmazonServiceException ase) {
		      
			  System.out.println("Caught an AmazonServiceException, which " +
		            		"means your request made it " +
		                    "to Amazon S3, but was rejected with an error response" +
		                    " for some reason.");
		      
			  System.out.println("Error Message:    " + ase.getMessage());
		      System.out.println("HTTP Status Code: " + ase.getStatusCode());
		      System.out.println("AWS Error Code:   " + ase.getErrorCode());
		      System.out.println("Error Type:       " + ase.getErrorType());
		      System.out.println("Request ID:       " + ase.getRequestId());
		  } 
		  catch (AmazonClientException ace) {
		      
			  System.out.println("Caught an AmazonClientException, which " +
		            		"means the client encountered " +
		                    "an internal error while trying to " +
		                    "communicate with S3, " +
		                    "such as not being able to access the network.");
		      
			  System.out.println("Error Message: " + ace.getMessage());
		  }
	  }
	  
}
