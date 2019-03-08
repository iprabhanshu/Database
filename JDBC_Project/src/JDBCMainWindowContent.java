import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import javax.swing.*;
import javax.swing.border.*;
import java.sql.*;

@SuppressWarnings("serial")
public class JDBCMainWindowContent extends JInternalFrame implements ActionListener
{	
	String cmd = null;

	// DB Connectivity Attributes
	private Connection con = null;
	private Statement stmt = null;
	private ResultSet rs = null;

	private Container content;

	private JPanel detailsPanel;
	private JPanel exportButtonPanel;
	//private JPanel exportConceptDataPanel;
	private JScrollPane dbContentsPanel;

	private Border lineBorder;

	private JLabel idlb=new JLabel("Flight Id:                 ");
	private JLabel namelb=new JLabel("Flight Name:               ");
	private JLabel arrivallb=new JLabel("Arriving From:      ");
	private JLabel arrivaltimelb=new JLabel("Arrival Time:        ");
	private JLabel departurelb=new JLabel("Departure               ");
	private JLabel deptimelb=new JLabel("Departure Time:               ");
	private JLabel farelb=new JLabel("Fare:      ");
	private JLabel ptlb=new JLabel("Passenger Travelling:      ");
	private JLabel sllb=new JLabel("Seat Left:        ");

	private JTextField IDTF= new JTextField(10);
	private JTextField FlightNameTF=new JTextField(10);
	private JTextField ArrivalTF=new JTextField(10);
	private JTextField ArrivalTTF=new JTextField(10);
	private JTextField DepartureTF=new JTextField(10);
	private JTextField DepartureTTF=new JTextField(10);
	private JTextField FareTF=new JTextField(10);
	private JTextField PassengerTF=new JTextField(10);
	private JTextField SeatLeftTTF=new JTextField(10);


	private static QueryTableModel TableModel = new QueryTableModel();
	//Add the models to JTabels
	private JTable TableofDBContents=new JTable(TableModel);
	//Buttons for inserting, and updating members
	//also a clear button to clear details panel
	private JButton updateButton = new JButton("Update Flight");
	private JButton insertButton = new JButton("Add Flight Details");
	private JButton exportButton  = new JButton("Export");
	private JButton deleteButton  = new JButton("Remove Flight");
	private JButton clearButton  = new JButton("Clear");

	private JButton  EarnFlight = new JButton("Total Earning for flight");
	private JTextField TotalEarningTF  = new JTextField(12);
	private JButton avgAgeDepartment  = new JButton("Avg Departure To");
	private JTextField AvgDeparture  = new JTextField(12);
	private JButton ListAllDepartments  = new JButton("List All Departure");
	private JButton sumofPassenger  = new JButton("Total Passenger Travelling");



	public JDBCMainWindowContent( String aTitle)
	{	
		//setting up the GUI
		super(aTitle, false,false,false,false);
		setEnabled(true);

		initiate_db_conn();
		//add the 'main' panel to the Internal Frame
		content=getContentPane();
		content.setLayout(null);
		content.setBackground(Color.lightGray);
		lineBorder = BorderFactory.createEtchedBorder(15, Color.red, Color.black);

		//setup details panel and add the components to it
		detailsPanel=new JPanel();
		detailsPanel.setLayout(new GridLayout(11,2));
		detailsPanel.setBackground(Color.lightGray);
		detailsPanel.setBorder(BorderFactory.createTitledBorder(lineBorder, "CRUD Actions"));

		detailsPanel.add(idlb);			
		detailsPanel.add(IDTF);
		detailsPanel.add(namelb);		
		detailsPanel.add(FlightNameTF);
		detailsPanel.add(arrivallb);		
		detailsPanel.add(ArrivalTF);
		detailsPanel.add(arrivaltimelb);	
		detailsPanel.add(ArrivalTTF);
		detailsPanel.add(departurelb);		
		detailsPanel.add(DepartureTF);
		detailsPanel.add(deptimelb);
		detailsPanel.add(DepartureTTF);
		detailsPanel.add(farelb);
		detailsPanel.add(FareTF);
		detailsPanel.add(ptlb);
		detailsPanel.add(PassengerTF);
		detailsPanel.add(sllb);
		detailsPanel.add(SeatLeftTTF);

		//setup details panel and add the components to it
		exportButtonPanel=new JPanel();
		exportButtonPanel.setLayout(new GridLayout(3,2));
		exportButtonPanel.setBackground(Color.lightGray);
		exportButtonPanel.setBorder(BorderFactory.createTitledBorder(lineBorder, "Export Data"));
		exportButtonPanel.add(EarnFlight);
		exportButtonPanel.add(TotalEarningTF);
		exportButtonPanel.add(avgAgeDepartment);
		exportButtonPanel.add(AvgDeparture);
		exportButtonPanel.add(ListAllDepartments);
		exportButtonPanel.add(sumofPassenger);
		exportButtonPanel.setSize(500, 200);
		exportButtonPanel.setLocation(3, 300);
		content.add(exportButtonPanel);

		insertButton.setSize(100, 30);
		updateButton.setSize(100, 30);
		exportButton.setSize (100, 30);
		deleteButton.setSize (100, 30);
		clearButton.setSize (100, 30);

		insertButton.setLocation(370, 10);
		updateButton.setLocation(370, 110);
		exportButton.setLocation (370, 160);
		deleteButton.setLocation (370, 60);
		clearButton.setLocation (370, 210);

		insertButton.addActionListener(this);
		updateButton.addActionListener(this);
		exportButton.addActionListener(this);
		deleteButton.addActionListener(this);
		clearButton.addActionListener(this);

		this.ListAllDepartments.addActionListener(this);
		this.EarnFlight.addActionListener(this);
		this.avgAgeDepartment.addActionListener(this);
		this.sumofPassenger.addActionListener(this);


		content.add(insertButton);
		content.add(updateButton);
		content.add(exportButton);
		content.add(deleteButton);
		content.add(clearButton);


		TableofDBContents.setPreferredScrollableViewportSize(new Dimension(900, 300));

		dbContentsPanel=new JScrollPane(TableofDBContents,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		dbContentsPanel.setBackground(Color.lightGray);
		dbContentsPanel.setBorder(BorderFactory.createTitledBorder(lineBorder,"Database Content"));

		detailsPanel.setSize(360, 300);
		detailsPanel.setLocation(3,0);
		dbContentsPanel.setSize(700, 300);
		dbContentsPanel.setLocation(477, 0);

		content.add(detailsPanel);
		content.add(dbContentsPanel);

		setSize(982,645);
		setVisible(true);

		TableModel.refreshFromDB(stmt);
	}

	public void initiate_db_conn()
	{
		try
		{
			// Load the JConnector Driver
			Class.forName("com.mysql.jdbc.Driver");
			// Specify the DB Name
			String url="jdbc:mysql://localhost:3306/jdbc_project";
			// Connect to DB using DB URL, Username and password
			con = DriverManager.getConnection(url, "root", "admin");
			//Create a generic statement which is passed to the TestInternalFrame1
			stmt = con.createStatement();
		}
		catch(Exception e)
		{
			System.out.println("Error: Failed to connect to database\n"+e.getMessage());
		}
	}

	//event handling 
	public void actionPerformed(ActionEvent e)
	{
		Object target=e.getSource();
		if (target == clearButton)
		{
			IDTF.setText("");
			FlightNameTF.setText("");
			ArrivalTF.setText("");
			ArrivalTTF.setText("");
			DepartureTF.setText("");
			DepartureTTF.setText("");
			FareTF.setText("");
			PassengerTF.setText("");
			SeatLeftTTF.setText("");

		}
		
		if(target == exportButton ) {
			
			cmd = "select *  from flight_view;";

			try{					
				rs= stmt.executeQuery(cmd); 	
				writeToFile(rs);
			}
			catch(Exception e1){e1.printStackTrace();}
			
		}

		if (target == insertButton)
		{		 
			try
			{	
				String updateTemp = "INSERT INTO flight VALUES(" + null + ",'" + FlightNameTF.getText() + "','" + ArrivalTF.getText() + 
	                    "'," + ArrivalTTF.getText() + ",'" + DepartureTF.getText() + "'," + DepartureTTF.getText() + "," +
	                    FareTF.getText() + "," + PassengerTF.getText() + "," + SeatLeftTTF.getText() + " );";
				
				/*String updateTemp ="INSERT INTO details VALUES("+
				null +",'"+SourceTF.getText()+"','"+Destination.getText()+"',"+DepTTF.getText()+",'"+ArrTTF.getText()+"','"
				+FareTF.getText()+"','"+SeatsTF.getText()+"',"+HoursTF.getText()+","+ViaTF.getText()+");";
*/
				stmt.executeUpdate(updateTemp);

			}
			catch (SQLException sqle)
			{
				System.err.println("Error with  insert:\n"+sqle.toString());
			}
			finally
			{
				TableModel.refreshFromDB(stmt);
			}
		}
		if (target == deleteButton)
		{

			try
			{
				String updateTemp ="DELETE FROM flight WHERE fl_id = "+IDTF.getText()+";"; 
				stmt.executeUpdate(updateTemp);

			}
			catch (SQLException sqle)
			{
				System.err.println("Error with delete:\n"+sqle.toString());
			}
			finally
			{
				TableModel.refreshFromDB(stmt);
			}
		}
		if (target == updateButton)
		{	 	
			try
			{ 			
				
				String updateTemp = "CALL update_flight(" + IDTF.getText() + ", '" + FlightNameTF.getText() + "','" + ArrivalTF.getText() + 
	                    "'," + ArrivalTTF.getText() + ",'" + DepartureTF.getText() + "'," + DepartureTTF.getText() + "," +
	                    FareTF.getText() + "," + PassengerTF.getText() + "," + SeatLeftTTF.getText() + ");";


				stmt.executeUpdate(updateTemp);
				//these lines do nothing but the table updates when we access the db.
				rs = stmt.executeQuery("SELECT * from flight ");
				rs.next();
				rs.close();	
			}
			catch (SQLException sqle){
				System.err.println("Error with  update:\n"+sqle.toString());
			}
			finally{
				TableModel.refreshFromDB(stmt);
			}
		}

		/////////////////////////////////////////////////////////////////////////////////////
		//I have only added functionality of 2 of the button on the lower right of the template
		///////////////////////////////////////////////////////////////////////////////////

		if(target == this.ListAllDepartments){

			cmd = "select Departure from flight;";

			try{					
				rs= stmt.executeQuery(cmd); 	
				writeToFile(rs);
			}
			catch(Exception e1){e1.printStackTrace();}

		}

		if(target == this.EarnFlight){
			String flname = this.TotalEarningTF.getText();

			cmd = "select calculate_earning('"+ flname + "');";

			System.out.println(cmd);
			try{					
				rs= stmt.executeQuery(cmd); 	
				writeToFile(rs);
			}
			catch(Exception e1){e1.printStackTrace();}

		}
		
		if(target == this.avgAgeDepartment){
			String depart = this.AvgDeparture.getText();

			cmd = "Select Departure, AVG(Passenger_Travelling) from flight where Departure = '" + depart + "' GROUP BY Departure;";

			//System.out.println(cmd);
			try{					
				rs= stmt.executeQuery(cmd); 	
				writeToFile(rs);
			}
			catch(Exception e1){e1.printStackTrace();}

		}
		
		if(target == this.sumofPassenger){
			String sumtotal = this.AvgDeparture.getText();

			cmd = "Select SUM(Passenger_Travelling) from flight";

			//System.out.println(cmd);
			try{					
				rs= stmt.executeQuery(cmd); 	
				writeToFile(rs);
			}
			catch(Exception e1){e1.printStackTrace();}

		}

	}
	///////////////////////////////////////////////////////////////////////////

	private void writeToFile(ResultSet rs){
		try{
			System.out.println("In writeToFile");
			FileWriter outputFile = new FileWriter("Prabhanshu.csv");
			PrintWriter printWriter = new PrintWriter(outputFile);
			ResultSetMetaData rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();

			for(int i=0;i<numColumns;i++){
				printWriter.print(rsmd.getColumnLabel(i+1)+",");
			}
			printWriter.print("\n");
			while(rs.next()){
				for(int i=0;i<numColumns;i++){
					printWriter.print(rs.getString(i+1)+",");
				}
				printWriter.print("\n");
				printWriter.flush();
			}
			printWriter.close();
		}
		catch(Exception e){e.printStackTrace();}
	}
}
