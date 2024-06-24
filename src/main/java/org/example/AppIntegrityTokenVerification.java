package org.example;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.playintegrity.v1.PlayIntegrity;
import com.google.api.services.playintegrity.v1.model.DecodeIntegrityTokenRequest;
import com.google.api.services.playintegrity.v1.model.DecodeIntegrityTokenResponse;
import com.google.common.annotations.Beta;
import com.google.gson.Gson;
import org.json.JSONObject;
import java.io.*;
import java.util.Collections;
import static spark.Spark.*;

// Reference for this code: https://github.com/1nikolas/play-integrity-checker-server/tree/main
// AppIntegrity: https://developer.android.com/google/play/integrity/overview


public class AppIntegrityTokenVerification {
    @Beta
    public static void main(String[] args) {
        port(8080);

        post("/verifyIntegrity", (request, response) -> {
            String jsonBody = request.body();
            JSONObject jsonObj = new JSONObject(jsonBody);
            String integrityToken = jsonObj.getString("integrityToken");
            String packageName = jsonObj.getString("package");

            System.out.println("Received integrity token: " + integrityToken);
            System.out.println("Received package name: " + packageName);

            // Process the token here (e.g., verify with Google Play Integrity API)
            try {
                String credentialsJSON = getGoogleApplicationCredentials();
                GoogleCredential credentials = GoogleCredential.fromStream(new ByteArrayInputStream(credentialsJSON.getBytes()))
                        .createScoped(Collections.singleton("https://www.googleapis.com/auth/playintegrity"));

                PlayIntegrity playIntegrity = new PlayIntegrity.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credentials)
                        .setApplicationName(packageName)
                        .build();

                DecodeIntegrityTokenRequest requestBody = new DecodeIntegrityTokenRequest();
                requestBody.setIntegrityToken(integrityToken);

                DecodeIntegrityTokenResponse result = playIntegrity.v1().decodeIntegrityToken(packageName, requestBody).execute();
                System.out.println("Response: " + new Gson().toJson(result.getTokenPayloadExternal()));

                response.type("application/json");
                return new Gson().toJson(result.getTokenPayloadExternal());
            } catch (Exception e) {
                response.status(500);
                return "{\"error\": \"Google API error.\\n" + e.getMessage() + "\"}";
            }
        });
    }

    private static String getGoogleApplicationCredentials() {
        return """ 
                {
                  "type": "service_account",
                  "project_id": "sign3sdk",
                  "private_key_id": "51939c3555cac6da925db2cded08d1966a371046",
                  "private_key": "-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2Y7psplSk9BSL\\niyB0kH1LruTCA4yxcYxrTawjP7lY7Pe15rjJQk0cGd7ZdJa138a32OsPaYCjKHej\\nvYxd2ivSlM5xNgeuERN9NTQUdr0HoGgkKvlwzrisHWRLvHxPyN3faRBSA+kW/Vb7\\nEzMScsyZDBXhf+MGieIzZNp+pZveflb2k9X2LhVETmLI0p1iLMLrZZFcu2cHzSej\\nheaq7pLWc0jMisiCbqqjxzm/S/2f5sSuSj2YyWUx7px8UHEP+upmZCrX0HpV8RWL\\nKvGldnPe+IFcNsxEm7P3/1y1+zRUUfOONFODxyC3UwCKk/0spxIcz+0+lrglP/Kp\\nlpXWkzdLAgMBAAECggEAAUtJ2lV/yj/VGXxeOZWA3hWal+nQiL3GgkhlzUY5eXEM\\n/ORrMTpgM6/XTfGu1BGHZmyKlmxoNkAmQSoqIcxXfUIDCPiaK1kBq2TCPtbs1m4D\\n7yXC6VL+KnJ28LagpyxErr4fGIXglIItqNBlD19zJi7YxOXUZYp66WRwzG8+YOC/\\niG0XBCKkDW1fsyzmayWu9BqQJkyhAJp/w5WYbvb07AE5ked4KQ5GOsLwtOmvEdmn\\nIuzg67XDo2QPF2jnnLPm0ycf9eosir/dHRv9H+NOkFAadC3JLwVuZ5GxLe8gExbR\\nN6EbOaUp5+98MJtnn5IclhHIdw+0ASKH/wJvlP6QMQKBgQDnNltecfFLAHyWneId\\nyRZmfSAglxb42G8liloZBa0kCaty8ySktLPDquduqL8Z85xXkayaNJC86Y9Of0yQ\\n+dQr6XrMXH15WHISYjCqB7dT6ks1YEDvG99XwW+EzJ8gvUSfEUlVrvaBXLIXrR4Y\\nlv2/jq4RQ8YanxZ+DVXVCC9qmQKBgQDJ8W0TMdh9JvT2QbtR4JniVByPrYpjmMtc\\n5B8hkcWf0aeWd2GzNHD5HsSPKQ25icJzZmEp8vvoD0h3kjaDQwM4IHLw5yfPe+V8\\n1/4BNxvqw0/VdpksivGHJ6Tm+1wSXvTwC1Q5bRmKYQUJ2LqVUhdX9fsHAeEYYhUD\\nc9NryVrjgwKBgQCZFBX+O7YjkBjsp29ThRU9mc4xhZcB/lnoIudv9aAiYfyPSIeU\\nPMdf3sEHNNYBWpbCK4J5PiAt0vy8Xe/VdHKS+CwDLaEtO++aWEQb7qvj9RUCK84S\\nAQQn17v70nXweR77qbhB8GroTvTiVA2+/gjWUKLSb2qzqWx83FtdhePO0QKBgALo\\np1mPbj7fQMgLSldzlMPd0kt/hG7K1KMR5iDlqKMUsgVI0u9x4e60ssAdJ0V05ogj\\nAWNJTLebAMA4KvDZFkLItN8jdT9X2YZi1hXOZWiMR6obezlZBwGbYJSzKjBZjc2n\\nXB83oxaLLzdlF8ru08dAk80WCdgAEa1rDbCPxe9hAoGAYbF43wVIR6R5ienHgo1M\\no0SW3hGCKe+csz79AIVSv7eWMZrGLHVLW4OYqyLmGKa4yGZQlSQggXz42D/R4EAA\\nYP73EatK8llhlLUNiME6RQzo0RV8t+sGnaXdqDitkU+qYA0xb/uRogceWKofKF0P\\nrRv2r+apMLA+NqiRdg+vVfY=\\n-----END PRIVATE KEY-----\\n",
                  "client_email": "sign3sdk@sign3sdk.iam.gserviceaccount.com",
                  "client_id": "109515130432388007890",
                  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                  "token_uri": "https://oauth2.googleapis.com/token",
                  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
                  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/sign3sdk%40sign3sdk.iam.gserviceaccount.com",
                  "universe_domain": "googleapis.com"
                }               
             """;
    }
}
