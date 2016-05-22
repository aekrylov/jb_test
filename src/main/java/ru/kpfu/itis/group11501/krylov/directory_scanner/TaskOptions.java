/**
 * By anthony.kryloff@gmail.com
 * Date: 22.05.16 14:27
 */
package ru.kpfu.itis.group11501.krylov.directory_scanner;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import org.kohsuke.args4j.spi.OneArgumentOptionHandler;
import org.kohsuke.args4j.spi.Setter;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.PatternSyntaxException;

/**
 * By anthony.kryloff@gmail.com
 * Date: 22.05.16 14:27
 */

/**
 * Auxiliary class for parsing console arguments using args4j
 */
public class TaskOptions {
    Path input;
    Path output;

    @Option(name = "--input", usage = "input dir", required = true, aliases = {"-in"})
    public void setInput(Path path) throws CmdLineException {
        input = path.toAbsolutePath().normalize();
        if(!Files.exists(input))
            throw new CmdLineException("Input directory doesn't exist: " + input);
    }

    @Option(name = "--output", usage = "output directory", required = true, aliases = {"-out"})
    public void setOutput(Path path) {
        output = path.toAbsolutePath().normalize();
    }

    @Option(name = "--mask", usage = "mask for files to be copied", required = true,
            aliases = {"-m"}, handler = MaskOptionHandler.class)
    String mask;

    @Option(name = "--waitInterval", usage = "interval between scans", required = true,
            aliases = {"-wait"}, handler = PositiveIntegerOptionHandler.class)
    int waitInterval;

    @Option(name = "--includeSubfolders", usage = "include subfolders",
            aliases = {"-r"}, handler = ExplicitBooleanOptionHandler.class)
    boolean recursive = false;

    @Option(name = "--autoDelete", usage = "auto delete files copied",
            aliases = {"-d"}, handler = ExplicitBooleanOptionHandler.class)
    boolean autoDelete = false;


    /**
     * Positive integer option handler
     */
    public static class PositiveIntegerOptionHandler extends OneArgumentOptionHandler<Integer> {

        public PositiveIntegerOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Integer> setter) {
            super(parser, option, setter);
        }

        @Override
        protected Integer parse(String argument) throws NumberFormatException, CmdLineException {
            int value = Integer.parseInt(argument);
            if(value < 0)
                throw new CmdLineException(owner, "Incorrect interval, must be positive: "+value);

            return value;
        }
    }

    /**
     * Glob mask option handler, checks if mask is a valid glob
     */
    public static class MaskOptionHandler extends OneArgumentOptionHandler<String> {

        public MaskOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super String> setter) {
            super(parser, option, setter);
        }

        @Override
        protected String parse(String argument) throws NumberFormatException, CmdLineException {
            try {
                FileSystems.getDefault().getPathMatcher("glob:"+argument);
            } catch (PatternSyntaxException e) {
                throw new CmdLineException(owner, "Illegal symbols in mask", e);
            }
            return argument;
        }

        @Override
        public String getDefaultMetaVariable() {
            return "MASK";
        }
    }

}
