package com.example.vulnerable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class VulnerableExample {

    // Hardcoded credentials (Security issue)
    private static final String DB_USER = "admin";
    private static final String DB_PASS = "password123";

    public static void main(String[] args) {
        String userInput = args.length > 0 ? args[0] : "default";

        try {
            // SQL Injection vulnerability
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", DB_USER, DB_PASS);
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM users WHERE username = '" + userInput + "'";
            ResultSet rs = stmt.executeQuery(query);

            // Unused variable (Code quality)
            int unused = 42;

            while (rs.next()) {
                System.out.println("User: " + rs.getString("username"));
            }

            // No resource cleanup (Performance/quality)
        } catch (Exception e) {
            // Swallowing exception (Bad practice)
        }
    }
}
