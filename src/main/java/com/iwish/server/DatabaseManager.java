package com.iwish.server;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.iwish.common.HashUtil;
import com.iwish.common.Response;

public class DatabaseManager {
    private final Properties props = new Properties();

    public DatabaseManager() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/db.properties")) {
            if (in == null) throw new IllegalStateException("db.properties not found");
            props.load(in);
        }
        Class.forName("com.mysql.cj.jdbc.Driver");
        initialize();
    }

    private Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("IWISH_DB_URL", props.getProperty("db.url"));
        String user = System.getenv().getOrDefault("IWISH_DB_USER", props.getProperty("db.user"));
        String pass = System.getenv().getOrDefault("IWISH_DB_PASSWORD", props.getProperty("db.password"));

        if (url == null || url.isBlank() || user == null || user.isBlank() || pass == null || pass.isBlank())
            throw new SQLException("Database configuration is incomplete");

        return DriverManager.getConnection(url, user, pass);
    }

    private void initialize() throws Exception {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users(id INT PRIMARY KEY AUTO_INCREMENT,name VARCHAR(100) NOT NULL,email VARCHAR(150) NOT NULL UNIQUE,password_hash VARCHAR(255) NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            try { s.executeUpdate("ALTER TABLE users MODIFY password_hash VARCHAR(255) NOT NULL"); } catch (SQLException ignored) { }
            s.executeUpdate("CREATE TABLE IF NOT EXISTS friendships(id INT PRIMARY KEY AUTO_INCREMENT,sender_id INT NOT NULL,receiver_id INT NOT NULL,status VARCHAR(20) NOT NULL DEFAULT 'PENDING',created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,UNIQUE KEY uq_friend_pair(sender_id,receiver_id),FOREIGN KEY(sender_id) REFERENCES users(id) ON DELETE CASCADE,FOREIGN KEY(receiver_id) REFERENCES users(id) ON DELETE CASCADE)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS items(id INT PRIMARY KEY AUTO_INCREMENT,name VARCHAR(150) NOT NULL,description VARCHAR(500),default_price DECIMAL(12,2) NOT NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS wishlist_items(id INT PRIMARY KEY AUTO_INCREMENT,owner_id INT NOT NULL,item_id INT NULL,custom_name VARCHAR(150) NOT NULL,description VARCHAR(500),price DECIMAL(12,2) NOT NULL,collected_amount DECIMAL(12,2) NOT NULL DEFAULT 0,status VARCHAR(20) NOT NULL DEFAULT 'OPEN',created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE,FOREIGN KEY(item_id) REFERENCES items(id) ON DELETE SET NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS contributions(id INT PRIMARY KEY AUTO_INCREMENT,wishlist_item_id INT NOT NULL,buyer_id INT NOT NULL,amount DECIMAL(12,2) NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(wishlist_item_id) REFERENCES wishlist_items(id) ON DELETE CASCADE,FOREIGN KEY(buyer_id) REFERENCES users(id) ON DELETE CASCADE)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS notifications(id INT PRIMARY KEY AUTO_INCREMENT,user_id INT NOT NULL,message VARCHAR(500) NOT NULL,type VARCHAR(40) NOT NULL,is_read BOOLEAN NOT NULL DEFAULT FALSE,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
            seedItems(c);
        }
    }

    private void seedItems(Connection c) throws SQLException {
        String sql = "INSERT INTO items(name,description,default_price) SELECT ?,?,? FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM items WHERE name=?)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            String[][] x = {
                {"PlayStation 5","Gaming console","30000"},
                {"Apple AirPods Pro","Wireless earbuds","12000"},
                {"Smart Watch","Modern smartwatch","7000"},
                {"Laptop","Portable computer","45000"},
                {"Perfume","Premium perfume","3500"}
            };
            for (String[] i : x) {
                p.setString(1,i[0]);
                p.setString(2,i[1]);
                p.setBigDecimal(3,new BigDecimal(i[2]));
                p.setString(4,i[0]);
                p.executeUpdate();
            }
        }
    }

    private List<Map<String,Object>> rows(ResultSet rs) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();

        while (rs.next()) {
            Map<String,Object> m = new LinkedHashMap<>();
            for (int i = 1; i <= md.getColumnCount(); i++)
                m.put(md.getColumnLabel(i), rs.getObject(i));
            list.add(m);
        }

        return list;
    }

    private Response dataResponse(String message, Object data) {
        Response r = Response.ok(message);
        r.data = data;
        return r;
    }

    private Response rowsResponse(String message, List<Map<String,Object>> rows) {
        Response r = Response.ok(message);
        r.rows = rows;
        return r;
    }

    public Response register(Map<String,Object> d) {
        String name = String.valueOf(d.getOrDefault("name","")).trim();
        String email = String.valueOf(d.getOrDefault("email","")).trim().toLowerCase();
        String password = String.valueOf(d.getOrDefault("password",""));

        if (name.isBlank() || name.length() > 100 || email.isBlank() || email.length() > 150
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || password.length() < 8)
            return Response.fail("Enter a valid name, email, and password (8+ characters).");

        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement("INSERT INTO users(name,email,password_hash) VALUES(?,?,?)")) {

            p.setString(1,name);
            p.setString(2,email);
            p.setString(3,HashUtil.passwordHash(password));
            p.executeUpdate();

            return Response.ok("Registration successful.");
        } catch (SQLIntegrityConstraintViolationException e) {
            return Response.fail("Email already exists.");
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response login(Map<String,Object> d) {
        String email = String.valueOf(d.getOrDefault("email","")).trim().toLowerCase();
        String password = String.valueOf(d.getOrDefault("password",""));

        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement("SELECT id,name,email,password_hash FROM users WHERE email=?")) {

            p.setString(1,email);

            try (ResultSet r = p.executeQuery()) {
                if (!r.next() || !HashUtil.verify(password, r.getString("password_hash")))
                    return Response.fail("Invalid email or password.");

                Map<String,Object> user = new LinkedHashMap<>();
                user.put("id",r.getInt("id"));
                user.put("name",r.getString("name"));
                user.put("email",r.getString("email"));

                return dataResponse("Login successful.",user);
            }
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response users(Map<String,Object> d) {
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT id,name,email FROM users WHERE id<>? AND name LIKE ? ORDER BY name")) {

            int id = ((Number)d.get("userId")).intValue();
            p.setInt(1,id);
            p.setString(2,"%"+String.valueOf(d.getOrDefault("search",""))+"%");

            try (ResultSet r = p.executeQuery()) {
                return rowsResponse("Users",rows(r));
            }
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response friends(Map<String,Object> d) {
        String sql = "SELECT u.id,u.name,u.email,f.status," +
                "CASE WHEN f.sender_id=? THEN 'SENT' ELSE 'RECEIVED' END direction " +
                "FROM friendships f JOIN users u ON u.id=CASE WHEN f.sender_id=? " +
                "THEN f.receiver_id ELSE f.sender_id END " +
                "WHERE f.sender_id=? OR f.receiver_id=? ORDER BY u.name";

        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            int id = ((Number)d.get("userId")).intValue();

            p.setInt(1,id);
            p.setInt(2,id);
            p.setInt(3,id);
            p.setInt(4,id);

            try (ResultSet r = p.executeQuery()) {
                return rowsResponse("Friends",rows(r));
            }
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response addFriend(Map<String,Object> d) {
        int me = ((Number)d.get("userId")).intValue();
        int other = ((Number)d.get("otherId")).intValue();

        if (me == other)
            return Response.fail("You cannot add yourself.");

        try (Connection c = getConnection()) {
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT id FROM friendships WHERE (sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?)")) {

                p.setInt(1,me);
                p.setInt(2,other);
                p.setInt(3,other);
                p.setInt(4,me);

                try (ResultSet r = p.executeQuery()) {
                    if (r.next())
                        return Response.fail("A friendship request already exists.");
                }
            }

            try (PreparedStatement p = c.prepareStatement(
                    "INSERT INTO friendships(sender_id,receiver_id,status) VALUES(?,?,'PENDING')")) {

                p.setInt(1,me);
                p.setInt(2,other);
                p.executeUpdate();
            }

            notify(c,other,"You received a new friend request.","FRIEND_REQUEST");

            return Response.ok("Friend request sent.");
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response friendDecision(Map<String,Object> d) {
        int me = ((Number)d.get("userId")).intValue();
        int other = ((Number)d.get("otherId")).intValue();
        String status = String.valueOf(d.get("status"));

        if (!status.equals("ACCEPTED") && !status.equals("DECLINED"))
            return Response.fail("Invalid decision.");

        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(
                     "UPDATE friendships SET status=? WHERE sender_id=? AND receiver_id=? AND status='PENDING'")) {

            p.setString(1,status);
            p.setInt(2,other);
            p.setInt(3,me);

            if (p.executeUpdate() == 0)
                return Response.fail("Request not found.");

            if (status.equals("ACCEPTED"))
                notify(c,other,"Your friend request was accepted.","FRIEND_ACCEPTED");

            return Response.ok("Friend request updated.");
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response removeFriend(Map<String,Object> d) {
        int me = ((Number)d.get("userId")).intValue();
        int other = ((Number)d.get("otherId")).intValue();

        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(
                     "DELETE FROM friendships WHERE (sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?)")) {

            p.setInt(1,me);
            p.setInt(2,other);
            p.setInt(3,other);
            p.setInt(4,me);
            p.executeUpdate();

            return Response.ok("Friend removed.");
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response items() {
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT id,name,description,default_price FROM items ORDER BY name");
             ResultSet r = p.executeQuery()) {

            return rowsResponse("Items",rows(r));
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response myWishlist(Map<String,Object> d) {
        return wishlistFor(((Number)d.get("userId")).intValue());
    }

    private Response wishlistFor(int ownerId) {
        String sql = "SELECT w.id,w.owner_id,w.item_id,w.custom_name,w.description," +
                "w.price,w.collected_amount,w.status," +
                "(w.price-w.collected_amount) remaining " +
                "FROM wishlist_items w WHERE w.owner_id=? ORDER BY w.created_at DESC";

        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1,ownerId);

            try (ResultSet r = p.executeQuery()) {
                return rowsResponse("Wish list",rows(r));
            }
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response friendWishlist(Map<String,Object> d) {
        int me = ((Number)d.get("userId")).intValue();
        int owner = ((Number)d.get("ownerId")).intValue();

        String sql = "SELECT COUNT(*) FROM friendships WHERE status='ACCEPTED' " +
                "AND ((sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?))";

        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1,me);
            p.setInt(2,owner);
            p.setInt(3,owner);
            p.setInt(4,me);

            try (ResultSet r = p.executeQuery()) {
                r.next();

                if (r.getInt(1) == 0)
                    return Response.fail("You can only view an accepted friend's wish list.");
            }

            return wishlistFor(owner);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response addWish(Map<String,Object> d) {
        try {
            BigDecimal price = new BigDecimal(String.valueOf(d.get("price")));
            String name = String.valueOf(d.getOrDefault("name", "")).trim();
            String description = String.valueOf(d.getOrDefault("description", ""));

            if (name.isBlank() || name.length() > 150 || description.length() > 500 || price.compareTo(BigDecimal.ZERO) <= 0 || price.scale() > 2)
                return Response.fail("Enter a valid name, description and positive price.");

            try (Connection c = getConnection();
                 PreparedStatement p = c.prepareStatement(
                         "INSERT INTO wishlist_items(owner_id,item_id,custom_name,description,price) VALUES(?,?,?,?,?)")) {

                p.setInt(1,((Number)d.get("userId")).intValue());

                Object itemId = d.get("itemId");
                if (itemId == null)
                    p.setNull(2,Types.INTEGER);
                else
                    p.setInt(2,((Number)itemId).intValue());

                p.setString(3,name);
                p.setString(4,description);
                p.setBigDecimal(5,price);
                p.executeUpdate();

                return Response.ok("Item added to your wish list.");
            }
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response updateWish(Map<String,Object> d) {
        try {
            BigDecimal price = new BigDecimal(String.valueOf(d.get("price")));
            String name = String.valueOf(d.getOrDefault("name", "")).trim();
            String description = String.valueOf(d.getOrDefault("description", ""));

            if (name.isBlank() || name.length() > 150 || description.length() > 500 || price.compareTo(BigDecimal.ZERO) <= 0 || price.scale() > 2)
                return Response.fail("Enter a valid name, description and positive price.");

            try (Connection c = getConnection();
                 PreparedStatement p = c.prepareStatement(
                         "UPDATE wishlist_items SET custom_name=?,description=?,price=? " +
                         "WHERE id=? AND owner_id=? AND collected_amount=0")) {

                p.setString(1,name);
                p.setString(2,description);
                p.setBigDecimal(3,price);
                p.setInt(4,((Number)d.get("wishId")).intValue());
                p.setInt(5,((Number)d.get("userId")).intValue());

                return p.executeUpdate() > 0
                        ? Response.ok("Item updated.")
                        : Response.fail("Item cannot be updated after contributions have started.");
            }
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response deleteWish(Map<String,Object> d) {
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(
                     "DELETE FROM wishlist_items WHERE id=? AND owner_id=? AND collected_amount=0")) {

            p.setInt(1,((Number)d.get("wishId")).intValue());
            p.setInt(2,((Number)d.get("userId")).intValue());

            return p.executeUpdate() > 0
                    ? Response.ok("Item deleted.")
                    : Response.fail("Item cannot be deleted after contributions have started.");
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response contribute(Map<String,Object> d) {
        int buyer = ((Number)d.get("userId")).intValue();
        int wishId = ((Number)d.get("wishId")).intValue();

        try {
            BigDecimal amount = new BigDecimal(String.valueOf(d.get("amount")));

            if (amount.compareTo(BigDecimal.ZERO) <= 0)
                return Response.fail("Amount must be greater than zero.");

            try (Connection c = getConnection()) {
                c.setAutoCommit(false);

                try {
                    String sql = "SELECT owner_id,price,collected_amount,status,custom_name " +
                            "FROM wishlist_items WHERE id=? FOR UPDATE";

                    try (PreparedStatement p = c.prepareStatement(sql)) {
                        p.setInt(1,wishId);

                        try (ResultSet r = p.executeQuery()) {
                            if (!r.next()) {
                                c.rollback();
                                return Response.fail("Wish item not found.");
                            }

                            int owner = r.getInt("owner_id");
                            BigDecimal price = r.getBigDecimal("price");
                            BigDecimal collected = r.getBigDecimal("collected_amount");
                            String status = r.getString("status");
                            String name = r.getString("custom_name");

                            if (owner == buyer) {
                                c.rollback();
                                return Response.fail("You cannot contribute to your own item.");
                            }

                            try (PreparedStatement friendship = c.prepareStatement(
                                    "SELECT COUNT(*) FROM friendships WHERE status='ACCEPTED' AND ((sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?))")) {
                                friendship.setInt(1, buyer);
                                friendship.setInt(2, owner);
                                friendship.setInt(3, owner);
                                friendship.setInt(4, buyer);
                                try (ResultSet fr = friendship.executeQuery()) {
                                    fr.next();
                                    if (fr.getInt(1) == 0) {
                                        c.rollback();
                                        return Response.fail("You can only contribute to an accepted friend's item.");
                                    }
                                }
                            }

                            if ("COMPLETED".equals(status)) {
                                c.rollback();
                                return Response.fail("This item is already completed.");
                            }

                            BigDecimal remaining = price.subtract(collected);

                            if (amount.compareTo(remaining) > 0) {
                                c.rollback();
                                return Response.fail("Contribution exceeds remaining amount: " + remaining);
                            }

                            try (PreparedStatement x = c.prepareStatement(
                                    "INSERT INTO contributions(wishlist_item_id,buyer_id,amount) VALUES(?,?,?)")) {
                                x.setInt(1,wishId);
                                x.setInt(2,buyer);
                                x.setBigDecimal(3,amount);
                                x.executeUpdate();
                            }

                            BigDecimal total = collected.add(amount);
                            String newStatus = total.compareTo(price) >= 0 ? "COMPLETED" : "OPEN";

                            try (PreparedStatement x = c.prepareStatement(
                                    "UPDATE wishlist_items SET collected_amount=?,status=? WHERE id=?")) {
                                x.setBigDecimal(1,total);
                                x.setString(2,newStatus);
                                x.setInt(3,wishId);
                                x.executeUpdate();
                            }

                            String contributorNames = "your friend";
                            try (PreparedStatement contributors = c.prepareStatement(
                                    "SELECT GROUP_CONCAT(DISTINCT u.name ORDER BY u.name SEPARATOR ', ') " +
                                    "FROM contributions co JOIN users u ON u.id=co.buyer_id WHERE co.wishlist_item_id=?")) {
                                contributors.setInt(1, wishId);
                                try (ResultSet cr = contributors.executeQuery()) {
                                    if (cr.next() && cr.getString(1) != null) contributorNames = cr.getString(1);
                                }
                            }
                            notify(c, owner, "A contribution was added by " + contributorNames + " to your wish item: " + name, "CONTRIBUTION");
                            notify(c, buyer, "Your contribution was recorded for: " + name, "CONTRIBUTION");

                            if ("COMPLETED".equals(newStatus)) {
                                notify(c, buyer, "Your gift contribution completed: " + name, "GIFT_COMPLETED");
                                notify(c, owner, "Your wish item was fully bought by: " + contributorNames, "GIFT_RECEIVED");
                            }

                            c.commit();
                            return Response.ok("Contribution completed.");
                        }
                    }
                } catch (Exception e) {
                    c.rollback();
                    throw e;
                } finally {
                    c.setAutoCommit(true);
                }
            }
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    private void notify(Connection c,int userId,String message,String type) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO notifications(user_id,message,type) VALUES(?,?,?)")) {

            p.setInt(1,userId);
            p.setString(2,message);
            p.setString(3,type);
            p.executeUpdate();
        }
    }

    public Response notifications(Map<String,Object> d) {
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT id,message,type,is_read,created_at FROM notifications " +
                     "WHERE user_id=? ORDER BY created_at DESC")) {

            p.setInt(1,((Number)d.get("userId")).intValue());

            try (ResultSet r = p.executeQuery()) {
                return rowsResponse("Notifications",rows(r));
            }
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    public Response markRead(Map<String,Object> d) {
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(
                     "UPDATE notifications SET is_read=TRUE WHERE id=? AND user_id=?")) {

            p.setInt(1,((Number)d.get("notificationId")).intValue());
            p.setInt(2,((Number)d.get("userId")).intValue());
            p.executeUpdate();

            return Response.ok("Marked as read.");
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }
}
