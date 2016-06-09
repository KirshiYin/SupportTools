import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.Border;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfQueueItem;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
/* A program to delegate signing tasks of documentum users
 * author:Kristina Nikolova
 * 10.02.2016
 */	
public class TaskDelegation extends JPanel implements ActionListener {

	private static final long serialVersionUID = -2388691969051630653L;

	String UserName;
	String Password;
	String DocBase;

	static IDfClientX clientx;
	static IDfClient client;
	static IDfDocbaseMap myMap;
	static IDfSessionManager sMgr;
	static IDfSession session;
	static DfLoginInfo loginInfoObj;
	static IDfCollection collection;

	private JButton connectButton;
	private JTextArea taskOutput;
	private JButton delegate;
	private JLabel selectWhomToDelegate;
	private JLabel labelDB;
	private JLabel userName;
	private JLabel password;
	private JLabel selectQuery = new JLabel("Type in the userId of the current performer");
	private JLabel specifyItem;
	private JLabel or;
	private JCheckBox all;
	private JTextField userNameField;
	private JPasswordField passwordField;
	private Connection connection;
	private JButton disconnect;
	private JButton exit;
	private JButton runQuery;
	private String dqlSelect;
	private DfQuery querySelect;
	private IDfQueueItem qi;
	private JTextField performerField;
	private JTextField delegatetoUser;
	private JTextField itemField;
	private IDfWorkitem wi;
	private IDfId queueItemId;
	private IDfWorkflow wf;
	private int task_state;
	private int resultsCounter1 = 0;
	private String getItemId;
	private String getPerformerField;
	private IDfQuery selectSpecfiedItem;

	String[] docbases = { "docbase1", "docbase2", "docbase3", "docbase4", "docbase5", "docbase6" };
	JComboBox<String> docbaseSelection = new JComboBox<>(docbases);
	String selectedDocBase = null;

	class Connection extends SwingWorker<Void, Void> {
		/*
		 * Main task. Executed in background thread.
		 */
		@Override
		public Void doInBackground() {
			createSession();
			// if the login attempt was unsuccessful, the connect button is
			// enabled
			connectButton.setEnabled(true);
			return null;
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			try {
				get();
				//re-enables the Query Tasks button
				runQuery.setEnabled(true);
				// System.out.println("do in background task completed");
			} catch (InterruptedException | ExecutionException e) {
				taskOutput.append(e.getMessage());
			}
			setCursor(null); // turn off the wait cursor

		}

	}

	public TaskDelegation() {
		super(new BorderLayout(0, 10));// set a horizonal gap btw the components

		connectButton = new JButton("Connect");
		connectButton.setActionCommand("Connect");
		connectButton.addActionListener(this);

		delegate = new JButton("Delegate Tasks");
		// disables the button until the select query is executed
		delegate.setEnabled(false);
		delegate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				runQuery();
			}
		});
		disconnect = new JButton("Disconnect");
		disconnect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (collection != null) {
					try {
						collection.close();
						taskOutput.append("closed collection" + "\n");
					} catch (DfException e) {
						taskOutput.setText(e.getMessage() + "\n");
					}
				}
				sMgr.release(session);
				taskOutput.append("released session" + "\n");
				taskOutput.append("You may exit the application now");
				connectButton.setEnabled(true);
				//clears the fields
				itemField.setText("");
				performerField.setText("");
				delegatetoUser.setText("");
				all.setSelected(false);
			}

		});
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		taskOutput = new JTextArea(15, 80);
		taskOutput.setMargin(new Insets(5, 5, 5, 5));
		taskOutput.setEditable(true);
		taskOutput.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		labelDB = new JLabel();
		labelDB.setText("DocBase");

		selectWhomToDelegate = new JLabel();
		selectWhomToDelegate.setText("Type in the user to whom to delegate");
		specifyItem = new JLabel("Specify which task to delegate by item_id");
		userName = new JLabel("UserName");
		password = new JLabel("Password");
		userNameField = new JTextField(10);
		passwordField = new JPasswordField(15);
		performerField = new JTextField(6);
		delegatetoUser = new JTextField(6);
		itemField = new JTextField(16);
		all = new JCheckBox("Tick to delegate all tasks of the user");
		or = new JLabel("or");

		exit = new JButton("Exit");

		// exists from the application and closes the window
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(1);
			}
		});
		runQuery = new JButton("Query Tasks");
		runQuery.setEnabled(false);
		runQuery.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectQuery();
				delegate.setEnabled(false);
			
			}
		});
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints gc = new GridBagConstraints();
		/*
		 * IllegalArgumentException is thrown about illegal anchor value this
		 * piece of code catches the exception
		 */
		if (!(gc instanceof GridBagConstraints))
			throw new IllegalArgumentException();

		// first row
		gc.weightx = 1;
		gc.weighty = 0;

		gc.gridx = 1;
		gc.gridy = 0;
		gc.fill = GridBagConstraints.NONE;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(10, 10, 0, 0);
		panel.add(userName, gc);

		gc.gridx = 1;
		gc.gridy = 0;
		gc.insets = new Insets(10, 75, 0, 0);
		gc.anchor = GridBagConstraints.WEST;
		panel.add(userNameField, gc);
		// second row

		gc.gridx = 1;
		gc.gridy = 1;
		gc.fill = GridBagConstraints.NONE;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(10, 10, 0, 0);
		panel.add(password, gc);

		gc.gridx = 1;
		gc.gridy = 1;
		gc.insets = new Insets(10, 75, 0, 0);
		gc.anchor = GridBagConstraints.WEST;
		panel.add(passwordField, gc);

		// third row

		gc.gridx = 1;
		gc.gridy = 2;
		gc.fill = GridBagConstraints.NONE;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(10, 10, 30, 0);
		panel.add(labelDB, gc);

		gc.gridx = 1;
		gc.gridy = 2;
		gc.insets = new Insets(10, 75, 30, 0);
		gc.anchor = GridBagConstraints.WEST;
		panel.add(docbaseSelection, gc);

		// fourth row
		gc.gridx = 1;
		gc.gridy = 3;
		gc.fill = GridBagConstraints.NONE;
		gc.anchor = GridBagConstraints.WEST;
		gc.insets = new Insets(0, 75, 100, 0);
		panel.add(connectButton, gc);

		// gc for the right side of the gui

		GridBagConstraints gc2 = new GridBagConstraints();

		// first row

		gc2.weightx = 1;
		gc2.weighty = 2.0;

		gc2.gridx = 1;
		gc2.gridy = 0;
		gc2.fill = GridBagConstraints.NONE;
		gc2.anchor = GridBagConstraints.EAST;
		gc2.insets = new Insets(0, 0, 0, 20);
		panel.add(selectQuery, gc2);
		// second row

		gc2.gridx = 1;
		gc2.gridy = 1;
		gc2.anchor = GridBagConstraints.EAST;
		panel.add(performerField, gc2);

		// sixth row

		gc2.gridx = 1;
		gc2.gridy = 2;
		gc2.anchor = GridBagConstraints.EAST;
		gc2.insets = new Insets(0, 0, 25, 15);
		panel.add(runQuery, gc2);

		// third row
		gc2.gridx = 1;
		gc2.gridy = 2;
		gc2.anchor = GridBagConstraints.EAST;
		gc2.insets = new Insets(25, 0, 0, 15);
		panel.add(selectWhomToDelegate, gc2);

		// fourth row
		gc2.gridx = 1;
		gc2.gridy = 3;
		gc2.anchor = GridBagConstraints.EAST;
		gc2.insets = new Insets(0, 0, 55, 15);
		panel.add(all, gc2);

		// fourth row
		gc2.gridx = 1;
		gc2.gridy = 3;
		gc2.anchor = GridBagConstraints.EAST;
		gc2.insets = new Insets(0, 0, 20, 15);
		panel.add(or, gc2);

		// 3rd
		gc2.gridx = 1;
		gc2.gridy = 3;
		gc2.anchor = GridBagConstraints.EAST;
		gc2.insets = new Insets(20, 0, 0, 200);
		panel.add(specifyItem, gc2);

		// fourth row
		gc2.gridx = 1;
		gc2.gridy = 3;
		gc2.anchor = GridBagConstraints.EAST;
		gc2.insets = new Insets(20, 0, 0, 15);
		panel.add(itemField, gc2);

		// fifth row
		gc2.gridx = 1;
		gc2.gridy = 3;
		gc2.anchor = GridBagConstraints.EAST;
		gc2.insets = new Insets(0, 0, 115, 15);
		panel.add(delegatetoUser, gc2);

		// seventh row
		gc2.gridx = 1;
		gc2.gridy = 3;
		gc2.anchor = GridBagConstraints.EAST;
		gc2.insets = new Insets(100, 0, 0, 15);
		panel.add(delegate, gc2);

		// eight row
		gc.gridx = 1;
		gc.gridy = 4;
		gc.anchor = GridBagConstraints.CENTER;
		gc.insets = new Insets(0, 0, 10, 0);
		panel.add(new JScrollPane(taskOutput), gc);

		// 9th row
		gc.gridx = 1;
		gc.gridy = 5;
		gc.anchor = GridBagConstraints.CENTER;
		gc.insets = new Insets(0, 0, 10, 100);
		panel.add(disconnect, gc);
		gc.anchor = GridBagConstraints.CENTER;
		gc.insets = new Insets(0, 100, 10, 0);
		panel.add(exit, gc);

		add(panel, BorderLayout.CENTER);
		panel.setBackground(new Color(130, 130, 219));
		setBackground(new Color(149, 149, 200));
	}

	// invoked when the user presses Connect button
	public void actionPerformed(ActionEvent evt) {
		connectButton.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		connection = new Connection();
		connection.execute();
		delegate.setEnabled(false);

	}

	public void selectQuery() {
		// gets the performer`s name from the performer name field and adds it
		// to the query
		getPerformerField = performerField.getText().trim().toLowerCase();
		dqlSelect = "SELECT item_id FROM dm_queue WHERE name = '" + getPerformerField + "'"
				+ " and item_type = 'manual' ";
		querySelect = new DfQuery();
		querySelect.setDQL(dqlSelect);

		SwingWorker<Void, String> selectQueryWorker = new SwingWorker<Void, String>() {

			@Override
			protected Void doInBackground() throws Exception {
				// validates the performerField
				if (!performerField.getText().isEmpty()) {
					try {
						collection = querySelect.execute(session, DfQuery.DF_READ_QUERY);
						taskOutput.append("Executing query : " + dqlSelect + "\n");
						while (collection.next()) {
							wi = (IDfWorkitem) session.getObject(collection.getId("item_id")); // selecting
							queueItemId = wi.getQueueItemId();
							// selecting from the dmi_queue_item table
							qi = (IDfQueueItem) session.getObject(queueItemId);
							wf = (IDfWorkflow) session.getObject(qi.getWorkitem().getWorkflowId());
							System.out.println(
									"Item_id: " + collection.getId("item_id") + " Task_name: " + qi.getTaskName());
							System.out.println("trying to get exp name: " + qi.getItemName() + " " + qi.getObjectId()
									+ " " + wf.getObjectName());
							
							task_state = wi.getRuntimeState();
							publish(qi.getTaskName());
						
							resultsCounter1++;
						}
						if (resultsCounter1 == 0) {
							taskOutput.append("No signing tasks found" + "\n");
						}

					} catch (DfException e) {
						taskOutput.append(e.getMessage());
					} catch (Exception e) {
						// thrown if the user clicks on RunQuery without a
						// connection
						taskOutput.setText("An error has occured: " + e.getMessage() + "\n" + "Possible causes : "
								+ "\n" + "Not connected to a Docbase/No query typed/Wrong query" + "\n");
					} finally {
						if (collection != null) {
							try {
								collection.close();
							} catch (DfException e) {
								System.out.println(e.getMessage());
								taskOutput.setText(e.getMessage() + "\n");
							}
						}
					}
					// displayed if the performer field is empty
				} else {
					taskOutput.append("Please type a performer name" + "\n");
				}
				return null;
			}

			@Override
			protected void process(List<String> chunks2) {
				String objectname1 = chunks2.get(chunks2.size() - 1);
				try {
					taskOutput.append("Item_id: " + collection.getId("item_id") + " Task_name: " + objectname1
							+ " Experiment Name " + wf.getObjectName() + "\n");
				} catch (DfException e) {
					taskOutput.append(e.getMessage());
				}
			}

			protected void done() {
				// clears the counter for precise result
				resultsCounter1 = 0;
				// re-enables the delegate button after the select query completes
					delegate.setEnabled(true);
				
			}

		};
		selectQueryWorker.execute();
	}

	public void runQuery() {
		SwingWorker<Void, String> queryWorker = new SwingWorker<Void, String>() {

			@Override
			protected Void doInBackground() throws Exception {
				getItemId = itemField.getText().trim();
				String dqlSpecified = "SELECT item_id FROM dm_queue WHERE name = '" + getPerformerField + "'"
						+ " and item_type = 'manual' and item_id = '" + getItemId + "'";
				selectSpecfiedItem = new DfQuery();
				selectSpecfiedItem.setDQL(dqlSpecified);
				if (!delegatetoUser.getText().isEmpty()) {
					// if all is ticked, delegate all tasks by using the first
					// query
					try {
						if (all.isSelected()) {
							collection = querySelect.execute(session, DfQuery.DF_READ_QUERY);
							System.out.println("ticked");
						} else {
							collection = selectSpecfiedItem.execute(session, DfQuery.DF_READ_QUERY);
							System.out.println("not ticked");
						}
						taskOutput.append(dqlSpecified + "\n");
						// a value to check if the query has results, remains 0
						// if
						// no rows
						// are returned
						int resultsCounter = 0;
						// Loops through the results

						while (collection.next()) {
							wi = (IDfWorkitem) session.getObject(collection.getId("item_id")); // selecting
							queueItemId = wi.getQueueItemId();
							// selecting from the dmi_queue_item table
							qi = (IDfQueueItem) session.getObject(queueItemId);
							wf = (IDfWorkflow) session.getObject(qi.getWorkitem().getWorkflowId());
							// checking task_state
							switch (wi.getRuntimeState()) {

							case 0:
								System.out.println("Dormant");
								break;
							case 1:
								System.out.println("Acquired");
								break;
							case 2:
								System.out.println("Finished");
								break;
							case 3:
								System.out.println("Paused");
								break;
							default:
								System.out.println("No runtime state found");
								taskOutput.append("No runtime state found");
							}
							// TODO*****************************
							switch (wi.getRuntimeState()) {
							case 0:
								// if the task is dormant, acquire and delegate
								// it
								wi.acquire();
								taskOutput.append("Acquiring item " + "\n");
								// delegates the task to the user specified in
								// the
								// delegatetoUser field
								// checks if the delegatetoUser field is empty
								if (!delegatetoUser.getText().isEmpty()) {

									wi.delegateTask(delegatetoUser.getText().trim().toLowerCase());

									taskOutput.append(
											"Delegating dormant task with " + "task name: " + qi.getTaskName() + "\n");
								} else {
									taskOutput.append("Please enter a user to whom to delegate the task " + "\n");
								}
								break;
							// if the task has already been acquired, only
							// delegate
							// it
							case 1:
								if (!delegatetoUser.getText().isEmpty()) {
									wi.delegateTask(delegatetoUser.getText().trim().toLowerCase());
									taskOutput.append(
											"Delegating acquired task with " + "task name: " + qi.getTaskName() + "\n");
								} else {
									taskOutput.append("Please enter a user to whom to delegate the task " + "\n");
								}
								break;
							default:
								break;

							}

							publish(qi.getTaskName());
							resultsCounter++;
						}

						// if the dql has no results
						if (resultsCounter == 0) {
							taskOutput.append("No tasks found" + "\n");
						}

					} catch (Exception e) {
						// thrown if the user clicks on Run without a connection
						taskOutput.setText("An error has occured: " + e.getMessage() + "\n" + "Possible causes : "
								+ "\n" + "Not connected to a Docbase/No query typed/Wrong query" + "\n");
						// closes the collection
					} finally {
						if (collection != null) {
							try {
								collection.close();
							} catch (DfException e) {
								System.out.println(e.getMessage());
								taskOutput.setText(e.getMessage() + "\n");
							}
						}

					}
				} else {
					taskOutput.append("Please enter a user name to delegate to" + "\n");
				}
				return null;
			}

			@Override
			protected void process(List<String> chunks) {
				// String objectname2 = chunks.get(chunks.size() - 1);
			}

			@Override
			protected void done() {
				taskOutput
						.append("The current session will now be disconnected. Click on Connect to reconnect or choose another Docbase "
								+ "\n");
				releaseSession();
			}

		};
		queryWorker.execute();
	}

	@SuppressWarnings("deprecation")
	public void createSession() {
		try {
			selectedDocBase = (String) docbaseSelection.getSelectedItem();
			clientx = new DfClientX();
			client = clientx.getLocalClient();
			loginInfoObj = new DfLoginInfo();
			 loginInfoObj.setUser(userNameField.getText());
			 loginInfoObj.setPassword(passwordField.getText());
			sMgr = client.newSessionManager();
			sMgr.setIdentity(selectedDocBase, loginInfoObj);
			session = sMgr.newSession(selectedDocBase);

			if (session != null && session.isConnected()) {
				System.out.println("docbase is connected successfully ");
				System.out.println("Connected to repository name: " + session.getDocbaseName());
				System.out.println("Connected to repository ID  : " + session.getDocbaseId());
				taskOutput.setText("docbase is connected successfully " + "\n" + "Connected to repository name: "
						+ session.getDocbaseName() + "\n" + "Connected to repository ID  : " + session.getDocbaseId()
						+ "\n" + "Connected User: " + loginInfoObj.getUser() + "\n");
			}
		} catch (Throwable e) {
			System.out.println(e.getMessage());
			taskOutput.setText(e.getMessage());
		}
	}

	public void releaseSession() {
		sMgr.release(session);
		//clears the fields
		itemField.setText("");
		performerField.setText("");
		delegatetoUser.setText("");
		all.setSelected(false);
		connectButton.setEnabled(true);// re-enables the connect btn
										// after the query run has
										// completed
		try {
			if (session != null && session.isConnected()) {
				System.out.println("Still connected to repository");
				System.out.println("Connected to repository name: " + session.getDocbaseName());
				System.out.println("Connected to repository ID  : " + session.getDocbaseId());
				taskOutput.append("\n" + "Still connected to repository" + "\n" + "Connected to repository name: "
						+ session.getDocbaseName() + "\n" + "Connected to repository ID  : " + session.getDocbaseId()
						+ "\n");
			}
		} catch (DfException ed) {
			System.out.println(ed.getMessage());
			taskOutput.setText(ed.getMessage() + "\n");
		}
	}

	/**
	 * Create the GUI and show it. As with all GUI code, this must run on the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("Program to delegate signing tasks in Documentum repositories");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		JComponent newContentPane = new TaskDelegation();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setSize(new Dimension(1000, 600));
		frame.setPreferredSize(new Dimension(1000, 600));
		frame.setMinimumSize(new Dimension(1000, 600));
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
