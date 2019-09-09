package com.aws.service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@Service
public class AwsService {

	private Logger logger = LoggerFactory.getLogger(AwsService.class);

	@Autowired
	private AmazonS3 s3client;
	
	@Bean
    public static AmazonS3 S3Client() {
        return (AmazonS3) AmazonS3ClientBuilder.standard()
        		.withRegion("ap-south-1")
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

	@Value("${jsa.s3.bucket}")
	private String bucketName;
	@Value("${endpointUrl}")
	private String endpointUrl;
	
	/* list files method */
	public List<String> listFiles() {

		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName);
		List<String> keys = new ArrayList<>();
		ObjectListing objects = s3client.listObjects(listObjectsRequest);

		while (true) {
			List<S3ObjectSummary> summaries = objects.getObjectSummaries();
			if (summaries.size() < 1) {
				break;
			}

			for (S3ObjectSummary item : summaries) {
				if (!item.getKey().endsWith("/"))
					keys.add(item.getKey());
			}

			objects = s3client.listNextBatchOfObjects(objects);
		}

		return keys;
	}
	
	/* upload file method*/
	public void upload(MultipartFile file) {
		String fileName = generateFileName(file);
		try {
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(file.getSize());
			s3client.putObject(bucketName, fileName, file.getInputStream(), metadata);
		} catch (IOException ioe) {
			logger.error("IOException: " + ioe.getMessage());
		} catch (AmazonServiceException ase) {
			logger.info("Caught an AmazonServiceException from PUT requests, rejected reasons:");
			logger.info("Error Message:    " + ase.getMessage());
			logger.info("HTTP Status Code: " + ase.getStatusCode());
			logger.info("AWS Error Code:   " + ase.getErrorCode());
			logger.info("Error Type:       " + ase.getErrorType());
			logger.info("Request ID:       " + ase.getRequestId());
			throw ase;
		} catch (AmazonClientException ace) {
			logger.info("Caught an AmazonClientException: ");
			logger.info("Error Message: " + ace.getMessage());
			throw ace;
		}
	}
	
	private String generateFileName(MultipartFile multiPart) {
		 return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
	}
	
	/* delete file method*/
	public String deleteFileFromS3Bucket(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        return "Successfully deleted";
    }
	
	/* download file method*/
	public URL getDownloadUrl(String key) {
		return s3client.getUrl(bucketName, key);
	}
	
}
