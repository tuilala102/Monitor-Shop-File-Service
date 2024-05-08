package com.mshop.fileservice.controller;

import com.mshop.fileservice.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {


    private final FileService fileService;

    @PostMapping("/string")
    public String saveFileString(@RequestBody String base64File) throws Exception {
        return fileService.saveFileString(base64File);
    }

    @PostMapping("/strings")
    public Map<String, String> getFileStrings(@RequestBody List<String> keys) throws Exception {
        return fileService.readFileStrings(keys);
    }

    @GetMapping("/string")
    public String getFileString(@RequestParam("key") String key) throws Exception {
        return fileService.readFileString(key);
    }



    @DeleteMapping("/")
    public void deleteFile(@RequestParam("key") String key) throws Exception {
        fileService.deleteFile(key);
    }

}
