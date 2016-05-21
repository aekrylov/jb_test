/**
 * By anthony.kryloff@gmail.com
 * Date: 20.05.16 14:46
 */
package ru.kpfu.itis.group11501.krylov.directory_scanner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.TimerTask;

/**
 * By anthony.kryloff@gmail.com
 * Date: 20.05.16 14:46
 */
public class CopyWorker extends TimerTask implements Runnable {
    private String in;
    private String out;
    private String mask;
    private boolean recursive;
    private boolean autoDelete;

    private PrintStream logStream;

    public CopyWorker(String in, String out, String mask, boolean recursive, boolean autoDelete) {
        this(in, out, mask, recursive, autoDelete, System.out);
    }

    public CopyWorker(String in, String out, String mask, boolean recursive, boolean autoDelete, PrintStream logStream) {
        this.in = in;
        this.out = out;
        this.mask = mask;
        this.recursive = recursive;
        this.autoDelete = autoDelete;
        this.logStream = logStream;
    }

    @Override
    public void run() {
        //File inputDir = new File(in);
        //File outputDir = new File(out);

        Path root = FileSystems.getDefault().getPath(in).toAbsolutePath().normalize();
        Path outRoot = FileSystems.getDefault().getPath(out).toAbsolutePath().normalize();

        int depth = recursive ? Integer.MAX_VALUE : 1;

        try {
            Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), depth, new Visitor(root, outRoot));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //File [] inputFiles = inputDir.listFiles();
    }

/*
    private void process(File dir, File out) {
        out.mkdirs();

        File [] files = dir.listFiles(new MaskFilter(mask));
        for(File file: files) {
            if(recursive && file.isDirectory()) {
                process(file, );
            }
        }
    }
*/

    private class MaskFilter implements FileFilter {
        public MaskFilter(String mask) {
            this.mask = mask;
        }

        private String mask;


        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory() || pathname.getName().matches(mask);
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
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if(autoDelete && !dir.equals(root)) {
                Files.delete(dir);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            logStream.println("Visiting "+path.getFileName());
            //check for patter match

            if(!matcher.matches(path.getFileName())) {
                return FileVisitResult.CONTINUE;
            }

            Path dest = outRoot.resolve(root.relativize(path));
            Files.createDirectories(dest.getParent());

            //moving is faster than copying and deleting
            try {
                if(autoDelete) {
                    Files.move(path, dest);
                } else {
                    Files.copy(path, dest);
                }
            } catch (FileAlreadyExistsException e) {
                logStream.println("File already exists: "+e.getFile());
            }


            return FileVisitResult.CONTINUE;
        }
    }
}
