package ru.urfu.practicalwork2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class HttpUserAgentTask {
    private static final URI ENDPOINT = URI.create("https://httpbin.org/user-agent");

    private HttpUserAgentTask() {
    }

    public static String fetchUserAgent() throws IOException, InterruptedException {
        return fetchUserAgent(ENDPOINT);
    }

    public static String fetchUserAgent(URI endpoint) throws IOException, InterruptedException {
        if (endpoint == null) {
            throw new IllegalArgumentException("Адрес HTTP-запроса null");
        }
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(endpoint)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("хттп запрос завершился с кодом " + response.statusCode());
        }
        return JsonStringFieldParser.extractStringField(response.body(), "user-agent");
    }

    public static void run() {
        try {
            // По условию в консоль выводится только значение user-agent.
            System.out.println(fetchUserAgent());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println(" http запрос был прерван.");
        } catch (IOException exception) {
            System.err.println("ошибка: " + exception.getMessage());
        }
    }

    public static void main(String[] args) {
        run();
    }
}
