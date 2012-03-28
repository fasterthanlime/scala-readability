Here I'll keep a log of the issues I'm encountering and the various thoughts that
go through my head as I'm going through the project. Not particularly safe for W,
your mileage may vary. Enjoy!

## Preparation

### Thanks

Thanks a lot to Philippe Suter and Etienne Kneuss for setting me up with a basic
Scala compiler plug-in to work with, along with an sbt build file that provides
a launcher script. Those are precious resources and undoubtedly saved me many hours.

### Buse vs the World

The subject of this project is naturally derived from [Buse's work][buse] who
tried to develop a metric for software readability. Buse came up with SnippetSniper,
a system to collect human readability ratings on snippets of code, and then used this
data to train various neural algorithms, which could then be used to quickly score
pieces of code for readability.

Buse's study was done on pieces of Java code, due to its huge popularity and the
number of people literate in this language. In this project, we're interested in
Scala readability. The language is regularly being accused of being too complex for
beginners. I've personally often felt that there (well) more than one way to do a
simple thing.

Buse's study focused on fairly low-level code aspects: punctuation, for example.
In this study, we'll attempt to use the compiler's knowledge of the code (ie.
the AST) to recognize higher-level patterns. The study should be able to answer
questions like: is code containing foreach loops instead of for loops usually more
or less readable than the other options?

With a bit of luck, I'll be able to train a classifier to have a readability
metric on Scala code as well, and apply it to open-source scala libraries.

## March 28, 2012

Taking up this project after a few weeks of doubts concerning my studies at EPFL,
but I've decided to stay with it until the end of the bachelor, so here goes.

### RangePosition

The first problem I encountered was to retrieve the start and end position of nodes.
Philippe Suter suggested the use of -Yrangepos, a compiler option that turns
OffsetPosition to RangePosition in the AST. Apparently, though, it only does so
[for certain types of AST nodes](https://groups.google.com/d/topic/scala-internals/gpg1br4zR3A/discussion).

The 2.10.x branch contains improvements: more OffsetPositions are turned into
RangePosition. Cases that don't work are probably bugs and should be reported.
In the meantime, using scalac's GUI AST explorer gives me some interesting insight:

    scalac -Ybrowse:typer -Yrangepos somefile.scala

### Samples

I've begun writing a few scala code samples to demonstrate different ways of doing
the same task, or simply to showcase the usage of a particular feature. By running the
AST browser on them, I hope to be able to develop code that recognizes the AST structure
of some patterns, to detect their usage in the code and thus use these patters as
criterions (similar to the way Buse used punctuation)

[buse]: http://arrestedcomputing.com/readability/ "Buse's work"

