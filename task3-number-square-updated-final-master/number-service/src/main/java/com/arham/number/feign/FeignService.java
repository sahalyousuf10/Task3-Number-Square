package com.arham.number.feign;

import com.arham.number.model.NumberModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.Future;

@FeignClient(name = "number-square-api", url = "http://localhost:8051/api/square")
public interface FeignService {
    @GetMapping("/value/{number}")
    NumberModel findSquareOfANumber(@PathVariable int number);
}
