---
title: Why test in Haskell?
author: Daniel Patterson
date: October 5th, 2014
---

Every so often, the question comes up, should you test in Haskell, and
if so, how should you do it?

Most people agree that you should test pure, especially complicated,
algorithmic code. Quickcheck[^quickcheck] is a great way to do this,
and most Haskellers have internalized this (Quickcheck was invented
here, so it must provide value!). What's less clear (or at least, more
debated!) is whether you should be testing monadic code, glue code,
and code that just isn't all that complicated.

## Quickcheck?

A lot  Haskell I'm writing these days is with the web framework Snap, and
web handlers often have the type `Handler App App ()` - where
`Handler` is a web monad (giving access to request information, and
the ability to write response data), and `App` indicates access to
application specific state (like database connections, templates,
etc).

So the inputs (ie, how to run this action) include any HTTP request
and any application state, and the only outputs are side effects (as
all it returns is unit). Using Quickcheck here is... challenging. You
could restrict the generated requests to have the right URL, and even
have the right query parameters, but since the query parameters are
just text, if they were supposed to be more structured (like an
interger), the chance of actually generating text that was just a
number is pretty low... And then if the number were supposed to be the
id of an element in the database....

But assume that we restrict it so that it's only generating ids for
elements in the database, what are the properties we are asserting?
Let's say that the handler looked up the element, and rendered it on
the page. So then we want to assert something about the content of the
response (which is wrapped up in the `Handler` monad). But maybe it
should also increment a view count in the database. And assuming that
we wrote all these into properties, what are the elements in the
database that it is choosing among? And in some senses we've now
restricted too much, because we may want to see what the behavior is
like for slightly invalid inputs. Say, integer id's that don't
correspond to elements in the database. This is all certainly
possible, and may be worth doing, but it seems pretty difficult. Which
is totally different from the experience of testing nice pure
functions!

Let's try to tease out a little bit of why testing this kind of code
with Quickcheck is hard. One problem is that the input space, as
determined by the type, is massive. And for most of the possible
inputs, the result should be some version of a no-op. Another problem
is the dependence on state, as the possible inputs are contingent on
external state, and the outputs are primarily changes to state, each
of which, again, is a massive space.

But having massive input and output spaces is not necessarily a reason
not to be using randomized testing. Indeed, this is exactly the kind
of thing that fuzz-testing of web browsers, for example, has done with
great effect[^webfuzzing]. The problem in this case is that the size of the input and
output space is not at all in proportion to the complexity of the code.
If we were writing an HTTP server, we may indeed want to be generating
random requests, throwing them at the web server, and making sure it was
generating well-formed responses (404s being perfectly fine).

## Not that complicated...

But we're just writing a little bit of glue code. Which isn't that
complicated. And can be tested manually pretty easily. And may change
rapidly.

Which means spending a lot of time setting up property based tests
(which in these sorts of cases are necessarily going to be quite a bit
more complicated than quintessential Quickcheck examples like showing
that `reverse . reverse = id`).

But you're still writing code that has types that massively
underspecify it's behavior. Which should make you nervous, at least a
little. Now granted, you should keep that underspecified code as thin
as possible - validate the query parameters, the URL, etc, and then
call a function with a type that much more clearly specifies what it is
supposed to do. For example (this is coming from Snap code, with some
details ellided, but should be reasonably easy to understand):

```
f :: Handler App App ()
f = route [("/foo/:id", do i <- read <$> getParam "id"
                           res <- lookupAndRenderFoo (FooId i)
                           writeText res)]

lookupAndRenderFoo :: FooId -> Handler App App Text
lookupAndRenderFoo = undefined
```

And certainly, this is a good pattern to use. We went from a function
that had as input space any HTTP request (and any application specific
state), and as output any HTTP response (as well as any side effects
in the `Handler` monad) and split it into two functions. One still has
the same input and output as before, but is very short, and the other
is a function with input the id of a specific element, and as output
`Text`, but still can perform any side effects in and read any data
from within the `Handler` monad.

## Increasing complexity?

We could split that further, and write a function with type `Foo ->
Text`, but we would start getting in our own way, as if we wanted to
render with a template, the templates exist within the context of the
`Handler` monad, so we would have to look up a template first, and we
would have ended up creating many new functions, as well as a bit of
extra complexity, all for the sake of splitting our code up into
layers, where the last one is pure and easy to test (the rest still
have all the same problems).

Depending on how complex that last layer is, this may totally be worth
it. If your code is dealing with human lives or livelihoods, by all
means, isolate that code into as small a portion as possible and test
the hell out of it. But it makes coding harder, and makes you move
slower. And if you want to change the logic, you may now have to
change many different functions, instead of just one.

Which is where we come to the argument that testing slows things down,
and that for rapidly changing code, it just doesn't matter.

## What about just not sampling?

But if we step back a bit, we realize that what Quickcheck is trying
to do is to sample representatively (well, with a bias towards edge
cases) over the type of the input. And it's easy to see why that's
appealing, as it gives you reasonable confidence that any use of the
function behaves as desired. But if we forget about that, as we
already know that our types completely underspecify the behavior, we
realize all that we really care about is that the code does what we
think it should do on a few example cases. That's what we were going
to manually verify after writing the code anyway.

Which is easy to test. With Snap, I'd write some tests for the above
snippet like[^hspec-snap]:

```
do f <- create ()
   let i = show . unFooId . fooId $ f
   get ("/foo/" ++ i) >>= should200
   get ("/foo/" ++ i) >>= shouldHaveText (fooDescription f)
   get ("/foo/" ++ show (1 + i)) >>= should404
```

And call it a day. This misses vasts swaths of inputs, and asserts
very little about the outputs, but it also tells you a huge amount more
about the correctness of the code than the fact that it typechecked
did. And as you iterate and refactor your application, you get the
assurance that this handler:

1. still exists.
2. still looks up the element from the database.
3. still puts the description somewhere on the page.
4. doesn't work for ids that don't correspond to elements in the database.

Which seems like a lot of assurance for a very small amount of
work. And if your application is fast moving, this benefits you even
more, as the faster you move, the more likely you are to break things
(at least, that's always been my experience!).  If you do decide to
rewrite this handler, fixing these tests is going to take a tiny
amount of time (probably less time than you spend manually confirming
that the change worked).

## Why this should be expected to work.

To take it a little further, and perhaps justify from a somewhat
theoretical point of view why these sorts of tests are so valuable,
consider all possible implementations of any function (or monadic
action). The possibly implementations with the given type are a subset
of all the possible implementations, but still potentially a pretty
large one (our example of a web handler certainly has this
property).

This perspective gives us some intuition on why it is much easier to
test simple, pure functions. There are only four possible
implementations of a `Bool -> Bool` function, so testing `not` via
sampling seems pretty tractable. To go even further, we get into the
territory of "Theorems for Free"[^freetheorems], where there is only
one implementation for an `(a,b) -> a` function, so testing `fst` is
pointless.

But returning to our case of massive spaces of well-typed
implementations: A single test, like one of the above, corresponds to
another subset of all the possible implementations. For example, the
first test corresponds to the subset that return success when passed
the given url via GET request. Since we're in Haskell, we also get a
guarantee that the set of implementations that fulfill the test is a
(non)strict subset of the set of implementations that fulfill the
type, as if this were not the case, our test case wouldn't type
check. The problem with the first test, of course, is that there are
all sorts of bogus implementations that fulfill it. For example, the
handler that always returns success would match that test.

But even still, it _is_ a strict subset of the implementations that fulfill the
type (for example, the handler that always returns 404 is not in this
set), so we're guaranteed to have improved the chance that our code is
correct, even with such a weak test (granted, it actually may not be
that weak of a test - in one project, I have a menu generated from a
data structure in code, and a test that iterates through all elements
of the menu, checking that hitting each url results in a 200. And this
has caught many refactoring problems!).

Where we really start to benefit is as we add a few more tests. The
second test shows that the handler must somehow get an element out of
the database (provided our `create ()` test function is creating
relatively unique field names), which is _another_ (strict) subset of
the set of implementations that fulfill the type. And we now know that
our implementation must be somewhere in the intersection of these two
subsets.

It shouldn't be hard to convince yourself that through the process of
just writing a few (well chosen) tests you can vastly reduce the
possibility of writing incorrect implementations. Which, when we are
writing relatively straightforward code, will probably be good enough
to ensure that the code is actually correct. And will continue to
verify that as the code evolves. Pretty good for a couple lines of code.


[^quickcheck]: For those who haven't used Quickcheck, it allows you to
specify properties that a function should satisfy, and possibly a way
to generate random values of the input type (if your input is a
standard type, it already knows how to do this), and it will generate
some number of inputs and verify that the property holds for all of
them.

[^webfuzzing]: See, for example,
[www.squarefree.com/2014/02/03/fuzzers-love-assertions/](http://www.squarefree.com/2014/02/03/fuzzers-love-assertions/).

[^hspec-snap]: This syntax is based on the
[hspec-snap](http://hackage.haskell.org/package/hspec-snap) package,
which I chose because I'm familiar with it (and wrote it). The
`create` line is from some not-yet-integrated-or-released, at least at
time of publishing, work to add factory support to the library
(sorry!). With that said, the advice should hold no matter what you're
doing.

[^freetheorems]: See Wadler's "Theorems for Free", 1989.
