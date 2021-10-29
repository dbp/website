# Mercury tidbits - dependent types and file io

<em>Note: this was originally posted as two separate parts, 1 week apart, and has been compressed for posterity</em>

I just started learning a functional/logic language called [Mercury](http://www.mercury.cs.mu.oz.au), which has features that make it feel (at least to my initial impressions) like a mix between Prolog and Haskell. It has all the features that make it a viable Prolog, but it also adds static typing (with full type inference) and purity (all side effects are dealt with by passing around the state of the world). Since I recently was interested in learning Prolog, but had no desire to give up static typing or purity, Mercury seemed like a neat thing to learn.

While it is not very well known, the language has been around for over 15 years, and has a high quality self-hosting compiler.

Getting to play around with logic/declarative programming is interesting (and indeed the main reason why I'm interested in learning it), but what seems even more interesting with Mercury is how they have incorporated typing to the logic programming (which, unless I'm mistaken, is a new thing). As a tiny code example:

    :- pred head(list(T), T).
    :- mode head(in,    out) is semidet.
    :- mode head(in(non_empty_list), out) is det.
    head(Xs, X) :- Xs = [X | _].

The first line says that this is a predicate (logic statement) that has two parts, the first is a list of some type T (it is polymorphic), the second is an item of type T.

The fourth line should be familiar to a prolog programmer, but briefly, the right side says that Xs is defined as X cons'd to an unnamed element. `head` can be seen as defining a relationship between Xs and X, where the specifics are that Xs is a list that has as it's first element X.

Now with regular prolog, only the fourth line would be necessary, and that definition allows some interesting generalization. Because `head([1,2,3],Y)` will bind `Y` to `1`, while `head([1,2,3],1)` will be true (or some truthy value), and if `head(X,Y)` were used in a set of other statements, together they would only yield a result if X (wherever it was bound, or unified, to a value) had as it's first value Y, whatever Y was.

Since Mercury is statically typed, it adds what it calls modes to predicates, which specify whether a certain argument (that's probably not the right word!) is going to be given, or whether it is going to be figured out by the predicate. The other thing it has is specifications about whether the predicate is deterministic. There are a couple options, but the two that are relevant to this example are `det`, which means fully deterministic, for every input there is exactly one output, and `semidet`, which means for some inputs there is an output, for others there is not (ie, the unification fails). These allow the compiler to do really interesting things, like tell you if you are not covering all of the possible cases if you declare something as `det` (whereas the same code, as `semidet`, would not cause any errors).

What is fascinating about this predicate head is that it has two modes defined, one being the obvious head that we know from Haskell etc:

    :- mode head(in,    out) is semidet.

Which states that the first argument is the input (the list) and the second is the output (the element), and it is `semidet` because for an empty list it will fail. The next is more interesting:

    :- mode head(in(non_empty_list), out) is det.

This says for an input that is a non_empty_list (defined in the standard libraries, and included below), the second argument is the output, and this is `det`, ie fully deterministic. What this basically means is that failure is incorporated into the type system, because something that is `semidet` can fail, but something that is `det` cannot (neat!). Now the standard modes are defined (something like):

    :- mode in == (ground >> ground).
    :- mode out == (free    >> ground).

Ground is a something that is bound, and the `>>` is showing what is happening before and after the unification (the analog to function application). So something of mode `in` will be bound before and after, whereas something of mode `out` will not be bound before (that's what `free` means) and it will be bound afterwards. That's pretty straightforward.

What get's more interesting is something like `non_empty_list`, where `inst` stands for instantiation state, ie one of various states that a variable can be in (with ground and free being the most obvious ones):

    :- inst non_empty_list == bound([ground | ground]).

What this means is that a `non_empty_list` is defined as one that has a ground element cons'd to another ground element. (I don't know the syntax well enough to explain what `bound` means in this context, but it seems straightforward). What this should allow you to do is write programs that operate on things like non-empty-lists, and have the compiler check to make sure you are never using an empty list where you shouldn't. Pretty cool!

Obviously you can write data types in Haskell that also do not allow an empty list, like:

    data NonEmptyList a = NonEmptyList a [a]

And could build functions to convert between them and normal lists, but the fact that it is so easy to build this kind of type checking on top of existing types with Mercury is really fascinating.

This is (obviously) just scratching the surface of Mercury (and the reason all of this stuff actually works is probably more due to the theoretical underpinnings of logic programming than anything else), but seeing the definition of `head` gave me enough of an 'aha!' moment that it seemed worth sharing.

If any of this piqued your interest, all of it comes out of the (wonderful) tutorial provided at the [Mercury Project Documentation](http://www.mercury.csse.unimelb.edu.au/information/documentation.html) page. If there are any inaccuracies (which there probably are!) send them to [daniel@dbpatterson.com](mailto:daniel@dbpatterson.com).

<hr/>

<em>Note: this is the beginning of the second post</em>


The language that I've been learning recently is a pure (ie, side-effect free) logic/functional language named [Mercury](http://www.mercury.csse.unimelb.edu.au). There is a wonderful [tutorial (PDF)](http://www.mercury.csse.unimelb.edu.au/information/papers/book.pdf) available, which explains the basics, but beyond that, the primary documentation is the [language reference](http://www.mercury.csse.unimelb.edu.au/information/doc-release/mercury_ref/index.html) (which is well written, but reasonably dense) and Mercury's [standard library reference](http://www.mercury.csse.unimelb.edu.au/information/doc-release/mercury_library/index.html) (which is autogenerated and includes types and source comments, nothing else).

Doing I/O in a pure language is a bit of a conundrum - Haskell solved this by forcing all I/O into a special monad that keeps track of sequencing (and has a mythical state of the world that it changes each time it does something, so as not to violate referential transparency). Mercury has a simpler (though equivalent) approach - every predicate that does IO must take an world state and must give back a new world state. Old world states can not be re-used (Mercury's mode system keep track of that), and so the state of the world is manually threaded throughout the program. A simple example would be:

    main(IO_0,IO_final) :- io.write_string("Hello World!",IO_0,IO_1),
                           io.nl(IO_1,IO_final).

Where the first function consumes the `IO_0` state and produces `IO_1` (while printing "Hello World!") and the second function consumes `IO_1` and produces `IO_final` (while printing a newline character).

Of course, manually threading those could become pretty tedious, so they have a shorthand, where the same code above could be written as:


    main(!IO) :- io.write_string("Hello World!",!IO),
                 io.nl(!IO).

This is just syntax sugar, and can work with any parameters that are dealt with in the same way (and naming it IO for io state is just convention). It definitely makes dealing with I/O more pleasant.

The task that I set was to figure out how to read in a file. This is not covered in the tutorial, and I thought it would be a simple matter of looking through the library reference for the [io library](http://www.mercury.csse.unimelb.edu.au/information/doc-release/mercury_library/io.html). One of the first predicates looks promising:

    :- pred io.read_file(io.maybe_partial_res(list(char))::out,
                         io::di,
                         io::uo) is det.

But on second thought, something seems to be missing. The second and third parameters are the world states (the type is io, the mode di stands for destructive-input, meaning the variable cannot be used again, uo means unique output, which means that no other variable in the program can have that value), and the first one is going to be the contents of the file itself. But where is the file name?

The comment provides the necessary pointer:

    % Reads all the characters from the current input stream until
    % eof or error.

Hmm. So all of these functions operate on whatever the current input stream is. How do we set that? `io.set_input_stream` looks pretty good:

    % io.set_input_stream(NewStream, OldStream, !IO):
    % Changes the current input stream to the stream specified.
    % Returns the previous stream.
    %
    :- pred io.set_input_stream(io.input_stream::in,
                                io.input_stream::out,
                                io::di, io::uo) is det.

But even better is `io.see`, which will try to open a file and if successful, will set it to the current stream (the alternative is to use `io.open_input` and then `io.set_input_stream`):

    % io.see(File, Result, !IO).
    % Attempts to open a file for input, and if successful,
    % sets the current input stream to the newly opened stream.
    % Result is either 'ok' or 'error(ErrorCode)'.
    %
    :- pred io.see(string::in, io.res::out, io::di, io::uo) is det.

With that in mind, let's go ahead and implement a predicate to read files (much like I was expecting to find in the standard library, and what I put into a module of similar utilities I've started, titled, in tribute to Haskell, `prelude`):

    :- pred prelude.read_file(string::in,
                              maybe(string)::out,
                              io::di,io::uo) is det.
    prelude.read_file(Path,Contents,!IO) :-
      io.see(Path,Result,!IO),
      ( Result = ok,
        io.read_file_as_string(File,!IO),
        io.seen(!IO),
        (
          File = ok(String),
          Contents = yes(String)
        ;
          File = error(_,_),
          Contents = no
        )
      ;
        Result = error(_),
        Contents = no
      ).

To walk through what this code is doing, the type says that this is a predicate that does I/O (that's what the last two arguments are for), that it takes in a string (the path) and give out a maybe(string), and that this whole thing is deterministic (ie, it always succeeds, which is accomplished by wrapping the failure into the return type: either yes(value) or no).

The first line tries to open the file at the path and bind it as the current input stream. I then pattern match on the results of that - if it failed, just bind Contents (the return value) to no. Otherwise, we try to read the contents out of the file and then close the file and set the input stream to the default one again (that is what the predicate io.seen does). Similarly we handle (well, really don't handle, at least not well) reading the file failing. If it succeeds, we set the return type to the contents of the file.

What is interesting about this code is that while it is written in the form of logical statements, it feels very much like the way one does I/O in Haskell - probably a bit of that is my own bias (as a Haskell programmer, I am likely to write everything like I would write Haskell code, kind of how my python code always ends up with lambda's and maps in it), but it also is probably a function of the fact that doing I/O in a statically type pure language is going to always be pretty similar - lots of dealing with error conditions, and not much else!

Anyhow, this was just a tiny bit of code, but it is a predicate that is immediately useful, especially when trying to use Mercury for random scripting tasks (what I often do with new languages, regardless of their reputed ability for scripting).
