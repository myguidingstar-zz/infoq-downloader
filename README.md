# InfoQ-downloader

Download talks from Infoq to view offline.

## Prerequisites

You will need [CasperJS][1] with either [PhantomJS][2] or [SlimerJS][3] above installed.

Also, [Aria2][4] will be used to download slide and video files.

If the slides are .swf files, you need `swfrender` from
SWFTools[5]. Videos of .flv format will be converted to `.webm` using
`avconv`[6].

[1]: http://casperjs.org
[2]: http://phantomjs.org
[3]: http://slimerjs.org
[4]: http://aria2.sourceforge.net
[5]: http://wiki.swftools.org/wiki/Swfrender
[6]: https://libav.org/avconv.html

## Usage

Clone infoq-download to your machine:

```bash
git clone https://github.com/myguidingstar/infoq-downloader
cd infoq-downloader
```

Now run infoq-downloader with CasperJs+PhantomJS (in `infoq-downloader` directory):

```bash
casper ./lib/infoq-casper.js http://www.infoq.com/presentations/a-presentation-name
```
If you prefer SlimerJS, try this instead:

```bash
casper --engine=slimerjs ./lib/infoq-casper.js http://www.infoq.com/presentations/a-presentation-name
```

After a while, you'll see something like:
> You can now run aria2c to download slides and video with:
> (download resume is enabled)
> aria2c -x 16 --auto-file-renaming=false -i a-presentation-name/aria2.txt

Then just have arial2 download the files as instructed:

```bash
aria2c -x 16 --auto-file-renaming=false -i a-presentation-name/aria2.txt
```

Then the provided `no-flash.sh` should be used to convert flash slides
and videos to standard formats:

```bash
./no-flash a-presentation-name # the directory of newly downloaded presentation
```

## Time to hack

InfoQ is written in [ChlorineJS][5], a subset of Clojure that compiles to Javascript. You'll need [Leiningen][6] to compile it.

[5]: https://github.com/chlorinejs
[6]: http://leiningen.org

Make changes to `src/infoq-casper.cl2` then:

```bash
lein cl2c auto compile
```

This will watch for changes and re-compile `*.cl2` files to Javascript.

Or you may want to compile only once:

```bash
lein cl2c once compile
```

## License

Copyright © 2014-2015 Hoang Minh Thang

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
