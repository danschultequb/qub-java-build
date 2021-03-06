package qub;

public interface JavacProcessBuilderTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavacProcessBuilder.class, () ->
        {
            runner.testGroup("get(RealDesktopProcess)", () ->
            {
                runner.test("with null RealDesktopProcess", (Test test) ->
                {
                    test.assertThrows(() -> JavacProcessBuilder.get((RealDesktopProcess)null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with non-null RealDesktopProcess", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();
                        test.assertNotNull(builder);
                        test.assertEqual(Path.parse("javac"), builder.getExecutablePath());
                        test.assertEqual(Iterable.create(), builder.getArguments());
                        test.assertEqual(process.getCurrentFolderPath(), builder.getWorkingFolderPath());
                    }
                });
            });

            runner.testGroup("get(ProcessFactory)", () ->
            {
                runner.test("with null ProcessFactory", (Test test) ->
                {
                    test.assertThrows(() -> JavacProcessBuilder.get((ProcessFactory)null),
                        new PreConditionFailure("processFactory cannot be null."));
                });

                runner.test("with non-null ProcessFactory",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final InMemoryFileSystem fileSystem = process.getFileSystem();
                    final Folder workingFolder = fileSystem.getFolder("/fake/working/folder/").await();
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(process.getParallelAsyncRunner(), workingFolder);
                    final JavacProcessBuilder builder = JavacProcessBuilder.get(processFactory).await();
                    test.assertNotNull(builder);
                    test.assertEqual(Path.parse("javac"), builder.getExecutablePath());
                    test.assertEqual(Iterable.create(), builder.getArguments());
                    test.assertEqual(workingFolder.getPath(), builder.getWorkingFolderPath());
                });
            });

            runner.testGroup("addOutputFolder(Folder)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertThrows(() -> builder.addOutputFolder((Folder)null),
                            new PreConditionFailure("outputFolder cannot be null."));

                        test.assertEqual(Iterable.create(), builder.getArguments());
                    }
                });

                runner.test("with non-null", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        final Folder outputsFolder = process.getCurrentFolder().getFolder("outputs").await();
                        test.assertSame(builder, builder.addOutputFolder(outputsFolder));
                        test.assertEqual(Iterable.create("-d", "outputs"), builder.getArguments());
                    }
                });

                runner.test("with non-null when output folder has already been added", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        final Folder outputsFolder = process.getCurrentFolder().getFolder("outputs").await();
                        test.assertSame(builder, builder.addOutputFolder(outputsFolder));
                        test.assertEqual(Iterable.create("-d", "outputs"), builder.getArguments());

                        final Folder binFolder = process.getCurrentFolder().getFolder("bin").await();
                        test.assertSame(builder, builder.addOutputFolder(binFolder));
                        test.assertEqual(Iterable.create("-d", "outputs", "-d", "bin"), builder.getArguments());
                    }
                });
            });

            runner.testGroup("addXlintUnchecked()", () ->
            {
                runner.test("when it hasn't been called yet", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertSame(builder, builder.addXlintUnchecked());
                        test.assertEqual(Iterable.create("-Xlint:unchecked"), builder.getArguments());
                    }
                });

                runner.test("when it has already been called", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertSame(builder, builder.addXlintUnchecked());
                        test.assertEqual(Iterable.create("-Xlint:unchecked"), builder.getArguments());

                        test.assertSame(builder, builder.addXlintUnchecked());
                        test.assertEqual(Iterable.create("-Xlint:unchecked", "-Xlint:unchecked"), builder.getArguments());
                    }
                });
            });

            runner.testGroup("addXlintDeprecation()", () ->
            {
                runner.test("when it hasn't been called yet", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertSame(builder, builder.addXlintDeprecation());
                        test.assertEqual(Iterable.create("-Xlint:deprecation"), builder.getArguments());
                    }
                });

                runner.test("when it has already been called", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertSame(builder, builder.addXlintDeprecation());
                        test.assertEqual(Iterable.create("-Xlint:deprecation"), builder.getArguments());

                        test.assertSame(builder, builder.addXlintDeprecation());
                        test.assertEqual(Iterable.create("-Xlint:deprecation", "-Xlint:deprecation"), builder.getArguments());
                    }
                });
            });

            runner.testGroup("addJavaSourceVersion()", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertThrows(() -> builder.addJavaSourceVersion(null),
                            new PreConditionFailure("javaSourceVersion cannot be null."));

                        test.assertEqual(Iterable.create(), builder.getArguments());
                    }
                });

                runner.test("with empty", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertThrows(() -> builder.addJavaSourceVersion(""),
                            new PreConditionFailure("javaSourceVersion cannot be empty."));

                        test.assertEqual(Iterable.create(), builder.getArguments());
                    }
                });

                runner.test("with non-empty", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertSame(builder, builder.addJavaSourceVersion("foo"));

                        test.assertEqual(Iterable.create("-source", "foo"), builder.getArguments());
                    }
                });
            });

            runner.testGroup("addJavaTargetVersion()", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertThrows(() -> builder.addJavaTargetVersion(null),
                            new PreConditionFailure("javaTargetVersion cannot be null."));

                        test.assertEqual(Iterable.create(), builder.getArguments());
                    }
                });

                runner.test("with empty", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertThrows(() -> builder.addJavaTargetVersion(""),
                            new PreConditionFailure("javaTargetVersion cannot be empty."));

                        test.assertEqual(Iterable.create(), builder.getArguments());
                    }
                });

                runner.test("with non-empty", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertSame(builder, builder.addJavaTargetVersion("bar"));

                        test.assertEqual(Iterable.create("-target", "bar"), builder.getArguments());
                    }
                });
            });

            runner.testGroup("addBootClasspath(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertThrows(() -> builder.addBootClasspath((String)null),
                            new PreConditionFailure("bootClasspath cannot be null."));

                        test.assertEqual(Iterable.create(), builder.getArguments());
                    }
                });

                runner.test("with empty", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertThrows(() -> builder.addBootClasspath(""),
                            new PreConditionFailure("bootClasspath cannot be empty."));

                        test.assertEqual(Iterable.create(), builder.getArguments());
                    }
                });

                runner.test("with non-empty", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertSame(builder, builder.addBootClasspath("bar"));

                        test.assertEqual(Iterable.create("-bootclasspath", "bar"), builder.getArguments());
                    }
                });
            });

            runner.testGroup("addBootClasspath(Path)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertThrows(() -> builder.addBootClasspath((Path)null),
                            new PreConditionFailure("bootClasspath cannot be null."));

                        test.assertEqual(Iterable.create(), builder.getArguments());
                    }
                });

                runner.test("with relative path", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertSame(builder, builder.addBootClasspath(Path.parse("bar")));

                        test.assertEqual(Iterable.create("-bootclasspath", "bar"), builder.getArguments());
                    }
                });

                runner.test("with rooted path", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertSame(builder, builder.addBootClasspath(Path.parse("/bar")));

                        test.assertEqual(Iterable.create("-bootclasspath", "/bar"), builder.getArguments());
                    }
                });
            });

            runner.testGroup("addBootClasspath(File)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertThrows(() -> builder.addBootClasspath((File)null),
                            new PreConditionFailure("bootClasspath cannot be null."));

                        test.assertEqual(Iterable.create(), builder.getArguments());
                    }
                });

                runner.test("with non-null", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();
                        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create();
                        final File bootClasspath = fileSystem.getFile("/folder/file").await();

                        test.assertSame(builder, builder.addBootClasspath(bootClasspath));

                        test.assertEqual(Iterable.create("-bootclasspath", "/folder/file"), builder.getArguments());
                    }
                });
            });

            runner.testGroup("addClasspath(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertThrows(() -> builder.addClasspath((String)null),
                            new PreConditionFailure("classpath cannot be null."));

                        test.assertEqual(Iterable.create(), builder.getArguments());
                    }
                });

                runner.test("with empty", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.assertThrows(() -> builder.addClasspath(""),
                            new PreConditionFailure("classpath cannot be empty."));

                        test.assertEqual(Iterable.create(), builder.getArguments());
                    }
                });

                runner.test("with non-empty", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.<JavacProcessBuilder>assertSame(builder, builder.addClasspath("hello"));

                        test.assertEqual(Iterable.create("-classpath", "hello"), builder.getArguments());
                    }
                });

                runner.test("with non-empty when classpath already added", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        test.<JavacProcessBuilder>assertSame(builder, builder.addClasspath("hello"));
                        test.assertEqual(Iterable.create("-classpath", "hello"), builder.getArguments());

                        test.<JavacProcessBuilder>assertSame(builder, builder.addClasspath("there"));
                        test.assertEqual(Iterable.create("-classpath", "hello", "-classpath", "there"), builder.getArguments());
                    }
                });
            });

            runner.testGroup("compile()", () ->
            {
                runner.test("with null warnings", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        final Warnings warnings = null;
                        final InMemoryCharacterToByteStream stream = InMemoryCharacterToByteStream.create();
                        final VerboseCharacterToByteWriteStream verbose = VerboseCharacterToByteWriteStream.create(stream);
                        test.assertThrows(() -> builder.compile(warnings, verbose).await(),
                            new PreConditionFailure("warnings cannot be null."));
                        test.assertEqual("", stream.getText().await());
                    }
                });

                runner.test("with null verbose", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final JavacProcessBuilder builder = JavacProcessBuilder.get(process).await();

                        final Warnings warnings = Warnings.Show;
                        final VerboseCharacterToByteWriteStream verbose = null;
                        test.assertThrows(() -> builder.compile(warnings, verbose).await(),
                            new PreConditionFailure("verbose cannot be null."));
                    }
                });

                runner.test("with Java file that doesn't exist", (Test test) ->
                {
                    try (final RealDesktopProcess process = RealDesktopProcess.create())
                    {
                        final JavacProcessBuilder javac = JavacProcessBuilder.get(process).await();
                        final Folder currentFolder = process.getCurrentFolder();
                        final Folder rootFolder = currentFolder.createFolder("temp").await();
                        try
                        {
                            javac.setWorkingFolder(rootFolder);
                            final Folder outputsFolder = rootFolder.getFolder("outputs").await();
                            javac.addOutputFolder(outputsFolder);

                            final File aJavaFile = rootFolder.getFile("sources/A.java").await();
                            javac.addSourceFile(aJavaFile);

                            final Warnings warnings = Warnings.Show;
                            final InMemoryCharacterToByteStream stream = InMemoryCharacterToByteStream.create();
                            final VerboseCharacterToByteWriteStream verbose = VerboseCharacterToByteWriteStream.create(stream);

                            final JavaCompilationResult result = javac.compile(warnings, verbose).await();

                            test.assertNotNull(result);
                            test.assertEqual(2, result.exitCode);
                            test.assertEqual("", result.output);
                            test.assertEqual(
                                Iterable.create(
                                    "error: file not found: sources\\A.java",
                                    "Usage: javac <options> <source files>",
                                    "use --help for a list of possible options"),
                                Strings.getLines(result.error));
                            test.assertEqual(Iterable.create(), result.issues);
                            test.assertFalse(QubBuildCompile.getClassFile(aJavaFile, rootFolder, outputsFolder).exists().await());
                            test.assertEqual(
                                Iterable.create(
                                    "VERBOSE: Running " + rootFolder + ": javac -d outputs sources/A.java...",
                                    "VERBOSE: error: file not found: sources\\A.java",
                                    "VERBOSE: Usage: javac <options> <source files>",
                                    "VERBOSE: use --help for a list of possible options"),
                                Strings.getLines(stream.getText().await()));
                        }
                        finally
                        {
                            test.assertNull(rootFolder.delete().await());
                        }
                    }
                });

                runner.test("with empty Java file", (Test test) ->
                {
                    try (final RealDesktopProcess process = RealDesktopProcess.create())
                    {
                        final JavacProcessBuilder javac = JavacProcessBuilder.get(process).await();
                        final Folder currentFolder = process.getCurrentFolder();
                        final Folder rootFolder = currentFolder.createFolder("temp").await();
                        try
                        {
                            final Folder outputsFolder = rootFolder.getFolder("outputs").await();
                            javac.addOutputFolder(outputsFolder);

                            final File bJavaFile = rootFolder.createFile("sources/B.java").await();
                            javac.addSourceFile(bJavaFile);

                            final Warnings warnings = Warnings.Show;
                            final InMemoryCharacterToByteStream stream = InMemoryCharacterToByteStream.create();
                            final VerboseCharacterToByteWriteStream verbose = VerboseCharacterToByteWriteStream.create(stream);

                            final JavaCompilationResult result = javac.compile(warnings, verbose).await();
                            test.assertNotNull(result);
                            test.assertEqual(0, result.exitCode);
                            test.assertEqual("", result.output);
                            test.assertEqual("", result.error);
                            test.assertEqual(Iterable.create(), result.issues);
                            test.assertFalse(QubBuildCompile.getClassFile(bJavaFile, rootFolder, outputsFolder).exists().await());
                        }
                        finally
                        {
                            test.assertNull(rootFolder.delete().await());
                        }
                    }
                });

                runner.test("with no errors", (Test test) ->
                {
                    try (final RealDesktopProcess process = RealDesktopProcess.create())
                    {
                        final JavacProcessBuilder javac = JavacProcessBuilder.get(process).await();
                        final Folder currentFolder = process.getCurrentFolder();
                        final Folder rootFolder = currentFolder.createFolder("temp").await();
                        try
                        {
                            javac.setWorkingFolder(rootFolder);
                            final Folder outputsFolder = rootFolder.getFolder("outputs").await();
                            javac.addOutputFolder(outputsFolder);

                            final File cJavaFile = rootFolder.getFile("sources/C.java").await();
                            cJavaFile.setContentsAsString(Strings.join('\n', Iterable.create(
                                "public class C",
                                "{",
                                "  private int value;",
                                "  public int getValue()",
                                "  {",
                                "    return value;",
                                "  }",
                                "}")));
                            javac.addSourceFile(cJavaFile);

                            final Warnings warnings = Warnings.Show;
                            final InMemoryCharacterToByteStream stream = InMemoryCharacterToByteStream.create();
                            final VerboseCharacterToByteWriteStream verbose = VerboseCharacterToByteWriteStream.create(stream);

                            final JavaCompilationResult result = javac.compile(warnings, verbose).await();
                            test.assertNotNull(result);
                            test.assertEqual(0, result.exitCode);
                            test.assertEqual("", result.output);
                            test.assertEqual("", result.error);
                            test.assertEqual(Iterable.create(), result.issues);
                            test.assertTrue(QubBuildCompile.getClassFile(cJavaFile, rootFolder, outputsFolder).exists().await());
                            test.assertEqual(
                                Iterable.create("VERBOSE: Running " + rootFolder + ": javac -d outputs sources/C.java..."),
                                Strings.getLines(stream.getText().await()));
                        }
                        finally
                        {
                            test.assertNull(rootFolder.delete().await());
                        }
                    }
                });

                runner.test("with \"error: class MyTestClass is public, should be declared in a file named MyTestClass.java\"", (Test test) ->
                {
                    try (final RealDesktopProcess process = RealDesktopProcess.create())
                    {
                        final JavacProcessBuilder javac = JavacProcessBuilder.get(process).await();
                        final Folder currentFolder = process.getCurrentFolder();
                        final Folder rootFolder = currentFolder.createFolder("temp").await();
                        try
                        {
                            javac.setWorkingFolder(rootFolder);
                            final Folder outputsFolder = rootFolder.getFolder("outputs").await();
                            javac.addOutputFolder(outputsFolder);

                            final File cJavaFile = rootFolder.getFile("sources/C.java").await();
                            cJavaFile.setContentsAsString(Strings.join('\n', Iterable.create(
                                "public class MyTestClass",
                                "{",
                                "  private int value;",
                                "  public int getValue()",
                                "  {",
                                "    return value;",
                                "  }",
                                "}")));
                            javac.addSourceFile(cJavaFile);

                            final Warnings warnings = Warnings.Show;
                            final InMemoryCharacterToByteStream stream = InMemoryCharacterToByteStream.create();
                            final VerboseCharacterToByteWriteStream verbose = VerboseCharacterToByteWriteStream.create(stream);

                            final JavaCompilationResult result = javac.compile(warnings, verbose).await();
                            test.assertNotNull(result);
                            test.assertEqual(1, result.exitCode);
                            test.assertEqual("", result.output);
                            test.assertEqual(
                                Iterable.create(
                                    "sources\\C.java:1: error: class MyTestClass is public, should be declared in a file named MyTestClass.java",
                                    "public class MyTestClass",
                                    "       ^",
                                    "1 error"),
                                Strings.getLines(result.error));
                            test.assertEqual(
                                Iterable.create(
                                    JavaCompilerIssue.error(
                                        "sources/C.java",
                                        1, 8,
                                        "class MyTestClass is public, should be declared in a file named MyTestClass.java")),
                                result.issues);
                            test.assertFalse(QubBuildCompile.getClassFile(cJavaFile, rootFolder, outputsFolder).exists().await());
                            test.assertEqual(
                                Iterable.create(
                                    "VERBOSE: Running " + rootFolder + ": javac -d outputs sources/C.java...",
                                    "VERBOSE: sources\\C.java:1: error: class MyTestClass is public, should be declared in a file named MyTestClass.java",
                                    "VERBOSE: public class MyTestClass",
                                    "VERBOSE:        ^",
                                    "VERBOSE: 1 error"),
                                Strings.getLines(stream.getText().await()));
                        }
                        finally
                        {
                            test.assertNull(rootFolder.delete().await());
                        }
                    }
                });

                runner.test("with \"error: class, interface, or enum expected\"", (Test test) ->
                {
                    try (final RealDesktopProcess process = RealDesktopProcess.create())
                    {
                        final JavacProcessBuilder javac = JavacProcessBuilder.get(process).await();
                        final Folder currentFolder = process.getCurrentFolder();
                        final Folder rootFolder = currentFolder.createFolder("temp").await();
                        try
                        {
                            javac.setWorkingFolder(rootFolder);
                            final Folder outputsFolder = rootFolder.getFolder("outputs").await();
                            javac.addOutputFolder(outputsFolder);

                            final File cJavaFile = rootFolder.getFile("sources/C.java").await();
                            cJavaFile.setContentsAsString("Im not a valid Java file");
                            javac.addSourceFile(cJavaFile);

                            final Warnings warnings = Warnings.Show;
                            final InMemoryCharacterToByteStream stream = InMemoryCharacterToByteStream.create();
                            final VerboseCharacterToByteWriteStream verbose = VerboseCharacterToByteWriteStream.create(stream);

                            final JavaCompilationResult result = javac.compile(warnings, verbose).await();
                            test.assertNotNull(result);
                            test.assertEqual(1, result.exitCode);
                            test.assertEqual("", result.output);
                            test.assertEqual(
                                Iterable.create(
                                    "sources\\C.java:1: error: class, interface, or enum expected",
                                    "Im not a valid Java file",
                                    "^",
                                    "1 error"),
                                Strings.getLines(result.error));
                            test.assertEqual(Iterable.create(
                                JavaCompilerIssue.error(
                                    "sources/C.java",
                                    1, 1,
                                    "class, interface, or enum expected")),
                                result.issues);
                            test.assertFalse(QubBuildCompile.getClassFile(cJavaFile, rootFolder, outputsFolder).exists().await());
                            test.assertEqual(
                                Iterable.create(
                                    "VERBOSE: Running " + rootFolder + ": javac -d outputs sources/C.java...",
                                    "VERBOSE: sources\\C.java:1: error: class, interface, or enum expected",
                                    "VERBOSE: Im not a valid Java file",
                                    "VERBOSE: ^",
                                    "VERBOSE: 1 error"),
                                Strings.getLines(stream.getText().await()));
                        }
                        finally
                        {
                            test.assertNull(rootFolder.delete().await());
                        }
                    }
                });

                runner.test("with \"error: ';' expected\" and \"error: reached end of file while parsing\"", (Test test) ->
                {
                    try (final RealDesktopProcess process = RealDesktopProcess.create())
                    {
                        final JavacProcessBuilder javac = JavacProcessBuilder.get(process).await();
                        final Folder currentFolder = process.getCurrentFolder();
                        final Folder rootFolder = currentFolder.createFolder("temp").await();
                        try
                        {
                            javac.setWorkingFolder(rootFolder);
                            final Folder outputsFolder = rootFolder.getFolder("outputs").await();
                            javac.addOutputFolder(outputsFolder);

                            final File cJavaFile = rootFolder.getFile("sources/C.java").await();
                            cJavaFile.setContentsAsString(Strings.join('\n', Iterable.create(
                                "public class MyTestClass",
                                "{",
                                "  private int value;",
                                "  public int getValue()",
                                "  {",
                                "    return value",
                                "  }",
                                "")))
                                .await();
                            javac.addSourceFile(cJavaFile.getPath().relativeTo(rootFolder));

                            final Warnings warnings = Warnings.Show;
                            final InMemoryCharacterToByteStream stream = InMemoryCharacterToByteStream.create();
                            final VerboseCharacterToByteWriteStream verbose = VerboseCharacterToByteWriteStream.create(stream);

                            final JavaCompilationResult result = javac.compile(warnings, verbose).await();
                            test.assertNotNull(result);
                            test.assertEqual(1, result.exitCode);
                            test.assertEqual("", result.output);
                            test.assertEqual(
                                Iterable.create(
                                    "sources\\C.java:6: error: \';\' expected",
                                    "    return value",
                                    "                ^",
                                    "sources\\C.java:7: error: reached end of file while parsing",
                                    "  }",
                                    "   ^",
                                    "2 errors"),
                                Strings.getLines(result.error));
                            test.assertEqual(
                                Iterable.create(
                                    JavaCompilerIssue.error(
                                        "sources/C.java",
                                        6, 17,
                                        "';' expected"),
                                    JavaCompilerIssue.error(
                                        "sources/C.java",
                                        7, 4,
                                        "reached end of file while parsing")),
                                result.issues);
                            test.assertFalse(QubBuildCompile.getClassFile(cJavaFile, rootFolder, outputsFolder).exists().await());
                            test.assertEqual(
                                Iterable.create(
                                    "VERBOSE: Running " + rootFolder + ": javac -d outputs sources/C.java...",
                                    "VERBOSE: sources\\C.java:6: error: ';' expected",
                                    "VERBOSE:     return value",
                                    "VERBOSE:                 ^",
                                    "VERBOSE: sources\\C.java:7: error: reached end of file while parsing",
                                    "VERBOSE:   }",
                                    "VERBOSE:    ^",
                                    "VERBOSE: 2 errors"),
                                Strings.getLines(stream.getText().await()));
                        }
                        finally
                        {
                            rootFolder.delete().await();
                        }
                    }
                });
            });

            runner.testGroup("getVersion(CharacterWriteStream)", () ->
            {
                runner.test("with null verbose", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final InMemoryCharacterToByteStream verbose = null;
                        final JavacProcessBuilder javacProcessBuilder = JavacProcessBuilder.get(process).await();

                        test.assertThrows(() -> javacProcessBuilder.getVersion(verbose),
                            new PreConditionFailure("verbose cannot be null."));
                    }
                });
                runner.test("with no javac arguments", (Test test) ->
                {
                    try (final RealDesktopProcess process = RealDesktopProcess.create())
                    {
                        final InMemoryCharacterToByteStream verbose = InMemoryCharacterToByteStream.create();
                        final JavacProcessBuilder javacProcessBuilder = JavacProcessBuilder.get(process).await();

                        final VersionNumber result = javacProcessBuilder.getVersion(verbose).await();
                        test.assertEqual(VersionNumber.parse("15.0.2").await(), result);
                    }
                });
            });
        });
    }
}
