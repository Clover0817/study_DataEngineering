import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.GenericOptionsParser;


public class UBERStudent20200944{

	public static class UBERMapper extends Mapper<LongWritable, Text, Text, LongWritable>{

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException 
		{
		      String[] uber = value.toString().split(",");
		      String region = uber[0];
		      String date = uber[1];
		      String vehicles = uber[2];
		      String trips = uber[3];
			
		      String[] days = {"SUN", "MON", "TUE", "WED", "THR", "FRI", "SAT"};
		      String[] dateInfo = date.split("/");
		      String month = dateInfo[0];
		      int num = Integer.parseInt(dateInfo[1]);
		      String day = days[num];
		      String year = dateInfo[2];

		      Text regionDate = new Text(region+","+day);
		      Text tripVehicle = new Text(trips+","+vehicles);
					context.write(regionDate, tripVehicle);
		}
	}

	public static class UBERReducer extends Reducer<Text,Text,Text,Text> 
	{
		private Text result = new Text();

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException 
		{
		    int tripAll = 0;
		    int vehicleAll = 0;

		    for (Text val : values) {
			String line = val.toString();
			String[] tripVehicle = line.split(",");

			int trip = Integer.parseInt(tripVehicle[0]);
			int vehicle = Integer.parseInt(tripVehicle[1]);

			tripAll += trip;
			vehicleAll += vehicle;
		    }

		    String info = Integer.toString(tripAll)+","+Integer.toString(vehicleAll);
		    result.set(info);
		    context.write(key, result);
       	       }
	}

	public static void main(String[] args) throws Exception 
	{
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) 
		{
			System.err.println("Usage: UBER <in> <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "UBERStudent20200944");
		job.setJarByClass(UBERStudent20200944.class);
		job.setMapperClass(UBERMapper.class);
		job.setReducerClass(UBERReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		FileSystem.get(job.getConfiguration()).delete( new Path(otherArgs[1]), true);
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
