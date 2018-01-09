package repository;

import database.MysqlDatabaseManager;
import items.Answer;
import items.Participant;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class AnswerRepository {
    private MysqlDatabaseManager databaseManager;

    public AnswerRepository(MysqlDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }


    public boolean addMany(Collection<Answer> answers, Participant participant) {

        if (null == participant || answers.isEmpty()) {
            return false;
        }


        try {

            databaseManager.executeUpdate("START TRANSACTION");

            for (Answer answer : answers) {
                PreparedStatement statement = databaseManager.prepareStatement("INSERT INTO answers (participant_niu, question_id, answer) VALUES (?, ?, ?)");
                statement.setInt(1, participant.getNIU());
                statement.setInt(2, answer.getQuestionId());
                statement.setString(3, answer.getAnswer());
                statement.executeUpdate();
            }

            databaseManager.executeUpdate("COMMIT");

            return true;
        } catch (SQLException e) {

            databaseManager.executeUpdate("ROLLBACK");

            System.out.println("Error saving participant into database: " + e.getLocalizedMessage());
        }

        return false;
    }


    public int countCorrectAnswers(Participant participant) {

        int correctAnswers = 0;

        PreparedStatement statement = databaseManager.prepareStatement("SELECT count(DISTINCT a.id) correct_answers FROM answers a LEFT JOIN questions q ON q.id = a.question_id WHERE a.participant_niu = ? AND q.correct = a.answer");

        ResultSet results;
        try {
            statement.setInt(1, participant.getNIU());
            results = statement.executeQuery();
            while (results.next()) {
                correctAnswers = results.getInt("correct_answers");
            }
        } catch (SQLException e) {
            System.out.println("Error getting questions from database: " + e.getLocalizedMessage());
        }

        return correctAnswers;
    }
}
