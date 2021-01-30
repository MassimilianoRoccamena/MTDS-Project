package app;

import java.util.List;
import java.util.ArrayList;


import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class Query3 
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
    }
}
