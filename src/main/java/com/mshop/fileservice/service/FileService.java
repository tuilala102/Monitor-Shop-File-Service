package com.mshop.fileservice.service;

import java.util.List;
import java.util.Map;

public interface FileService {

    String saveFileString(String base64) throws Exception;

    String readFileString(String key) throws Exception;

    Map<String, String> readFileStrings(List<String> keys) throws Exception;

    void deleteFile(String key) throws Exception;

}
