package org.aslak.github.merge;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.JsonReader;

import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.rest.GithubUtil;

public class TestUtils {


    public static PullRequest getPullRequest() throws FileNotFoundException {
        JsonReader reader = Json.createReader(new FileInputStream(Paths.get("src", "test", "resources", "payloads", "pull_request.json").toFile()));
        return GithubUtil.toPullRequest(reader.readObject().getJsonObject("pull_request"));
    }
}
