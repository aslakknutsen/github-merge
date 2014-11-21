package org.aslak.github.merge.rest;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.aslak.github.merge.model.Commit;
import org.aslak.github.merge.model.Commit.State;

public class JSONUtil {

    public static JsonArray commitsToJson(List<Commit> commits) {
        JsonArrayBuilder buidler = Json.createArrayBuilder();
        for(Commit commit : commits) {
            buidler.add(
                Json.createObjectBuilder()
                    .add("id", commit.getId())
                    .add("message", commit.getMessage())
                    .add("author", commit.getAuthor())
                    .add("state", commit.getState().name())
            );
        }
        return buidler.build();
    }

    public static List<Commit> commitsFromJson(JsonArray commits) {
        List<Commit> result = new ArrayList<>();
        for(int i = 0; i < commits.size(); i++) {
            JsonObject object = commits.getJsonObject(i);
            Commit commit = new Commit(object.getString("id"), object.getString("message"), object.getString("author"));
            commit.setState(State.valueOf(object.getString("state")));
            result.add(commit);
        }
        return result;
    }
}
