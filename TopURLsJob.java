package nasa;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;

public class TopURLsJob {
    public static class URLCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        @Override
        public void map(LongWritable key, Text value, Context ctx) throws IOException, InterruptedException {
            NasaLogParser.ParsedLog parsed = NasaLogParser.parse(value.toString());
            if(parsed != null)
                ctx.write(new Text(parsed.url), new IntWritable(1));
        }
    }

    public static class URLCountCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {
        @Override
        public void reduce(Text url, Iterable<IntWritable> counts, Context ctx) throws IOException, InterruptedException {
            int sum = 0;
            for(IntWritable c : counts) sum += c.get();
            ctx.write(url, new IntWritable(sum));
        }
    }

    public static class URLCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private TreeMap<Integer, String> freqMap = new TreeMap<>(Comparator.reverseOrder());
        @Override
        public void reduce(Text url, Iterable<IntWritable> counts, Context ctx) {
            int sum = 0;
            for(IntWritable c : counts) sum += c.get();
            freqMap.put(sum, url.toString());
        }
        @Override
        protected void cleanup(Context ctx) throws IOException, InterruptedException {
            int top = 0;
            for(Map.Entry<Integer, String> entry : freqMap.entrySet()) {
                if(top++ == 20) break;
                ctx.write(new Text(entry.getValue()), new IntWritable(entry.getKey()));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Top URLs");
        job.setJarByClass(TopURLsJob.class);
        job.setMapperClass(URLCountMapper.class);
        job.setCombinerClass(URLCountCombiner.class);
        job.setReducerClass(URLCountReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));  // HDFS input path
        FileOutputFormat.setOutputPath(job, new Path(args[1])); // HDFS output path

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}