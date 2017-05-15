package com.uw.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class ImageManipulation {	  
	
	  public String myHandler(String fileName, Context context) {
		  
	        LambdaLogger logger = context.getLogger();
	        String resultString = "success";
	        
	        boolean success = false;
	        success = greyScaleImage(fileName);
	       	        
	        logger.log("greyScaleImage returned :: "+success);
	        if(!success)
	        	resultString = "failure";
	        
	        return resultString.toString();
	  }
	  
	  public boolean greyScaleImage(String fileName){
		  
		  String bucketName = "image-samples";
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
	        
	      return result;
	  }
	  
	  public BufferedImage readImageS3(String bucketName, String imageName) throws IOException{
		  
		  AmazonS3 s3Client = new AmazonS3Client();
		  BufferedImage image = null;
		  try {
			  
			  S3Object xFile = s3Client.getObject(bucketName, imageName);
			  InputStream contents = xFile.getObjectContent();
			  ImageInputStream iin = ImageIO.createImageInputStream(xFile.getObjectContent());
			  image = ImageIO.read(iin);
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
			  
		      ByteArrayOutputStream os = new ByteArrayOutputStream();
		      ImageIO.write(convertedImage, "jpg", os);
		      byte[] buffer = os.toByteArray();
		      InputStream is = new ByteArrayInputStream(buffer);
		      ObjectMetadata meta = new ObjectMetadata();
		      meta.setContentLength(buffer.length);
		      meta.setContentType("image/jpg");
		      s3client.putObject(new PutObjectRequest(bucketName, processedImageName, is, meta));
		
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
	  
	  public void writeImage(BufferedImage convertedImage, String processedImageName) throws IOException{
		  
		  	 File ouptut = new File(processedImageName);
	         ImageIO.write(convertedImage, "jpg", ouptut);		  
	  }
	  
	  public static void main(String[] args){
		  boolean convert = false;

		  ImageManipulation obj = new ImageManipulation();
		  convert = obj.greyScaleImage("test.jpg");
		  
		  System.out.println(convert);
	  }
	  
}
