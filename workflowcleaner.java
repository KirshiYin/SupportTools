import java.util.Scanner;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.operations.IDfDeleteOperation;
/*
 * @ author Kristina Nikolova
 * date: 16.05.2016
 */
public class WorkflowCleaner {

	private static IDfFolder folder;
	private static IDfCollection collection;

	private static IDfSessionManager sessionMgr = null;
	protected static IDfSession session = null;
	private static IDfClient client;
	private static IDfClientX clientx;
	private static Scanner input = new Scanner(System.in);

	protected static void connectSession() throws Exception {
		System.out.println("Please choose a docbase from the list and type its name below");
		String[] docbases = { "docbase1", "docbase2", "docbase3", "docbase4", "docbase5", "docbase6" };
		for (int i = 0; i < docbases.length; i++) {
			System.out.println(docbases[i]);
		}

		String selectedDocbase = input.nextLine();
		client = DfClient.getLocalClient();
		sessionMgr = client.newSessionManager();

		// Setup login details.
		IDfLoginInfo login = new DfLoginInfo();
		if (selectedDocbase.equalsIgnoreCase(docbases[0]) || selectedDocbase.equalsIgnoreCase(docbases[2])
				|| selectedDocbase.equalsIgnoreCase(docbases[3]) || selectedDocbase.equalsIgnoreCase(docbases[4])
				|| selectedDocbase.equalsIgnoreCase(docbases[5]) || selectedDocbase.equalsIgnoreCase(docbases[6])) {
			login.setUser(selectedDocbase);
			login.setPassword("password");
			sessionMgr.setIdentity(selectedDocbase, login);
		} else {
			login.setUser(docbases[1]);
			login.setPassword("specialpw");
			sessionMgr.setIdentity(docbases[1], login);
		}

		session = sessionMgr.newSession(selectedDocbase);
		System.out.println("connected to docbase: " + session.getDocbaseName());
	}

	protected static void releaseSession() throws Exception {

		if (session != null) {
			sessionMgr.release(session);
		}
		System.out.println("disconnected from docbase");
	}

	protected static void terminateWorkflow() {
		// a counter to track the results of the query
		int counter = 0;
		// checks the workflow id
		System.out.println("Please type the experiment`s name to search for current workflows");
		// gets the user input for workflow name
		String wfName = input.nextLine();

		IDfQuery selectWfQuery = new DfQuery();
		selectWfQuery
				.setDQL("select r_act_name as task_state, r_object_id, object_name from dm_workflow where r_runtime_state =1 and object_name like '"
						+ wfName + "%';");
		System.out.println("executing DQL: " + selectWfQuery.getDQL());
		try {
			collection = selectWfQuery.execute(session, DfQuery.DF_READ_QUERY);
			// Loop through the results
			while (collection.next()) {
				IDfWorkflow wf = (IDfWorkflow) session.getObject(collection.getId("r_object_id"));

				System.out.println("object name: " + wf.getObjectName() + " workflowId: " + wf.getObjectId());
				counter++;
			}

		} catch (DfException e) {
			System.out.println("An error has occured: " + e.getMessage());
		}
		// if there are no results, close the collection and don`t run the next
		// query
		if (counter < 1) {
			System.out.println("There were no results for the given experiment name");
			if (collection != null) {
				try {
					collection.close();
				} catch (DfException e) {
					System.out.println("Cannot close collection " + e.getMessage());
				}
				System.out.println("closed collection");
			}
		} else {
			System.out.println("Please paste the workflow id here: ");
			String pastedWf = input.nextLine();
			try {
				IDfQuery query = new DfQuery();
				query.setDQL(
						"select r_act_name as task_state, r_object_id, object_name from dm_workflow where r_runtime_state =1 and r_object_id = '"
								+ pastedWf.trim() + "';");
				System.out.println("executing DQL: " + query.getDQL());
				try {
					collection = query.execute(session, DfQuery.DF_READ_QUERY);
					clientx = new DfClientX();// used for factory methods
					// Loop through the results
					while (collection.next()) {
						IDfWorkflow wf = (IDfWorkflow) session.getObject(collection.getId("r_object_id"));

						 wf.abort();
						 wf.destroy();
						System.out.println("terminated workflow");
					}

				} catch (DfException e) {
					System.out.println("An error has occured: " + e.getMessage());
				}

			} finally {
				// Close the IDfCollection.
				if (collection != null) {
					try {
						collection.close();
					} catch (DfException e) {
						System.out.println("Cannot close collection " + e.getMessage());
					}
					System.out.println("closed collection");
				}
			}
		}
	}

	protected static void deleteFolder() {
		int counter = 0;
		System.out.println("Please enter the experiment`s name");
		String userInputFolder = input.nextLine();
		// checks source and working folders starting with the given name
		IDfQuery selectFolderQuery = new DfQuery();
		selectFolderQuery.setDQL("select r_object_id, object_name from dm_folder where object_name like '"
				+ userInputFolder.trim() + "%';");
		System.out.println("executing DQL: " + selectFolderQuery.getDQL());
		try {
			collection = selectFolderQuery.execute(session, DfQuery.DF_READ_QUERY);
			// Loop through the results
			while (collection.next()) {
				folder = (IDfFolder) session.getObject(collection.getId("r_object_id"));
				System.out.println("folder name: " + folder.getObjectName());
				counter++;
			}

		} catch (DfException e) {
			System.out.println("An error has occured: " + e.getMessage());
		}
		// if there are no results, close the collection and don`t run the next
		// query
		if (counter < 1) {
			System.out.println("There were no results for the given experiment name");
			if (collection != null) {
				try {
					collection.close();
				} catch (DfException e) {
					System.out.println("Cannot close collection " + e.getMessage());
				}
				System.out.println("closed collection");
			}
		} else {
			System.out.println("Please paste the folder name here");
			String pastedFolderName = input.nextLine();
			try {
				IDfQuery query = new DfQuery();
				query.setDQL(" select r_object_id, object_name from dm_folder where object_name like '"
						+ pastedFolderName.trim() + "%';");
				System.out.println("executing DQL: " + query.getDQL());
				try {
					collection = query.execute(session, DfQuery.DF_READ_QUERY);
					clientx = new DfClientX();// used for factory methods
					// Loop through the results
					while (collection.next()) {
						folder = (IDfFolder) session.getObject(collection.getId("r_object_id"));
						System.out.println("folder to be deleted: " + folder.getObjectName());
						IDfDeleteOperation delo = clientx.getDeleteOperation();
						 delo.add(folder);

						 delo.setVersionDeletionPolicy(IDfDeleteOperation.ALL_VERSIONS);

						 delo.enableDeepDeleteFolderChildren(true);

						if (delo.execute()) {
							System.out.println("Folder has been deleted.");
						} else {
							System.out.println("Folder could not be deleted");
						}
					}

				} catch (DfException e) {
					System.out.println("An error has occured: " + e.getMessage());
				}
			} finally {
				// Close the IDfCollection.
				if (collection != null) {
					try {
						collection.close();
					} catch (DfException e) {
						System.out.println("Cannot close collection " + e.getMessage());
					}
					System.out.println("closed collection");
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		connectSession();
		terminateWorkflow();
		deleteFolder();
		releaseSession();

	}

}
