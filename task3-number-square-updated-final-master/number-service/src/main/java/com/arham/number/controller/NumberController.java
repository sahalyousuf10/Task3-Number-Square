package com.arham.number.controller;

import com.arham.number.model.NumberModel;
import com.arham.number.service.NumberService;
import jdk.internal.dynalink.linker.LinkerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/api/numbers")
public class NumberController {
    @Autowired
    private NumberService service;
    @PostMapping(value = "/file", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public ResponseEntity<?> saveFileAsync(@RequestParam(value = "file") MultipartFile file) throws Exception {
        List<NumberModel> numberModel = service.saveFileAsyncAtomic(file);
        return ResponseEntity.status(HttpStatus.OK).body(numberModel);
    }

    @GetMapping("/hello")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("Hello Arham");
    }
}
