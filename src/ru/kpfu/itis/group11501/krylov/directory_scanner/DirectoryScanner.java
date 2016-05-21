/**
 * By anthony.kryloff@gmail.com
 * Date: 20.05.16 10:25
 */
package ru.kpfu.itis.group11501.krylov.directory_scanner;

import java.util.*;

/**
 * By anthony.kryloff@gmail.com
 * Date: 20.05.16 10:25
 */
public class DirectoryScanner {

    private static final int NUM_THREADS = 5;

    //private static Thread [] threads = new Thread[NUM_THREADS];

    public static void main(String[] args) {

        //temporary workaround for debugging
        //TODO log to file
        String argString = "";
        for(String arg:args) {
            argString += arg + " ";
        }
        Scanner sc = new Scanner(argString);

        Timer timer = new Timer(false);
        ScanTask task = null;

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            Map<Param, String> params = getParams(line);

            //reset all threads to new command
            if(task != null) {
                task.stop();
            }
            System.out.println(params);

            timer.cancel();
            if(params.get(Param.ACTION).equals("exit")) {
                System.exit(0);
            }

            //reset timer
            timer = new Timer(false);
            task = new ScanTask(params);
            timer.schedule(task, new Date(), Integer.parseInt(params.get(Param.INTERVAL)));

        }

    }

    public static Map<Param, String> getParams(String line) {
        String [] args = line.split(" --");

        HashMap<Param, String> params = new HashMap<>();
        params.put(Param.ACTION, args[0]);
        for(int i = 1; i < args.length; i++) {
            String [] kv = args[i].split("\\s+", 2);
            params.put(Param.getParam(kv[0]), kv[1]);
        }

        return params;
    }

    private enum Param {
        ACTION("action"),
        INPUT_DIR("input"),
        OUTPUT_DIR("output"),
        MASK("mask"),
        INTERVAL("waitInterval"),
        RECURSIVE("includeSubfolders"),
        AUTO_DELETE("autoDelete");

        String value;

        Param(String value) {
            this.value = value;
        }

        public static Param getParam(String name) {
            Param [] params = values();
            for(Param param: params) {
                if(param.value.equals(name)) {
                    return param;
                }
            }

            return null;
        }
    }

    private static class ScanTask extends TimerTask {
        private Thread [] threads = new Thread[NUM_THREADS];
        private Map<Param, String> params;
        private ThreadGroup workers = new ThreadGroup("CopyWorkers");

        public ScanTask(Map<Param, String> params) {
            this.params = params;
            System.out.println("new scan task");
        }

        @Override
        public void run() {
            //TODO distribute files between concurrent workers

            System.out.println("starting task");
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(workers, new CopyWorker(
                        params.get(Param.INPUT_DIR),
                        params.get(Param.OUTPUT_DIR),
                        params.get(Param.MASK),
                        Boolean.parseBoolean(params.get(Param.RECURSIVE)),
                        Boolean.parseBoolean(params.get(Param.AUTO_DELETE))
                ));
                threads[i].start();
                System.out.println("Started "+threads[i]);
            }
        }

        public void stop() {
            for (int i = 0; i < threads.length; i++) {
                threads[i].stop();
            }
        }
    }
}
