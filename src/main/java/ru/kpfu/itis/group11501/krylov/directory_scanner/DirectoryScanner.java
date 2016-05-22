/**
 * By anthony.kryloff@gmail.com
 * Date: 20.05.16 10:25
 */
package ru.kpfu.itis.group11501.krylov.directory_scanner;

import edu.rice.cs.util.ArgumentTokenizer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * By anthony.kryloff@gmail.com
 * Date: 20.05.16 10:25
 */
public class DirectoryScanner {

    static final int NUM_THREADS = 5;

    private static PrintStream out = System.out;

    public static void main(String[] args) {
        //check if logfile specified
        if(System.getProperty("logging") != null) {
            try {
                out = new PrintStream(System.getProperty("logging"));
            } catch (FileNotFoundException e) {
                System.err.println("Couldn't open log file:");
                e.printStackTrace();
            }
        }

        Scanner sc = new Scanner(System.in);

        ScanTask task = null;

        while (sc.hasNextLine()) {
            //split line to array for parsing
            String line = sc.nextLine();
            List<String> argv = ArgumentTokenizer.tokenize(line);
            String action = argv.remove(0);

            //stop current tasks
            if(task != null) {
                task.stop();
            }

            if(action.equals("exit")) {
                System.exit(0);
            }

            //parse command
            TaskOptions taskOptions = new TaskOptions();
            CmdLineParser parser = new CmdLineParser(taskOptions);
            try {
                parser.parseArgument(argv);
            } catch (CmdLineException e) {
                System.err.println("Scanner cannot start due to following:");
                System.err.println(e.getLocalizedMessage());
                parser.printUsage(System.err);
                continue;
            }

            task = new ScanTask(taskOptions);
            task.run();

        }

    }

    private static class ScanTask {
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(NUM_THREADS);

        private TaskOptions taskOptions;

        public ScanTask(TaskOptions params) {
            this.taskOptions = params;
        }

        public void run() {
            System.out.println("starting task");
            for (int i = 0; i < NUM_THREADS; i++) {
                CopyWorker worker = new CopyWorker(taskOptions, out, i);
                scheduler.scheduleAtFixedRate(worker, 0, taskOptions.waitInterval, TimeUnit.MILLISECONDS);
                System.out.println("Scheduled "+worker);
            }
        }

        public void stop() {
            System.out.println("shutting down tasks");
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(taskOptions.waitInterval, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("shut down tasks");
        }

    }

}
