package org.aslak.github.merge;


import org.aslak.github.merge.servlet.GithubAuthFilter;
import org.junit.Assert;
import org.junit.Test;

public class GithubAuthFilterExtractTest {

    public static String response = "{\"access_token\":\"37ccc0c21ba5916b8eafecf7acc82f4df59ebf69\",\"token_type\":\"bearer\",\"scope\":\"public_repo\"}";

    @Test
    public void shouldExtractAccessToken() {
        String token = GithubAuthFilter.extractAccessToken(response);
        Assert.assertEquals("37ccc0c21ba5916b8eafecf7acc82f4df59ebf69", token);
    }

    @Test
    public void shouldExtractScope() {
        String[] scopes = GithubAuthFilter.extractScope(response);
        Assert.assertEquals(1, scopes.length);
        Assert.assertEquals("public_repo", scopes[0]);
    }
}
