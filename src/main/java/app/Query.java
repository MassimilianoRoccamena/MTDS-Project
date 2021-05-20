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
    private static final String name = "CovidQueries";
    private static final boolean useCache = true;

    public static void main( String[] args )
    {
        // Setup
        LogUtils.setLogLevel();

        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final String appName = useCache ? name+"WithCache" : name+"NoCache";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName(name)
                .getOrCreate();

        final List<StructField> schemaFields = new ArrayList<StructField>();
        schemaFields.add(DataTypes.createStructField("country", DataTypes.StringType, false));
        schemaFields.add(DataTypes.createStructField("day", DataTypes.IntegerType, false));
        schemaFields.add(DataTypes.createStructField("cases", DataTypes.IntegerType, false));
        final StructType schema = DataTypes.createStructType(schemaFields);

        Dataset<Row> df = spark.read()
                                    .format("csv")
                                    .option("header", "true")
                                    .option("delimiter", ",")
                                    .schema(schema)
                                    .load("data/test.csv");

        System.out.println("<--- INPUT --->\n");
        df.show();

        // Q1
        System.out.println("<--- QUERY1 --->\n");

        final int horizon = 7;
        WindowSpec window = Window.partitionBy("country")
                                            .orderBy("day")
                                            .rowsBetween(-horizon+1, 0);

        df = df.withColumn("Q1", 
                                avg("cases").over(window));
        
        df.persist();
        df.show();

        // Q2
        System.out.println("<--- QUERY2 --->\n");

        window = Window.partitionBy("country")
                        .orderBy("day");

        df = df.withColumn("lagged",
                                     lag(df.col("Q1"), 1).over(window));
        
        df = df.withColumn("lagged",
                                     when(df.col("lagged").isNull(), df.col("Q1")).otherwise(df.col("lagged")));  // Replace nulls with not lagged

        df = df.withColumn("Q2",
                                    expr("Q1/lagged"));

        df = df.drop("lagged");

        df.persist();
        df.show();

        // Q3
        System.out.println("<--- QUERY3 --->\n");

        df = df.filter("day != 0");
        Dataset<Row> df2 = df.select("day").distinct().orderBy("day");
        df2 = df2.withColumn("id", monotonically_increasing_id());

        window = Window.partitionBy("day")
                        .orderBy(desc("Q2"));
        df = df.withColumn("rank", rank().over(window)).orderBy("day");

        int top = 10;
        Dataset<Row> df_temp;
        for (int i=1; i<=top; i++)
        {
            df_temp = df.select("country").where("rank == " + i);
            df_temp = df_temp.withColumn("id", monotonically_increasing_id());
            df2 = df2.join(df_temp, "id");
            df2 = df2.withColumnRenamed("country", "Q3-"+i);
        }

        df2 = df2.drop("id");

        df2.persist();
        df2.show();
        
        // Close
        spark.close();
    }
}