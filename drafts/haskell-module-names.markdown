---
title: "How to organize modules in a Haskell Web App"
author: Daniel Patterson
---

> A note: I don't write single-page apps. Perhaps some of this translates to
> people who do, but I don't know. When I say "web app", I mean server-rendered
> html pages that have forms and buttons and store their state on the server.

Different people have different preferences for how to organize code in their
applications. One of the really cool things about most Haskell web frameworks is
they let you organize your code however you want.

This upside is that a tiny project can be a single file and that understanding
projects comes just from understanding the language, not framework-specific 
magic that makes particular paths special.

The downside, of course, is that people are on their own to figure out best
practices. I've tried a lot of different things (over the past ~11 years
building web stuff in Haskell), and this system is the result of that
experience, primarily using the [Snap](http://snapframework.com/) web framework
and then more recently the [Fn framework](http://fnhaskell.com/) that I
co-wrote (I've also used
[Scotty](https://hackage.haskell.org/package/scotty) and
[Servant](https://haskell-servant.github.io/) and I think the advice would work
equally well for them).

### 1. Pure type modules

For each record, which in a database backed application, will usually correspond
to a table, define a separate module. I would use `Types.Person` if `Person`
were the name of the type. This should contain the record, which, contrary to
many examples, should _not_ have prefixed field names (the prefix, where
necessary, is already present in the module name!): just name the fields the
most natural names, e.g.:

    data Person = Person { id :: Int, firstName :: Text } deriving (Eq, Show)

This module should also include any type class instances for `Person` (e.g.,
serialization), and related types. For example, if there is a data type that is
a field within the record (e.g., you might have a `role` field that has a fixed
number of options; in the database it is represented textually, but it shouldn't
be in your application), define it within the `Types.Person` module rather
than giving it it's own module, unless it's useful to other modules.

> A note about casing: I don't think this is controversial, but match what is most
> natural in whatever domain the name appears in. So field names in Haskell should
> be camelCase, in the database should be snake_case, and in frontend templates I
> hyphenate-them. Transforming between these can be automated.

Having pure modules to define types is really helpful to avoid module
circularity; most of the time the issue is that you'll end up needing to allow
more core application types refer to specific data for the application (e.g., in
[Fn](http://fnhaskell.com/) web handlers pass around a "context" that contains
database connections, request information, etc. It necessarily is used many
places, but you may also want it to be able to contain information about a
logged in user. By having the types on their own, it's much easier to pull those
types into the definition of core data types like the "context").

### 2. State modules for manipulating state with consistent names

There are tons of different libraries for dealing with databases, but from the
perspective of module organization, each module `Types.Person` should be matched
with `State.Person`, and just like the field names in the `Person` record
shouldn't have any prefix or suffix, neither should functions in the
`State.Person` module. So, for example, I'll usually have `get`, `create`, and
`delete` as functions, and perhaps `getByFoo` or `deleteByBar`. The reason for
this is the `State.Person` module is expected to always be imported qualified
(it ends up looking more uniform anyway).

### 3. Qualify modules that are for a different part of the application

In general, organizing the application around the records (i.e., database
tables) works pretty well. It won't be 100% (and it doesn't matter, because
Haskell doesn't care), but usually I'll have a `Handler.Person` module to go
along with the `Types.Person`, which would contain web code to handle routing,
form parsing and various high level glue, and `State.Person` which has state
manipulation (database queries, business logic, etc).

Within `State.Person`, import `Types.Person` unqualified. There should be no
conflicts. From `Handler.Person`, `Types.Person` should be imported unqualified
as well. That way you can use the type `Person` unqualified. `State.Person`
should be imported qualified as `State`. Thus to look up a person by id we might
invoke `State.get`. 

If we needed to access a `Document` record, we import `Types.Document` qualified
as `Document` and `State.Document` fully qualified. There is a little redundancy
in the type/constructor name (if you have to write `Document.Document` and it
bothers you, you can important `Document(Document)` separately), but the former
means you can have `Document.createdAt` as the record field for when the
document was created and `createdAt` for when a `Person` was created. Similarly,
`State.Document.get` would look up a `Document` by id. This of course is done
symmetrically when you are working within `Handler.Document` (assuming it existed).

Other `Handler` modules should also be imported fully qualified. It's less
common to need this, but it comes up, and the clarity of the full qualification
is great. If you end up splitting modules in more fine grained ways than these
three (and sometimes I have, e.g., splitting out form validation, or the code
that is used in templates), the same general principles apply: within the
`Category1.X` module, any `Category2.X` is imported qualified as `Category2`
(unless `Category2` is `Types` in which case it's imported unqualified), and
`Category3.Y` is imported fully qualified. 

### Summary

Although it wasn't the original intention (just making code more understandable
was), I've realized this naming scheme really matches the mantra that name
length should match name locality (i.e., the further from definition, the longer
the name should be), writ large. Functions that are highly relevant to a
particular module have short names (since they are unqualified or minimally
qualified), whereas ones from very different parts of the application have
longer names that tell more about what they are for. It also helps to serve as a
reminder when things start to get tangled, as you end up using more fully
qualified functions (and that's a sign that maybe some refactoring is needed).
