---
title: TDD with Haskell and the Snap Web Framework
author: Daniel Patterson
date: February 9th, 2014
---

Test driven development is nothing new - people have been talking about it in various forms for at least 14 years[^1]. Within the web development community, it is used most pervasively in dynamic languages, with Ruby probably being the best example, both in quality of libraries and pervasive use. And with good reason - without tests you really have no idea if the changes you've made have just broken your whole site. However, when I'm writing tests for Ruby code, most of what I'm writing is covering a lot more than just types, so while there are certainly lots of bugs that Haskell's type system catches, types don't catch everything. So I'm announcing a library that should make it a lot easier to write tests with Haskell and the Snap web framework - tests that involve the database, web requests, etc. The general design is inspired by RSpec, and the library is available on hackage at [http://hackage.haskell.org/package/snap-testing](http://hackage.haskell.org/package/snap-testing).

### Examples and preview of use

Here are a few basic tests:

``` haskell
name "/ success" $
  succeeds (get "/")
name "/foo/bar not found" $
  notfound (get "/foo/bar")
```

The tests are hierarchical, with (optional) names on the nodes of the tree. There are simple functions to check against status codes, modification of state, response bodies, etc. For a more complicated example, consider:

``` haskell
name "/auth/new_user" $ do
  name "success" $
    succeeds (get "/auth/new_user")
  name "creates a new account" $
    cleanup clearAccounts $
    changes (+1) countAccounts (post "/auth/new_user" $ params
                                [ ("new_user.name", "Jane")
                                , ("new_user.email", "jdoe@c.com")
                                , ("new_user.password", "foobar")])
```

We can also run arbitrary handlers to get data that is used later on in tests, like:

``` haskell
token <- fmap fromJust $ eval (newToken site)
...
eval (invalidateToken token)
name "should not show invalidated tokens" $
  notcontains (get site_url) (tokenText token)
```

Finally, it is (relatively) easy to add new utilities. For example, we can write an application specific utility (in my case, users have both a auth snaplet `AuthUser` and an application specific `Account`) to create a new user and log in as them:

``` haskell
withUser :: SnapTesting App a -> SnapTesting App a
withUser = modifySite $ \site -> do
  (_, au) <- fmap fromJust getRandomUser
  with auth $ forceLogin au
  site
```

Where `getRandomUser` is a helper with signature:

``` haskell
getRandomUser :: AppHandler (Maybe (Account, AuthUser))
```

We can also get the random user using `eval` (shown above with the `token`) and then use that later with a helper like this:

``` haskell
loginAs :: AuthUser -> SnapTesting App a -> SnapTesting App a
loginAs au = modifySite $ \site -> do
  with auth $ forceLogin au
  site
```

For example:

``` haskell
loginAs user $ do
  name "success with right login" $
    succeeds (get site_url)
  name "has site name in response" $
    contains (get site_url) "Some Site"
  name "/edit displays a page with a form on it" $
    contains (get $ B.append site_url "/edit") "<form"
```

There are multiple possible report generators (and it is easy to write more) - currently two in the library - a report of the tree of tests printed to the console, and one that uses linux's desktop-notification protocol to display whether the tests passed or failed (with counts). The console one looks like:

```
/ success
  PASSED
/foo/bar not found
  PASSED
/auth/new_user
  success
    PASSED
  creates a new account
    PASSED
...
```

### API description

All the tests are run in the `SnapTesting` monad, which takes two parameters - one being what is usually called `App` - the main datastructure with the application state, and the second being the current value. To run tests, use the following function:

``` haskell
runSnapTests :: [[TestResult] -> IO ()] -> Handler b b ()
             -> SnapletInit b b -> SnapTesting b () -> IO ()
```

This is a big signature, but easy to piece apart. The first argument is a list of repart generators - they take a list of `TestResult` and do something with them. `TestResult` is defined as:

``` haskell
data TestResult = ResultName Text [TestResult] | ResultPass Text | ResultFail Text
```

The second argument is the top level handler that all requests will be run against. It can be any handler, but it should probably be something like `route routes`, where `routes` is what you pass to `addRoutes` in your initializer. The third argument is the initializer for your application, and the final argument is the test block. Putting it all together, we get something like:

``` haskell
main :: IO ()
main = runSnapTests [consoleReport, linuxDesktopReport] (route routes) app $ do
  name "/ success" $
    succeeds (get "/")
  name "/foo/bar not found" $
    notfound (get "/foo/bar")
...
```

Which we can put in a file like `Test.hs` and run with runghc.

There are many functions you can use in a `SnapTesting` block. You can look at the docs for signatures, or above for uses, but brief descriptions are:

``` haskell
name -- add a named node to the test tree
succeeds -- assert that the given request (built by get or post) succeeds
notfound -- assert request 404s
redirects -- assert request redirects (status 3XX)
redirectsto -- assert request redirects to specific url
changes -- given a predicate and a monadic value, check difference before and after running request
contains -- assert response contains regular expression
notcontains -- assert response does not contain regular expression
cleanup -- run an action after a test block
eval -- evaluate an action, returning the value
```

Further, there are a couple functions for building requests:

``` haskell
get -- creates a get request, given a url (no params)
post -- creates a post request, given a url and params map
params -- helper to turn tupled list into params map
```

And finally a helper for writing new utilities, that allows you to modify the top level handler (the entire site, essentially) for a block:

``` haskell
modifySite
```

### Future Improvements

There are two major things that I'd like to improve:

  - The first is to print reports incrementally, not just at the end.
  - The second is to not pay the initialization cost for each request. Right now, the snaplet testing api (what this library uses) is designed to take an initializer and an action, and it runs the initializer and then the action. Ideally, this could be separated, as especially with sites with lots of templates, initialization takes a non-trivial amount of time.


[^1]: Extreme programming was published in 1999, giving a lower bound on the age.
