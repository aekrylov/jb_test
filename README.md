Directory Scanner
=====
A simple tool that scans directory for files that match pattern and moves/copies them

Usage:

    java [-Dlogging=logfile] DirectoryScanner
    >scan <OPTIONS>
    ...
    >exit

## options:

- `--input | -in PATH` - input directory, must exist
- `--output | -out PATH` - output directory
- `--mask | -m MASK` - file mask in [Java Glob format](https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob)
- `--waitInterval | -wait N` - how often to check input directory, in milliseconds
- `--includeSubfolders | -r [VALUE]` - if `true`, the scanner will process directory recursively (default: `false`)
- `---autoDelete | -d [VALUE]` - if `true`, the scanner will automatically delete filed it copied (default: `false`)

## examples

    java -Dlogging=log.txt ru.kpfu.itis.group11501.krylov.directory_scanner.DirectoryScanner
    >scan --input "./test/input/f1" --output "./test/output2" --mask "*" --waitInterval 10000
    >scan --input ./test/input --output "./test/output" --mask *.txt --waitInterval 30000 --includeSubfolders --autoDelete false
    >scan -in ./test/input -out "./test/output" -m *.txt -wait 30000 -r -d
    >exit