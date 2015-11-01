## Projects

* ### Fn - A functional web framework.
  Fn is a web framework written in the functional language Haskell,
  with the explicit goal of writing code in a more functional style -
  handlers to web requests are normal functions with arguments and
  return types (rather than monadic actions), and there are no monad
  transformers. In many ways it makes writing web code more like
  writing plain Haskell. [github.com/dbp/fn](https:/github.com/dbp/fn).

* ### Hworker - A reliable at-least-once job processor.
  Hworker is a Redis-backed background job processor written in
  Haskell. It handles running jobs that, for various reasons (usually
  that they take a while to complete), have to run in the background
  (sending email is a common example). It has a strong focus on
  reliability, in that once a job has been queued, it is guaranteed to
  run at least once. [github.com/dbp/hworker](https://github.com/dbp/hworker).

* ### Rivet - A migration tool.
  Rivet is the beginning of a database migration tool for Haskell,
  that allows you to both write SQL migrations, but also ones that
  have to run arbitrary Haskell code. It's pretty early, but is used
  in various projects.
  [github.com/dbp/rivet](https://github.com/dbp/rivet).

* ### Lexical Style Sheets (LSS)
  LSS is an experiment in making CSS more maintainable. The main goal is that
  all styling is statically (or lexically) scoped to a specific part of
  the DOM tree, by attaching groups of styles to specific nodes. These
  groups are named and can be parametrized (so, for example, if you have
  a few different menus that share most styling in common, this is
  supported), and you can also define constants in your `.lss` files.
  It's available to be used at [github.com/dbp/lss](https://github.com/dbp/lss).

* ### Pyret Programming Language
  A research / teaching language being developed by a small team at
  Brown University. It's a scripting language that tries to learn from
  the best features that scripting languages have, but it also has
  serious functional roots and optional
  types. [pyret.org](http://www.pyret.org).


* ### Position Sites
  A platform for building dynamic websites that imposes no constraints
  on the shape of information you will be using. It is still pretty
  new, but is being used by clients currently, and the source is
  available at
  [github.com/dbp/positionsites](https://github.com/dbp/positionsites). Descriptions
  of how some of it works are available at
  [positionsites.com/docs](http://positionsites.com/docs).


* ### Analyze
  A lightweight web based analytics and error reporting platform, used
  with clients of [Position Studios](http://positionstudios.com). It
  is open source, available at
  [github.com/dbp/analyze](http://github.com/dbp/analyze).


<!-- * ### Democracy Now! Player -->

<!--   A simple player that automatically plays the current days show of -->
<!--   the news program [Democracy Now!](http://democracynow.org). It -->
<!--   remembers where you are automatically, and can synchronize that -->
<!--   across devices (mobile, desktop, etc) through a lightweight -->
<!--   mechanism. Old version is available at -->
<!--   [github.com/dbp/dnplayer](https://github.com/dbp/dnplayer), and the -->
<!--   backend of a new, as-yet unreleased mobile version is available at -->
<!--   [github.com/dbp/dnplayer_server](https://github.com/dbp/dnplayer_server). -->

* ### Rustle
  An api search for the [Rust](http://rust-lang.org) programming language that allows you to search for core functions by type (or by name). This is pretty outdated at this point, but can be  read about at [github.com/dbp/rustle](http://github.com/dbp/rustle).


<!-- * ### WeShift -->
<!--   A webapp for workplaces that allows workers to keep track of hours and changes to their shifts, switch with other people, and track timesheets for discrepencies. It's intended to be useable with or without participation by management. Open to anyone who wants to use it at [weshift.org](http://weshift.org), or anyone who wants to hack on it at [hub.darcs.net/position/weshift](http://hub.darcs.net/position/weshift). -->

<!-- * ### Housetab -->
<!--   A webapp for expense tracking among groups of people. Based on the idea that certain people pay for things, but groups of people might share the purchase (whether it be rent, food, etc). Can cope with different people at different percentages of sharing, and other stuff too. Online at [housetab.org](http://housetab.org), source at [darcsden.com/position/housetab](http://darcsden.com/position/housetab). -->
<!--   <br><br> -->
