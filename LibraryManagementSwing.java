import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LibraryManagementSwing extends JFrame {

    // 🧠 Database connection
    static final String URL = "jdbc:mysql://localhost:3306/library_db";
    static final String USER = "root";
    static final String PASS = "12@Bruno"; // <— change this
    Connection conn;

    // GUI components
    JTextField txtBookId, txtTitle, txtAuthor, txtISBN, txtMemberId, txtName, txtEmail;
    JTextArea output;

    public LibraryManagementSwing() {
        setTitle("Library Management System");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        // Connect to database
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "DB Connection Failed: " + e.getMessage());
            System.exit(0);
        }

        // ==== TOP PANEL (Input Fields) ====
        JPanel inputPanel = new JPanel(new GridLayout(7,2,5,5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Book / Member Info"));

        inputPanel.add(new JLabel("Book ID:"));
        txtBookId = new JTextField(); inputPanel.add(txtBookId);

        inputPanel.add(new JLabel("Title:"));
        txtTitle = new JTextField(); inputPanel.add(txtTitle);

        inputPanel.add(new JLabel("Author:"));
        txtAuthor = new JTextField(); inputPanel.add(txtAuthor);

        inputPanel.add(new JLabel("ISBN:"));
        txtISBN = new JTextField(); inputPanel.add(txtISBN);

        inputPanel.add(new JLabel("Member ID:"));
        txtMemberId = new JTextField(); inputPanel.add(txtMemberId);

        inputPanel.add(new JLabel("Name:"));
        txtName = new JTextField(); inputPanel.add(txtName);

        inputPanel.add(new JLabel("Email:"));
        txtEmail = new JTextField(); inputPanel.add(txtEmail);

        add(inputPanel, BorderLayout.NORTH);

        // ==== CENTER (Output Area) ====
        output = new JTextArea();
        output.setEditable(false);
        add(new JScrollPane(output), BorderLayout.CENTER);

        // ==== BOTTOM (Buttons) ====
        JPanel buttonPanel = new JPanel(new GridLayout(1,5,10,10));
        JButton addBook = new JButton("Add Book");
        JButton addMember = new JButton("Add Member");
        JButton borrow = new JButton("Borrow");
        JButton ret = new JButton("Return");
        JButton view = new JButton("View Books");

        buttonPanel.add(addBook);
        buttonPanel.add(addMember);
        buttonPanel.add(borrow);
        buttonPanel.add(ret);
        buttonPanel.add(view);

        add(buttonPanel, BorderLayout.SOUTH);

        // ==== BUTTON ACTIONS ====
        addBook.addActionListener(e -> addBook());
        addMember.addActionListener(e -> addMember());
        borrow.addActionListener(e -> borrowBook());
        ret.addActionListener(e -> returnBook());
        view.addActionListener(e -> viewBooks());
    }

    // ✅ Add Book
    void addBook() {
        try {
            String sql = "INSERT INTO books (book_id, title, author, isbn, available) VALUES (?, ?, ?, ?, TRUE)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(txtBookId.getText()));
            ps.setString(2, txtTitle.getText());
            ps.setString(3, txtAuthor.getText());
            ps.setString(4, txtISBN.getText());
            ps.executeUpdate();
            output.append("✅ Book added successfully!\n");
        } catch (Exception ex) {
            output.append("❌ Error adding book: " + ex.getMessage() + "\n");
        }
    }

    // ✅ Add Member
    void addMember() {
        try {
            String sql = "INSERT INTO members (member_id, name, email) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(txtMemberId.getText()));
            ps.setString(2, txtName.getText());
            ps.setString(3, txtEmail.getText());
            ps.executeUpdate();
            output.append("✅ Member added successfully!\n");
        } catch (Exception ex) {
            output.append("❌ Error adding member: " + ex.getMessage() + "\n");
        }
    }

    // 🟡 Borrow Book
    void borrowBook() {
        try {
            int bookId = Integer.parseInt(txtBookId.getText());
            int memberId = Integer.parseInt(txtMemberId.getText());
            PreparedStatement psCheck = conn.prepareStatement("SELECT available FROM books WHERE book_id=?");
            psCheck.setInt(1, bookId);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next() && rs.getBoolean("available")) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO transactions (book_id, member_id, transaction_date, status) VALUES (?, ?, CURDATE(), 'Borrowed')");
                ps.setInt(1, bookId);
                ps.setInt(2, memberId);
                ps.executeUpdate();

                conn.prepareStatement("UPDATE books SET available=FALSE WHERE book_id=" + bookId).executeUpdate();
                output.append("📚 Book borrowed successfully!\n");
            } else {
                output.append("❌ Book not available!\n");
            }
        } catch (Exception ex) {
            output.append("❌ Error borrowing: " + ex.getMessage() + "\n");
        }
    }

    // ✅ Return Book
    void returnBook() {
        try {
            int bookId = Integer.parseInt(txtBookId.getText());
            int memberId = Integer.parseInt(txtMemberId.getText());
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE transactions SET status='Returned' WHERE book_id=? AND member_id=? AND status='Borrowed'");
            ps.setInt(1, bookId);
            ps.setInt(2, memberId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                conn.prepareStatement("UPDATE books SET available=TRUE WHERE book_id=" + bookId).executeUpdate();
                output.append("✅ Book returned successfully!\n");
            } else {
                output.append("⚠️ No active borrow record found.\n");
            }
        } catch (Exception ex) {
            output.append("❌ Error returning: " + ex.getMessage() + "\n");
        }
    }

    // 📖 View All Books
    void viewBooks() {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM books");
            output.append("\n--- Books List ---\n");
            while (rs.next()) {
                output.append("ID: " + rs.getInt("book_id") + " | " +
                        rs.getString("title") + " by " + rs.getString("author") +
                        " | Available: " + (rs.getBoolean("available") ? "Yes" : "No") + "\n");
            }
        } catch (Exception ex) {
            output.append("❌ Error loading books: " + ex.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryManagementSwing().setVisible(true));
    }
}
