package lk.ijse.dep11.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import lk.ijse.dep11.td.Employee;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainFormController {
    public AnchorPane root;
    public TextField txtId;
    public TextField txtName;
    public TextField txtContact;
    public Button btnSave;
    public Button btnDelete;
    public TableView<Employee> tblEmployee;
    public TextField txtSearch;
    public Button btnNew;

    private List<Employee> employeeArrayList = new ArrayList<>();

    public void initialize() throws IOException {
        Control[] controls = {btnDelete,txtSearch};
        for (Control c : controls) {
            c.setDisable(true);
        }

        tblEmployee.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblEmployee.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblEmployee.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("contact"));


        tblEmployee.getSelectionModel().selectedItemProperty().addListener((value,previous,current)->{
            if(current == null) return;
            txtId.setText(current.getId());
            txtName.setText(current.getName());
            txtContact.setText(current.getContact());
            btnDelete.setDisable(false);
        });

        Platform.runLater(()->{
            employeeArrayList = readEmployeeDetails();
            ObservableList<Employee> employeeObservableList = FXCollections.observableList(employeeArrayList);
            tblEmployee.setItems(employeeObservableList);
            tblEmployee.setDisable(employeeArrayList.size() == 0);
            btnNew.fire();
            txtName.requestFocus();
        });

        Platform.runLater(()->{
            root.getScene().getWindow().setOnCloseRequest(event ->{
                saveEmployeeDetails();
            });
        });

        tblEmployee.setOnKeyPressed(event ->{
            if(event.getCode() == KeyCode.DELETE){
                btnDelete.fire();
            }
        });


    }

    public void btnSaveOnAction(ActionEvent event) {
        if(!isValid()) return;
        if(!tblEmployee.isDisable()){
            for (Employee employee: getTableList()) {
                if (employee.getId().equals(txtId.getText())){
                    tblEmployee.getSelectionModel().getSelectedItem().setName(txtName.getText());
                    tblEmployee.getSelectionModel().getSelectedItem().setContact(txtContact.getText());
                    tblEmployee.refresh();
                    return;
                }
                if(employee.getContact().equals(txtContact.getText())){
                    txtContact.selectAll();
                    txtContact.requestFocus();
                    new Alert(Alert.AlertType.ERROR,"Contact number is already exist").show();
                    return;
                }
            }
        }else {
            tblEmployee.setDisable(false);
            txtSearch.setDisable(false);
        }

        getTableList().add(new Employee(txtId.getText(),txtName.getText(),txtContact.getText()));
        btnNew.fire();

    }

    public void btnDeleteOnAction(ActionEvent event) {
        getTableList().remove(tblEmployee.getSelectionModel().getSelectedItem());
        tblEmployee.refresh();
        tblEmployee.getSelectionModel().clearSelection();
        btnDelete.setDisable(true);
    }

    public void btnNewOnAction(ActionEvent event) {
        txtId.setText(generateId());
        txtName.clear();
        txtContact.clear();
        txtName.requestFocus();
    }

    private boolean isValid(){
        if(!txtName.getText().matches("[A-Za-z ]+")){
            txtName.selectAll();
            txtName.requestFocus();
            new Alert(Alert.AlertType.ERROR,"Invalid Name").show();
            return false;
        }
        if(!txtContact.getText().matches("[0][1-9]{2}-\\d{7}")){
            txtContact.selectAll();
            txtContact.requestFocus();
            new Alert(Alert.AlertType.ERROR,"Invalid Contact").show();
            return false;
        }
        return true;
    }

    private String generateId(){
        if(getTableList().size() == 0) return "E-001";
        return String.format("E-%03d",Integer.parseInt(getTableList().get(getTableList().size()-1).getId().substring(2))+1);
    }
    private ObservableList<Employee> getTableList(){
        return tblEmployee.getItems();
    }

    private ArrayList<Employee> readEmployeeDetails(){
        try {
            File file = new File("target/employee.db");
            if(!file.exists()) return new ArrayList<>();
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            try {
                return  (ArrayList<Employee>) ois.readObject();
            }finally {
                ois.close();
            }

        }catch (Exception ex){
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Updating process failed").show();
            return new ArrayList<>();
        }

    }

    private void saveEmployeeDetails() {
        try {
            File file = new File("target/employee.db");
            if(!file.exists()) file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            try {
                oos.writeObject(employeeArrayList);
            }finally {
                oos.close();
            }

        }catch (Exception ex){
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Employees details weren't saved").show();
        }


    }
}


