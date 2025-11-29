package isKahoot.Game;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Carrega perguntas do ficheiro quizzes.json.
 */
public class QuestionLoader {

    private static final Gson gson = new Gson();

    /**
     * Carrega perguntas do ficheiro quizzes.json.
     *
     * @param filename nome do ficheiro (ex: "quizzes.json")
     * @return lista de perguntas carregadas
     */
    public static List<Question> loadFromJson(String filename) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filename)));

            // Converte diretamente para array de Questions usando Gson
            JsonObject root = gson.fromJson(content, JsonObject.class);
            JsonArray questionsArray = root.getAsJsonArray("questions");

            List<Question> questions = new ArrayList<>();

            for (int i = 0; i < questionsArray.size(); i++) {
                // Converte cada objeto JSON para Question usando Gson
                Question question = gson.fromJson(questionsArray.get(i), Question.class);
                questions.add(question);
            }

            System.out.println("[LOADER] Carregadas " + questions.size() + " perguntas de " + filename);
            return questions;

        } catch (Exception e) {
            System.err.println("[LOADER] Erro ao carregar " + filename + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}