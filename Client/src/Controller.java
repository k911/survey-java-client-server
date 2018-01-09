import items.Answer;
import items.Participant;
import items.Question;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Controller {

    public Label niuLabel, errorLabel;

    public TextField niuText,
            nameText,
            questionText,
            answerTextA,
            answerTextB,
            answerTextC,
            answerTextD;

    public RadioButton radioButtonA,
            radioButtonB,
            radioButtonC,
            radioButtonD;

    public Button startQuiz,
            nextButton;

    private int currentQuestionIndex;
    private boolean finished = false;
    private ArrayList<Question> questions;
    private Map<String, String> answersMap;
    private ArrayList<Answer> answers;
    private Participant participant;

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;

    public Controller() {
        this.currentQuestionIndex = 0;
        this.answers = new ArrayList<>();
        this.answersMap = new HashMap<>();
        int port = 4242;
        String address = "localhost";

        try {
            socket = new Socket(InetAddress.getByName(address), port);
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Question> loadQuestions() {
        ArrayList<Question> questions = null;

        try {
            out.writeUTF("get_questions");
            out.flush();

            questions = (ArrayList<Question>) in.readObject();
            Collections.shuffle(questions);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return questions;
    }

    public void start(ActionEvent actionEvent) {

        String niu = niuText.getText();

        errorLabel.setText("");

        if (niu.isEmpty()) {
            errorLabel.setText("NIU must NOT be empty.");
            return;
        }

        if (!niu.matches("^[1-9]\\d*$")) {
            errorLabel.setText("NIU must be an numeric value");
            return;
        }

        Participant localParticipant = new Participant(Integer.valueOf(niu), nameText.getText());
        try {
            out.writeUTF("register");
            out.writeObject(localParticipant);
            out.flush();
            if (in.readUTF().equals("SUCCESS")) {
                participant = localParticipant;
                niuText.setEditable(false);
                nameText.setEditable(false);
                startQuiz.setVisible(false);

                questions = loadQuestions();
                System.out.println("Loaded " + this.questions.size() + " questions");
                showCurrentQuestion();
            } else {
                errorLabel.setText("Error while registering. NIU probably exists on server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showCurrentQuestion() {

        Question currentQuestion = questions.get(currentQuestionIndex);

        questionText.setText(currentQuestion.getQuestion());

        Map<String, String> answers = currentQuestion.getAnswers();

        ArrayList<String> abcd = new ArrayList<>();
        abcd.add("a");
        abcd.add("b");
        abcd.add("c");
        abcd.add("d");
        Collections.shuffle(abcd);

        answersMap.clear();
        answersMap.put("a",abcd.get(0));
        answersMap.put("b",abcd.get(1));
        answersMap.put("c",abcd.get(2));
        answersMap.put("d",abcd.get(3));

        answerTextA.setText(answers.get(answersMap.get("a")));
        answerTextB.setText(answers.get(answersMap.get("b")));
        answerTextC.setText(answers.get(answersMap.get("c")));
        answerTextD.setText(answers.get(answersMap.get("d")));
    }

    private String getCurrentAnswerFromRadio() {
        if (radioButtonA.isSelected()) {
            return "a";
        }
        if (radioButtonB.isSelected()) {
            return "b";
        }
        if (radioButtonC.isSelected()) {
            return "c";
        }
        if (radioButtonD.isSelected()) {
            return "d";
        }

        return "";
    }

    public void nextQuestion(ActionEvent actionEvent) {

        if(finished) {
            try {
                out.writeUTF("exit");
                out.flush();
                socket.close();
            } catch (IOException e) {
                System.out.println("INFO: Already closed.");
            }
            return;
        }

        int questionsMaxIndex = questions.size() - 1;

        answers.add(new Answer(questions.get(currentQuestionIndex).getId(), answersMap.get(this.getCurrentAnswerFromRadio())));

        if (currentQuestionIndex == questionsMaxIndex - 1) {
            nextButton.setText("Finish");
        }

        if (currentQuestionIndex == questionsMaxIndex) {
            String result;
            try {
                out.writeUTF("save_answers");
                out.writeObject(answers);
                out.flush();
                result = in.readUTF();

                if (result.equals("SUCCESS")) {
                    float percentage = (in.readInt() * 100.0f) / questions.size();
                    result = String.valueOf(percentage) + " %";
                }

            } catch (IOException e) {
                result = "Unexpected exception";
                e.printStackTrace();
            }
            errorLabel.setText("Finished! Result: " + result);
            finished = true;
            return;
        }


        ++currentQuestionIndex;
        showCurrentQuestion();
    }
}
