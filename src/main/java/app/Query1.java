package app;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

public class Query1 
{
    private static final String name = "Query1";
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

        final List<StructField> schemaFields = new ArrayList<>();
        mySchemaFields.add(DataTypes.createStructField("day", DataTypes.LongType, true));
        mySchemaFields.add(DataTypes.createStructField("country", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("cases", DataTypes.IntegerType, true));
        final StructType schema = DataTypes.createStructType(schemaFields);
    }
}
