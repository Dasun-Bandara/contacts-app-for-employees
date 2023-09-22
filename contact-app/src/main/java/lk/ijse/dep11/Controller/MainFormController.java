package lk.ijse.dep11.Controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import lk.ijse.dep11.td.Employee;

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

    public void initialize(){
        Control[] controls = {btnDelete,tblEmployee,txtSearch};
        for (Control c : controls) {
            c.setDisable(true);
        }

        tblEmployee.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblEmployee.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblEmployee.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("contact"));

        Platform.runLater(()->{
            btnNew.fire();
            txtName.requestFocus();
        });

    }

    public void btnSaveOnAction(ActionEvent event) {
        if(!isValid()){
            return;
        }
        tblEmployee.setDisable(false);
        getTableList().add(new Employee(txtId.getText(),txtName.getText(),txtContact.getText()));
        txtSearch.setDisable(false);
    }

    public void btnDeleteOnAction(ActionEvent event) {
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
        if(getTableList().size() == 0) return "C-001";
        return String.format("C-%03d",Integer.parseInt(getTableList().get(getTableList().size()-1).getId().substring(2))+1);
    }
    private List<Employee> getTableList(){
        return tblEmployee.getItems();
    }
}


