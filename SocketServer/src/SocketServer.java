import database.MysqlConnectionFactory;
import database.MysqlDatabaseManager;
import database.SimpleStatementFactory;
import server.ServerCall;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {

    public static void main(String[] args) {
        String database = "database";
        MysqlConnectionFactory connectionFactory = new MysqlConnectionFactory("jdbc:mysql://127.0.0.1?characterEncoding=utf-8&useSSL=false", "root", "root");
        Connection connection = connectionFactory.make();
        SimpleStatementFactory statementFactory = new SimpleStatementFactory(connection);
        MysqlDatabaseManager databaseManager = new MysqlDatabaseManager(statementFactory, database);

        boolean debug = true;
        if (!databaseManager.exists() || debug) {
            if (databaseManager.exists()) {
                databaseManager.drop();
                System.out.println("Database successfully dropped.");
            }
            databaseManager.create();
            setDatabase(database, connection);
            System.out.println("Database successfully created.");
            createDatabase(databaseManager);
            insertData(databaseManager);
            System.out.println("Schema successfully created.");
        }

        int port = 4242;
        ServerSocket serverSocket = null;
        try {
            connection.close();

            serverSocket = new ServerSocket(port);
            ExecutorService exec = Executors.newCachedThreadPool();
            int connectionId = 0;
            while (true) {

                Socket socket = serverSocket.accept();

                exec.execute(new ServerCall(socket, connectionFactory, connectionId).getFt());

                ++connectionId;
            }

        } catch (SQLException e) {
            System.out.println("Error while closing initial connection.");
        } catch (IOException e) {
            System.out.println("Could not create server socket on port: " + port + ".");
            e.printStackTrace();
        } finally {
            if (null != serverSocket) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static void setDatabase(String database, Connection connection) {
        // Set database for connection
        try {
            connection.setCatalog(database);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createDatabase(MysqlDatabaseManager database) {
        try {
            database.executeUpdate("START TRANSACTION");
            database.executeUpdate("CREATE TABLE `answers` (`id` int(10) UNSIGNED NOT NULL,`question_id` int(10) UNSIGNED NOT NULL,`answer` enum('a','b','c','d') COLLATE utf8mb4_unicode_ci NOT NULL,`participant_niu` int(10) UNSIGNED NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            database.executeUpdate("CREATE TABLE `participants` (`niu` int(10) UNSIGNED NOT NULL,`name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            database.executeUpdate("CREATE TABLE `questions` (`id` int(10) UNSIGNED NOT NULL,`question` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,`answer_a` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,`answer_b` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,`answer_c` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,`answer_d` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,`correct` enum('a','b','c','d') COLLATE utf8mb4_unicode_ci NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            database.executeUpdate("ALTER TABLE `answers` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `question_id` (`question_id`,`participant_niu`), ADD KEY `FK_NIU_PARTICIPANT_NIU` (`participant_niu`);");
            database.executeUpdate("ALTER TABLE `participants` ADD PRIMARY KEY (`niu`);");
            database.executeUpdate("ALTER TABLE `questions` ADD PRIMARY KEY (`id`);");
            database.executeUpdate("ALTER TABLE `answers` MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;");
            database.executeUpdate("ALTER TABLE `participants` MODIFY `niu` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;");
            database.executeUpdate("ALTER TABLE `questions` MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;");
            database.executeUpdate("ALTER TABLE `answers` ADD CONSTRAINT `FK_ID_QUESTION_ID` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`), ADD CONSTRAINT `FK_NIU_PARTICIPANT_NIU` FOREIGN KEY (`participant_niu`) REFERENCES `participants` (`niu`) ON DELETE CASCADE ON UPDATE CASCADE;");
            database.executeUpdate("COMMIT");
        } catch (Throwable e) {
            database.executeUpdate("ROLLBACK");
        }
    }

    private static void insertData(MysqlDatabaseManager database) {
        try {
            database.executeUpdate("START TRANSACTION");
            database.executeUpdate("INSERT INTO `questions` (`question`, `answer_a`, `answer_b`, `answer_c`, `answer_d`, `correct`) VALUES ('Co to jest AI?', 'sztuczna inteligencja', 'protokół internetowy', 'producent sprzętu komputerowego', 'język programowania', 'a')");
            database.executeUpdate("INSERT INTO `questions` (`question`, `answer_a`, `answer_b`, `answer_c`, `answer_d`, `correct`) VALUES ('DNS to:', 'protokół przesyłania plików i folderów', 'poprawna nazwa adresu www', 'specjalny program oferujący usługi sieciowe', 'serwer, który zmienia nazwę domenową na odpowiadający jej numer ip', 'd')");
            database.executeUpdate("COMMIT");
        } catch (Throwable e) {
            database.executeUpdate("ROLLBACK");
        }
    }
}
