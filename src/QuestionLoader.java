// QuestionLoader.java
import com.google.gson.*;
import java.io.*;
import java.util.*;

public class QuestionLoader {
    public static List<Question> loadFromJson(String filePath) throws IOException {
        Gson gson = new Gson();
        Reader reader = new FileReader(filePath);
        JsonObject obj = gson.fromJson(reader, JsonObject.class);
        List<Question> questions = new ArrayList<>();

        for (JsonElement qElem : obj.getAsJsonArray("questions")) {
            questions.add(gson.fromJson(qElem, Question.class));
        }

        reader.close();
        return questions;
    }
}
