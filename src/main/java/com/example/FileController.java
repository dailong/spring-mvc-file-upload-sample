package com.example;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.concurrent.Callable;

/**
 * Created by pkpk1234 on 2017/5/5.
 */

@RestController
@RequestMapping(path = "/files")
public class FileController {
    private static Logger logger = LoggerFactory.getLogger(FileController.class);
    private Path upload_files = Paths.get("upload_files");

    @GetMapping(path="/read/{filename}",name = "read")
    public ResponseEntity<?> getFile(@PathVariable("filename") String filename) {
        Path filePath = upload_files.resolve(filename);
        File file = filePath.toFile();
        logger.info("file >>>>>> " + file.getAbsolutePath());
        if (file.exists()) {
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload")
    public Callable<ResponseEntity<?>> uploadFile(@RequestParam MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        Path target = upload_files.resolve(fileName);
        logger.info("target >>>>>> " + target.toAbsolutePath());
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            URI fileUri = URI.create(MvcUriComponentsBuilder.fromMappingName("read").build()+fileName);
            logger.info("fileUri >>>>" + fileUri);
            return () -> ResponseEntity.created(fileUri).contentType(MediaType.TEXT_PLAIN).body("Create Successful");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
