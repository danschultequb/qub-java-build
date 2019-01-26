package qub;

public class FakeJavaCompilerTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(FakeJavaCompiler.class, () ->
        {
            runner.test("constructor()", (Test test) ->
            {
                final FakeJavaCompiler compiler = new FakeJavaCompiler();
                test.assertEqual(0, compiler.getExitCode());
            });

            runner.testGroup("setExitCode()", () ->
            {
                runner.test("with 0", (Test test) ->
                {
                    final FakeJavaCompiler compiler = new FakeJavaCompiler();
                    compiler.setExitCode(0);
                    test.assertEqual(0, compiler.getExitCode());
                });

                runner.test("with 1", (Test test) ->
                {
                    final FakeJavaCompiler compiler = new FakeJavaCompiler();
                    compiler.setExitCode(1);
                    test.assertEqual(1, compiler.getExitCode());
                });
            });

            runner.testGroup("compile()", () ->
            {
                runner.test("with null sourceFiles", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                    fileSystem.createRoot("/");
                    final Iterable<File> sourceFiles = null;
                    final Folder rootFolder = fileSystem.getFolder("/").throwErrorOrGetValue();
                    final Folder outputFolder = fileSystem.getFolder("/outputs").throwErrorOrGetValue();

                    final FakeJavaCompiler compiler = new FakeJavaCompiler();
                    test.assertThrows(() -> compiler.compile(sourceFiles, rootFolder, outputFolder, "1.8", new Console()), new PreConditionFailure("sourceFiles cannot be null."));
                });

                runner.test("with empty sourceFiles", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                    fileSystem.createRoot("/");
                    final Iterable<File> sourceFiles = Iterable.empty();
                    final Folder rootFolder = fileSystem.getFolder("/").throwErrorOrGetValue();
                    final Folder outputFolder = fileSystem.getFolder("/outputs").throwErrorOrGetValue();

                    final FakeJavaCompiler compiler = new FakeJavaCompiler();
                    test.assertThrows(() -> compiler.compile(sourceFiles, rootFolder, outputFolder, "1.8", new Console()), new PreConditionFailure("sourceFiles cannot be empty."));
                });

                runner.test("with null outputFolder", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                    fileSystem.createRoot("/");
                    final Iterable<File> sourceFiles = Iterable.create(
                        fileSystem.createFile("/sources/a/B.java").throwErrorOrGetValue());
                    final Folder rootFolder = fileSystem.getFolder("/").throwErrorOrGetValue();
                    final Folder outputFolder = null;

                    final FakeJavaCompiler compiler = new FakeJavaCompiler();
                    test.assertThrows(() -> compiler.compile(sourceFiles, rootFolder, outputFolder, "1.8", new Console()), new PreConditionFailure("outputFolder cannot be null."));
                });

                runner.test("with null console", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                    fileSystem.createRoot("/");
                    final Iterable<File> sourceFiles = Iterable.create(
                        fileSystem.createFile("/sources/a/B.java").throwErrorOrGetValue());
                    final Folder rootFolder = fileSystem.getFolder("/").throwErrorOrGetValue();
                    final Folder outputFolder = fileSystem.getFolder("/outputs").throwErrorOrGetValue();

                    final FakeJavaCompiler compiler = new FakeJavaCompiler();
                    test.assertThrows(() -> compiler.compile(sourceFiles, rootFolder, outputFolder, "8", null), new PreConditionFailure("console cannot be null."));
                });

                runner.test("with null javaVersion", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                    fileSystem.createRoot("/");
                    final Iterable<File> sourceFiles = Iterable.create(
                        fileSystem.createFile("/sources/a/B.java").throwErrorOrGetValue());
                    final Folder rootFolder = fileSystem.getFolder("/").throwErrorOrGetValue();
                    final Folder outputFolder = fileSystem.getFolder("/outputs").throwErrorOrGetValue();

                    final FakeJavaCompiler compiler = new FakeJavaCompiler();
                    final JavaCompilationResult result = compiler.compile(sourceFiles, rootFolder, outputFolder, null, new Console()).throwErrorOrGetValue();
                    test.assertNotNull(result);
                    test.assertEqual(0, result.getExitCode());
                    test.assertSuccess(true, fileSystem.getFile("/outputs/a/B.class").thenResult(File::exists));
                });

                runner.test("with empty javaVersion", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                    fileSystem.createRoot("/");
                    final Iterable<File> sourceFiles = Iterable.create(
                        fileSystem.createFile("/sources/a/B.java").throwErrorOrGetValue());
                    final Folder rootFolder = fileSystem.getFolder("/").throwErrorOrGetValue();
                    final Folder outputFolder = fileSystem.getFolder("/outputs").throwErrorOrGetValue();

                    final FakeJavaCompiler compiler = new FakeJavaCompiler();
                    final JavaCompilationResult result = compiler.compile(sourceFiles, rootFolder, outputFolder, "", new Console()).throwErrorOrGetValue();
                    test.assertNotNull(result);
                    test.assertEqual(0, result.getExitCode());
                    test.assertSuccess(true, fileSystem.getFile("/outputs/a/B.class").thenResult(File::exists));
                });

                runner.test("with \"1.8\" javaVersion", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                    fileSystem.createRoot("/");
                    final Iterable<File> sourceFiles = Iterable.create(
                        fileSystem.createFile("/sources/a/B.java").throwErrorOrGetValue());
                    final Folder rootFolder = fileSystem.getFolder("/").throwErrorOrGetValue();
                    final Folder outputFolder = fileSystem.getFolder("/outputs").throwErrorOrGetValue();

                    final FakeJavaCompiler compiler = new FakeJavaCompiler();
                    final JavaCompilationResult result = compiler.compile(sourceFiles, rootFolder, outputFolder, "1.8", new Console()).throwErrorOrGetValue();
                    test.assertNotNull(result);
                    test.assertEqual(0, result.getExitCode());
                    test.assertSuccess(true, fileSystem.getFile("/outputs/a/B.class").thenResult(File::exists));
                });
            });
        });
    }
}