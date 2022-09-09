package com.arham.future.number.square.api.controller;

import com.arham.future.number.square.api.dto.NumberDto;
import com.arham.future.number.square.api.service.SquareService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/square")
public class SquareController {
    @Autowired
    private SquareService service;
    @GetMapping("/value/{number}")
    public NumberDto findSquareOfANumber(@PathVariable int number) {
        NumberDto dto = service.findSquareOfANumber(number);
        return dto;
    }
}
