/*This a program which finds the review id, title and filename based on the user input. 
 * It checks the first word of the review name and if it is not found,
 * it checks if the word is contained in all the reviews.
 * Author: Kristina Nikolova
 * Version: 1.0
 * Created on 17.07.2015
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class ReviewFinder {
	// declaring the constant variables needed for establishing the connection
	// to the DB
	public static final String DBURL = "jdbc:oracle:thin:@servername";
	public static final String DBUSER = "user";
	public static final String DBPASS = "pw";

	public static void main(String[] args) throws SQLException {
		try {
			// Loads Oracle JDBC Driver
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

			// Connects to Oracle Database
			Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
			Statement statement = con.createStatement();
			Scanner in = new Scanner(System.in);
			System.out
					.println("Please enter the filename or the first word of the filename: ");
			// converts the user input to UpperCase before entering the loop.
			// This way the search is case insensitive
			String filename = in.nextLine().toUpperCase();

			// building up the SQL query
			ResultSet rs = statement
					.executeQuery("select reviewid, filename,title, case when deleted=0 then 'exists' else 'deleted' end as state from t_document where UPPER(filename) like '"
							+ filename + "%'");
			// checking if the result is empty
			if (!rs.isBeforeFirst()) {
				System.out.println("The review was not found");
				System.out
						.println("Checking if the existing reviews contain the word...");
				rs = statement
						.executeQuery("select reviewid, filename,title, case when deleted=0 then 'exists' else 'deleted' end as state from t_document where UPPER(filename) like '"
								+ "%" + filename + "%'");
			}

			// looping through the results of the query
			while (rs.next()) {

				int id = rs.getInt("reviewid");
				String reviewName = rs.getString("filename");
				String title = rs.getString("title");
				String state = rs.getString("state");
				// prints out the results of the query if any
				System.out.println("Review ID: " + id + " Filename: "
						+ reviewName + " Title: " + title + " State: " + state);
			}
			// checking if the program has finished executing
			if (Thread.currentThread().isAlive()) {
				System.out.println("The search has been completed");
			}
			// closing connection
			rs.close();
			statement.close();
			con.close();
		} catch (SQLException se) {
			// Handles errors for JDBC
			System.out.println("An error has occured "
					+ se.getMessage().toString());
		} catch (Exception e) {
			// Handles errors for Class.forName
			System.out.println("An error has occured  "
					+ e.getMessage().toString());
		}
	}

}