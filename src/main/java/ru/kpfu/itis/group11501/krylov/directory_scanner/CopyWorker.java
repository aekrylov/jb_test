/**
 * By anthony.kryloff@gmail.com
 * Date: 20.05.16 14:46
 */
package ru.kpfu.itis.group11501.krylov.directory_scanner;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

/**
 * By anthony.kryloff@gmail.com
 * Date: 20.05.16 14:46
 */
public class CopyWorker implements Runnable {
    private String mask;
    private boolean recursive;
    private boolean autoDelete;

    private Path root;
    private Path outRoot;

    private PrintStream logStream;

    private int num;

    public CopyWorker(Path root, Path outRoot, String mask, boolean recursive, boolean autoDelete, PrintStream logStream, int num) {
        this.mask = mask;
        this.recursive = recursive;
        this.autoDelete = autoDelete;
        this.root = root;
        this.outRoot = outRoot;
        this.logStream = logStream;

        this.num = num;
    }


    public CopyWorker(TaskOptions options, PrintStream logStream, int i) {
        this.root = options.input;
        this.outRoot = options.output;
        this.mask = options.mask;
        this.recursive = options.recursive;
        this.autoDelete = options.autoDelete;
        this.logStream = logStream;
        num = i;
    }

    @Override
    public void run() {
        logStream.println();
        int depth = recursive ? Integer.MAX_VALUE : 1;

/*
        if(!recursive) {
            File dir = new File(root.toUri());
            File [] copy = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches(mask);
                }
            });
            for (int i = num; i < copy.length; i+= DirectoryScanner.NUM_THREADS) {
                if(copy[i].isDirectory())
                    continue;

                Path dest = outRoot.resolve(copy[i].getName());

            }
        }
*/

        try {
            Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), depth, new Visitor(root, outRoot));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class Visitor extends SimpleFileVisitor<Path> {
        private Path root, outRoot;

        private final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:"+mask);

        public Visitor(Path root, Path outRoot) {
            this.root = root;
            this.outRoot = outRoot;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            logStream.println("Visiting dir: " + root.relativize(dir));
            Path dest = outRoot.resolve(root.relativize(dir));

            try {
                Files.createDirectories(dest);
            } catch (IOException e) {
                //couldn't create directory so skip it
                System.err.println("Couldnt create directory: "+dest);
                e.printStackTrace();
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if(autoDelete && !dir.equals(root)) {
                try{
                    Files.delete(dir);
                } catch (DirectoryNotEmptyException e) {
                    logStream.println("dir is non empty, leaving: "+dir);
                    //todo
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {

            //process file if it matches pattern and "assigned" to this worker by hashcode
            if(!matcher.matches(path.getFileName()) ||
                    Math.abs(path.getFileName().hashCode() % DirectoryScanner.NUM_THREADS) != num) {
                return FileVisitResult.CONTINUE;
            }

            logStream.println("Visiting file: " + root.relativize(path));

            Path dest = outRoot.resolve(root.relativize(path));

            //moving is faster than copying and deleting
            //TODO rewrite files?
            try {
                if(autoDelete) {
                    Files.move(path, dest, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (FileAlreadyExistsException e) {
                logStream.println("File already exists: "+e.getFile());
            }


            return FileVisitResult.CONTINUE;
        }
    }
}
