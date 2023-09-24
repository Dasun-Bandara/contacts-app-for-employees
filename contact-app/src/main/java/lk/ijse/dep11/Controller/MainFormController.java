package lk.ijse.dep11.Controller;

import com.opencsv.CSVReader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import lk.ijse.dep11.td.Employee;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

    private ObservableList<Employee> employeeObservableList;

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
            employeeObservableList = FXCollections.observableList(employeeArrayList);
            tblEmployee.setItems(employeeObservableList);
            tblEmployee.setDisable(employeeArrayList.size() == 0);
            txtSearch.setDisable(tblEmployee.isDisable());
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

        txtSearch.textProperty().addListener(event ->{
            String regex = "";
            char[] charArray = txtSearch.getText().toCharArray();
            for (char element: charArray) {
                if(element == '+' | element == '*' | element == '.' | element == '?'){
                    regex += "["+element+"]";
                    continue;
                }
                if(!Character.isLetter(element)){
                    regex += element;
                }else {
                    regex += "["+element+""+(Character.isLowerCase(element) ? Character.toUpperCase(element) : Character.toLowerCase(element))+"]";
                }
            }
            search(regex);
            if(txtSearch.getText().isBlank()){
                tblEmployee.setItems(employeeObservableList);
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
        if(!txtContact.getText().matches("0[1-9][0-9]-\\d{7}")){
            txtContact.selectAll();
            txtContact.requestFocus();
            new Alert(Alert.AlertType.ERROR,"Invalid Contact").show();
            return false;
        }
        return true;
    }

    private String generateId(){
        ArrayList<Integer> idList = new ArrayList<>();
        if(getTableList().size() == 0) return "E-001";
        for (Employee employee: getTableList()) {
            idList.add(Integer.parseInt(employee.getId().substring(2)));
        }
        return String.format("E-%03d",Collections.max(idList)+1);
    }
    private ObservableList<Employee> getTableList(){
        return employeeObservableList;
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

    private ArrayList<String[]> readCsvFile(File csvFile){
        ArrayList<String[]> temp = new ArrayList<>();
        try {
            FileReader fr = new FileReader(csvFile);
            CSVReader reader = new CSVReader(fr);
            String[] line;
            while ((line = reader.readNext()) != null){
                temp.add(line);
            }
            return temp;

        }catch (Exception e){
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"CSV file reading process is failed").show();
            return temp;
        }
    }
    private void updateCsvFile(File csvFile){
        ArrayList<String[]> csvList = readCsvFile(csvFile);
        String[] patterns = {"E-\\d{3}","[A-Za-z ]+","0[1-9]{2}-\\d{7}"};
        loop:
        for (String[] line: csvList) {
            for (int i = 0; i < 3; i++) {
                if (!line[i].matches(patterns[i])){
                    continue loop;
                }
            }
            for (int i = 0; i < employeeArrayList.size(); i++) {
                if(!(employeeArrayList.get(i).getId().equals(line[0])) && (employeeArrayList.get(i).getContact().equals(line[2]))) continue loop;
                if (employeeArrayList.get(i).getId().equals(line[0])){
                    employeeArrayList.remove(i);
                    employeeArrayList.add(i,new Employee(line[0],line[1],line[2]));
                    tblEmployee.refresh();
                    continue loop;
                }
            }
            employeeArrayList.add(new Employee(line[0],line[1],line[2]));
            tblEmployee.refresh();
        }
    }

    public void tblEmployeeOnDragOver(DragEvent dragEvent) {
        if(!dragEvent.getDragboard().getFiles().get(0).getName().endsWith(".csv")){
            dragEvent.consume();
            new Alert(Alert.AlertType.INFORMATION,"Drop only csv files here").show();
            return;
        }
        dragEvent.acceptTransferModes(TransferMode.ANY);

    }
    public void tblEmployeeOnDragDropped(DragEvent dragEvent) {
        String path = dragEvent.getDragboard().getFiles().get(0).getAbsolutePath();
        File file = new File(path);
        updateCsvFile(file);
    }

    public void search(String regex){
        Pattern pattern = Pattern.compile(regex);
        ArrayList<Employee> temp = new ArrayList<>();
        for (Employee employee: employeeArrayList) {
            Matcher matcherId = pattern.matcher(employee.getId());
            Matcher matcherName = pattern.matcher(employee.getName());
            Matcher matcherContact = pattern.matcher(employee.getContact());
            if (matcherId.find() | matcherName.find() | matcherContact.find()){
                temp.add(employee);
            }
        }
        ObservableList<Employee> obTemp = FXCollections.observableList(temp);
        tblEmployee.setItems(obTemp);
        tblEmployee.refresh();
    }

}


