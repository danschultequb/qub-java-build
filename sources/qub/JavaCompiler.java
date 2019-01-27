package qub;

/**
 * An abstract class used to interact with a Java compiler.
 */
public abstract class JavaCompiler
{
    private String version;
    private String bootClasspath;
    private Iterable<Dependency> dependencies;
    private Folder qubFolder;

    public String getVersion()
    {
        return version;
    }

    public JavaCompiler setVersion(String version)
    {
        this.version = version;
        return this;
    }

    public String getBootClasspath()
    {
        return bootClasspath;
    }

    public JavaCompiler setBootClasspath(String bootClasspath)
    {
        this.bootClasspath = bootClasspath;
        return this;
    }

    public JavaCompiler setDependencies(Iterable<Dependency> dependencies)
    {
        this.dependencies = dependencies;
        return this;
    }

    public Iterable<Dependency> getDependencies()
    {
        return dependencies;
    }

    public JavaCompiler setQubFolder(Folder qubFolder)
    {
        this.qubFolder = qubFolder;
        return this;
    }

    public Folder getQubFolder()
    {
        return qubFolder;
    }

    /**
     * Check that this system has the proper JRE installed to compile to the provided Java version.
     * @param javaVersion The Java version to check for.
     * @param console The console to use.
     * @return Whether or not the proper JRE is installed to compile to the provided Java version.
     */
    public Result<Void> checkJavaVersion(String javaVersion, Console console)
    {
        PreCondition.assertNotNull(console, "console");

        Result<Void> result;

        setVersion(javaVersion);

        if (Strings.isNullOrEmpty(javaVersion))
        {
            result = Result.success();
        }
        else
        {
            final String javaHome = console.getEnvironmentVariable("JAVA_HOME");
            if (Strings.isNullOrEmpty(javaHome))
            {
                result = Result.error(new NotFoundException("Can't compile for a specific Java version if the JAVA_HOME environment variable is not specified."));
            }
            else if (javaVersion.equals("1.8") || javaVersion.equals("8"))
            {
                final Folder javaFolder = console.getFileSystem().getFolder(javaHome).throwErrorOrGetValue().getParentFolder().throwErrorOrGetValue();
                final Iterable<Folder> jreAndJdkFolders = javaFolder.getFolders().throwErrorOrGetValue();
                final Iterable<Folder> jre18Folders = jreAndJdkFolders.where((Folder jreOrJdkFolder) -> jreOrJdkFolder.getName().startsWith("jre1.8.0_"));
                if (!jre18Folders.any())
                {
                    result = Result.error(new NotFoundException("No installed JREs found for Java version " + Strings.escapeAndQuote(javaVersion) + "."));
                }
                else
                {
                    final Folder jre18Folder = jre18Folders.maximum((Folder lhs, Folder rhs) -> Comparison.from(lhs.getName().compareTo(rhs.getName())));
                    result = jre18Folder.getFile("lib/rt.jar")
                        .then((File bootClasspathFile) ->
                        {
                            setBootClasspath(bootClasspathFile.toString());
                            return null;
                        });
                }
            }
            else
            {
                result = Result.error(new NotFoundException("No bootclasspath runtime jar file could be found for Java version " + Strings.escapeAndQuote(javaVersion) + "."));
            }
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    /**
     * Get the arguments that will be used to invoke the Java compiler.
     * @param sourceFiles The source files to compile.
     * @param rootFolder The root of the project folder.
     * @param outputFolder The folder where the compiled files will be put.
     * @return The arguments that will be used to invoke the Java compiler.
     */
    public Iterable<String> getArguments(Iterable<File> sourceFiles, Folder rootFolder, Folder outputFolder)
    {
        PreCondition.assertNotNullAndNotEmpty(sourceFiles, "sourceFiles");
        PreCondition.assertNotNull(rootFolder, "rootFolder");
        PreCondition.assertNotNull(outputFolder, "outputFolder");

        final List<String> result = List.create();

        result.addAll("-d", outputFolder.toString());
        result.add("-Xlint:unchecked");
        result.add("-Xlint:deprecation");

        final String version = getVersion();
        if (!Strings.isNullOrEmpty(version))
        {
            result.addAll("-source", version);
        }

        final String bootClasspath = getBootClasspath();
        if (!Strings.isNullOrEmpty(bootClasspath))
        {
            result.addAll("-bootclasspath", bootClasspath);
        }

        final Iterable<Dependency> dependencies = getDependencies();
        if (!Iterable.isNullOrEmpty(dependencies))
        {
            if (qubFolder == null)
            {
                throw new NotFoundException("Cannot resolve project dependencies without a qubFolder.");
            }

            final Iterable<String> dependencyPaths = dependencies.map((Dependency dependency) ->
            {
                final String dependencyRelativePath =
                    dependency.getPublisher() + "/" +
                    dependency.getProject() + "/" +
                    dependency.getVersion() + "/" +
                    dependency.getProject() + ".jar";
                return qubFolder.getFile(dependencyRelativePath).throwErrorOrGetValue().toString();
            });

            result.addAll("-classpath", Strings.join(';', dependencyPaths));
        }

        result.addAll(sourceFiles.map((File sourceFile) -> sourceFile.relativeTo(rootFolder).toString()));

        PostCondition.assertNotNullAndNotEmpty(result, "result");

        return result;
    }

    /**
     * Compile all of the provided Java files using the provided Java version. All of the compiled
     * class files will be put into the outputFolder.
     * @param sourceFiles The source files to compile.
     * @param rootFolder The folder that contains all of the source files to compile.
     * @param outputFolder The output folder where the compiled results will be placed.
     * @param console The Console or Process to use.
     * @return The result of the compilation.
     */
    public abstract Result<JavaCompilationResult> compile(Iterable<File> sourceFiles, Folder rootFolder, Folder outputFolder, Console console);
}
