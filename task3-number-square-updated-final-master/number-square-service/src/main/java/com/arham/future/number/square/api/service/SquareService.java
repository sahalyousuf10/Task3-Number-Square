package com.arham.future.number.square.api.service;

import com.arham.future.number.square.api.dto.NumberDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
public class SquareService {

    public NumberDto findSquareOfANumber(int number) {
        log.info("Using FEIGN - calculating square of the number: " + number + " - by " + Thread.currentThread().getName());
        try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            NumberDto numberDto = new NumberDto();
            int squared = number * number;
            numberDto.setNumber(number);
            numberDto.setNumberSquare(squared);
            return numberDto;
    }
//    public Future<NumberDto> findSquareOfANumber(int number) throws ExecutionException, InterruptedException {
//        Future<NumberDto> numberDtoFuture = null;
//        NumberDto numberDto = new NumberDto();
//        int squaredNumber = number * number;
//        numberDto.setNumber(number);
//        numberDto.setNumberSquare(squaredNumber);
//        log.info("calculating square of the number: " + number + " - by " + Thread.currentThread().getName());
//        return numberDtoFuture;
//    }
}
