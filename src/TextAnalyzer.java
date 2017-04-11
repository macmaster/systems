
/** TextAnalyzer.java
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 4/09/17
 * 
 * Hadoop MapReduce job.
 */

import java.io.*;
import java.util.*;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;

// Do not change the signature of this class
public class TextAnalyzer extends Configured implements Tool {

    // The four template data types are:
    // <Input Key Type, Input Value Type, Output Key Type, Output Value Type>
    // reference: https://learnhadoopwithme.wordpress.com/tag/writablecomparable/
    public static class TextMapper extends Mapper<LongWritable, Text, Text, CountPair> {
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // cleans the line. then maps a word count.
            String line = value.toString().toLowerCase();
            line.replaceAll("[^A-Za-z0-9]", " ");
            String[] words = line.split("[^A-Za-z0-9]+");
            Set<String> explored = new HashSet<String>();

            // search each unique context word.
            for (int i = 0; i < words.length; i++) {
                Text contextWord = new Text(words[i]);
                if (explored.add(contextWord.toString())) {
                    for (int j = 0; j < words.length; j++) {
                        if (i != j) { // tally query word
                            Text queryWord = new Text(words[j]);
                            LongWritable count = new LongWritable(1);
                            if (!words[i].isEmpty() && !words[j].isEmpty()) {
                                context.write(contextWord, new CountPair(queryWord, count));
                            }
                        }
                    }
                }
            }
        }
    }

    // combiner's output key/value types have to be the same as those of mapper
    public static class TextCombiner extends Reducer<Text, CountPair, Text, CountPair> {
        public void reduce(Text key, Iterable<CountPair> tuples, Context context) throws IOException, InterruptedException {
            // Map of query words to total counts
            // group multiple tuples -> single tuple with single count.
            Map<String, Long> queryMap = new HashMap<>();
            // Fill map with context words and their counts
            for (CountPair tuple : tuples) {
                String word = tuple.text.toString();
                Long tally = tuple.count.get();

                // Update the current hashmap value
                if (queryMap.containsKey(word)) {
                    Long count = queryMap.get(word);
                    queryMap.put(word, count + tally);
                } else { // first cache-hit in map.
                    queryMap.put(word, tally);
                }
            }

            // Write out to context. (emit query cache)
            for (String word : queryMap.keySet()) {
                Text contextWord = new Text(key.toString());
                Text queryWord = new Text(word);
                LongWritable count = new LongWritable(queryMap.get(word));
                context.write(contextWord, new CountPair(queryWord, count));
                //System.out.format("combiner: (%s, %s)%n", contextWord, new CountPair(queryWord, count));
            }
        }
    }

    // input is the output key / value types of your mapper function
    public static class TextReducer extends Reducer<Text, CountPair, Text, Text> {
        public void reduce(Text key, Iterable<CountPair> queryTuples, Context context) throws IOException, InterruptedException {
            // group multiple tuples -> single tuple with single count.
            Map<String, Long> queryMap = new HashMap<>();
            for (CountPair tuple : queryTuples) {
                String word = tuple.text.toString();
                Long tally = tuple.count.get();
                if (queryMap.containsKey(word)) {
                    Long count = queryMap.get(word);
                    queryMap.put(word, count + tally);
                } else { // first cache-hit in map.
                    queryMap.put(word, tally);
                }
            }

            // emit query cache.
            Text contextWord = new Text(key.toString());
            StringBuilder textTable = new StringBuilder("\n");
            for (String word : queryMap.keySet()) {
                Text queryWord = new Text(word);
                LongWritable count = new LongWritable(queryMap.get(word));
                textTable.append(new Text(new CountPair(queryWord, count).toString()) + "\n");
            }

            // emit query table results for context word.
            context.write(contextWord, new Text(textTable.toString()));
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();

        // Create and setup MapReduce job
        @SuppressWarnings("deprecation")
        Job job = new Job(conf, "rpm953_trs2277"); // Replace with your EIDs
        job.setJarByClass(TextAnalyzer.class);
        job.setMapperClass(TextMapper.class);
        job.setCombinerClass(TextCombiner.class);
        job.setReducerClass(TextReducer.class);

        // Specify key / value types
        // change if mapper and combiner's output type different from Text
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(CountPair.class); // value is a count pair
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // Input
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);

        // Output
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        FileSystem.get(conf).delete(new Path(args[1]), true);
        job.setOutputFormatClass(TextOutputFormat.class);

        // Execute job and return status
        return job.waitForCompletion(true) ? 0 : 1;
    }

    // Do not modify the main method
    public static void main(String[] args) throws Exception {
        int result = ToolRunner.run(new Configuration(), new TextAnalyzer(), args);
        System.exit(result);
    }

    /**
     * Writable container for a comparable pair of (text, long).
     * reference: https://learnhadoopwithme.wordpress.com/tag/writablecomparable/
     */
    public static class CountPair implements WritableComparable<CountPair> {
        public Text text;
        public LongWritable count;

        /**
         * Constructs a new Pair object. (left, right) <br>
         */
        public CountPair() {
            set(new Text(""), new LongWritable(0));
        }

        /**
         * Constructs a new Pair object. (left, right) <br>
         */
        public CountPair(Text text, LongWritable count) {
            set(text, count);
        }

        /**
         * sets the count pair values. <br>
         */
        public void set(Text text, LongWritable count) {
            this.text = text;
            this.count = count;
        }

        /**
         * deserialize the object.
         */
        @Override
        public void readFields(DataInput input) throws IOException {
            this.text.readFields(input);
            this.count.readFields(input);
        }

        /**
         * serialize the object.
         */
        @Override
        public void write(DataOutput output) throws IOException {
            this.text.write(output);
            this.count.write(output);
        }

        @Override
        public String toString() {
            return String.format("<%s, %d>", text.toString(), count.get());
        }

        @Override
        public int hashCode() {
            return (text.hashCode() * 7) + (count.hashCode() * 11);
        }

        @Override
        public boolean equals(Object other) {
            if (other != null && other instanceof CountPair) {
                CountPair pair = (CountPair) other;
                return text.equals(pair.text) && count.equals(pair.count);
            } else {
                return false;
            }
        }

        /**
         * Compare two writable pairs.
         * First by string, then by count.
         */
        @Override
        public int compareTo(CountPair other) {
            if (text.compareTo(other.text) != 0) {
                return text.compareTo(other.text); // equal
            } else {
                return count.compareTo(other.count);
            }
        }

    }
}
