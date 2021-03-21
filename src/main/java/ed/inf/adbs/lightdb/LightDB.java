package ed.inf.adbs.lightdb;

import java.io.FileNotFoundException;
import java.io.FileReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

/**
 * Lightweight in-memory database system (main class)
 */
public class LightDB {

    /**
     * main method
     * @param args main arguments: database directory, input file and output file
     */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: LightDB database_dir input_file output_file");
			return;
		}
		String databaseDir = args[0];
		String inputFile = args[1];
		String outputFile = args[2];

		executeQuery(databaseDir, inputFile, outputFile);
	}

    /**
     * Executes the query from the input file and stores the result in the output file
     * @param db database directory
     * @param input input file
     * @param output output file
     */
	public static void executeQuery (String db, String input, String output) {
        try {
            Statement statement = CCJSqlParserUtil.parse(new FileReader(input)); //get the query statement
            DatabaseCatalog dbCat = DatabaseCatalog.getInstance(); //get the DatabaseCatalog instance
            dbCat.initialiseInfo(db, output); //initialise it
            if (statement != null) {
                SelectStatement selState = new SelectStatement(statement); //create a SelectStatement instance with the input query
                selState.generateAndExecuteQueryPlan(); //generate the operator's tree and execute the query
            }
        } catch (JSQLParserException | FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
