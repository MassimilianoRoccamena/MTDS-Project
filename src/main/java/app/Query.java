package app;

import java.util.List;
import java.util.ArrayList;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

import static org.apache.spark.sql.functions.*;

public class Query 
{
    private static final String name = "Query3";
    private static final boolean useCache = true;

    public static void main( String[] args )
    {
        // Setup
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final String appName = useCache ? name+"WithCache" : name+"NoCache";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName(name)
                .getOrCreate();

        final List<StructField> schemaFields = new ArrayList<StructField>();
        schemaFields.add(DataTypes.createStructField("country", DataTypes.StringType, true));
        schemaFields.add(DataTypes.createStructField("day", DataTypes.IntegerType, true));
        schemaFields.add(DataTypes.createStructField("cases", DataTypes.IntegerType, true));
        final StructType schema = DataTypes.createStructType(schemaFields);

        Dataset<Row> df = spark.read()
                                    .option("header", "false")
                                    .option("delimiter", ",")
                                    .schema(schema)
                                    .load("data/test.csv");

        // Q1
        WindowSpec window = Window.partitionBy("country")
                                            .orderBy("day")
                                            .rangeBetween(-7, 0);

        df = df.withColumn("Q1", 
                                                avg("cases").over(window));
        
        df.persist();
        df.show();

        // Q2
        window = Window.partitionBy("country")
                        .orderBy("day");

        df = df.withColumn("lagged",
                                     lag(df.col("Q1"), 1).over(window));

        df = df.withColumn("Q2",
                                    expr("cases/lagged").over(window));

        df = df.drop("lagged");

        df.persist();
        df.show();

        // Q3

        df.groupBy("country").agg(sum("Q2"));

        df = df.orderBy(desc("sum(Q2)")).limit(10).select("country");

        df.show();
        
        // Close
        spark.close();
    }
}