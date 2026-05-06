import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class LibraryServlet extends HttpServlet {

    private static final String URL      = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "";   // XAMPP default

    @Override
    public void init() throws ServletException {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (Exception e) { e.printStackTrace(); }
    }

    // ========== HEADER & FOOTER (embedded CSS) ==========
    private void printHeader(HttpServletRequest req, PrintWriter out, String title) {
        String ctx = req.getContextPath();
        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>" + title + "</title>");
        out.println("<link rel='preconnect' href='https://fonts.googleapis.com'>");
        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap' rel='stylesheet'>");
        out.println("<style>");
        out.println(":root { --primary: #4361ee; --primary-dark: #3a56d4; --danger: #e63946; --success: #2ecc71; --bg: #f8f9fa; --card-bg: #ffffff; --text: #212529; --text-light: #6c757d; --border: #dee2e6; --nav-bg: #1e293b; }");
        out.println("* { box-sizing: border-box; margin: 0; padding: 0; }");
        out.println("body { font-family: 'Inter', sans-serif; background: var(--bg); color: var(--text); line-height: 1.6; }");
        out.println("nav { background: var(--nav-bg); padding: 0 2rem; display: flex; align-items: center; height: 60px; box-shadow: 0 2px 8px rgba(0,0,0,0.15); }");
        out.println("nav a { color: #cbd5e1; text-decoration: none; font-weight: 500; margin-right: 2rem; transition: color 0.2s; }");
        out.println("nav a:hover { color: #ffffff; }");
        out.println(".container { max-width: 1200px; margin: 2rem auto; padding: 0 1.5rem; }");
        out.println("h2 { margin-bottom: 1.5rem; color: var(--nav-bg); font-weight: 600; }");
        out.println("table { width: 100%; border-collapse: separate; border-spacing: 0; background: var(--card-bg); border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08); margin-bottom: 2rem; }");
        out.println("th, td { padding: 1rem; text-align: left; }");
        out.println("th { background: #f1f5f9; color: var(--nav-bg); font-weight: 600; font-size: 0.9rem; text-transform: uppercase; letter-spacing: 0.5px; }");
        out.println("td { border-bottom: 1px solid var(--border); }");
        out.println("tr:last-child td { border-bottom: none; }");
        out.println("tr:hover td { background: #f8fafc; }");
        out.println("form { background: var(--card-bg); padding: 2rem; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); margin-bottom: 2rem; }");
        out.println("label { display: block; margin-top: 1.2rem; font-weight: 500; color: var(--nav-bg); }");
        out.println("input, select { width: 100%; padding: 0.75rem; margin-top: 0.4rem; border: 1px solid var(--border); border-radius: 8px; font-size: 1rem; transition: border-color 0.2s; background: #fff; }");
        out.println("input:focus, select:focus { outline: none; border-color: var(--primary); box-shadow: 0 0 0 3px rgba(67,97,238,0.1); }");
        out.println("input[type=submit], button { background: var(--primary); color: white; border: none; padding: 0.75rem 1.8rem; margin-top: 1.5rem; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 1rem; transition: background 0.2s, transform 0.1s; }");
        out.println("input[type=submit]:hover, button:hover { background: var(--primary-dark); transform: translateY(-1px); }");
        out.println("input[type=submit]:active, button:active { transform: translateY(0); }");
        out.println("a.action-btn { display: inline-block; padding: 0.4rem 1rem; background: var(--primary); color: white; text-decoration: none; border-radius: 6px; font-weight: 500; margin-right: 0.3rem; transition: background 0.2s; }");
        out.println("a.action-btn:hover { background: var(--primary-dark); }");
        out.println("a.delete-btn { background: var(--danger); }");
        out.println("a.delete-btn:hover { background: #c82232; }");
        out.println(".error { color: var(--danger); margin-top: 0.5rem; font-weight: 500; }");
        out.println(".search-form { display: flex; gap: 0.75rem; margin-bottom: 1.5rem; }");
        out.println(".search-form input[type=text] { flex: 1; margin-top: 0; }");
        out.println(".search-form button { margin-top: 0; padding: 0.75rem 1.5rem; }");
        out.println(".back-link { display: inline-block; margin-bottom: 1rem; color: var(--primary); text-decoration: none; font-weight: 500; }");
        out.println(".back-link:hover { text-decoration: underline; }");
        out.println("@media (max-width: 768px) { nav { flex-direction: column; height: auto; padding: 1rem; } nav a { margin: 0.3rem 0; } .container { margin: 1rem auto; } }");
        out.println("</style></head><body>");
        out.println("<nav>");
        out.println("<a href='" + ctx + "/library/'>Home</a>");
        out.println("<a href='" + ctx + "/library/books'>Books</a>");
        out.println("<a href='" + ctx + "/library/authors'>Authors</a>");
        out.println("<a href='" + ctx + "/library/members'>Members</a>");
        out.println("<a href='" + ctx + "/library/loans'>Loans</a>");
        out.println("</nav>");
        out.println("<div class='container'>");
    }

    private void printFooter(PrintWriter out) {
        out.println("</div></body></html>");
    }

    // ==================== DO GET ====================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        switch (path) {
            case "/"             : showHome(req, out);                      break;
            case "/books"        : showBooks(req, out);                     break;
            case "/authors"      : showAuthors(req, out);                   break;
            case "/members"      : showMembers(req, out);                   break;
            case "/loans"        : showLoans(req, out);                     break;
            case "/editBook"     : showEditBookForm(req, resp, out);         break;
            case "/deleteBook"   : deleteBook(req, resp);                   break;
            case "/editAuthor"   : showEditAuthorForm(req, resp, out);       break;
            case "/deleteAuthor" : deleteAuthor(req, resp);                 break;
            case "/editMember"   : showEditMemberForm(req, resp, out);       break;
            case "/deleteMember" : deleteMember(req, resp);                 break;
            default:
                printHeader(req, out, "Not Found");
                out.println("<p class='error'>Page not found.</p>");
                out.println("<a href='" + req.getContextPath() + "/library/'>Back to Home</a>");
                printFooter(out);
        }
    }

    // ==================== DO POST ====================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if ("/addAuthor".equals(path))      { addAuthor(req, resp); }
        else if ("/addBook".equals(path))   { addBook(req, resp); }
        else if ("/borrow".equals(path))    { borrowBook(req, resp); }
        else if ("/return".equals(path))    { returnBook(req, resp); }
        else if ("/updateBook".equals(path)){ updateBook(req, resp); }
        else if ("/updateAuthor".equals(path)){ updateAuthor(req, resp); }
        else if ("/addMember".equals(path)) { addMember(req, resp); }
        else if ("/updateMember".equals(path)){ updateMember(req, resp); }
        else { resp.sendRedirect(req.getContextPath() + "/library/"); }
    }

    // ==================== HOME ====================
    private void showHome(HttpServletRequest req, PrintWriter out) {
        printHeader(req, out, "Library Home");
        out.println("<h2>Welcome to the Library</h2>");
        out.println("<p>Manage books, authors, members, and loans from the navigation above.</p>");
        printFooter(out);
    }

    // ==================== MEMBERS ====================
    private void showMembers(HttpServletRequest req, PrintWriter out) {
        printHeader(req, out, "Members");
        String ctx = req.getContextPath();

        out.println("<table><thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Actions</th></tr></thead><tbody>");
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM members ORDER BY member_id")) {
            while (rs.next()) {
                int id = rs.getInt("member_id");
                out.printf("<tr><td>%d</td><td>%s</td><td>%s</td>" +
                    "<td>" +
                        "<a href='" + ctx + "/library/editMember?id=%d' class='action-btn'>Edit</a> " +
                        "<a href='" + ctx + "/library/deleteMember?id=%d' class='action-btn delete-btn' " +
                        "onclick=\"return confirm('Delete this member?')\">Delete</a>" +
                    "</td></tr>",
                    id, rs.getString("name"), rs.getString("email"), id, id);
            }
        } catch (SQLException e) {
            out.println("<tr><td colspan='4' class='error'>Error: " + e.getMessage() + "</td></tr>");
        }
        out.println("</tbody></table>");

        out.println("<h3>Add Member</h3>");
        out.println("<form action='" + ctx + "/library/addMember' method='post'>");
        out.println("<label>Name</label><input name='name' required>");
        out.println("<label>Email</label><input name='email' type='email'>");
        out.println("<input type='submit' value='Add Member'>");
        out.println("</form>");

        printFooter(out);
    }

    private void addMember(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name  = req.getParameter("name");
        String email = req.getParameter("email");
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("INSERT INTO members (name, email) VALUES (?,?)")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        resp.sendRedirect(req.getContextPath() + "/library/members");
    }

    private void showEditMemberForm(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) {
        int id = Integer.parseInt(req.getParameter("id"));
        String ctx = req.getContextPath();
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("SELECT * FROM members WHERE member_id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                printHeader(req, out, "Edit Member");
                out.println("<h2>Edit Member</h2>");
                out.println("<form action='" + ctx + "/library/updateMember' method='post'>");
                out.println("<input type='hidden' name='member_id' value='" + id + "'>");
                out.println("<label>Name</label><input name='name' value='" + rs.getString("name") + "' required>");
                out.println("<label>Email</label><input name='email' value='" + rs.getString("email") + "'>");
                out.println("<input type='submit' value='Update Member'>");
                out.println("</form>");
                printFooter(out);
            } else {
                printHeader(req, out, "Not Found");
                out.println("<p class='error'>Member not found.</p><a href='" + ctx + "/library/members'>Back</a>");
                printFooter(out);
            }
        } catch (SQLException e) {
            printHeader(req, out, "Error");
            out.println("<p class='error'>" + e.getMessage() + "</p>");
            printFooter(out);
        }
    }

    private void updateMember(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id       = Integer.parseInt(req.getParameter("member_id"));
        String name  = req.getParameter("name");
        String email = req.getParameter("email");
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("UPDATE members SET name=?, email=? WHERE member_id=?")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        resp.sendRedirect(req.getContextPath() + "/library/members");
    }

    private void deleteMember(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("DELETE FROM members WHERE member_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        resp.sendRedirect(req.getContextPath() + "/library/members");
    }

    // ==================== BOOKS (dynamic author creation, availability) ====================
    private void showBooks(HttpServletRequest req, PrintWriter out) {
        printHeader(req, out, "Books");
        String ctx = req.getContextPath();

        String search = req.getParameter("search");
        out.println("<form method='get' action='" + ctx + "/library/books' class='search-form'>");
        out.println("<input type='text' name='search' placeholder='Search by title...' value='" +
                     (search != null ? search : "") + "'/>");
        out.println("<button type='submit'>Search</button>");
        out.println("</form>");

        out.println("<table><thead><tr><th>ID</th><th>Title</th><th>Author</th><th>ISBN</th><th>Qty</th><th>Available</th><th>Actions</th></tr></thead><tbody>");

        String sql = "SELECT b.book_id, b.title, a.name AS author, b.isbn, b.quantity " +
                     "FROM books b JOIN authors a ON b.author_id = a.author_id";
        if (search != null && !search.trim().isEmpty()) {
            sql += " WHERE b.title LIKE '%" + search.replace("'", "''") + "%'";
        }

        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("book_id");
                int qty = rs.getInt("quantity");
                String available = (qty > 0) ? "<span style='color: var(--success); font-weight:600;'>Yes</span>"
                                            : "<span style='color: var(--danger); font-weight:600;'>No</span>";
                String borrowLink = (qty > 0) ? "<a href='" + ctx + "/library/loans?bookId=" + id + "' class='action-btn' style='background: var(--success);'>Borrow</a> " : "";

                out.printf("<tr>" +
                    "<td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%d</td>" +
                    "<td>%s</td>" +
                    "<td>" +
                        borrowLink +
                        "<a href='" + ctx + "/library/editBook?id=%d' class='action-btn'>Edit</a> " +
                        "<a href='" + ctx + "/library/deleteBook?id=%d' class='action-btn delete-btn' " +
                        "onclick=\"return confirm('Delete this book?')\">Delete</a>" +
                    "</td></tr>",
                    id, rs.getString("title"), rs.getString("author"),
                    rs.getString("isbn"), qty, available, id, id);
            }
        } catch (SQLException e) {
            out.println("<tr><td colspan='7' class='error'>Error: " + e.getMessage() + "</td></tr>");
        }
        out.println("</tbody></table>");

        out.println("<h3>Add New Book</h3>");
        out.println("<form action='" + ctx + "/library/addBook' method='post'>");
        out.println("<label>Title</label><input name='title' required>");
        out.println("<label>Author Name</label><input name='author_name' placeholder='Existing or new author' required>");
        out.println("<label>ISBN</label><input name='isbn' required>");
        out.println("<label>Quantity</label><input name='quantity' type='number' value='1' min='1'>");
        out.println("<input type='submit' value='Add Book'>");
        out.println("</form>");

        printFooter(out);
    }

    // … (the rest of the methods for authors, loans, dynamic author creation, etc. – all are included below for completeness)

    // ==================== DYNAMIC AUTHOR ====================
    private int getOrCreateAuthor(String name) {
        String findSql = "SELECT author_id FROM authors WHERE LOWER(name) = LOWER(?)";
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement(findSql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("author_id");
        } catch (SQLException e) { e.printStackTrace(); return -1; }

        String insertSql = "INSERT INTO authors (name) VALUES (?)";
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    private void addBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String title      = req.getParameter("title");
        String authorName = req.getParameter("author_name").trim();
        String isbn       = req.getParameter("isbn");
        int qty           = Integer.parseInt(req.getParameter("quantity"));

        if (authorName.isEmpty()) {
            resp.getWriter().println("<p class='error'>Author name required.</p> <a href='" +
                    req.getContextPath() + "/library/books' class='back-link'>Back</a>");
            return;
        }

        int authorId = getOrCreateAuthor(authorName);
        if (authorId == -1) {
            resp.getWriter().println("<p class='error'>Could not create author.</p> <a href='" +
                    req.getContextPath() + "/library/books' class='back-link'>Back</a>");
            return;
        }

        String sql = "INSERT INTO books (title, author_id, isbn, quantity) VALUES (?,?,?,?)";
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setInt(2, authorId);
            ps.setString(3, isbn);
            ps.setInt(4, qty);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                resp.sendRedirect(req.getContextPath() + "/library/books");
            } else {
                resp.getWriter().println("<p class='error'>Insert failed.</p> <a href='" +
                        req.getContextPath() + "/library/books' class='back-link'>Back</a>");
            }
        } catch (SQLException e) {
            resp.getWriter().println("<p class='error'>DB error: " + e.getMessage() + "</p>");
            resp.getWriter().println("<a href='" + req.getContextPath() + "/library/books' class='back-link'>Back</a>");
        }
    }

    private void showEditBookForm(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) {
        int id = Integer.parseInt(req.getParameter("id"));
        String ctx = req.getContextPath();
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("SELECT * FROM books WHERE book_id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                printHeader(req, out, "Edit Book");
                out.println("<h2>Edit Book</h2>");
                out.println("<form action='" + ctx + "/library/updateBook' method='post'>");
                out.println("<input type='hidden' name='book_id' value='" + id + "'>");
                out.println("<label>Title</label><input name='title' value='" + rs.getString("title") + "' required>");
                out.println("<label>Author</label><select name='author_id' required>");
                try (Connection c2 = DriverManager.getConnection(URL, USER, PASSWORD);
                     Statement st2 = c2.createStatement();
                     ResultSet rs2 = st2.executeQuery("SELECT author_id, name FROM authors ORDER BY name")) {
                    while (rs2.next()) {
                        int authId = rs2.getInt("author_id");
                        String sel = (authId == rs.getInt("author_id")) ? " selected" : "";
                        out.printf("<option value='%d'%s>%s</option>", authId, sel, rs2.getString("name"));
                    }
                } catch (SQLException e) { out.println("<option disabled>Error</option>"); }
                out.println("</select>");
                out.println("<label>ISBN</label><input name='isbn' value='" + rs.getString("isbn") + "' required>");
                out.println("<label>Quantity</label><input name='quantity' type='number' value='" + rs.getInt("quantity") + "'>");
                out.println("<input type='submit' value='Update Book'>");
                out.println("</form>");
                printFooter(out);
            } else {
                printHeader(req, out, "Not Found");
                out.println("<p class='error'>Book not found.</p><a href='" + ctx + "/library/books'>Back</a>");
                printFooter(out);
            }
        } catch (SQLException e) {
            printHeader(req, out, "Error");
            out.println("<p class='error'>" + e.getMessage() + "</p>");
            printFooter(out);
        }
    }

    private void updateBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = Integer.parseInt(req.getParameter("book_id"));
        String title = req.getParameter("title");
        int authorId = Integer.parseInt(req.getParameter("author_id"));
        String isbn = req.getParameter("isbn");
        int qty = Integer.parseInt(req.getParameter("quantity"));
        String sql = "UPDATE books SET title=?, author_id=?, isbn=?, quantity=? WHERE book_id=?";
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setInt(2, authorId);
            ps.setString(3, isbn);
            ps.setInt(4, qty);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        resp.sendRedirect(req.getContextPath() + "/library/books");
    }

    private void deleteBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("DELETE FROM books WHERE book_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        resp.sendRedirect(req.getContextPath() + "/library/books");
    }

    // ==================== AUTHORS ====================
    private void showAuthors(HttpServletRequest req, PrintWriter out) {
        printHeader(req, out, "Authors");
        String ctx = req.getContextPath();
        out.println("<table><thead><tr><th>ID</th><th>Name</th><th>Actions</th></tr></thead><tbody>");
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM authors")) {
            while (rs.next()) {
                int id = rs.getInt("author_id");
                out.printf("<tr><td>%d</td><td>%s</td>" +
                    "<td>" +
                        "<a href='" + ctx + "/library/editAuthor?id=%d' class='action-btn'>Edit</a> " +
                        "<a href='" + ctx + "/library/deleteAuthor?id=%d' class='action-btn delete-btn' " +
                        "onclick=\"return confirm('Delete this author?')\">Delete</a>" +
                    "</td></tr>",
                    id, rs.getString("name"), id, id);
            }
        } catch (SQLException e) {
            out.println("<tr><td colspan='3' class='error'>Error: " + e.getMessage() + "</td></tr>");
        }
        out.println("</tbody></table>");
        out.println("<h3>Add Author</h3>");
        out.println("<form action='" + ctx + "/library/addAuthor' method='post'>");
        out.println("<label>Name</label><input name='name' required>");
        out.println("<input type='submit' value='Add Author'>");
        out.println("</form>");
        printFooter(out);
    }

    private void addAuthor(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("INSERT INTO authors (name) VALUES (?)")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        resp.sendRedirect(req.getContextPath() + "/library/authors");
    }

    private void showEditAuthorForm(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) {
        int id = Integer.parseInt(req.getParameter("id"));
        String ctx = req.getContextPath();
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("SELECT * FROM authors WHERE author_id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                printHeader(req, out, "Edit Author");
                out.println("<h2>Edit Author</h2>");
                out.println("<form action='" + ctx + "/library/updateAuthor' method='post'>");
                out.println("<input type='hidden' name='author_id' value='" + id + "'>");
                out.println("<label>Name</label><input name='name' value='" + rs.getString("name") + "' required>");
                out.println("<input type='submit' value='Update Author'>");
                out.println("</form>");
                printFooter(out);
            } else {
                printHeader(req, out, "Not Found");
                out.println("<p class='error'>Author not found.</p><a href='" + ctx + "/library/authors'>Back</a>");
                printFooter(out);
            }
        } catch (SQLException e) {
            printHeader(req, out, "Error");
            out.println("<p class='error'>" + e.getMessage() + "</p>");
            printFooter(out);
        }
    }

    private void updateAuthor(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = Integer.parseInt(req.getParameter("author_id"));
        String name = req.getParameter("name");
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("UPDATE authors SET name = ? WHERE author_id = ?")) {
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        resp.sendRedirect(req.getContextPath() + "/library/authors");
    }

    private void deleteAuthor(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("DELETE FROM authors WHERE author_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        resp.sendRedirect(req.getContextPath() + "/library/authors");
    }

    // ==================== LOANS (with member dropdown) ====================
    private void showLoans(HttpServletRequest req, PrintWriter out) {
        printHeader(req, out, "Loans");
        String ctx = req.getContextPath();

        // Pre-fill book ID if passed
        String preBookId = req.getParameter("bookId");
        String bookIdVal = (preBookId != null) ? preBookId : "";

        out.println("<table><thead><tr><th>Loan ID</th><th>Book</th><th>Member</th><th>Loan Date</th><th>Due Date</th><th>Status</th></tr></thead><tbody>");
        String sql = "SELECT l.loan_id, b.title, m.name AS member_name, l.loan_date, l.due_date, l.status " +
                     "FROM loans l " +
                     "JOIN books b ON l.book_id = b.book_id " +
                     "JOIN members m ON l.member_id = m.member_id " +
                     "ORDER BY l.status, l.due_date";
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                out.printf("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                    rs.getInt("loan_id"), rs.getString("title"),
                    rs.getString("member_name"), rs.getDate("loan_date"),
                    rs.getDate("due_date"), rs.getString("status"));
            }
        } catch (SQLException e) {
            out.println("<tr><td colspan='6' class='error'>Error: " + e.getMessage() + "</td></tr>");
        }
        out.println("</tbody></table>");

        // Borrow form (member dropdown)
        out.println("<h3>Borrow a Book</h3>");
        out.println("<form action='" + ctx + "/library/borrow' method='post'>");
        out.println("<label>Book ID</label><input name='book_id' type='number' value='" + bookIdVal + "' required>");
        out.println("<label>Member</label><select name='member_id' required>");
        out.println("<option value=''>-- Select Member --</option>");
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT member_id, name FROM members ORDER BY name")) {
            while (rs.next()) {
                out.printf("<option value='%d'>%s</option>", rs.getInt("member_id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            out.println("<option disabled>Error loading members</option>");
        }
        out.println("</select>");
        out.println("<input type='submit' value='Borrow'>");
        out.println("</form>");

        // Return form
        out.println("<h3>Return a Book</h3>");
        out.println("<form action='" + ctx + "/library/return' method='post'>");
        out.println("<label>Loan ID</label><input name='loan_id' type='number' required>");
        out.println("<input type='submit' value='Return'>");
        out.println("</form>");

        printFooter(out);
    }

    private void borrowBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int bookId   = Integer.parseInt(req.getParameter("book_id"));
        int memberId = Integer.parseInt(req.getParameter("member_id"));

        // Check quantity
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("SELECT quantity FROM books WHERE book_id = ?")) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("quantity") <= 0) {
                    resp.getWriter().println("<p class='error'>No copies available. <a href='" +
                            req.getContextPath() + "/library/loans' class='back-link'>Back</a></p>");
                    return;
                }
            } else {
                resp.getWriter().println("<p class='error'>Book not found. <a href='" +
                        req.getContextPath() + "/library/loans' class='back-link'>Back</a></p>");
                return;
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Transaction: reduce qty, insert loan
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD)) {
            c.setAutoCommit(false);
            try {
                PreparedStatement reduce = c.prepareStatement("UPDATE books SET quantity = quantity - 1 WHERE book_id = ?");
                reduce.setInt(1, bookId);
                reduce.executeUpdate();

                LocalDate today = LocalDate.now();
                LocalDate due = today.plusDays(14);
                PreparedStatement loanPs = c.prepareStatement(
                    "INSERT INTO loans (book_id, member_id, loan_date, due_date) VALUES (?,?,?,?)");
                loanPs.setInt(1, bookId);
                loanPs.setInt(2, memberId);
                loanPs.setDate(3, Date.valueOf(today));
                loanPs.setDate(4, Date.valueOf(due));
                loanPs.executeUpdate();

                c.commit();
            } catch (SQLException e) {
                c.rollback();
                resp.getWriter().println("<p class='error'>Error: " + e.getMessage() + " <a href='" +
                        req.getContextPath() + "/library/loans' class='back-link'>Back</a></p>");
                return;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        resp.sendRedirect(req.getContextPath() + "/library/loans");
    }

    private void returnBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int loanId = Integer.parseInt(req.getParameter("loan_id"));

        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("SELECT status, book_id FROM loans WHERE loan_id = ?")) {
            ps.setInt(1, loanId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if ("returned".equals(rs.getString("status"))) {
                    resp.getWriter().println("<p class='error'>Already returned. <a href='" +
                            req.getContextPath() + "/library/loans' class='back-link'>Back</a></p>");
                    return;
                }
                int bookId = rs.getInt("book_id");

                c.setAutoCommit(false);
                try {
                    PreparedStatement updateLoan = c.prepareStatement(
                        "UPDATE loans SET return_date = ?, status = 'returned' WHERE loan_id = ?");
                    updateLoan.setDate(1, Date.valueOf(LocalDate.now()));
                    updateLoan.setInt(2, loanId);
                    updateLoan.executeUpdate();

                    PreparedStatement incQty = c.prepareStatement("UPDATE books SET quantity = quantity + 1 WHERE book_id = ?");
                    incQty.setInt(1, bookId);
                    incQty.executeUpdate();

                    c.commit();
                } catch (SQLException e) {
                    c.rollback();
                    resp.getWriter().println("<p class='error'>Error: " + e.getMessage() + " <a href='" +
                            req.getContextPath() + "/library/loans' class='back-link'>Back</a></p>");
                    return;
                }
            } else {
                resp.getWriter().println("<p class='error'>Loan ID not found. <a href='" +
                        req.getContextPath() + "/library/loans' class='back-link'>Back</a></p>");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        resp.sendRedirect(req.getContextPath() + "/library/loans");
    }
}