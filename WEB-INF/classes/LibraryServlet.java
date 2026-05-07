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

    // ==================== HEADER & FOOTER (Tailwind + Background Image) ====================
    private void printHeader(HttpServletRequest req, PrintWriter out, String title) {
        String ctx = req.getContextPath();
        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>" + title + "</title>");

        // Tailwind CSS CDN
        out.println("<script src='https://cdn.tailwindcss.com'></script>");
        out.println("<script>");
        out.println("tailwind.config = {");
        out.println("  theme: {");
        out.println("    extend: {");
        out.println("      fontFamily: { sans: ['Inter', 'sans-serif'] },");
        out.println("      animation: { 'fade-in': 'fadeIn 0.4s ease-out', 'slide-up': 'slideUp 0.4s ease-out' },");
        out.println("      keyframes: {");
        out.println("        fadeIn: { '0%': { opacity: '0' }, '100%': { opacity: '1' } },");
        out.println("        slideUp: { '0%': { opacity: '0', transform: 'translateY(20px)' }, '100%': { opacity: '1', transform: 'translateY(0)' } }");
        out.println("      }");
        out.println("    }");
        out.println("  }");
        out.println("}");
        out.println("</script>");

        // Google Fonts
        out.println("<link rel='preconnect' href='https://fonts.googleapis.com'>");
        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap' rel='stylesheet'>");

        // Custom background and overlay
        out.println("<style>");
        out.println("body {");
        out.println("  background: url('" + ctx + "/images/bg.jpg') no-repeat center center fixed;");
        out.println("  background-size: cover;");
        out.println("  position: relative;");
        out.println("}");
        out.println("body::before {");
        out.println("  content: '';");
        out.println("  position: fixed;");
        out.println("  top: 0; left: 0;");
        out.println("  width: 100%; height: 100%;");
        out.println("  background-color: rgba(0,0,0,0.45);");
        out.println("  z-index: -1;");
        out.println("}");
        out.println("</style>");

        out.println("</head><body class='font-sans antialiased text-white'>");

        // Navigation
        out.println("<nav class='bg-gray-900/80 backdrop-blur-md shadow-lg'>");
        out.println("<div class='max-w-7xl mx-auto px-4'>");
        out.println("<div class='flex items-center justify-between h-16'>");
        out.println("<div class='flex items-baseline space-x-4'>");
        out.println("<a href='" + ctx + "/library/' class='text-gray-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200'>Home</a>");
        out.println("<a href='" + ctx + "/library/books' class='text-gray-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200'>Books</a>");
        out.println("<a href='" + ctx + "/library/authors' class='text-gray-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200'>Authors</a>");
        out.println("<a href='" + ctx + "/library/members' class='text-gray-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200'>Members</a>");
        out.println("<a href='" + ctx + "/library/loans' class='text-gray-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200'>Loans</a>");
        out.println("</div></div></div></nav>");

        // Main container
        out.println("<div class='max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8 animate-fade-in'>");
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
                out.println("<div class='text-center py-12'><h2 class='text-3xl font-bold'>Page not found</h2>");
                out.println("<a href='" + req.getContextPath() + "/library/' class='mt-4 inline-block text-blue-400 hover:underline'>← Back to Home</a></div>");
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
        out.println("<div class='text-center'>");
        out.println("<h1 class='text-5xl font-bold mb-4 drop-shadow-lg'>📚 Library Management System</h1>");
        out.println("<p class='text-xl text-gray-200'>Organize. Manage. Empower.</p>");
        out.println("</div>");
        printFooter(out);
    }

    // ==================== MEMBERS ====================
    private void showMembers(HttpServletRequest req, PrintWriter out) {
        printHeader(req, out, "Members");
        String ctx = req.getContextPath();

        out.println("<div class='bg-white shadow-lg rounded-lg overflow-hidden mb-8 text-gray-800'>");
        out.println("<table class='min-w-full divide-y divide-gray-200'>");
        out.println("<thead class='bg-gray-50'><tr>");
        out.println("<th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>ID</th>");
        out.println("<th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Name</th>");
        out.println("<th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Email</th>");
        out.println("<th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>Actions</th>");
        out.println("</tr></thead><tbody class='bg-white divide-y divide-gray-200'>");

        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM members ORDER BY member_id")) {
            while (rs.next()) {
                int id = rs.getInt("member_id");
                out.printf("<tr class='hover:bg-gray-50 transition-colors duration-150'>" +
                    "<td class='px-6 py-4 whitespace-nowrap'>%d</td>" +
                    "<td class='px-6 py-4 whitespace-nowrap'>%s</td>" +
                    "<td class='px-6 py-4 whitespace-nowrap'>%s</td>" +
                    "<td class='px-6 py-4 whitespace-nowrap text-sm'>" +
                        "<a href='" + ctx + "/library/editMember?id=%d' class='text-indigo-600 hover:text-indigo-900 mr-3'>Edit</a>" +
                        "<a href='" + ctx + "/library/deleteMember?id=%d' class='text-red-600 hover:text-red-900' onclick=\"return confirm('Delete this member?')\">Delete</a>" +
                    "</td></tr>", id, rs.getString("name"), rs.getString("email"), id, id);
            }
        } catch (SQLException e) {
            out.println("<tr><td colspan='4' class='px-6 py-4 text-red-500'>Error: " + e.getMessage() + "</td></tr>");
        }
        out.println("</tbody></table></div>");

        // Add Member form
        out.println("<div class='bg-white shadow-lg rounded-lg p-6 max-w-md text-gray-800'>");
        out.println("<h3 class='text-lg font-medium mb-4'>Add New Member</h3>");
        out.println("<form action='" + ctx + "/library/addMember' method='post' class='space-y-4'>");
        out.println("<div><label class='block text-sm font-medium text-gray-700'>Name</label><input name='name' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
        out.println("<div><label class='block text-sm font-medium text-gray-700'>Email</label><input name='email' type='email' class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
        out.println("<button type='submit' class='inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 transition-colors duration-200'>Add Member</button>");
        out.println("</form></div>");
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
                out.println("<div class='max-w-md text-gray-800'>");
                out.println("<h2 class='text-2xl font-bold mb-4'>Edit Member</h2>");
                out.println("<form action='" + ctx + "/library/updateMember' method='post' class='bg-white shadow rounded-lg p-6 space-y-4'>");
                out.println("<input type='hidden' name='member_id' value='" + id + "'>");
                out.println("<div><label class='block text-sm font-medium text-gray-700'>Name</label><input name='name' value='" + rs.getString("name") + "' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
                out.println("<div><label class='block text-sm font-medium text-gray-700'>Email</label><input name='email' value='" + rs.getString("email") + "' class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
                out.println("<button type='submit' class='inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 transition-colors duration-200'>Update Member</button>");
                out.println("</form></div>");
                printFooter(out);
            } else {
                printHeader(req, out, "Not Found");
                out.println("<p class='text-red-500'>Member not found. <a href='" + ctx + "/library/members' class='text-blue-400 hover:underline'>Back</a></p>");
                printFooter(out);
            }
        } catch (SQLException e) {
            printHeader(req, out, "Error");
            out.println("<p class='text-red-500'>" + e.getMessage() + "</p>");
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

    // ==================== BOOKS (Dynamic Author, Availability, Borrow Link) ====================
    private void showBooks(HttpServletRequest req, PrintWriter out) {
        printHeader(req, out, "Books");
        String ctx = req.getContextPath();

        String search = req.getParameter("search");
        out.println("<form method='get' action='" + ctx + "/library/books' class='flex gap-2 mb-6'>");
        out.println("<input type='text' name='search' placeholder='Search by title...' value='" + (search != null ? search : "") + "' class='flex-1 rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm text-gray-800'>");
        out.println("<button type='submit' class='inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 transition-colors duration-200'>Search</button>");
        out.println("</form>");

        out.println("<div class='bg-white shadow-lg rounded-lg overflow-hidden mb-8 text-gray-800'>");
        out.println("<table class='min-w-full divide-y divide-gray-200'>");
        out.println("<thead class='bg-gray-50'><tr>");
        out.println("<th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>ID</th>");
        out.println("<th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Title</th>");
        out.println("<th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Author</th>");
        out.println("<th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>ISBN</th>");
        out.println("<th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Qty</th>");
        out.println("<th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Available</th>");
        out.println("<th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Actions</th>");
        out.println("</tr></thead><tbody class='bg-white divide-y divide-gray-200'>");

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
                String available = (qty > 0)
                        ? "<span class='inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800'>Yes</span>"
                        : "<span class='inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800'>No</span>";
                String borrowLink = (qty > 0)
                        ? "<a href='" + ctx + "/library/loans?bookId=" + id + "' class='text-indigo-600 hover:text-indigo-900 mr-3'>Borrow</a>"
                        : "";

                out.printf("<tr class='hover:bg-gray-50 transition-colors duration-150'>" +
                    "<td class='px-6 py-4'>%d</td>" +
                    "<td class='px-6 py-4 font-medium'>%s</td>" +
                    "<td class='px-6 py-4'>%s</td>" +
                    "<td class='px-6 py-4'>%s</td>" +
                    "<td class='px-6 py-4'>%d</td>" +
                    "<td class='px-6 py-4'>%s</td>" +
                    "<td class='px-6 py-4 text-sm space-x-2'>" +
                        borrowLink +
                        "<a href='" + ctx + "/library/editBook?id=%d' class='text-indigo-600 hover:text-indigo-900'>Edit</a>" +
                        "<a href='" + ctx + "/library/deleteBook?id=%d' class='text-red-600 hover:text-red-900' onclick=\"return confirm('Delete this book?')\">Delete</a>" +
                    "</td></tr>", id, rs.getString("title"), rs.getString("author"), rs.getString("isbn"), qty, available, id, id);
            }
        } catch (SQLException e) {
            out.println("<tr><td colspan='7' class='px-6 py-4 text-red-500'>Error: " + e.getMessage() + "</td></tr>");
        }
        out.println("</tbody></table></div>");

        // Add Book form
        out.println("<div class='bg-white shadow-lg rounded-lg p-6 max-w-md text-gray-800'>");
        out.println("<h3 class='text-lg font-medium mb-4'>Add New Book</h3>");
        out.println("<form action='" + ctx + "/library/addBook' method='post' class='space-y-4'>");
        out.println("<div><label class='block text-sm font-medium text-gray-700'>Title</label><input name='title' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
        out.println("<div><label class='block text-sm font-medium text-gray-700'>Author Name</label><input name='author_name' placeholder='Existing or new author' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
        out.println("<div><label class='block text-sm font-medium text-gray-700'>ISBN</label><input name='isbn' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
        out.println("<div><label class='block text-sm font-medium text-gray-700'>Quantity</label><input name='quantity' type='number' value='1' min='1' class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
        out.println("<button type='submit' class='inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 transition-colors duration-200'>Add Book</button>");
        out.println("</form></div>");
        printFooter(out);
    }

    // Dynamic author handling
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
            resp.getWriter().println("<p class='text-red-500'>Author name required. <a href='" + req.getContextPath() + "/library/books' class='text-blue-400 hover:underline'>Back</a></p>");
            return;
        }
        int authorId = getOrCreateAuthor(authorName);
        if (authorId == -1) {
            resp.getWriter().println("<p class='text-red-500'>Could not create author. <a href='" + req.getContextPath() + "/library/books' class='text-blue-400 hover:underline'>Back</a></p>");
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
            if (rows > 0) resp.sendRedirect(req.getContextPath() + "/library/books");
            else resp.getWriter().println("<p class='text-red-500'>Insert failed. <a href='" + req.getContextPath() + "/library/books' class='text-blue-400 hover:underline'>Back</a></p>");
        } catch (SQLException e) {
            resp.getWriter().println("<p class='text-red-500'>DB error: " + e.getMessage() + "</p>");
            resp.getWriter().println("<a href='" + req.getContextPath() + "/library/books' class='text-blue-400 hover:underline'>Back</a>");
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
                out.println("<div class='max-w-md text-gray-800'>");
                out.println("<h2 class='text-2xl font-bold mb-4'>Edit Book</h2>");
                out.println("<form action='" + ctx + "/library/updateBook' method='post' class='bg-white shadow rounded-lg p-6 space-y-4'>");
                out.println("<input type='hidden' name='book_id' value='" + id + "'>");
                out.println("<div><label class='block text-sm font-medium text-gray-700'>Title</label><input name='title' value='" + rs.getString("title") + "' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");

                // Author dropdown
                out.println("<div><label class='block text-sm font-medium text-gray-700'>Author</label><select name='author_id' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'>");
                try (Connection c2 = DriverManager.getConnection(URL, USER, PASSWORD);
                     Statement st2 = c2.createStatement();
                     ResultSet rs2 = st2.executeQuery("SELECT author_id, name FROM authors ORDER BY name")) {
                    while (rs2.next()) {
                        int authId = rs2.getInt("author_id");
                        String sel = (authId == rs.getInt("author_id")) ? " selected" : "";
                        out.printf("<option value='%d'%s>%s</option>", authId, sel, rs2.getString("name"));
                    }
                } catch (SQLException e) { out.println("<option disabled>Error</option>"); }
                out.println("</select></div>");

                out.println("<div><label class='block text-sm font-medium text-gray-700'>ISBN</label><input name='isbn' value='" + rs.getString("isbn") + "' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
                out.println("<div><label class='block text-sm font-medium text-gray-700'>Quantity</label><input name='quantity' type='number' value='" + rs.getInt("quantity") + "' class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
                out.println("<button type='submit' class='inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 transition-colors duration-200'>Update Book</button>");
                out.println("</form></div>");
                printFooter(out);
            } else {
                printHeader(req, out, "Not Found");
                out.println("<p class='text-red-500'>Book not found. <a href='" + ctx + "/library/books' class='text-blue-400 hover:underline'>Back</a></p>");
                printFooter(out);
            }
        } catch (SQLException e) {
            printHeader(req, out, "Error");
            out.println("<p class='text-red-500'>" + e.getMessage() + "</p>");
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

    // ==================== AUTHORS (similar Tailwind styling) ====================
    private void showAuthors(HttpServletRequest req, PrintWriter out) {
        printHeader(req, out, "Authors");
        String ctx = req.getContextPath();

        out.println("<div class='bg-white shadow-lg rounded-lg overflow-hidden mb-8 text-gray-800'>");
        out.println("<table class='min-w-full divide-y divide-gray-200'>");
        out.println("<thead class='bg-gray-50'><tr><th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>ID</th><th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Name</th><th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Actions</th></tr></thead><tbody class='divide-y divide-gray-200'>");
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM authors")) {
            while (rs.next()) {
                int id = rs.getInt("author_id");
                out.printf("<tr class='hover:bg-gray-50'><td class='px-6 py-4'>%d</td><td class='px-6 py-4'>%s</td>" +
                    "<td class='px-6 py-4 text-sm'><a href='" + ctx + "/library/editAuthor?id=%d' class='text-indigo-600 hover:text-indigo-900 mr-3'>Edit</a>" +
                    "<a href='" + ctx + "/library/deleteAuthor?id=%d' class='text-red-600 hover:text-red-900' onclick=\"return confirm('Delete?')\">Delete</a></td></tr>", id, rs.getString("name"), id, id);
            }
        } catch (SQLException e) { out.println("<tr><td colspan='3' class='px-6 py-4 text-red-500'>Error: " + e.getMessage() + "</td></tr>"); }
        out.println("</tbody></table></div>");

        out.println("<div class='bg-white shadow-lg rounded-lg p-6 max-w-md text-gray-800'>");
        out.println("<h3 class='text-lg font-medium mb-4'>Add Author</h3>");
        out.println("<form action='" + ctx + "/library/addAuthor' method='post' class='space-y-4'>");
        out.println("<div><label class='block text-sm font-medium text-gray-700'>Name</label><input name='name' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
        out.println("<button type='submit' class='inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 transition-colors duration-200'>Add Author</button>");
        out.println("</form></div>");
        printFooter(out);
    }

    // Author edit/add/delete methods
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
                out.println("<div class='max-w-md text-gray-800'>");
                out.println("<h2 class='text-2xl font-bold mb-4'>Edit Author</h2>");
                out.println("<form action='" + ctx + "/library/updateAuthor' method='post' class='bg-white shadow rounded-lg p-6 space-y-4'>");
                out.println("<input type='hidden' name='author_id' value='" + id + "'>");
                out.println("<div><label class='block text-sm font-medium text-gray-700'>Name</label><input name='name' value='" + rs.getString("name") + "' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
                out.println("<button type='submit' class='inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 transition-colors duration-200'>Update Author</button>");
                out.println("</form></div>");
                printFooter(out);
            } else {
                printHeader(req, out, "Not Found");
                out.println("<p class='text-red-500'>Author not found. <a href='" + ctx + "/library/authors' class='text-blue-400 hover:underline'>Back</a></p>");
                printFooter(out);
            }
        } catch (SQLException e) {
            printHeader(req, out, "Error");
            out.println("<p class='text-red-500'>" + e.getMessage() + "</p>");
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

        String preBookId = req.getParameter("bookId");
        String bookIdVal = (preBookId != null) ? preBookId : "";

        out.println("<div class='bg-white shadow-lg rounded-lg overflow-hidden mb-8 text-gray-800'>");
        out.println("<table class='min-w-full divide-y divide-gray-200'>");
        out.println("<thead class='bg-gray-50'><tr><th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Loan ID</th><th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Book</th><th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Member</th><th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Loan Date</th><th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Due Date</th><th class='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase'>Status</th></tr></thead><tbody class='divide-y divide-gray-200'>");
        String sql = "SELECT l.loan_id, b.title, m.name AS member_name, l.loan_date, l.due_date, l.status " +
                     "FROM loans l " +
                     "JOIN books b ON l.book_id = b.book_id " +
                     "JOIN members m ON l.member_id = m.member_id " +
                     "ORDER BY l.status, l.due_date";
        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                out.printf("<tr class='hover:bg-gray-50'><td class='px-6 py-4'>%d</td><td class='px-6 py-4'>%s</td><td class='px-6 py-4'>%s</td><td class='px-6 py-4'>%s</td><td class='px-6 py-4'>%s</td><td class='px-6 py-4'>%s</td></tr>",
                    rs.getInt("loan_id"), rs.getString("title"),
                    rs.getString("member_name"), rs.getDate("loan_date"),
                    rs.getDate("due_date"), rs.getString("status"));
            }
        } catch (SQLException e) {
            out.println("<tr><td colspan='6' class='px-6 py-4 text-red-500'>Error: " + e.getMessage() + "</td></tr>");
        }
        out.println("</tbody></table></div>");

        // Borrow form with member dropdown
        out.println("<div class='bg-white shadow-lg rounded-lg p-6 max-w-md text-gray-800 mb-6'>");
        out.println("<h3 class='text-lg font-medium mb-4'>Borrow a Book</h3>");
        out.println("<form action='" + ctx + "/library/borrow' method='post' class='space-y-4'>");
        out.println("<div><label class='block text-sm font-medium text-gray-700'>Book ID</label><input name='book_id' type='number' value='" + bookIdVal + "' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
        out.println("<div><label class='block text-sm font-medium text-gray-700'>Member</label><select name='member_id' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'>");
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
        out.println("</select></div>");
        out.println("<button type='submit' class='inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 transition-colors duration-200'>Borrow</button>");
        out.println("</form></div>");

        // Return form
        out.println("<div class='bg-white shadow-lg rounded-lg p-6 max-w-md text-gray-800'>");
        out.println("<h3 class='text-lg font-medium mb-4'>Return a Book</h3>");
        out.println("<form action='" + ctx + "/library/return' method='post' class='space-y-4'>");
        out.println("<div><label class='block text-sm font-medium text-gray-700'>Loan ID</label><input name='loan_id' type='number' required class='mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'></div>");
        out.println("<button type='submit' class='inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 transition-colors duration-200'>Return</button>");
        out.println("</form></div>");

        printFooter(out);
    }

    private void borrowBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int bookId   = Integer.parseInt(req.getParameter("book_id"));
        int memberId = Integer.parseInt(req.getParameter("member_id"));

        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = c.prepareStatement("SELECT quantity FROM books WHERE book_id = ?")) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("quantity") <= 0) {
                    resp.getWriter().println("<p class='text-red-500'>No copies available. <a href='" + req.getContextPath() + "/library/loans' class='text-blue-400 hover:underline'>Back</a></p>");
                    return;
                }
            } else {
                resp.getWriter().println("<p class='text-red-500'>Book not found. <a href='" + req.getContextPath() + "/library/loans' class='text-blue-400 hover:underline'>Back</a></p>");
                return;
            }
        } catch (SQLException e) { e.printStackTrace(); }

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
                resp.getWriter().println("<p class='text-red-500'>Error: " + e.getMessage() + " <a href='" + req.getContextPath() + "/library/loans' class='text-blue-400 hover:underline'>Back</a></p>");
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
                    resp.getWriter().println("<p class='text-red-500'>Already returned. <a href='" + req.getContextPath() + "/library/loans' class='text-blue-400 hover:underline'>Back</a></p>");
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
                    resp.getWriter().println("<p class='text-red-500'>Error: " + e.getMessage() + " <a href='" + req.getContextPath() + "/library/loans' class='text-blue-400 hover:underline'>Back</a></p>");
                    return;
                }
            } else {
                resp.getWriter().println("<p class='text-red-500'>Loan ID not found. <a href='" + req.getContextPath() + "/library/loans' class='text-blue-400 hover:underline'>Back</a></p>");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        resp.sendRedirect(req.getContextPath() + "/library/loans");
    }
}