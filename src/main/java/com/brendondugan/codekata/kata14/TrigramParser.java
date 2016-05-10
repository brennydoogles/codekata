package com.brendondugan.codekata.kata14;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by brendon on 5/9/2016.
 */
public class TrigramParser {
    private Map<String, List<String>> trigrams;

    public TrigramParser() {
        this.trigrams = new HashMap<>();
    }

    public Map<String, List<String>> parseFile(File inputFile) throws IOException, ExecutionException, InterruptedException {
        FileReader reader = new FileReader(inputFile);
        ExecutorService executorService = Executors.newFixedThreadPool(4);// Let's run a thread per core on my machine
        List<Future<Map<String, List<String>>>> futures = new ArrayList<>();
        int character;
        StringBuilder builder = new StringBuilder("");
        while ((character = reader.read()) != -1) {
            char letter = (char) character;
            // Filter out characters that don't contribute to words
            if (Character.isLetter(letter) || Character.isSpaceChar(letter) || "'\r\n\t.".indexOf(letter) != -1){ // Current character is relevant and not some garbage
                if('.' == letter){ // If we hit a period we're at the end of a sentence, so let's start parsing
                    String sentence = builder.toString().trim();
                    Callable<Map<String, List<String>>> thread = ()-> TrigramParser.parseString(sentence); // Build a new thread to process
                    futures.add(executorService.submit(thread)); // Submit it to the executorservice
                    builder.setLength(0); // Clear the buffer and start again
                }
                else{
                    if("\r\n\t".indexOf(letter) != -1){
                        letter = ' ';
                    }
                    builder.append(letter);
                }
            }
        }
        while (!futures.isEmpty()){
            Iterator<Future<Map<String, List<String>>>> futureIterator = futures.iterator();
            while (futureIterator.hasNext()) {
                Future<Map<String, List<String>>> f = futureIterator.next();
                if(f.isDone()){
                    this.mergeResult(f.get());
                    futureIterator.remove(); // avoids a ConcurrentModificationException
                }

            }
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        return this.trigrams;
    }

    private void mergeResult(Map<String, List<String>> result){
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            if(this.trigrams.containsKey(key)){
                this.trigrams.put(key, this.mergeLists(this.trigrams.get(key), value));
            }
            else{
                this.trigrams.put(key, value);
            }
        }
    }

    private List<String> mergeLists(List<String> l1, List<String> l2){
        l2.stream().filter(s -> !l1.contains(s)).forEach(l1::add);
        return l1;
    }

    private static Map<String, List<String>> parseString(String inputString){
        inputString = inputString.replaceAll("\\s+"," ");
        Map<String, List<String>> results = new HashMap<>();
        List<String> wordList = Arrays.asList(inputString.split(" "));
        String first = "";
        String second = "";
        for(int i = 2; i < wordList.size(); i++){
            if (i == 2){
                first = wordList.get(0);
                second = wordList.get(1);
            }
            String key = String.format("%s %s", first.toLowerCase(), second.toLowerCase());
            String current = wordList.get(i);
            if(results.containsKey(key)){
                results.get(key).add(current);
            }
            else{
                ArrayList<String> value = new ArrayList<>();
                value.add(current);
                results.put(key, value);
            }
            first = second;
            second = current;
        }
        return results;
    }
}
