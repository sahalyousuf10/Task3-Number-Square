package com.arham.number.service;

import com.arham.number.feign.FeignService;
import com.arham.number.model.NumberModel;
import com.arham.number.repository.NumberRepository;
import com.arham.number.valueobject.ValueObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Slf4j
@Service
public class NumberService {
    @Autowired
    private NumberRepository repository;
    @Autowired
    private FeignService feignService;
    @Autowired
    private SquareService squareService;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public List<NumberModel> saveFileAsyncBuffer(MultipartFile file) throws Exception {
        double startTime = System.currentTimeMillis();
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        String directory = "C:\\Users\\askar\\Downloads\\Arham\\task3-number-square-service-updated\\number-service\\outputFile4.csv";
        Path path = Paths.get(directory);

        // Write Data to buffer
        try(AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE)){
            // Write to async channel from buffer
            // starting from position 0
            inputStreamReader = new InputStreamReader(file.getInputStream());
            br = new BufferedReader(inputStreamReader);
            String result = null;
            String line = br.readLine();
            line = br.readLine();
            List<NumberModel> finalModelList = new ArrayList<>();
            while (line != null) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int num = Integer.parseInt(line);
                CompletableFuture<NumberModel> numberModelCompletableFuture = getSquaredModel(num);
                numberModelCompletableFuture.thenApplyAsync(numberModel -> {
                    buffer.put((numberModel.getNumber() + "," + numberModel.getNumberSquare() + "\n").getBytes());
                    buffer.flip();
                    Future<Integer> future = null;
                    finalModelList.add(numberModel);

                    try {
                        log.info("write file with " + numberModel.getNumber() + "," + numberModel.getNumberSquare() +
                                " using Thread " + Thread.currentThread().getName());
                        future = asyncChannel.write(buffer, asyncChannel.size());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return future;
                }).join();
                buffer.clear();
                line = br.readLine();
            }
//            while (true) {
//                if (executor.isTerminated()) {
//                    break;
//                }
//            }
            System.out.println("exiting main thread");
            double endTime = System.currentTimeMillis();
            System.out.println("Total app run time " + (endTime - startTime)/1000 + " seconds");
            return finalModelList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

//    private CompletableFuture<NumberModel> getSquaredModelNew(int num) {
//        CompletableFuture<NumberModel> futureSearchInDb = new CompletableFuture<>();
//        CompletableFuture<NumberModel> futureCalculateInService = new CompletableFuture<>();
//
//        CompletableFuture.runAsync(() -> {
//            Optional<NumberModel> outputModel = repository.findById(num);
//            if (outputModel.isPresent()) {
//                log.info("Number: " + num + " already in db");
//                futureSearchInDb.complete(outputModel.get());
//            } else {
//                log.info("Number " + num + " not in db. To be calculated by service");
//                futureCalculateInService.complete(null);
//            }
//        });
//
//        return futureCalculateInService
//                .thenApplyAsync(result -> {
//                    log.info("Invoking Square service using " + Thread.currentThread().getName());
//                    NumberModel numberModel = squareService.findSquareOfANumber(num);
//                    log.info("Database saving sq: " + numberModel.getNumberSquare() + " of the num: " + num);
//                    repository.save(numberModel);
//                    return numberModel;
//                }, executor)
//                .applyToEither(futureSearchInDb, Function.identity());
//    }

    public List<NumberModel> saveFileAsyncAtomic(MultipartFile file) throws Exception {
        InputStreamReader is = null;
        BufferedReader br = null;
        String path = "C:\\Users\\askar\\Downloads\\Arham\\task3-number-square-service-updated\\number-service\\outputFile3.csv";
        AtomicReference<FileWriter> fr = new AtomicReference<>(new FileWriter(path));
        AtomicReference<BufferedWriter> bw = new AtomicReference<>(new BufferedWriter(fr.get()));

        try {
            is = new InputStreamReader(file.getInputStream());
            br = new BufferedReader(is);
            String line = br.readLine();
            line = br.readLine();
            List<NumberModel> finalModelList = new ArrayList<>();
            List<CompletableFuture<NumberModel>> futureObj = new ArrayList<>();
            while (line != null) {
                int num = Integer.parseInt(line);
                CompletableFuture<NumberModel> numberModelCompletableFuture = getSquaredModel(num);
                    numberModelCompletableFuture.thenApplyAsync(numberModel -> {
                        try {
                            log.info("write in file: " + numberModel.getNumber() + "," + numberModel.getNumberSquare());
                            bw.get().write(numberModel.getNumber() + "," + numberModel.getNumberSquare() + "\n");
                            finalModelList.add(numberModel);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return 42;
//                        return CompletableFuture.completedFuture(numberModel);
                    }).join();
                line = br.readLine();
            }

            System.out.println(finalModelList);
            return finalModelList;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                br.close();
            }
            if (bw.get() != null) {
                bw.get().close();
            }
        }
    }
    public List<NumberModel> saveFileAsyncListWriter(MultipartFile file) throws Exception {
        InputStreamReader is;
        BufferedReader br = null;
        BufferedWriter bw = null;
        String path = "C:\\Users\\askar\\Downloads\\Arham\\task3-number-square-service-updated\\number-service\\outputFile2.csv";
        FileWriter fw = new FileWriter(path);

        try {
            is = new InputStreamReader(file.getInputStream());
            br = new BufferedReader(is);
            bw = new BufferedWriter(fw);
            String line = br.readLine();
            line = br.readLine();
            List<CompletableFuture<NumberModel>> futureObj = new ArrayList<>();
            while (line != null) {
                int num = Integer.parseInt(line);
                CompletableFuture<NumberModel> returnedModel = getSquaredModel(num);
                futureObj.add(returnedModel);
                line = br.readLine();
            }

            List<NumberModel> finalModelList = new ArrayList<>();
            for (CompletableFuture<NumberModel> fu : futureObj) {
                NumberModel numberModel = fu.get();
                finalModelList.add(numberModel);
                bw.write(numberModel.getNumber() + "," + numberModel.getNumberSquare() + "\n");
            }
            System.out.println(finalModelList);
            return finalModelList;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.close();
            }
        }
    }

    private CompletableFuture<NumberModel> getSquaredModel(int num) {
        CompletableFuture<NumberModel> futureSearchInDb = new CompletableFuture<>();
        CompletableFuture<NumberModel> futureCalculateInService = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            Optional<NumberModel> outputModel = repository.findById(num);
            if (outputModel.isPresent()) {
                log.info("Number: " + num + " already in db");
                futureSearchInDb.complete(outputModel.get());
            } else {
                log.info("Number " + num + " not in db. To be calculated by service");
                futureCalculateInService.complete(null);
            }
        });

        return futureCalculateInService
                .thenApplyAsync(result -> {
                    NumberModel numberModel = squareService.findSquareOfANumber(num);
                    log.info("Database saving sq: " + numberModel.getNumberSquare() + " of the num: " + num);
                    repository.save(numberModel);
                    return numberModel;
                    }, executor)
                .applyToEither(futureSearchInDb, Function.identity());
    }

    public List<NumberModel> saveFileAsyncOld(MultipartFile file) throws Exception {
        InputStreamReader is = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        String path = "C:\\Users\\askar\\Downloads\\Arham\\task3-number-square-service-updated\\number-service\\outputFile1.csv";
        FileWriter fw = new FileWriter(path);

        try {
            is = new InputStreamReader(file.getInputStream());
            br = new BufferedReader(is);
            bw = new BufferedWriter(fw);
            String line = br.readLine();
            line = br.readLine();
            List<Integer> integerList = new ArrayList<>();
            while (line != null) {
                int num = Integer.parseInt(line);
                integerList.add(num);
                line = br.readLine();
            }

            List<CompletableFuture<NumberModel>> futureObj = new ArrayList<>();
            for (int num : integerList) {
                CompletableFuture<NumberModel> returnedModel = getSquaredModel(num);
                futureObj.add(returnedModel);
            }

            List<NumberModel> finalModelList = new ArrayList<>();
            for (CompletableFuture<NumberModel> fu : futureObj) {
                NumberModel numberModel = fu.get();
                log.info("saving numberModel " + numberModel + " in final list");
                finalModelList.add(numberModel);
                log.info("writing in file " + numberModel.getNumber() + "," + numberModel.getNumberSquare());
                bw.write(numberModel.getNumber() + "," + numberModel.getNumberSquare() + "\n");
            }
            System.out.println(finalModelList);
            return finalModelList;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.close();
            }
        }
    }

    private CompletableFuture<NumberModel> getSquaredModelOld(int num) {
        CompletableFuture<NumberModel> futureSearchInDb = new CompletableFuture<>();
        CompletableFuture<NumberModel> futureCalculateInService = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            Optional<NumberModel> outputModel = repository.findById(num);
            if (outputModel.isPresent()) {
                log.info("Number: " + num + " already in db");
                futureSearchInDb.complete(outputModel.get());
            } else {
                log.info("Number " + num + " not in db. To be calculated by service");
                futureCalculateInService.complete(null);
            }
        }, executor);

        return futureCalculateInService
                .thenApplyAsync(result -> {
                    NumberModel numberModel = squareService.findSquareOfANumber(num);
                    log.info("Database saving sq: " + numberModel.getNumberSquare() + " of the num: " + num);
                    repository.save(numberModel);
                    return numberModel;
                }, executor)
                .applyToEither(futureSearchInDb, Function.identity());
    }

}
