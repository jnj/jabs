package jabs;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.nio.file.StandardWatchEventKinds.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Properties properties = loadProperties();
        String gitDir = properties.getProperty("git.dir");
        GitWatcher gitWatcher = new GitWatcher(gitDir);
        System.out.println("git dir is " + gitDir);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        ArrayList<File> javaFiles = new ArrayList<>();
        Path src = Paths.get(new File(gitDir).getParentFile().toPath().toString(), "src");
        FileSupport.findFiles(src.toFile(), ".java", javaFiles);

        System.out.println("Java files:");
        for (File file : javaFiles) {
            System.out.println(file.getAbsolutePath());
        }

        FileSystem fileSystem = FileSystems.getDefault();
        WatchService fsWatchService = fileSystem.newWatchService();
        Path path = new File(gitDir).toPath();
        WatchKey watchKey = path.register(fsWatchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

        while (true) {
            WatchKey key = fsWatchService.take();
            if (key.equals(watchKey)) {
                boolean change = false;

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == OVERFLOW) {
                        continue;
                    }
                    change = true;
                }

                if (change) {
                    if (gitWatcher.hasLastCommitChanged()) {
                        System.out.println("Starting build...");
                        compileFiles(compiler, javaFiles);
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
    }

    private static Properties loadProperties() throws IOException {
        URL propFile = Thread.currentThread().getContextClassLoader().getResource("jabs.properties");
        Properties properties = new Properties();
        properties.load(propFile.openStream());
        return properties;
    }

    static void compileFiles(JavaCompiler compiler, List<File> srcFiles) {
        System.out.println("compiler: " + compiler.toString());
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, null, null)) {
            Iterable<? extends JavaFileObject> javaFiles = fileManager.getJavaFileObjectsFromFiles(srcFiles);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticCollector, null, null, javaFiles);
            Boolean success = task.call();
            System.out.println("Successful compile? " + success);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
