package items;

import java.io.Serializable;

public class Participant implements Serializable {
    private int NIU;
    private String name;

    public Participant(int NIU, String name) {

        if (name.isEmpty()) {
            throw new RuntimeException("Name cannot be empty");
        }

        this.NIU = NIU;
        this.name = name;
    }

    public int getNIU() {
        return NIU;
    }

    public String getName() {
        return name;
    }
}
