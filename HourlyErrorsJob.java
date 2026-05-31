package nasa;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;

public class HourlyErrorsJob {
    static SimpleDateFormat inputFmt = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
    static SimpleDateFormat hourFmt = new SimpleDateFormat("HH");

    public static class ErrorRateMapper extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        public void map(LongWritable key, Text value, Context ctx) throws IOException, InterruptedException {
            NasaLogParser.ParsedLog log = NasaLogParser.parse(value.toString());
            if(log == null) return;
            try {
                Date d = inputFmt.parse(log.timestamp);
                String hour = hourFmt.format(d);
                String status = log.status;
                ctx.write(new Text(hour), new Text(status));
            } catch(Exception e) {}
        }
    }
    public static class ErrorRateReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text hour, Iterable<Text> codes, Context ctx) throws IOException, InterruptedException {
            int ok = 0, e404 = 0, e500 = 0, all = 0;
            for(Text t : codes) {
                all++;
                String c = t.toString();
                if("200".equals(c)) ok++;
                else if("404".equals(c)) e404++;
                else if("500".equals(c)) e500++;
            }
            double errRate = 100.0 * (e404 + e500) / Math.max(1, all);
            ctx.write(hour, new Text(ok + "\t" + e404 + "\t" + e500 + "\t" + String.format("%.2f", errRate) + "%"));
        }
    }
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Hourly Errors");
        job.setJarByClass(HourlyErrorsJob.class);

        job.setMapperClass(ErrorRateMapper.class);
        job.setReducerClass(ErrorRateReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}