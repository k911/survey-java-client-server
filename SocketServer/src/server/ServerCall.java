package server;

import database.ConnectionFactory;
import database.MysqlDatabaseManager;
import database.SimpleStatementFactory;
import database.StatementFactory;
import items.Answer;
import items.Participant;
import items.Question;
import repository.AnswerRepository;
import repository.ParticipantRepository;
import repository.QuestionRepository;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.util.Collection;
import java.util.concurrent.Callable;

public class ServerCall implements Callable<String> {
    private final String database = "database";
    private Socket socket;
    private MysqlDatabaseManager databaseManager;
    private FutureTaskCallback<String> ft;
    private int connectionId;
    private Participant participant;

    public ServerCall(Socket socket, ConnectionFactory connectionFactory, int connectionId) {
        this.connectionId = connectionId;
        Connection privateConnection = connectionFactory.make(database);
        StatementFactory statementFactory = new SimpleStatementFactory(privateConnection);
        this.socket = socket;
        this.databaseManager = new MysqlDatabaseManager(statementFactory, database);
        this.ft = new FutureTaskCallback<>(this, privateConnection);
    }

    public FutureTaskCallback<String> getFt() {
        return ft;
    }

    @Override
    public String call() {
        String txt = socket.getInetAddress().getHostName();
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            action_loop:
            while (true) {
                System.out.println("[" + this.connectionId + "] Waiting for command..");
                String command = in.readUTF();
                System.out.println("[" + this.connectionId + "] Command: " + command);

                switch (command) {
                    case "get_questions":
                        QuestionRepository questionRepository = new QuestionRepository(databaseManager);
                        Collection<Question> questions = questionRepository.getQuestions();
                        out.writeObject(questions);
                        break;
                    case "register":
                        ParticipantRepository participantRepository = new ParticipantRepository(databaseManager);
                        Participant localParticipant = (Participant) in.readObject();

                        if (participantRepository.add(localParticipant)) {
                            participant = localParticipant;
                            out.writeUTF("SUCCESS");
                        } else {
                            out.writeUTF("FAILURE");
                        }

                        break;
                    case "save_answers":
                        AnswerRepository answerRepository = new AnswerRepository(databaseManager);
                        Collection<Answer> answers = (Collection<Answer>) in.readObject();

                        if (answerRepository.addMany(answers, participant)) {
                            out.writeUTF("SUCCESS");
                            out.writeInt(answerRepository.countCorrectAnswers(participant));
                        } else {
                            out.writeUTF("FAILURE");
                        }

                        break;
                    default:
                        System.out.println("[" + this.connectionId + "] No such command: " + command);
                        break;
                    case "exit":
                        break action_loop;
                }

                out.flush();
            }
            socket.close();
        } catch (EOFException e) {
            System.err.println("Unexpected end of stream");
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error while closing socket on error.");
            }
        }

        return "[" + this.connectionId + "] Socket " + txt + " is closed.";
    }
}