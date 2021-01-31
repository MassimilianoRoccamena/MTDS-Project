package app;

import java.util.List;
import java.util.ArrayList;


import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.functions;

public class Query 
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

        //Q1
        int windowSize = 7;

        Dataset<Row> input = spark.read().json("data/data.csv");

        WindowSpec window = Window.partitionBy("country")
                                            .orderBy("day")
                                            .rangeBetween(-windowSize, 0);

        Dataset<Row> output1 = input.withColumn("Q1", functions.avg("cases")
                                                                .over(window));
        
        output1.show();

        //Q2
        window = Window.partitionBy("country")
                            .orderBy("day");

        Dataset<Row> output2 = output1.withColumn("Q2",
                                                    (output1.Q1 - functions.lag(output1.Q1, 1).over(window))/100)

        output2.show();

        //Q3
        
        spark.close();
    }
}