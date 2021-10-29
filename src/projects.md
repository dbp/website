## Projects

* ### Fn - A functional web framework.
  Fn is a web framework written in the functional language Haskell,
  with the explicit goal of writing code in a more functional style -
  handlers to web requests are normal functions with arguments and
  return types (rather than monadic actions), and there are no monad
  transformers. In many ways it makes writing web code more like
  writing plain Haskell. [github.com/positiondev/fn](https:/github.com/positiondev/fn).

* ### Hworker - A reliable at-least-once job processor.
  Hworker is a Redis-backed background job processor written in
  Haskell. It handles running jobs that, for various reasons (usually
  that they take a while to complete), have to run in the background
  (sending email is a common example). It has a strong focus on
  reliability, in that once a job has been queued, it is guaranteed to
  run at least once. [github.com/dbp/hworker](https://github.com/dbp/hworker).

* ### Periodic - A reliable scheduled job runner.
  Periodic is a Redis-backed scheduled job runner (ie, like cron) written in
  Haskell. It handles running jobs at particular intervals.
  [github.com/positiondev/periodic](https://github.com/positiondev/periodic).

* ### Rivet - A migration tool.
  Rivet is the beginning of a database migration tool for Haskell,
  that allows you to both write SQL migrations, but also ones that
  have to run arbitrary Haskell code. It's pretty early, but is used
  in various projects.
  [github.com/dbp/rivet](https://github.com/dbp/rivet).

* ### Pyret Programming Language
  A research / teaching language being developed by a small team at
  Brown University. It's a scripting language that tries to learn from
  the best features that scripting languages have, but it also has
  serious functional roots and optional
  types. [pyret.org](http://www.pyret.org).
