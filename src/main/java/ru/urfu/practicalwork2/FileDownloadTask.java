package ru.urfu.practicalwork2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FileDownloadTask implements Task {
    private final URI source;
    private final Path destination;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicLong downloadedBytes = new AtomicLong();
    private final CountDownLatch completion = new CountDownLatch(1);
    private final Object stateLock = new Object();

    private volatile boolean finished;
    private volatile Thread worker;
    private volatile HttpURLConnection connection;
    private volatile InputStream activeInput;
    private volatile OutputStream activeOutput;

    public FileDownloadTask(URI source, Path destination) {
        this.source = Objects.requireNonNull(source, "URI is null");
        this.destination = Objects.requireNonNull(destination, "путь is null")
                .toAbsolutePath().normalize();
        String scheme = source.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("надо http:// и https://");
        }
    }

    @Override
    public void start() {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("уже запущено");
        }
        worker = new Thread(this::download, "file-download-worker");
        worker.start();
    }

    @Override
    public void stop() {
        synchronized (stateLock) {
            if (!started.get() || finished) {
                return;
            }
            cancelled.set(true);
        }

        HttpURLConnection currentConnection = connection;
        if (currentConnection != null) {
            currentConnection.disconnect();
        }
        closeQuietly(activeInput);
        closeQuietly(activeOutput);

        Thread currentWorker = worker;
        if (currentWorker != null) {
            currentWorker.interrupt();
        }
    }

    public boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
        return completion.await(timeout, unit);
    }

    public void awaitCompletion() throws InterruptedException {
        completion.await();
    }

    public boolean isRunning() {
        return started.get() && !finished;
    }

    public boolean wasCancelled() {
        return cancelled.get();
    }

    public long getDownloadedBytes() {
        return downloadedBytes.get();
    }

    public Path getDestination() {
        return destination;
    }

    private void download() {
        Path temporaryFile = null;
        try {
            if (cancelled.get()) {
                return;
            }

            Path parent = destination.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            String prefix = "." + destination.getFileName();
            while (prefix.length() < 3) {
                prefix += "_";
            }
            temporaryFile = Files.createTempFile(parent, prefix, ".part");

            HttpURLConnection httpConnection = (HttpURLConnection) source.toURL().openConnection();
            connection = httpConnection;
            httpConnection.setRequestMethod("GET");
            httpConnection.setConnectTimeout(10_000);
            httpConnection.setReadTimeout(15_000);
            httpConnection.setInstanceFollowRedirects(true);

            if (cancelled.get()) {
                return;
            }
            int statusCode = httpConnection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new IOException("сервер вернул HTTP " + statusCode);
            }

            try (InputStream input = new BufferedInputStream(httpConnection.getInputStream());
                 OutputStream output = new BufferedOutputStream(
                         new FileOutputStream(temporaryFile.toFile()))) {
                activeInput = input;
                activeOutput = output;

                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while (!cancelled.get() && (bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    downloadedBytes.addAndGet(bytesRead);
                }
                output.flush();
            } finally {
                activeInput = null;
                activeOutput = null;
            }

            synchronized (stateLock) {
                if (cancelled.get()) {
                    return;
                }

                try {
                    Files.move(temporaryFile, destination,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException exception) {
                    Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING);
                }
                temporaryFile = null;
                finished = true;
            }
            System.out.println("файл скачан: " + destination);
        } catch (IOException exception) {
            if (!cancelled.get()) {
                System.err.println("ошибка: " + exception.getMessage());
            }
        } catch (RuntimeException exception) {
            if (!cancelled.get()) {
                System.err.println("ошибка рантайма: " + exception.getMessage());
            }
        } finally {
            activeInput = null;
            activeOutput = null;
            HttpURLConnection currentConnection = connection;
            if (currentConnection != null) {
                currentConnection.disconnect();
            }
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException exception) {
                    System.err.println("не удалить временный файл: " + exception.getMessage());
                }
            }
            synchronized (stateLock) {
                finished = true;
            }
            if (cancelled.get()) {
                System.out.println("загрузка остановлена");
            }
            completion.countDown();
        }
    }

    private static void closeQuietly(AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception ignored) {
            // Поток закрывается ради отмены операции; повторная ошибка не нужна.
        }
    }

    public static void run(Scanner scanner) {
        System.out.print("Введите URL файла: ");
        String url = scanner.nextLine().trim();
        System.out.print("Введите путь для сохранения: ");
        String path = scanner.nextLine().trim();

        try {
            FileDownloadTask task = new FileDownloadTask(URI.create(url), Path.of(path));
            task.start();
            System.out.println("Загрузка началась в отдельном потоке");
            System.out.println("Введите stop для отмены");
            String command = scanner.nextLine().trim();
            if ("stop".equalsIgnoreCase(command)) {
                task.stop();
            }
            task.awaitCompletion();
        } catch (IllegalArgumentException exception) {
            System.err.println("ошибка: " + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println("Ожидание загрузки прервано");
        }
    }

    public static void main(String[] args) {
        run(new Scanner(System.in));
    }
}
