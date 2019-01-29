package qub;

public class Build
{
    private JavaCompiler javaCompiler;

    /**
     * Get the JavaCompiler that should be used to compile Java source files. If no JavaCompiler has
     * been set, then the provided creator function will be used to initialize the JavaCompiler and
     * then the initialized JavaCompiler will be returned.
     * @param creator The creator function that will initialize the JavaCompiler if it hasn't been
     *                set yet.
     * @return The JavaCompiler to use to compile Java source files.
     */
    private JavaCompiler getJavaCompiler(Function0<JavaCompiler> creator)
    {
        PreCondition.assertNotNull(creator, "creator");

        if (javaCompiler == null)
        {
            javaCompiler = creator.run();
        }
        final JavaCompiler result = javaCompiler;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    /**
     * Set the JavaCompiler that will be used to compile Java source files.
     * @param javaCompiler The JavaCompiler that will be used to compile Java source files.
     */
    public void setJavaCompiler(JavaCompiler javaCompiler)
    {
        PreCondition.assertNotNull(javaCompiler, "javaCompiler");

        this.javaCompiler = javaCompiler;
    }


    public void main(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        final CommandLine commandLine = console.getCommandLine();
        if (commandLine.contains(Build::showUsage))
        {
            console.writeLine("Usage: qub-build");
            console.writeLine("  Used to compile and package source code projects.");
        }
        else
        {
            final Stopwatch stopwatch = console.getStopwatch();
            stopwatch.start();

            console.write("Compiling...");

            final Folder rootFolder = console.getCurrentFolder().throwErrorOrGetValue();
            final File projectJsonFile = rootFolder.getFile("project.json").throwErrorOrGetValue();
            final ProjectJSON projectJson = ProjectJSON.parse(projectJsonFile).throwErrorOrGetValue();
            final ProjectJSONJava projectJsonJava = projectJson.getJava();
            if (projectJsonJava == null)
            {
                console.writeLine();
                console.writeLine(" No language specified in project.json. Nothing to compile.");
            }
            else
            {
                final JavaCompiler javaCompiler = getJavaCompiler(JavacJavaCompiler::new);
                javaCompiler.checkJavaVersion(projectJsonJava.getVersion(), console)
                    .then(() ->
                    {
                        final Iterable<Dependency> dependencies = projectJsonJava.getDependencies();
                        if (!Iterable.isNullOrEmpty(dependencies))
                        {
                            final String qubHome = console.getEnvironmentVariable("QUB_HOME");
                            if (Strings.isNullOrEmpty(qubHome))
                            {
                                throw new NotFoundException("Cannot resolve project dependencies without a QUB_HOME environment variable.");
                            }
                            javaCompiler.setQubFolder(console.getFileSystem().getFolder(qubHome).throwErrorOrGetValue());
                            javaCompiler.setDependencies(projectJsonJava.getDependencies());
                        }

                        Iterable<PathPattern> sourceFilePatterns = projectJsonJava.getSourceFilePatterns();
                        if (Iterable.isNullOrEmpty(sourceFilePatterns))
                        {
                            sourceFilePatterns = Iterable.create(PathPattern.parse("**/*.java"));
                        }
                        final Iterable<File> javaSourceFiles = getJavaSourceFiles(rootFolder, sourceFilePatterns).throwErrorOrGetValue();

                        String outputFolderName = "outputs";
                        if (!Strings.isNullOrEmpty(projectJsonJava.getOutputFolder()))
                        {
                            outputFolderName = projectJsonJava.getOutputFolder();
                        }
                        final Folder outputsFolder = rootFolder.getFolder(outputFolderName).throwErrorOrGetValue();
                        final File parseJsonFile = outputsFolder.getFile("parse.json").throwErrorOrGetValue();
                        final List<File> newJavaSourceFiles = List.create();
                        final List<File> deletedJavaSourceFiles = List.create();
                        final List<File> modifiedJavaSourceFiles = List.create();
                        final List<File> nonModifiedJavaSourceFiles = List.create();
                        final List<ParseJSONSourceFile> parseJsonSourceFiles = List.create();
                        final Value<Boolean> compileEverything = Value.create(false);
                        outputsFolder.create()
                            .then(() ->
                            {
                                compileEverything.set(true);
                                newJavaSourceFiles.addAll(javaSourceFiles);
                                parseJsonSourceFiles.addAll(ParseJSONSourceFile.create(javaSourceFiles, rootFolder));
                            })
                            .catchError(FolderAlreadyExistsException.class, () ->
                            {
                                ParseJSON.parse(parseJsonFile).then((ParseJSON parseJson) ->
                                {
                                    compileEverything.set(shouldCompileEverything(parseJson.getProjectJson(), projectJson));

                                    for (final File javaSourceFile : javaSourceFiles)
                                    {
                                        final Path javaSourceFileRelativePath = javaSourceFile.relativeTo(rootFolder);

                                        parseJson.getSourceFile(javaSourceFileRelativePath)
                                            .then((ParseJSONSourceFile parseJsonSource) ->
                                            {
                                                if (!javaSourceFile.getLastModified().throwErrorOrGetValue().equals(parseJsonSource.getLastModified()))
                                                {
                                                    modifiedJavaSourceFiles.add(javaSourceFile);
                                                    parseJsonSourceFiles.add(ParseJSONSourceFile.create(javaSourceFile, rootFolder, javaSourceFiles));
                                                }
                                                else
                                                {
                                                    nonModifiedJavaSourceFiles.add(javaSourceFile);
                                                    parseJsonSourceFiles.add(parseJsonSource);
                                                }
                                            })
                                            .catchError(NotFoundException.class, () ->
                                            {
                                                newJavaSourceFiles.add(javaSourceFile);
                                                parseJsonSourceFiles.add(ParseJSONSourceFile.create(javaSourceFile, rootFolder, javaSourceFiles));
                                            });
                                    }

                                    for (final ParseJSONSourceFile parseJsonSource : parseJson.getSourceFiles())
                                    {
                                        final Path parseJsonSourceFilePath = parseJsonSource.getRelativePath();
                                        final File parseJsonSourceFile = rootFolder.getFile(parseJsonSourceFilePath).throwErrorOrGetValue();
                                        if (!javaSourceFiles.contains(parseJsonSourceFile))
                                        {
                                            deletedJavaSourceFiles.add(parseJsonSourceFile);
                                        }
                                    }
                                })
                                .catchError(FileNotFoundException.class, () ->
                                {
                                    compileEverything.set(true);
                                    newJavaSourceFiles.addAll(javaSourceFiles);
                                    parseJsonSourceFiles.addAll(ParseJSONSourceFile.create(javaSourceFiles, rootFolder));
                                })
                                .throwError();
                            })
                            .throwError();

                        final ParseJSON updatedParseJson = new ParseJSON();
                        updatedParseJson.setProjectJson(projectJson);
                        updatedParseJson.setSourceFiles(parseJsonSourceFiles);
                        updatedParseJson.write(parseJsonFile);

                        for (final File deletedSourceFile : deletedJavaSourceFiles)
                        {
                            final File classFile = getClassFile(deletedSourceFile, rootFolder, outputsFolder);
                            classFile.delete().catchError(java.io.FileNotFoundException.class, () -> {});
                        }

                        final Set<File> javaSourceFilesToCompile = Set.create();
                        if (compileEverything.get())
                        {
                            javaSourceFilesToCompile.addAll(javaSourceFiles);
                        }
                        else
                        {
                            javaSourceFilesToCompile.addAll(newJavaSourceFiles);
                            javaSourceFilesToCompile.addAll(modifiedJavaSourceFiles);
                            javaSourceFilesToCompile.addAll(nonModifiedJavaSourceFiles.where((File javaSourceFile) ->
                            {
                                final Path javaSourceFileRelativePath = javaSourceFile.relativeTo(rootFolder);
                                final ParseJSONSourceFile parseJsonSourceFile = updatedParseJson.getSourceFile(javaSourceFileRelativePath).throwErrorOrGetValue();
                                final Iterable<Path> dependencyRelativeFilePaths = parseJsonSourceFile.getDependencies();
                                boolean shouldCompile = false;
                                if (!Iterable.isNullOrEmpty(dependencyRelativeFilePaths))
                                {
                                    final Iterable<File> dependencyFiles = dependencyRelativeFilePaths
                                        .map((Path dependencyRelativeFilePath) ->
                                            rootFolder.getFile(dependencyRelativeFilePath).throwErrorOrGetValue());
                                    for (final File dependencyFile : dependencyFiles)
                                    {
                                        if (modifiedJavaSourceFiles.contains(dependencyFile) ||
                                            deletedJavaSourceFiles.contains(dependencyFile))
                                        {
                                            shouldCompile = true;
                                            break;
                                        }
                                    }
                                }
                                return shouldCompile;
                            }));
                        }

                        if (javaSourceFilesToCompile.any())
                        {
                            javaCompiler
                                .compile(javaSourceFilesToCompile, rootFolder, outputsFolder, console)
                                .throwError();
                        }
                    })
                    .catchError((Throwable error) ->
                    {
                        console.writeLine();
                        console.writeLine(" " + error.getMessage());
                    });
            }

            final Duration compilationDuration = stopwatch.stop().toSeconds();
            console.writeLine(" Done (" + compilationDuration.toString("0.0") + ")");
        }
    }

    public static boolean shouldCompileEverything(ProjectJSON oldProjectJson, ProjectJSON newProjectJson)
    {
        boolean result = false;

        if (oldProjectJson != null && oldProjectJson.getJava() != null &&
            newProjectJson != null && newProjectJson.getJava() != null)
        {
            final ProjectJSONJava oldProjectJsonJava = oldProjectJson.getJava();
            final ProjectJSONJava newProjectJsonJava = newProjectJson.getJava();

            if (!result)
            {
                final String oldProjectJsonJavaVersion = oldProjectJsonJava.getVersion();
                final String newProjectJsonJavaVersion = newProjectJsonJava.getVersion();
                if (!Comparer.equal(oldProjectJsonJavaVersion, newProjectJsonJavaVersion))
                {
                    result = !isJava8(oldProjectJsonJavaVersion) || !isJava8(newProjectJsonJavaVersion);
                }
            }

            if (!result)
            {
                final Iterable<Dependency> oldProjectJsonJavaDependencies = oldProjectJsonJava.getDependencies();
                final Iterable<Dependency> newProjectJsonJavaDependencies = newProjectJsonJava.getDependencies();
                if (!Iterable.isNullOrEmpty(oldProjectJsonJavaDependencies))
                {
                    result = Iterable.isNullOrEmpty(newProjectJsonJavaDependencies) ||
                        oldProjectJsonJavaDependencies.contains((Dependency oldDependency) ->
                            !newProjectJsonJavaDependencies.contains(oldDependency));
                }
            }
        }

        return result;
    }

    public static boolean isJava8(String javaVersion)
    {
        return Strings.isOneOf(javaVersion, Iterable.create("8", "1.8", "8.0"));
    }

    public static boolean isJava11(String javaVersion)
    {
        return Strings.isOneOf(javaVersion, Iterable.create("11", "11.0"));
    }

    /**
     * Get all of the Java source files found in the provided folder.
     * @param folder The folder to look for Java source files in.
     * @return All of the Java source files found in the provided folder.
     */
    public static Result<Iterable<File>> getJavaSourceFiles(Folder folder, Iterable<PathPattern> sourceFilePatterns)
    {
        PreCondition.assertNotNull(folder, "folder");
        PreCondition.assertNotNullAndNotEmpty(sourceFilePatterns, "sourceFilePatterns");

        return folder.getFilesRecursively()
            .thenResult((Iterable<File> files) ->
            {
                Result<Iterable<File>> result;
                final Iterable<File> javaSources = files
                    .where((File file) -> sourceFilePatterns.contains((PathPattern pattern) -> pattern.isMatch(file.getPath().relativeTo(folder))));
                if (!javaSources.any())
                {
                    result = Result.error(new NotFoundException("No java source files found in " + folder + "."));
                }
                else
                {
                    result = Result.success(javaSources);
                }
                return result;
            });
    }

    public static File getClassFile(File sourceFile, Folder rootFolder, Folder outputFolder)
    {
        final Path sourceFileRelativeToRootPath = sourceFile.relativeTo(rootFolder);
        final String sourceFileRelativePathFirstSegment = sourceFileRelativeToRootPath.getSegments().first();
        final Folder sourceFolder = rootFolder.getFolder(sourceFileRelativePathFirstSegment).throwErrorOrGetValue();
        final Path sourceFileRelativeToSourcePath = sourceFile.relativeTo(sourceFolder);
        return outputFolder.getFile(sourceFileRelativeToSourcePath.changeFileExtension(".class")).throwErrorOrGetValue();
    }

    private static boolean showUsage(CommandLineArgument argument)
    {
        final String argumentString = argument.toString();
        return argumentString.equals("/?") || argumentString.equals("-?");
    }

    public static void main(String[] args)
    {
        PreCondition.assertNotNull(args, "args");

        try (final Console console = new Console(args))
        {
            new Build().main(console);
        }
    }
}