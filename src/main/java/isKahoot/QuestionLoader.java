package isKahoot;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QuestionLoader {

    public static List<Question> loadFromJson(String s) throws IOException {
        Gson gson = new Gson();

        // Carregar ficheiro perguntas.json da pasta resources
        InputStream in = QuestionLoader.class
                .getClassLoader()
                .getResourceAsStream("perguntas.json");

        if (in == null) {
            throw new FileNotFoundException("ficheiro perguntas.json não encontrado");
        }

        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);

        JsonObject obj = gson.fromJson(reader, JsonObject.class);
        List<Question> questions = new ArrayList<>();

        for (JsonElement qElem : obj.getAsJsonArray("questions")) {
            questions.add(gson.fromJson(qElem, Question.class));
        }

        reader.close();
        return questions;
    }
}
