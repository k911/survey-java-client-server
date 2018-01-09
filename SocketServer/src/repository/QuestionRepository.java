package repository;

import database.MysqlDatabaseManager;
import items.Question;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class QuestionRepository {
    private MysqlDatabaseManager databaseManager;

    public QuestionRepository(MysqlDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }


    public Collection<Question> getQuestions() {

        Collection<Question> questions = new ArrayList<>();

        try {
            ResultSet results = databaseManager.executeQuery("SELECT * FROM questions;");
            while (results.next()) {
                Map<String, String> answers = new HashMap<>();
                answers.put("a", results.getString("answer_a"));
                answers.put("b", results.getString("answer_b"));
                answers.put("c", results.getString("answer_c"));
                answers.put("d", results.getString("answer_d"));
                questions.add(new Question(results.getInt("id"), results.getString("question"), answers));
            }
        } catch (SQLException e) {
            System.out.println("Error getting questions from database: " + e.getLocalizedMessage());
            return questions;
        }

        return questions;
    }
}
