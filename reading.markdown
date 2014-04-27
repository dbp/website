## Things I've read

This is a chronological list of some things I've been reading, with
brief notes, reflections, impressions, etc. The main criteria for
inclusion are: non-triviality (usually this means some length, but not
always), and interest (so an uninteresting paper or book doesn't merit
inclusion).

Often, clusters of papers are related. This is due to the wonderful
experience of reading research papers, where one paper references
another, so I start reading that instead, but the second paper is
actually based on a third, etc. Finding papers like this (vs. searching
online) is also great because it naturally selects papers that were
well written and/or influential.

### March 2014

- _Scrap Your Boilerplate: A Practical Design Pattern for Generic Programming_. Ralf Lammel, Simon Peyton Jones. 2003. [PDF](http://www.ldpreload.com/p/syb/hmap.pdf).

    > _Presenting patterns for generic traversal and modifications of
    > data structures where you only have to write the cases you care
    > about. Written for an audience of normal Haskell programmers._

- _Finally Tagless, Partially Evaluated: Tagless Staged Interpreters for Simpler Typed Languages_. J Carette, O Kiselyov, CC Shan. 2007. [PDF](http://okmij.org/ftp/tagless-final/JFP.pdf).

    > _Writing embedded typed languages inside typed languages without
    > tags or interpretation (the primary idea is to use functions
    > instead of data to represent terms of the language). Really neat
    > presentation, as it uses both OCaml and Haskell with real,
    > working examples. The language syntaxes are module signatures in
    > the former, and type class instances in the latter. Then the
    > semantics are modules or class instances respectively._

- _Fun with Type Functions_. Oleg Kiselyov, Ken Shan, and Simon Peyton Jones. 2010. [PDF](http://research.microsoft.com/~simonpj/papers/assoc-types/fun-with-type-funs/typefun.pdf)

    > _A tutorial style introduction to type level functions, which
    > are implemented in terms of type synonym families. One of the
    > main motivations for this is parametrizing type classes over
    > types in a more straightforwardly functional way (as contrasted
    > with functional dependencies). Very readable._

- _Computing at school in the UK: from guerrilla to gorillaS_. Simon Peyton Jones, Simon Humphreys, Bill Mitchell. 2013. [PDF](http://research.microsoft.com/en-us/um/people/simonpj/papers/cas/ComputingAtSchoolCACM.pdf)

    > _This was an interesting mix of motivation for why teaching
    > computer science as a core science (along with Physics,
    > Chemistry, and Biology) starting in primary schoool is
    > important, and also how the group "Computing at School" in the
    > UK has essentially succeeded at doing this._


- _Towards a Declarative Web_. (a.k.a. Haste Report). Anton Ekblad. 2012. [PDF](http://ekblad.cc/hastereport.pdf)

    > _Compilation of Haskell to Javascript. Not a super new idea, but
    > seems to get pretty good performance out of a relatively minimal
    > implementation. And, people seem to be using it for real work,
    > which is definitely neat!_
