package qub;

public class QubBuildCompileParameters
{
    private final CharacterToByteWriteStream outputWriteStream;
    private final Folder folderToBuild;
    private final EnvironmentVariables environmentVariables;
    private final ProcessFactory processFactory;
    private final QubFolder qubFolder;
    private final Folder qubBuildDataFolder;
    private Warnings warnings;
    private boolean buildJson;
    private VerboseCharacterToByteWriteStream verbose;

    public QubBuildCompileParameters(CharacterToByteWriteStream outputWriteStream, Folder folderToBuild, EnvironmentVariables environmentVariables, ProcessFactory processFactory, QubFolder qubFolder, Folder qubBuildDataFolder)
    {
        PreCondition.assertNotNull(outputWriteStream, "outputWriteStream");
        PreCondition.assertNotNull(folderToBuild, "folderToBuild");
        PreCondition.assertNotNull(environmentVariables, "environmentVariables");
        PreCondition.assertNotNull(processFactory, "processFactory");
        PreCondition.assertNotNull(qubFolder, "qubFolder");
        PreCondition.assertNotNull(qubBuildDataFolder, "qubBuildDataFolder");

        this.outputWriteStream = outputWriteStream;
        this.folderToBuild = folderToBuild;
        this.environmentVariables = environmentVariables;
        this.processFactory = processFactory;
        this.qubFolder = qubFolder;
        this.qubBuildDataFolder = qubBuildDataFolder;
        this.warnings = QubBuildCompileParameters.getWarningsDefault();
        this.buildJson = QubBuildCompileParameters.getBuildJsonDefault();
        this.verbose = QubBuildCompileParameters.getVerboseDefault();
    }

    /**
     * Get the CharacterWriteStream that output will be written to.
     * @return The CharacterWriteStream that output will be written to.
     */
    public CharacterToByteWriteStream getOutputWriteStream()
    {
        return this.outputWriteStream;
    }

    /**
     * Get the folder that will be built.
     * @return The folder that will be built.
     */
    public Folder getFolderToBuild()
    {
        return this.folderToBuild;
    }

    /**
     * Get the environment variables that this application is being run with.
     * @return The environment variables that this application is being run with.
     */
    public EnvironmentVariables getEnvironmentVariables()
    {
        return this.environmentVariables;
    }

    /**
     * Get the ProcessFactory that will be used to invoke other processes.
     * @return The ProcessFactory that will be used to invoke other processes.
     */
    public ProcessFactory getProcessFactory()
    {
        return this.processFactory;
    }

    /**
     * Get the Qub folder for this application.
     * @return The Qub folder for this application.
     */
    public QubFolder getQubFolder()
    {
        return this.qubFolder;
    }

    /**
     * Get the qub-build data folder for this application.
     * @return The qub-build data folder for this application.
     */
    public Folder getQubBuildDataFolder()
    {
        return this.qubBuildDataFolder;
    }

    /**
     * Get how warnings should be treated at build time.
     * @return How warnings should be treated at build time.
     */
    public Warnings getWarnings()
    {
        return this.warnings;
    }

    /**
     * Set how warnings should be treated at build time.
     * @param warnings How warnings should be treated at build time.
     * @return This object for method chaining.
     */
    public QubBuildCompileParameters setWarnings(Warnings warnings)
    {
        PreCondition.assertNotNull(warnings, "warnings");

        this.warnings = warnings;
        return this;
    }

    /**
     * Get whether or not to use a build.json file.
     * @return Whether or not to use a build.json file.
     */
    public boolean getBuildJson()
    {
        return this.buildJson;
    }

    /**
     * Set whether or not to use a build.json file.
     * @param buildJson Whether or not to use a build.json file.
     * @return This object for method chaining.
     */
    public QubBuildCompileParameters setBuildJson(boolean buildJson)
    {
        this.buildJson = buildJson;
        return this;
    }

    /**
     * Get the VerboseCharacterWriteStream where verbose logs will be written to.
     * @return The VerboseCharacterWriteStream where verbose logs will be written to.
     */
    public VerboseCharacterToByteWriteStream getVerbose()
    {
        return this.verbose;
    }

    /**
     * Set the VerboseCharacterWriteStream where verbose logs will be written to.
     * @param verbose The VerboseCharacterWriteStream where verbose logs will be written to.
     * @return This object for method chaining.
     */
    public QubBuildCompileParameters setVerbose(VerboseCharacterToByteWriteStream verbose)
    {
        PreCondition.assertNotNull(verbose, "verbose");

        this.verbose = verbose;
        return this;
    }

    /**
     * Get the default value for the --warnings parameter.
     * @return The default value for the --warnings parameter.
     */
    static Warnings getWarningsDefault()
    {
        return Warnings.Show;
    }

    /**
     * Get the default value for the --buildjson parameter.
     * @return The default value for the --buildjson parameter.
     */
    static boolean getBuildJsonDefault()
    {
        return true;
    }

    /**
     * Get the default value for the --verbose parameter.
     * @return The default value for the --verbose parameter.
     */
    static VerboseCharacterToByteWriteStream getVerboseDefault()
    {
        return VerboseCharacterToByteWriteStream.create(InMemoryCharacterToByteStream.create()).setIsVerbose(false);
    }
}
