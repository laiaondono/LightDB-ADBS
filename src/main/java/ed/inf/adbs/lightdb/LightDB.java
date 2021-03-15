package ed.inf.adbs.lightdb;

import java.io.FileNotFoundException;
import java.io.FileReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

/**
 * Lightweight in-memory database system
 *
 */
public class LightDB {

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: LightDB database_dir input_file output_file");
			return;
		}
		String databaseDir = args[0];
		String inputFile = args[1];
		String outputFile = args[2];

		//parsingExample(inputFile);
		executeQuery(databaseDir, inputFile, outputFile);
	}

	public static void executeQuery (String db, String input, String output) {
        try {
            Statement statement = CCJSqlParserUtil.parse(new FileReader(input));
            DatabaseCatalog dbCat = DatabaseCatalog.getInstance();
            dbCat.initialiseInfo(db, input, output);
            if (statement != null) {
                SelectStatement selState = new SelectStatement(statement);
                selState.generateOpTree();
            }
        } catch (JSQLParserException | FileNotFoundException e) {
            e.printStackTrace();
        }

    }

	/**
	 * Example method for getting started with JSQLParser. Reads SQL statement from
	 * a file and prints it to screen; then extracts SelectBody from the query and
	 * prints it to screen.
	 */

	public static void parsingExample(String filename) {
		try {
			Statement statement = CCJSqlParserUtil.parse(new FileReader(filename));
//            Statement statement = CCJSqlParserUtil.parse("SELECT * FROM Boats");
			if (statement != null) {
				System.out.println("Read statement: " + statement);
				Select select = (Select) statement;
				System.out.println("Select body is " + select.getSelectBody());
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
}
