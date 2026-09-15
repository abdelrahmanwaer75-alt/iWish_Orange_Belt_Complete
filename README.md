# i-Wish — Orange Belt Java Desktop Application

A complete Java desktop Client/Server application for the Orange Belt project.

## Stack
- Java 17
- JavaFX 21
- MySQL 8+
- JDBC
- TCP Sockets
- Maven
- FXML + CSS

## Features
- Register / Sign in
- Add, accept, decline and remove friends
- Create, update and delete wish-list items
- Browse friends and their wish lists
- Contribute money toward a friend's item
- Automatic completion notifications
- Receiver notifications when an item is fully funded
- Server Start/Stop console
- Database initialization and seed items

## 1. Database
1. Install MySQL 8+.
2. Run `database/schema.sql`.
3. Edit `src/main/resources/db.properties`.
4. The application will also create missing tables automatically.

Default database:
- host: localhost
- port: 3306
- database: iwish
- user: root
- password: root

Change the password before running.

## 2. Run Server
From the project root:

```bash
mvn -q -DskipTests package
java -cp target/classes;target/dependency/* com.iwish.server.ServerMain
```

On Linux/macOS replace `;` with `:`. The server listens on port 5555.

If you prefer IntelliJ/NetBeans, run:
`com.iwish.server.ServerMain`

## 3. Run Client
Run:
`com.iwish.client.ClientApp`

Or:

```bash
mvn javafx:run
```

## Demo flow
1. Register two accounts.
2. Add each other as friends.
3. Accept the request.
4. Add a wish-list item.
5. Open the friend's wish list.
6. Contribute an amount.
7. Repeat until the price is complete.
8. Check Notifications.

## GitHub delivery
Recommended repository contents:
- Source code
- `database/schema.sql`
- `database/backup.sql`
- `README.md`
- team contribution section
- screenshots/demo video

## Security note
This is an academic project. Passwords are stored as SHA-256 hashes for demonstration. For production systems, use a modern password-hashing algorithm such as Argon2/bcrypt/scrypt, TLS, and proper authorization.
