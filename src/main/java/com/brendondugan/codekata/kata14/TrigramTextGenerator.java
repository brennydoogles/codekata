package com.brendondugan.codekata.kata14;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by brendon on 5/9/2016.
 */
public class TrigramTextGenerator {
    private Map<String, List<String>> trigrams;
    private int wordCount;
    private Random random;

    public TrigramTextGenerator(Map<String, List<String>> trigrams, int wordCount) {
        this.trigrams = trigrams;
        this.wordCount = wordCount;
        this.random = new Random();
    }

    public String buildText() {
        StringBuilder builder = new StringBuilder("");
        String[] punctuation = {".", ","};
        String first;
        String second;
        String current;
        String seedString = capitalize(getSeedString()); // First let's seed our text with a pseudo-random key
        builder.append(seedString).append(" ");
        String[] words = seedString.split(" ");
        first = words[0];
        second = words[1];
        // We're going to generate some words which will bring us somewhere in the ballpark of the wordcount (usually over)
        for (int i = 0; i < this.wordCount; i++) {
            String key = String.format("%s %s", first.toLowerCase(), second.toLowerCase());
            List<String> values = trigrams.get(key);
            String temp = values.get(random.nextInt(values.size()));
            String nextKey = String.format("%s %s", second.toLowerCase(), temp.toLowerCase());
            // We're going to keep our sentences going for as long as we can before we add punctuation
            // But once we hit a dead end (a word combination we've never encountered)
            // We'll just toss in some punctuation and re-seed.
            if(trigrams.containsKey(nextKey)){
                current = temp;
                builder.append(current).append(" ");
                first = second;
                second = current;
            }
            else{
                seedString = capitalize(getSeedString());
                trimStringBuilder(builder);
                builder.append(punctuation[random.nextInt(punctuation.length)]).append(" ");
                builder.append(seedString).append(" ");
                words = seedString.split(" ");
                first = words[0];
                second = words[1];
            }

        }
        // Let's make sure our story ends with a period (technically I should be sure to strip away any stray commas at the end,
        // but as we're mostly generating nonsense anyway It's not worth it)
        if(builder.charAt(builder.length() - 1) != '.'){
            builder.append('.');
        }
        return builder.toString();
    }

    private void trimStringBuilder(StringBuilder sb){
        // Trim trailing whitespace from the Stringbuilder! There should be only one, but you never know.
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    private String getSeedString() {
        boolean seeded = false;
        String output = "";
        while (!seeded) {
            // Grab a random key from our trigram list
            String[] keys = new String[trigrams.keySet().size()];
            trigrams.keySet().toArray(keys);
            String randomKey = keys[random.nextInt(keys.length)];
            // Before choosing this as our seed text let's make sure it has several possible words to follow it
            if (trigrams.get(randomKey).size() > 3) {
                seeded = true;
                output = randomKey;
            }
        }
        return output;
    }

    public String capitalize(String input){
        String output = input.substring(0, 1).toUpperCase() + input.substring(1);
        return output;
    }
}
