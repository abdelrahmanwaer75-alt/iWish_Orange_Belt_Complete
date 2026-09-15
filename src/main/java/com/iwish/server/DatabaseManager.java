package com.iwish.server;

import com.iwish.common.HashUtil;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

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
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password"));
    }

    private void initialize() throws Exception {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY AUTO_INCREMENT,name VARCHAR(100) NOT NULL,email VARCHAR(150) NOT NULL UNIQUE,password_hash VARCHAR(64) NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            s.execute("CREATE TABLE IF NOT EXISTS friendships (id INT PRIMARY KEY AUTO_INCREMENT,sender_id INT NOT NULL,receiver_id INT NOT NULL,status VARCHAR(20) NOT NULL DEFAULT 'PENDING',created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,UNIQUE KEY uq_friend_pair(sender_id,receiver_id),FOREIGN KEY(sender_id) REFERENCES users(id) ON DELETE CASCADE,FOREIGN KEY(receiver_id) REFERENCES users(id) ON DELETE CASCADE)");
            s.execute("CREATE TABLE IF NOT EXISTS items (id INT PRIMARY KEY AUTO_INCREMENT,name VARCHAR(150) NOT NULL,description VARCHAR(500),default_price DECIMAL(12,2) NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS wishlist_items (id INT PRIMARY KEY AUTO_INCREMENT,owner_id INT NOT NULL,item_id INT NULL,custom_name VARCHAR(150) NOT NULL,description VARCHAR(500),price DECIMAL(12,2) NOT NULL,collected_amount DECIMAL(12,2) NOT NULL DEFAULT 0,status VARCHAR(20) NOT NULL DEFAULT 'OPEN',created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE,FOREIGN KEY(item_id) REFERENCES items(id) ON DELETE SET NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS contributions (id INT PRIMARY KEY AUTO_INCREMENT,wishlist_item_id INT NOT NULL,buyer_id INT NOT NULL,amount DECIMAL(12,2) NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(wishlist_item_id) REFERENCES wishlist_items(id) ON DELETE CASCADE,FOREIGN KEY(buyer_id) REFERENCES users(id) ON DELETE CASCADE)");
            s.execute("CREATE TABLE IF NOT EXISTS notifications (id INT PRIMARY KEY AUTO_INCREMENT,user_id INT NOT NULL,message VARCHAR(500) NOT NULL,type VARCHAR(40) NOT NULL,is_read BOOLEAN NOT NULL DEFAULT FALSE,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
            seed();
        }
    }

    private void seed() throws SQLException {
        String q = "SELECT COUNT(*) FROM items";
        try (Connection c=getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(q)) {
            r.next();
            if (r.getInt(1)==0) {
                try (PreparedStatement p=c.prepareStatement("INSERT INTO items(name,description,default_price) VALUES(?,?,?)")) {
                    String[][] x={{"PlayStation 5","Gaming console","30000"},{"Apple AirPods Pro","Wireless earbuds","12000"},{"Smart Watch","Modern smartwatch","7000"},{"Laptop","Portable computer","45000"},{"Perfume","Premium perfume","3500"}};
                    for(String[] a:x){p.setString(1,a[0]);p.setString(2,a[1]);p.setBigDecimal(3,new BigDecimal(a[2]));p.addBatch();}
                    p.executeBatch();
                }
            }
        }
    }

    private List<Map<String,Object>> rows(ResultSet rs) throws SQLException {
        List<Map<String,Object>> out=new ArrayList<>();
        ResultSetMetaData md=rs.getMetaData();
        while(rs.next()){
            Map<String,Object> m=new LinkedHashMap<>();
            for(int i=1;i<=md.getColumnCount();i++) m.put(md.getColumnLabel(i),rs.getObject(i));
            out.add(m);
        }
        return out;
    }

    public Response register(Map<String,Object> d) {
        String name=String.valueOf(d.get("name")), email=String.valueOf(d.get("email")).trim().toLowerCase(), pass=String.valueOf(d.get("password"));
        if(name.isBlank()||email.isBlank()||pass.length()<4) return Response.fail("Please enter valid data.");
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO users(name,email,password_hash) VALUES(?,?,?)")){
            p.setString(1,name);p.setString(2,email);p.setString(3,HashUtil.sha256(pass));p.executeUpdate();
            return Response.ok("Account created successfully.");
        }catch(SQLIntegrityConstraintViolationException e){return Response.fail("Email already exists.");}
        catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response login(Map<String,Object> d) {
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("SELECT id,name,email FROM users WHERE email=? AND password_hash=?")){
            p.setString(1,String.valueOf(d.get("email")).trim().toLowerCase());p.setString(2,HashUtil.sha256(String.valueOf(d.get("password"))));
            try(ResultSet r=p.executeQuery()){if(r.next()){Response x=Response.ok("Login successful.");x.data=rows(r).get(0);return x;}}
            return Response.fail("Invalid email or password.");
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response users(Map<String,Object> d) {
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("SELECT id,name,email FROM users WHERE id<>? AND name LIKE ? ORDER BY name")){
            p.setInt(1,(Integer)d.get("userId"));p.setString(2,"%"+d.getOrDefault("search","")+"%");
            Response x=Response.ok("Users");try(ResultSet r=p.executeQuery()){x.rows=rows(r);}return x;
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response friends(Map<String,Object> d) {
        String sql="SELECT u.id,u.name,u.email,f.status,CASE WHEN f.sender_id=? THEN 'SENT' ELSE 'RECEIVED' END direction FROM friendships f JOIN users u ON u.id=CASE WHEN f.sender_id=? THEN f.receiver_id ELSE f.sender_id END WHERE (f.sender_id=? OR f.receiver_id=?) ORDER BY u.name";
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement(sql)){
            int id=(Integer)d.get("userId"); for(int i=1;i<=4;i++)p.setInt(i,id);
            Response x=Response.ok("Friends");try(ResultSet r=p.executeQuery()){x.rows=rows(r);}return x;
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response addFriend(Map<String,Object> d) {
        int me=(Integer)d.get("userId"), other=(Integer)d.get("otherId");
        if(me==other)return Response.fail("You cannot add yourself.");
        try(Connection c=getConnection()){
            String check="SELECT id,status FROM friendships WHERE (sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?)";
            try(PreparedStatement p=c.prepareStatement(check)){p.setInt(1,me);p.setInt(2,other);p.setInt(3,other);p.setInt(4,me);try(ResultSet r=p.executeQuery()){if(r.next())return Response.fail("A friendship request already exists.");}}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO friendships(sender_id,receiver_id,status) VALUES(?,?,'PENDING')")){p.setInt(1,me);p.setInt(2,other);p.executeUpdate();}
            notify(c,other,"You received a new friend request.","FRIEND_REQUEST");
            return Response.ok("Friend request sent.");
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response friendDecision(Map<String,Object> d) {
        int me=(Integer)d.get("userId"), other=(Integer)d.get("otherId"); String status=String.valueOf(d.get("status"));
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("UPDATE friendships SET status=? WHERE sender_id=? AND receiver_id=?")){
            p.setString(1,status);p.setInt(2,other);p.setInt(3,me);int n=p.executeUpdate();
            if(n==0)return Response.fail("Request not found.");
            if(status.equals("ACCEPTED"))notify(c,other,"Your friend request was accepted.","FRIEND_ACCEPTED");
            return Response.ok("Friend request updated.");
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response removeFriend(Map<String,Object> d) {
        int me=(Integer)d.get("userId"), other=(Integer)d.get("otherId");
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM friendships WHERE (sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?)")){
            p.setInt(1,me);p.setInt(2,other);p.setInt(3,other);p.setInt(4,me);p.executeUpdate();return Response.ok("Friend removed.");
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response items() {
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("SELECT id,name,description,default_price FROM items ORDER BY name")){
            Response x=Response.ok("Items");try(ResultSet r=p.executeQuery()){x.rows=rows(r);}return x;
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response myWishlist(Map<String,Object> d) { return wishlistFor((Integer)d.get("userId")); }

    private Response wishlistFor(int owner) {
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("SELECT w.id,w.owner_id,w.item_id,w.custom_name,w.description,w.price,w.collected_amount,w.status,(w.price-w.collected_amount) remaining,u.name owner_name FROM wishlist_items w JOIN users u ON u.id=w.owner_id WHERE w.owner_id=? ORDER BY w.created_at DESC")){
            p.setInt(1,owner);Response x=Response.ok("Wish list");try(ResultSet r=p.executeQuery()){x.rows=rows(r);}return x;
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response friendWishlist(Map<String,Object> d) {
        int me=(Integer)d.get("userId"), owner=(Integer)d.get("ownerId");
        try(Connection c=getConnection();PreparedStatement fp=c.prepareStatement("SELECT COUNT(*) FROM friendships WHERE status='ACCEPTED' AND ((sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?))")){
            fp.setInt(1,me);fp.setInt(2,owner);fp.setInt(3,owner);fp.setInt(4,me);
            try(ResultSet fr=fp.executeQuery()){fr.next();if(fr.getInt(1)==0)return Response.fail("You can only view an accepted friend's wish list.");}
            Response x=wishlistFor(owner); return x;
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response addWish(Map<String,Object> d) {
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO wishlist_items(owner_id,item_id,custom_name,description,price) VALUES(?,?,?,?,?)")){
            p.setInt(1,(Integer)d.get("userId"));Object item=d.get("itemId");if(item==null)p.setNull(2,Types.INTEGER);else p.setInt(2,(Integer)item);
            p.setString(3,String.valueOf(d.get("name")));p.setString(4,String.valueOf(d.getOrDefault("description","")));p.setBigDecimal(5,new BigDecimal(String.valueOf(d.get("price"))));p.executeUpdate();return Response.ok("Item added to your wish list.");
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response updateWish(Map<String,Object> d) {
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("UPDATE wishlist_items SET custom_name=?,description=?,price=? WHERE id=? AND owner_id=? AND collected_amount=0")){
            p.setString(1,String.valueOf(d.get("name")));p.setString(2,String.valueOf(d.getOrDefault("description","")));p.setBigDecimal(3,new BigDecimal(String.valueOf(d.get("price"))));p.setInt(4,(Integer)d.get("wishId"));p.setInt(5,(Integer)d.get("userId"));
            return p.executeUpdate()>0?Response.ok("Item updated."):Response.fail("Item cannot be updated after contributions have started.");
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response deleteWish(Map<String,Object> d) {
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM wishlist_items WHERE id=? AND owner_id=? AND collected_amount=0")){
            p.setInt(1,(Integer)d.get("wishId"));p.setInt(2,(Integer)d.get("userId"));return p.executeUpdate()>0?Response.ok("Item deleted."):Response.fail("Item cannot be deleted after contributions have started.");
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response contribute(Map<String,Object> d) {
        int buyer=(Integer)d.get("userId"), wid=(Integer)d.get("wishId");
        BigDecimal amount=new BigDecimal(String.valueOf(d.get("amount")));
        if(amount.compareTo(BigDecimal.ZERO)<=0)return Response.fail("Amount must be greater than zero.");
        try(Connection c=getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement lock=c.prepareStatement("SELECT owner_id,price,collected_amount,status,custom_name FROM wishlist_items WHERE id=? FOR UPDATE")){
                lock.setInt(1,wid);
                try(ResultSet r=lock.executeQuery()){
                    if(!r.next())return Response.fail("Wish item not found.");
                    int owner=r.getInt("owner_id"); BigDecimal price=r.getBigDecimal("price"), collected=r.getBigDecimal("collected_amount");
                    if(owner==buyer)return Response.fail("You cannot contribute to your own item.");
                    if("COMPLETED".equals(r.getString("status")))return Response.fail("This item is already completed.");
                    BigDecimal remaining=price.subtract(collected);
                    if(amount.compareTo(remaining)>0)return Response.fail("Contribution exceeds remaining amount: "+remaining);
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO contributions(wishlist_item_id,buyer_id,amount) VALUES(?,?,?)")){p.setInt(1,wid);p.setInt(2,buyer);p.setBigDecimal(3,amount);p.executeUpdate();}
                    BigDecimal newTotal=collected.add(amount);
                    String newStatus=newTotal.compareTo(price)>=0?"COMPLETED":"OPEN";
                    try(PreparedStatement p=c.prepareStatement("UPDATE wishlist_items SET collected_amount=?,status=? WHERE id=?")){p.setBigDecimal(1,newTotal);p.setString(2,newStatus);p.setInt(3,wid);p.executeUpdate();}
                    if(newStatus.equals("COMPLETED")){
                        notify(c,buyer,"Your gift contribution completed \""+r.getString("custom_name")+"\".","GIFT_COMPLETED");
                        notify(c,owner,"Your wish-list item \""+r.getString("custom_name")+"\" has been fully bought by your friend(s).","GIFT_RECEIVED");
                    } else {
                        notify(c,buyer,"Your contribution was recorded successfully.","CONTRIBUTION");
                    }
                    c.commit(); return Response.ok("Contribution completed.");
                }
            } catch(Exception e){c.rollback();throw e;} finally {c.setAutoCommit(true);}
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    private void notify(Connection c,int userId,String msg,String type) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT INTO notifications(user_id,message,type) VALUES(?,?,?)")){p.setInt(1,userId);p.setString(2,msg);p.setString(3,type);p.executeUpdate();}
    }

    public Response notifications(Map<String,Object> d) {
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("SELECT id,message,type,is_read,created_at FROM notifications WHERE user_id=? ORDER BY created_at DESC")){
            p.setInt(1,(Integer)d.get("userId"));Response x=Response.ok("Notifications");try(ResultSet r=p.executeQuery()){x.rows=rows(r);}return x;
        }catch(Exception e){return Response.fail(e.getMessage());}
    }

    public Response markRead(Map<String,Object> d) {
        try(Connection c=getConnection();PreparedStatement p=c.prepareStatement("UPDATE notifications SET is_read=TRUE WHERE id=? AND user_id=?")){
            p.setInt(1,(Integer)d.get("notificationId"));p.setInt(2,(Integer)d.get("userId"));p.executeUpdate();return Response.ok("Marked as read.");
        }catch(Exception e){return Response.fail(e.getMessage());}
    }
}
