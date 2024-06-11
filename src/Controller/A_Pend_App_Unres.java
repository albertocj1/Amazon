package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import Model.DbConnect;
import Model.admin;
import Model.complaint_ticket;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Observable;

import com.mysql.cj.xdevapi.Statement;


public class A_Pend_App_Unres {
    @FXML
    private TextField searchTextField;

    @FXML
    private TextField addFName, addMName, addLName, addContactNum, addEmailAddress, addHouseNum, addBrgy, addStreet, addCity;

    @FXML
    private ChoiceBox <String> addSubject;

    @FXML
    private ChoiceBox <String> addDept;

    @FXML
    private ChoiceBox <String> addCategory;

    @FXML
    private DatePicker addDate;

    @FXML
    private TextField addOrderID, addDesc;

    @FXML
    private TableView <complaint_ticket> pendingTable;

    @FXML
    private TableColumn <complaint_ticket, Integer> PcomplaintIDColumn;

    @FXML
    private TableColumn <complaint_ticket, Integer> PcomplainantIDColumn;

    @FXML
    private TableColumn <complaint_ticket, String> PsubjectColumn;

    @FXML
    private TableColumn <complaint_ticket, String> PdescriptionColumn;

    @FXML
    private TableColumn <complaint_ticket, Integer> PorderIDColumn;

    @FXML
    private TableColumn <complaint_ticket, String> PcategoryColumn;

    @FXML
    private TableColumn <complaint_ticket, Date> PcreatedDateColumn;

    @FXML
    Pane pendingpane, approvedpane, unresolvedpane, pending_preview, unresolved_preview, confirmation_approval, confirmation_delete, newTicket1, newTicket2;

    String query = null;
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    complaint_ticket complaint_ticket = null;

    ObservableList<complaint_ticket> complaint_ticketList = null;
    private int complainantID;
    private int adminID = 1;


    public void initialize(){
        loadPData();
        addSubject.setItems(FXCollections.observableArrayList("Missing Item", "Order not Processed Correctly", "Damaged Item"));
        addDept.setItems(FXCollections.observableArrayList("Order Fulfillment Department", "Parcel Tracking Department", "Product Replacement Department", "Returns Management Department"));
        addCategory.setItems(FXCollections.observableArrayList("Electronics", "Clothes", "Furniture", "Books"));

    }

    @FXML
    public void loadPData() {
        connection = DbConnect.getConnect();
        //refreshPTable();
        PcomplaintIDColumn.setCellValueFactory(new PropertyValueFactory<>("complaint_ID"));
        PcomplainantIDColumn.setCellValueFactory(new PropertyValueFactory<>("complainant_ID"));
        PsubjectColumn.setCellValueFactory(new PropertyValueFactory<>("complaint_Subject"));
        PdescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("complaint_Desc"));
        PorderIDColumn.setCellValueFactory(new PropertyValueFactory<>("complaint_OrderID"));
        PcategoryColumn.setCellValueFactory(new PropertyValueFactory<>("complaint_Category"));
        PcreatedDateColumn.setCellValueFactory(new PropertyValueFactory<>("complaint_CreatedDate"));

    }

    public void refreshPTable() {
        try { 
            complaint_ticketList.clear();

            if (searchTextField != null) {
                String searchKeyword = searchTextField.getText();

                String query = "SELECT * FROM complaint_ticket WHERE complaint_ID LIKE '%" + searchKeyword + "%' OR complainant_ID LIKE '%" + searchKeyword + "%' OR complaint_Subject LIKE '%" + searchKeyword + "%' OR complaint_Desc LIKE '%" + searchKeyword + "%' OR complaint_OrderID LIKE '%" + searchKeyword + "%' OR complaint_Category LIKE '%" + searchKeyword + "%' OR complaint_CreatedDate LIKE '%" + searchKeyword + "%'";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, "%" + searchKeyword + "%");
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    complaint_ticketList.add(new complaint_ticket(
                        resultSet.getInt("complaint_ID"),
                        resultSet.getInt("complainant_ID"),
                        resultSet.getInt("admin_ID"),
                        resultSet.getString("complaint_Subject"),
                        resultSet.getString("complaint_Desc"),
                        resultSet.getString("complaint_OrderID"),
                        resultSet.getString("complaint_Category"),
                        resultSet.getString("complaint_ProdInfo"),
                        resultSet.getInt("complaint_CustServRate"),
                        resultSet.getString("complaint_Status"),
                        resultSet.getBoolean("complaint_DropOffSched"),
                        resultSet.getDate("complaint_CreatedDate"),
                        resultSet.getString("complaint_Dept")
                    ));
                }
                    pendingTable.setItems(complaint_ticketList);
            } else {
                System.out.println("searchTextField is null");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            
    }

    @FXML
    public void handleSearch(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            refreshPTable();
        }
    }

    public void createComplainant(ActionEvent event) {
        try (Connection connection = DbConnect.getConnect()) {
            gotoTicket1();
            System.out.println("Connected to database.");
            String defaultInsertQuery = "INSERT INTO complainant (complainant_FName, complainant_MName, complainant_LName, complainant_ContactNum, complainant_EmailAdd, complainant_HouseNum, complainant_Brgy, complainant_Street, complainant_City) VALUES ('Default', 'Default', 'Default', '0000000000', 'default@example.com', '0', 'Default', 'Default', 'Default')";

            try (PreparedStatement statement = connection.prepareStatement(defaultInsertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.executeUpdate();
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    complainantID = generatedKeys.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Complainant ID: " + complainantID);
    }

    public void addComplaint(ActionEvent event) {
        System.out.println(complainantID);
        try {
            String FName = addFName.getText();
            String MName = addMName.getText();
            String LName = addLName.getText();
            String ContactNum = addContactNum.getText();
            String EmailAddress = addEmailAddress.getText();
            String HouseNum = addHouseNum.getText();
            String Brgy = addBrgy.getText();
            String Street = addStreet.getText();
            String City = addCity.getText();
            String Subject = addSubject.getValue();
            String Dept = addDept.getValue();
            String Description = addDesc.getText();
            String OrderID = addOrderID.getText();
            String Category = addCategory.getValue();
            LocalDate Date = addDate.getValue();

            try (Connection connection = DbConnect.getConnect()) {
                // Update complainant details
                String complainantUpdateQuery = "UPDATE complainant SET complainant_FName = ?, complainant_MName = ?, complainant_LName = ?, complainant_ContactNum = ?, complainant_EmailAdd = ?, complainant_HouseNum = ?, complainant_Brgy = ?, complainant_Street = ?, complainant_City = ? WHERE complainant_ID = " + complainantID;
                
                try (PreparedStatement complainantStatement = connection.prepareStatement(complainantUpdateQuery)) {
                    complainantStatement.setString(1, FName);
                    complainantStatement.setString(2, MName);
                    complainantStatement.setString(3, LName);
                    complainantStatement.setString(4, ContactNum);
                    complainantStatement.setString(5, EmailAddress);
                    complainantStatement.setString(6, HouseNum);
                    complainantStatement.setString(7, Brgy);
                    complainantStatement.setString(8, Street);
                    complainantStatement.setString(9, City);
                    complainantStatement.executeUpdate();
                }

                // Insert complaint ticket
                String complaint_ticketInsertQuery = "INSERT INTO complaint_ticket (complainant_ID, admin_ID, compt_Subject, compt_Desc, compt_OrderID, compt_Category, compt_ProdInfo, compt_CustServRate, compt_Status, compt_DropOffSched, compt_CreatedDate, compt_Dept) VALUES (?, ?, ?, ?, ?, ?, ?, 0, 'Approved', false, ?, ?)";

                try (PreparedStatement complaint_ticketStatement = connection.prepareStatement(complaint_ticketInsertQuery)) {
                    complaint_ticketStatement.setInt(1, complainantID);
                    complaint_ticketStatement.setInt(2, adminID);
                    complaint_ticketStatement.setString(3, Subject);
                    complaint_ticketStatement.setString(4, Description);
                    complaint_ticketStatement.setString(5, OrderID);
                    complaint_ticketStatement.setString(6, Category);
                    complaint_ticketStatement.setString(7, "Product Info");
                    complaint_ticketStatement.setDate(8, java.sql.Date.valueOf(Date));
                    complaint_ticketStatement.setString(9, Dept);


                    complaint_ticketStatement.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void gotoApproved(ActionEvent event) throws IOException {
        pendingpane.setVisible(false);
        approvedpane.setVisible(true);
        unresolvedpane.setVisible(false);
    }

    @FXML
    public void gotoUnresolved(ActionEvent event) throws IOException {
        pendingpane.setVisible(false);
        unresolvedpane.setVisible(true);
        approvedpane.setVisible(false);
        confirmation_delete.setVisible(false);
        unresolved_preview.setVisible(false);
    }

    @FXML
    public void gotoPending(ActionEvent event) throws IOException {
        pendingpane.setVisible(true);
        unresolvedpane.setVisible(false);
        approvedpane.setVisible(false);
        pending_preview.setVisible(false);
        confirmation_approval.setVisible(false);
        newTicket1.setVisible(false);
        newTicket2.setVisible(false);
    }

    @FXML
    public void gotoPendingPrev(ActionEvent event) throws IOException {
        pendingpane.setVisible(false);
        pending_preview.setVisible(true);
    }

    @FXML
    public void gotoApprovedConfirmation(ActionEvent event) throws IOException {
        pendingpane.setVisible(false);
        pending_preview.setVisible(false);
        unresolvedpane.setVisible(false);
        unresolved_preview.setVisible(false);
        confirmation_approval.setVisible(true);
    }

    @FXML
    public void gotoDeleteConfirmation(ActionEvent event) throws IOException {
        pendingpane.setVisible(false);
        pending_preview.setVisible(false);
        unresolvedpane.setVisible(false);
        confirmation_delete.setVisible(true);
    }
    
    @FXML
    public void gotoUnresolvedPrev(ActionEvent event) throws IOException {
        unresolvedpane.setVisible(false);
        unresolved_preview.setVisible(true);
    }

    @FXML
    public void gotoTicket1(){
        pendingpane.setVisible(false);
        newTicket1.setVisible(true);
    }

    @FXML
    public void gotoTicket2(ActionEvent event) throws IOException {
        newTicket1.setVisible(false);
        newTicket2.setVisible(true);
    }

    @FXML
    public void gotoOverview(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Admin-Dashboard.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void gotoHistory(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Admin-History.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}