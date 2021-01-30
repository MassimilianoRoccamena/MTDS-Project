package app;

import java.util.List;
import java.util.ArrayList;


import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.functions;

public class Query1 
{
    private static final String name = "Query3";
    private static final boolean useCache = true;

    public static void main( String[] args )
    {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final String appName = useCache ? name+"WithCache" : name+"NoCache";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName(name)
                .getOrCreate();

        int window = 7;

        Dataset<Row> input = spark.read().json("data/data.csv");
        WindowSpec windowSpec = Window.orderBy(input.col("cases")).rangeBetween(-7, 0);
        Dataset<Row> output = input.withColumn("MA(7)", functions.avg("cases").over(windowSpec));
        
        output.show();
    }
}