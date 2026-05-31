package nasa;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;

public class SuspiciousIPsJob {
    public static class IPMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        @Override
        public void map(LongWritable key, Text value, Context ctx) throws IOException, InterruptedException {
            NasaLogParser.ParsedLog log = NasaLogParser.parse(value.toString());
            // only emit IPv4-looking hosts to reduce invalid keys
            if (log != null && log.host != null && log.host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                ctx.write(new Text(log.host), new IntWritable(1));
            }
        }
    }
    public static class SuspiciousPartitioner extends Partitioner<Text, IntWritable> {
        @Override
        public int getPartition(Text ip, IntWritable val, int numPartitions) {
            if (numPartitions <= 0) return 0;

            String s = ip.toString().trim();

            // strip common junk (ports, commas)
            s = s.replaceAll("[:,].*$", "");

            String[] parts = s.split("\\.");
            if (parts.length != 4) return 0;

            try {
                int third = Integer.parseInt(parts[2]);
                return Math.floorMod(third, numPartitions);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
    public static class SuspiciousReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        @Override
        public void reduce(Text ip, Iterable<IntWritable> counts, Context ctx) throws IOException, InterruptedException {
            int sum = 0;
            for(IntWritable c : counts) sum += c.get();
            if(sum > 500)
                ctx.write(ip, new IntWritable(sum));
        }
    }
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Suspicious IPs");
        job.setJarByClass(SuspiciousIPsJob.class);

        job.setMapperClass(IPMapper.class);
        job.setPartitionerClass(SuspiciousPartitioner.class);
        job.setReducerClass(SuspiciousReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setNumReduceTasks(8); // set by cluster size

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}