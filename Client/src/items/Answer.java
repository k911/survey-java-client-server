package items;

import java.io.Serializable;

public class Answer implements Serializable {
    private int questionId;
    private String answer;

    public Answer(int questionId, String answer) {

        answer = answer.toLowerCase();

        if (!answer.matches("[abcd]")) {
            throw new RuntimeException("Answer must be a value of: a,b,c,d");
        }

        this.questionId = questionId;
        this.answer = answer;
    }

    public int getQuestionId() {
        return questionId;
    }

    public String getAnswer() {
        return answer;
    }
}
