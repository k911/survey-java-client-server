package repository;

import database.MysqlDatabaseManager;
import items.Participant;
import items.Question;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ParticipantRepository {
    private MysqlDatabaseManager databaseManager;

    public ParticipantRepository(MysqlDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }


    public boolean add(Participant participant) {

        if(null == participant) {
            return false;
        }

        PreparedStatement statement = databaseManager.prepareStatement("INSERT INTO participants (niu, name) VALUES (?, ?)");
        try {
            statement.setInt(1, participant.getNIU());
            statement.setString(2, participant.getName());
            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.out.println("Error saving participant into database: " + e.getLocalizedMessage());
        }

        return false;
    }

}
