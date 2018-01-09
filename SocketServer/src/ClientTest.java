
import items.Question;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;

public class ClientTest {
    public static void main(String args[]) {
        int port = 4242;
        String address = "localhost";
        try {
            Socket socket = new Socket(InetAddress.getByName(address), port);
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            out.writeUTF("get_questions");
            out.flush();

            Collection<Question> questions = (Collection<Question>) in.readObject();
            for (Question question : questions) {
                System.out.println("Question: " + question.getQuestion());
            }

            System.out.println(questions.size());

            out.writeUTF("exit");
            out.flush();
            out.close();

            socket.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}

