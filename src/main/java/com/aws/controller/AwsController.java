package com.aws.controller;

import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aws.service.AwsService;

@RestController
@RequestMapping("/aws/")
public class AwsController {

	@Autowired
	private AwsService awsService;
	
	@GetMapping("/files")
	public List<String> listAllFiles(){
		return awsService.listFiles();
	}
	
	@PostMapping("/upload")
    public String uploadMultipartFile(@RequestParam("uploadfile") MultipartFile[] files) {
		for(MultipartFile file:files) {
		awsService.upload(file);
		}
		return "Upload Done Successfully";
    }  
	
	@DeleteMapping("/deleteFile")
    public String deleteFile(@RequestPart(value = "url") String fileUrl) {
        return awsService.deleteFileFromS3Bucket(fileUrl);
    }
	
	@GetMapping("/download/url")
	public URL getUrl(@RequestPart(value = "url") String fileUrl) {
		return awsService.getDownloadUrl(fileUrl);
	}
	
}
