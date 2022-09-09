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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Slf4j
@Service
@Deprecated
public class NumberServiceOld {
    @Autowired
    private NumberRepository repository;
    @Autowired
    private FeignService feignService;
    @Autowired
    private SquareService squareService;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public List<NumberModel> saveFileAsyncNew2(MultipartFile file) throws Exception {
        InputStreamReader is = null;
        BufferedReader br = null;
        String path = "C:\\Users\\askar\\Downloads\\Arham\\task3-number-square-service-updated\\outputFile2.csv";
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
                CompletableFuture<NumberModel> numberModelCompletableFuture = getSquaredModelNew(num);
                numberModelCompletableFuture.thenApply(numberModel -> {
                    try {
                        bw.get().write(numberModel.getNumber() + "," + numberModel.getNumberSquare() + "\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return 23;
                });
//                numberModelCompletableFuture.thenApply(result -> futureObj.add(numberModelCompletableFuture));
                line = br.readLine();
            }
//            for (CompletableFuture<NumberModel> numberModel : futureObj) {
//                bw.get().write(numberModel.get().getNumber() + "," + numberModel.get().getNumberSquare() + "\n");
//                numberModel.thenApply(res -> {
//                    try {
//                        finalModelList.add(numberModel.get());
//                    } catch (InterruptedException | ExecutionException e) {
//                        throw new RuntimeException(e);
//                    }
//                    return "42";
//                });
//            }

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
    public List<NumberModel> saveFileAsyncNew1(MultipartFile file) throws Exception {
        InputStreamReader is = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        String path = "C:\\Users\\askar\\Downloads\\Arham\\task3-number-square-service-updated\\outputFile2.csv";
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
                CompletableFuture<NumberModel> returnedModel = getSquaredModelNew(num);
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

    private CompletableFuture<NumberModel> getSquaredModelNew(int num) {
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

    public List<NumberModel> saveFileAsyncNew(MultipartFile file) throws Exception {
        InputStreamReader is = null;
        BufferedReader br = null;
        BufferedWriter bw = null;

        try {
            is = new InputStreamReader(file.getInputStream());
            br = new BufferedReader(is);
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
                finalModelList.add(numberModel);
            }
            System.out.println(finalModelList);
            return finalModelList;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                br.close();
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

    public List<NumberModel> saveFileAsync(MultipartFile file) throws Exception {
        InputStreamReader is = null;
        BufferedReader br = null;
        List<Integer> integerList = new ArrayList<>();
        int count = 0;

        try {
            is = new InputStreamReader(file.getInputStream());
            br = new BufferedReader(is);
            String line = br.readLine();
            line = br.readLine();
            while (line != null) {
                int value = Integer.parseInt(line);
                integerList.add(value);
                line = br.readLine();
            }
            List<CompletableFuture<NumberModel>> futureObj = new ArrayList<>();
            List<ValueObject> valueObjectList = new ArrayList<>();
            for (int num : integerList) {
                ValueObject valueObject = getSquaredModelOld(num);
                valueObjectList.add(valueObject);
//                CompletableFuture<NumberModel> completableFuture = valueObject.getNumberModel();
//                futureObj.add(completableFuture);
            }

            List<NumberModel> squaredModel = new ArrayList<>();
            for (ValueObject vo : valueObjectList) {
                NumberModel numberModel = vo.getNumberModel().get();
                boolean flag = vo.isSaveInDatabase();
                if (flag) {
                    repository.save(numberModel);
                    count++; // just for demonstration
                }
                squaredModel.add(numberModel);
            }
            System.out.println("Total values saved in database are: " + count);
            System.out.println(squaredModel); // just for demonstration
            return squaredModel;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    private ValueObject getSquaredModelOld(int num) throws ExecutionException, InterruptedException {
        boolean flag = false;
        CompletableFuture<NumberModel> future1 =
                CompletableFuture.supplyAsync(
                        () -> repository.findById(num).orElse(null));
        if (future1.get() == null) {
            future1 = CompletableFuture.supplyAsync(
                    () -> squareService.findSquareOfANumber(num), executor);
            flag = true;
        }
        ValueObject vo = new ValueObject();
        vo.setNumberModel(future1);
        vo.setSaveInDatabase(flag);

        /*
        CompletableFuture<NumberModel> future2 = CompletableFuture.supplyAsync(
                () -> squareService.findSquareOfANumber(num), executor);
        CompletableFuture<NumberModel> future = future1.get() != null ?
                future1.thenApply(n -> n) :
                future2.thenApply(n -> n);
         */

//        CompletableFuture<Integer> future = future1.applyToEither(future2, n -> n.getNumberSquare());

        return vo;
    }

    public void saveFileAsyncOld(MultipartFile file) throws Exception {
        InputStreamReader is;
        BufferedReader br = null;
        List<Integer> integerList = new ArrayList<>();

        try {
            is = new InputStreamReader(file.getInputStream());
            br = new BufferedReader(is);
            String line = br.readLine();
            while(line != null) {
                int value = Integer.parseInt(line);
                log.info("Giving the number: " + value + " to service 2 - by " + Thread.currentThread().getName());
                Future<NumberModel> futureModel = executor.submit(() -> feignService.findSquareOfANumber(value));
//                if (futureModel.isDone()) {
//                    repository.save(futureModel.get());
//                }
                repository.save(futureModel.get());
                line = br.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    /*
    public void saveFile(MultipartFile file) throws Exception {
        InputStreamReader is = null;
        BufferedReader br = null;
        List<Integer> integerList = new ArrayList<>();

        try {
            is = new InputStreamReader(file.getInputStream());
            br = new BufferedReader(is);
            String line = br.readLine();
            line = br.readLine();
            while (line != null) {
                int value = Integer.parseInt(line);
                integerList.add(value);
                line = br.readLine();
            }
            List<Future<NumberModel>> futureObj = new ArrayList<>();
            for (int num : integerList) {
                CompletableFuture<NumberModel> completableFuture = getSquare(num);

                Future<NumberModel> modelFuture = executor.submit(() -> feignService.findSquareOfANumber(num));
                futureObj.add(modelFuture);
                log.info("Giving the number: " + num + " to service 2 - by " + Thread.currentThread().getName());
            }

            List<Integer> sq = new ArrayList<>();
            for (Future<NumberModel> fu : futureObj) {
                sq.add(fu.get().getNumberSquare());
            }
            System.out.println(sq);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            br.close();
        }
    }

     */

    public void saveFileAsyncCheck(MultipartFile file) throws Exception {
        InputStreamReader is = null;
        BufferedReader br = null;
        List<Integer> integerList = new ArrayList<>();

        try {
            is = new InputStreamReader(file.getInputStream());
            br = new BufferedReader(is);
            String line = br.readLine();
            line = br.readLine();
            while (line != null) {
                int value = Integer.parseInt(line);
                integerList.add(value);
                line = br.readLine();
            }
            List<Future<Integer>> futureObj = new ArrayList<>();
            for (int num : integerList) {
                CompletableFuture<Integer> completableFuture = getSquare(num);
                futureObj.add(completableFuture);
            }

            List<Integer> squares = new ArrayList<>();
            for (Future<Integer> fu : futureObj) {
                squares.add(fu.get()); // just for demonstration
            }
            System.out.println(squares); // just for demonstration

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                br.close();
            }
        }

    }

    private CompletableFuture<Integer> getSquare(int num) throws ExecutionException, InterruptedException {
        CompletableFuture<NumberModel> future1 = CompletableFuture.supplyAsync(
                () -> repository.findById(num).orElse(null));
        CompletableFuture<NumberModel> future2 = CompletableFuture.supplyAsync(
                () -> squareService.findSquareOfANumber(num), executor);
        CompletableFuture<Integer> future = future1.get() != null ?
                future1.thenApply(n -> n.getNumberSquare()) :
                future2.thenApply(n -> n.getNumberSquare());
//        CompletableFuture<Integer> future = future1.applyToEither(future2, n -> n.getNumberSquare());

        return future;
    }

    // find number in db
    // if not present find square from service
    // save in db

    // return future,  either from db or service
//    private CompletableFuture<NumberModel> getSquare(int num) {
    //
//                CompletableFuture.runAsync().applyToEither(a -> {
//
//                },executor).;
//    }

    //        log.info("Giving the number: " + num + " to service 2 - by " + Thread.currentThread().getName());
//        CompletableFuture<NumberModel> future = CompletableFuture.supplyAsync(
//                () -> feignService.findSquareOfANumber(num), executor);

//    .exceptionally(e -> {
//        System.out.println("Exception" + e.getMessage());
//        return new NumberModel();
//    });
}
