package ru.urfu.practicalwork2;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class AllTests {
    private static int passed;

    private AllTests() {
    }

    public static void main(String[] args) throws Exception {
        testArrayMinimumByAbsoluteValue();
        testQuickSort();
        testAverageSalaryWithStreams();
        testJsonStringExtraction();
        testHttpUserAgentClient();
        testFileDownload();
        System.out.println("Все проверки пройдены: " + passed);
    }

    private static void testArrayMinimumByAbsoluteValue() {
        OptionalInt result = ArrayMinimumAbsoluteValueTask.findMinimumByAbsoluteValue(
                new int[]{-8, 4, -2, 7, 3});
        assertEquals(-2, result.orElseThrow(), "минимум по модулю");
        assertEquals(0,
                ArrayMinimumAbsoluteValueTask.findMinimumByAbsoluteValue(
                        new int[]{Integer.MIN_VALUE, 0, Integer.MAX_VALUE}).orElseThrow(),
                "минимум должен находиться и при наличии Integer.MIN_VALUE");
        assertEquals(-5,
                ArrayMinimumAbsoluteValueTask.findMinimumByAbsoluteValue(
                        new int[]{Integer.MIN_VALUE, -5}).orElseThrow(),
                "модуль Integer.MIN_VALUE не должен переполняться");
        assertTrue(isEmptyArrayResult(), "пустой массив");
        passed++;
    }

    private static boolean isEmptyArrayResult() {
        return ArrayMinimumAbsoluteValueTask.findMinimumByAbsoluteValue(new int[0]).isEmpty();
    }

    private static void testQuickSort() {
        List<Double> values = new ArrayList<>(List.of(5.0, -2.0, 5.0, 0.0, 1.5, -9.0, 1.5));
        QuickSortTask.quickSort(values);
        assertEquals(List.of(-9.0, -2.0, 0.0, 1.5, 1.5, 5.0, 5.0), values, "Quick Sort с дубликатами");

        List<Double> specialValues = new ArrayList<>(List.of(Double.NaN, 0.0, -0.0,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
        QuickSortTask.quickSort(specialValues);
        for (int i = 1; i < specialValues.size(); i++) {
            assertTrue(Double.compare(specialValues.get(i - 1), specialValues.get(i)) <= 0,
                    "порядок значений Double");
        }
        QuickSortTask.quickSort(new ArrayList<>());
        QuickSortTask.quickSort(new ArrayList<>(List.of(1.0)));
        passed++;
    }

    private static void testAverageSalaryWithStreams() {
        List<Employee> employees = EmployeeAverageSalaryTask.createSampleEmployees();
        OptionalDouble average = EmployeeAverageSalaryTask
                .calculateAverageSalaryByDepartment(employees, "разработка");
        assertTrue(average.isPresent(), "отдел должен найтись без учёта регистра");
        assertEquals(135_000.0, average.getAsDouble(), 0.001, "средняя зарплата отдела");
        assertTrue(EmployeeAverageSalaryTask
                .calculateAverageSalaryByDepartment(employees, "Несуществующий отдел").isEmpty(),
                "отсутствующий отдел");
        passed++;
    }

    private static void testJsonStringExtraction() throws IOException {
        String json = "{\"other\":{\"items\":[1,true]},\"user-agent\":\"Test \\\"Agent\\\"\\nLine\\u0021\"}";
        assertEquals("Test \"Agent\"\nLine!",
                JsonStringFieldParser.extractStringField(json, "user-agent"), "декодирование JSON-строки");
        expectThrows(IOException.class,
                () -> JsonStringFieldParser.extractStringField("{\"user-agent\":17}", "user-agent"),
                "поле должно быть строкой");
        passed++;
    }

    private static void testHttpUserAgentClient() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/user-agent", exchange -> {
            byte[] body = "{\"user-agent\":\"Local-Java-Test/1.0\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/user-agent";
            assertEquals("Local-Java-Test/1.0",
                    HttpUserAgentTask.fetchUserAgent(java.net.URI.create(url)),
                    "HTTP GET + JSON user-agent");
        } finally {
            server.stop(0);
        }
        passed++;
    }

    private static void testFileDownload() throws Exception {
        byte[] payload = "downloaded-content".getBytes(StandardCharsets.UTF_8);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/file", exchange -> {
            exchange.sendResponseHeaders(200, payload.length);
            try (var output = exchange.getResponseBody()) {
                output.write(payload);
            }
        });
        server.start();
        Path directory = Files.createTempDirectory("download-test-");
        Path destination = directory.resolve("result.txt");
        try {
            URIHolder source = new URIHolder("http://127.0.0.1:" + server.getAddress().getPort() + "/file");
            FileDownloadTask task = new FileDownloadTask(source.uri(), destination);
            task.start();
            assertTrue(task.awaitCompletion(5, TimeUnit.SECONDS), "загрузка должна завершиться");
            assertEquals("downloaded-content", Files.readString(destination), "содержимое скачанного файла");
            assertTrue(!task.isRunning(), "задача должна завершиться");
        } finally {
            server.stop(0);
            deleteDirectory(directory);
        }
        passed++;
    }

    private static void testFileDownloadCancellation() throws Exception {
        CountDownLatch responseStarted = new CountDownLatch(1);
        CountDownLatch allowServerToFinish = new CountDownLatch(1);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/slow", exchange -> {
            try {
                exchange.sendResponseHeaders(200, 0);
                var output = exchange.getResponseBody();
                output.write("begin".getBytes(StandardCharsets.UTF_8));
                output.flush();
                responseStarted.countDown();
                allowServerToFinish.await(3, TimeUnit.SECONDS);
                output.write(new byte[1024 * 1024]);
                output.close();
            } catch (IOException ignored) {
                // Клиент разорвал соединение при отмене — ожидаемое поведение.
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();
        Path directory = Files.createTempDirectory("download-cancel-test-");
        Path destination = directory.resolve("partial.txt");
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/slow";
            FileDownloadTask task = new FileDownloadTask(java.net.URI.create(url), destination);
            task.start();
            assertTrue(responseStarted.await(3, TimeUnit.SECONDS), "сервер должен начать ответ");
            long waitDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (task.getDownloadedBytes() == 0 && System.nanoTime() < waitDeadline) {
                Thread.sleep(10);
            }
            assertTrue(task.getDownloadedBytes() > 0, "клиент должен начать читать тело ответа");
            task.stop();
            assertTrue(task.awaitCompletion(5, TimeUnit.SECONDS), "отменённая загрузка должна завершиться");
            assertTrue(task.wasCancelled(), "задача должна быть помечена отменённой");
            assertTrue(!Files.exists(destination), "неполный файл не должен появиться под конечным именем");
            try (var files = Files.list(directory)) {
                assertEquals(0L, files.count(), "временный файл должен быть удалён");
            }
        } finally {
            allowServerToFinish.countDown();
            server.stop(0);
            deleteDirectory(directory);
        }
        passed++;
    }

    private record URIHolder(java.net.URI uri) {
        private URIHolder(String value) {
            this(java.net.URI.create(value));
        }
    }

    private static void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (var files = Files.walk(directory)) {
                for (Path path : files.sorted((a, b) -> b.compareTo(a)).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }

    private static void assertEquals(double expected, double actual, double tolerance, String message) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }

    private static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable action, String message) {
        try {
            action.run();
        } catch (Throwable exception) {
            if (type.isInstance(exception)) {
                return;
            }
            throw new AssertionError(message + ": неверный тип исключения " + exception, exception);
        }
        throw new AssertionError(message + ": ожидалось исключение " + type.getSimpleName());
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
