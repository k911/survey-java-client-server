import java.util.Map;

public class Question {
    private int id;
    private String question;
    private Map<String, String> answers;

    public Question(int id, String question, Map<String, String> answers) {
        this.id = id;
        this.question = question;
        this.answers = answers;
    }

    public int getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }
}
