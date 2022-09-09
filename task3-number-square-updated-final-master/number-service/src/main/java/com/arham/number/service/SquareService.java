package com.arham.number.service;

import com.arham.number.model.NumberModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SquareService {
    public NumberModel findSquareOfANumber(int number) {
        log.info("calculating square of the number: " + number + " - by " + Thread.currentThread().getName());
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        NumberModel numberDto = new NumberModel();
        int squared = number * number;
        numberDto.setNumber(number);
        numberDto.setNumberSquare(squared);
        return numberDto;
    }
}
