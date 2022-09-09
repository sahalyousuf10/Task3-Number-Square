package com.arham.number.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.concurrent.Callable;

@Entity(name = "number_details")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NumberModel {
    @Id
    int number;
    int numberSquare;

//    @Override
//    public Object call() throws Exception {
//        return this;
//    }
}
