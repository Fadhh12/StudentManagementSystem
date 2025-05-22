// StudentManagementSystem.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class StudentManagementSystem extends JFrame {
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/student_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Kosongkan password atau sesuaikan dengan password MySQL Anda
    
    // UI Components
    private JTextField idField, nameField, gpaField, enrollDateField, majorField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton;
    
    public StudentManagementSystem() {
        // Set up the JFrame
        setTitle("Student Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create the form panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // ID Field
        formPanel.add(new JLabel("ID:"));
        idField = new JTextField();
        formPanel.add(idField);
        
        // Name Field
        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);
        
        // GPA Field
        formPanel.add(new JLabel("GPA:"));
        gpaField = new JTextField();
        formPanel.add(gpaField);
        
        // Enrollment Date Field
        formPanel.add(new JLabel("Enrollment Date (YYYY-MM-DD):"));
        enrollDateField = new JTextField();
        formPanel.add(enrollDateField);
        
        // Major Field
        formPanel.add(new JLabel("Major:"));
        majorField = new JTextField();
        formPanel.add(majorField);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by Name or ID:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        searchButton = new JButton("Search");
        searchPanel.add(searchButton);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        // Add Button
        addButton = new JButton("Add");
        buttonPanel.add(addButton);
        
        // Update Button
        updateButton = new JButton("Update");
        buttonPanel.add(updateButton);
        
        // Delete Button
        deleteButton = new JButton("Delete");
        buttonPanel.add(deleteButton);
        
        // Clear Button
        clearButton = new JButton("Clear");
        buttonPanel.add(clearButton);
        
        // Combine form and buttons
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(formPanel, BorderLayout.CENTER);
        inputPanel.add(searchPanel, BorderLayout.NORTH);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add input panel to north area
        add(inputPanel, BorderLayout.NORTH);
        
        // Create the table
        String[] columns = {"ID", "Name", "GPA", "Enrollment Date", "Major"};
        tableModel = new DefaultTableModel(columns, 0);
        studentTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(studentTable);
        
        // Add table to center area
        add(scrollPane, BorderLayout.CENTER);
        
        // Add event listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addStudent();
            }
        });
        
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStudent();
            }
        });
        
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteStudent();
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchStudents();
            }
        });
        
        // Add mouse listener to the table for row selection
        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow >= 0) {
                    idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    gpaField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    enrollDateField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                    majorField.setText(tableModel.getValueAt(selectedRow, 4).toString());
                }
            }
        });
        
        // Initialize database
        initializeDatabase();
        
        // Load initial data
        loadStudents();
    }
    
    private void initializeDatabase() {
        try {
            // Create the database and table if they don't exist
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            
            // Create database if not exists
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS student_db");
            
            // Use the database
            stmt.executeUpdate("USE student_db");
            
            // Create table if not exists
            String createTableSQL = "CREATE TABLE IF NOT EXISTS students (" +
                    "id VARCHAR(10) PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "gpa FLOAT," +
                    "enrollment_date DATE," +
                    "major VARCHAR(100)" +
                    ")";
            stmt.executeUpdate(createTableSQL);
            
            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database initialization error: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void loadStudents() {
        // Clear the table
        tableModel.setRowCount(0);
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students");
            
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                float gpa = rs.getFloat("gpa");
                String enrollmentDate = rs.getString("enrollment_date");
                String major = rs.getString("major");
                
                tableModel.addRow(new Object[]{id, name, gpa, enrollmentDate, major});
            }
            
            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void searchStudents() {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadStudents();
            return;
        }
        
        tableModel.setRowCount(0);
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String query = "SELECT * FROM students WHERE id LIKE ? OR name LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + searchTerm + "%");
            pstmt.setString(2, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                float gpa = rs.getFloat("gpa");
                String enrollmentDate = rs.getString("enrollment_date");
                String major = rs.getString("major");
                
                tableModel.addRow(new Object[]{id, name, gpa, enrollmentDate, major});
            }
            
            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching students: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void addStudent() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String gpaStr = gpaField.getText().trim();
        String enrollmentDate = enrollDateField.getText().trim();
        String major = majorField.getText().trim();
        
        // Basic validation
        if (id.isEmpty() || name.isEmpty() || gpaStr.isEmpty() || enrollmentDate.isEmpty() || major.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            float gpa = Float.parseFloat(gpaStr);
            
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String query = "INSERT INTO students (id, name, gpa, enrollment_date, major) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setFloat(3, gpa);
            pstmt.setString(4, enrollmentDate);
            pstmt.setString(5, major);
            
            int rowsAffected = pstmt.executeUpdate();
            conn.close();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Student added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add student", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid GPA format", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void updateStudent() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String gpaStr = gpaField.getText().trim();
        String enrollmentDate = enrollDateField.getText().trim();
        String major = majorField.getText().trim();
        
        // Basic validation
        if (id.isEmpty() || name.isEmpty() || gpaStr.isEmpty() || enrollmentDate.isEmpty() || major.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            float gpa = Float.parseFloat(gpaStr);
            
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String query = "UPDATE students SET name=?, gpa=?, enrollment_date=?, major=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setFloat(2, gpa);
            pstmt.setString(3, enrollmentDate);
            pstmt.setString(4, major);
            pstmt.setString(5, id);
            
            int rowsAffected = pstmt.executeUpdate();
            conn.close();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Student updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this, "No student found with ID: " + id, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid GPA format", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void deleteStudent() {
        String id = idField.getText().trim();
        
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an ID", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete student with ID: " + id + "?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                String query = "DELETE FROM students WHERE id=?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, id);
                
                int rowsAffected = pstmt.executeUpdate();
                conn.close();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Student deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    loadStudents();
                } else {
                    JOptionPane.showMessageDialog(this, "No student found with ID: " + id, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        gpaField.setText("");
        enrollDateField.setText("");
        majorField.setText("");
        searchField.setText("");
    }
    
    public static void main(String[] args) {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Set Nimbus look and feel for better UI
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        
        // Launch the application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                StudentManagementSystem app = new StudentManagementSystem();
                app.setVisible(true);
                app.setLocationRelativeTo(null); // Center the window
            }
        });
    }
}