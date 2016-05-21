Directory Scanner
=====
WIP

## options:

- `--input` - input directory
- `--output` - output directory
- `--mask` - file mask in [Java Glob format](https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob)
- `--waitInterval` - how often to check input directory, in milliseconds
- `--includeSubfolders` - if `true`, the scanner will process directory recursively
- `--autoDelete` - if `true`, the scanner will automatically delete filed it copied