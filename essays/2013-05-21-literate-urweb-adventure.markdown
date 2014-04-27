---
title: A Literate Ur/Web Adventure
author: Daniel Patterson
date: May 21th, 2013
---

[Ur/Web](http://www.impredicative.com/ur/) is a language / framework
for web programming that both makes it really hard to write code with
bugs / vulnerabilities and also makes it really easy to write
reactive, client-side code, all from a single, simple, codebase. But
it is built on some pretty deep type theory, and while it is an
incredibly practical research project, some corners of it still show -
like error messages that scroll pages off the screen. I've
experimented with it before, and have written a small application that is
beyond a demo, but still small enough to be digestible.

For completeness and clarity, I present it here in complete literate
style - all the files, interspersed with comments, are presented. They
are split into sections by file, which are named in headings. All the
text between the file name and the next file name that is not actual
code is within comments (that is what the `#`, `(*` and `*)` are for),
so you can copy the whole thing to the files and build the
project. All the files should go into a single directory. It builds
with the current version of Ur/Web. You can try out the application,
as it currently exists (which might have been changed since writing
this), at [lab.dbpmail.net/dn](http://lab.dbpmail.net/dn). The full
source, with history, is available at
[hub.darcs.net/dbp/dnplayer](http://hub.darcs.net/dbp/dnplayer).

The application is a video player for the daily news program
[Democracy Now!](http://democracynow.org). The main point of it is to remember
where in the show you are, so you can stop and resume it, across devices.
It should work on desktop and mobile applications - I have targetted
Chrome on Android, Chrome on computers, and Safari on iPhones/iPads. The main
reason for not supporting Firefox is that it does not support the (proprietary)
video/audio codecs that are the only format that Democracy Now! provides.

## dn.urp

    # .urp files are project files, which describe various meta-data about
    # Ur/Web applications. They declare libraries (like random, which we'll
    # see later), information about the database (both what it is named and
    # where to generate the sql for the tables that the application is using).
    # They separate meta-data declarations from the modules in the project by
    # a single blank line, which is why we have comments on all blank lines
    # prior to the end.
    library random
    database dbname=dn
    sql dn.sql
    # 
    # They also allow you to rewrite urls. By default, urls are generated
    # consistently as Module/function_name, which means that the main
    # function inside Dn, our main module, is our root url. We can rewrite
    # one url to another, but if we leave off the second, that rewrites to
    # root. We can also strip prefixes from urls with a rewrite with a *.
    # 
    rewrite url Dn/main 
    rewrite url Dn/*
    # 
    # safeGet allows us to declare that a function is safe to generate urls
    # to, ie that it won't cause side effects. Along the same safety lines,
    # we declare the external urls that we will generate and scripts we will
    # include - making injecting resources hosted elsewhere hard (as Ur/Web
    # won't allow you to create urls to anything not declared here).
    #
    # 
    safeGet player
    allow url http://dncdn.dvlabs.com/ipod/*
    allow url http://traffic.libsyn.com/democracynow/*
    allow url http://dbpmail.net/css/default.css
    allow url http://dbpmail.net
    allow url http://hub.darcs.net/dbp/dnplayer
    allow url http://democracynow.org
    allow url http://lab.dbpmail.net/dn/main.css
    script http://lab.dbpmail.net/static/jquery-1.9.1.min.js
    # One odd thing - Ur/Web doesn't have a static file server of its own, so
    # you need to host any FFI javascript elsewhere. Here's where the javascript for
    # this application, presented later, is hosted. For trying it out, leaving
    # this the same is fine, though if you want to change the javascript, or
    # not depend on my copy being up, you should change this and the reference in
    # the application.
    script http://lab.dbpmail.net/dn/dn.js
    # 
    # Next, we declare that we have foreign functions in a module called dnjs. This
    # refers to a header file (.urs), and we furthermore declare what functions within
    # it we are using. We declare them as effectful so that they aren't called multiple
    # times (like Haskell, Ur/Web is purely functional, so normal, non-effectful functions are not
    # guaranteed to be called exactly once - they could be optimized away if the compiler
    # did not see you use the result of the function, and could be inlined (and thus
    # duplicated) if it would be more efficient).
    # 
    ffi dnjs
    jsFunc Dnjs.init=init
    effectful Dnjs.init
    jsFunc Dnjs.set_offset=set_offset
    effectful Dnjs.set_offset

    # The last thing we declare is the modules in our project. $/ is a prefix that means to
    # look in the standard library, as we are using the option type (Some/None in OCaml/ML,
    # Just/Nothing in Haskell, and very roughly a safe null in other languages). sourceL is
    # a helper for reactive programming (to be discussed later). And finally, our main module,
    # which should be last.
    #         
    $/option
    sourceL
    dn

## dn.urs

    (*

`.urs` files are header files (signature files), which declare all the public functions in the module (in this case, the `Dn` module). We only export our `main` function here, but all functions that have urls that we generate within the applications are also implicitly exported.

The type of main, `unit -> transaction page`, means that it takes no input (`unit` is a value-less value, a placeholder for argumentless functions), and it produces a `page` (which is a collection of xml), within a `transaction`. `transaction`, like Haskell's IO monad, is the way that Ur/Web handles IO in a safe way. If you aren't familiar with IO in Haskell, you should go there and then come back.

    *)
    val main : unit -> transaction page

## random.urp


    # Random is a simple wrapper around librandom to provide us with random
    # strings, that we use for tokens. We included it above with the line
    # `library random`. Libraries are declared with separate package files,
    # and here we link against librandom.a, include the random header, and declare
    # that we are using functions declared in random.urs (that is the ffi line).
    # We also declare that all three functions are effectful, because they have
    # side effects
    #
    # NOTE: It has been pointed out that instead of doing this, we could either:
    #       A. use Ur/Web's builtin `rand` function, and construct the strings
    #          without using the FFI, or even easier:
    #       B. just use the integers than `rand` generates as tokens.
    #
    #       I didn't realize that `rand` existed when I wrote this, but I'm leaving
    #       it in because it is a (concise) introduction to the FFI, which, given
    #       the relatively small body of Ur/Web libraries, is probably something
    #       you'll end up using if you build any large applications.
    effectful Random.init
    effectful Random.str
    effectful Random.lower_str
    ffi random
    include random.h
    link librandom.a

## random.urs

    (*

 Like with main, we see that the signatures of these functions are
 'transaction unit' and `int -> transaction string`, which means the
 former takes no arguments, and the latter two take integers
 (lengths), and produce `string`s, within `transactions`. They are
 within `transaction` because they create side effects (ie, if you run
 them twice, you will likely not get the same result), and thus we
 want the compiler to treat them with care (as described
 earlier). Init seeds the random number generator, so it should be
 called before the other two are
 

    *)
    val init: transaction unit
    val str : int -> transaction string
    val lower_str : int -> transaction string

## random.h

    /*

Here we have the header file for the C library, which declares the same signatures as above, but
using the structs that Ur/Web uses, and the naming convention that it expects (uw_Module_name).

    */
    #include "types.h"

    uw_Basis_unit uw_Random_init(uw_context ctx);
    uw_Basis_string uw_Random_str(uw_context ctx, uw_Basis_int len);
    uw_Basis_string uw_Random_lower_str(uw_context ctx, uw_Basis_int len);

## random.c

    /*

And finally the C code to generate random strings.

    */
    #include "random.h"
    #include <stdlib.h>
    #include <time.h> 
    #include "urweb.h"

    /* Note: This is not cryptographically secure (bad PRNG) - do not
       use in places where knowledge of the strings is a security issue.
    */
    
    uw_Basis_unit uw_Random_init(uw_context ctx) {
      srand((unsigned int)time(0));
    }
    
    uw_Basis_string uw_Random_str(uw_context ctx, uw_Basis_int len) {
      uw_Basis_string s;
      int i;
    
      s = uw_malloc(ctx, len + 1);
    
      for (i = 0; i < len; i++) {
        s[i] = rand() % 93 + 33; /* ASCII characters 33 to 126 */
      }
      s[i] = 0;
    
      return s;
    }
    
    uw_Basis_string uw_Random_lower_str(uw_context ctx, uw_Basis_int len) {
      uw_Basis_string s;
      int i;
    
      s = uw_malloc(ctx, len + 1);
    
      for (i = 0; i < len; i++) {
        s[i] = rand() % 26 + 97; /* ASCII lowercase letters */
      }
      s[i] = 0;
    
      return s;
    }


## dn.ur

    (*

We'll now jump into the main web application, having seen a little bit
about how the various files are combined together. The first thing we have
is the data that we will be using - one database table, for our users, and one
cookie. The tables are declared with Ur/Web's record syntax, where `Token`, `Date`,
and `Offset` are the names of fields, and `string`, `string`, and `float` are the types.

All tables that are going to be used have to be declared, and Ur/Web will generate
SQL to create them. This is, in my opinion, one weakness, as it means that Ur/Web
doesn't play well with others (as it needs the tables to be named uw_Module_name),
and, even worse, if you rename modules, or refactor where the tables are stored,
the names of the tables need to change - if you are just creating a toy, you can
wipe out the database and re-initialize it, but obviously this isn't an option
for something that matters, and you just have to manually migrate the tables, based
on the newly generated database schemas. Luckily the tables / columns are predictably
named, but it still isn't great.

    *)
    (* Note: Date is the date string used in the urls, as the most
       convenient serialization, Offset is seconds into the show *)
    table u : {Token : string, Date : string, Offset : float} PRIMARY KEY Token
    cookie c : string
    (*

Ur/Web provides a mechanism to run certain code at times other than requests, called `task`s.
There are a couple categories, the simplest one being an initialization task, that is run
once when the application starts up. We use this to initialize our random library.

    *)
    task initialize = fn () => Random.init
    (*

Part of being a research project is that the standard libraries are
pretty minimal, and one thing that is absent is date handling. You can
format dates, add and subtract, and that's about it. Since a bit of
this application has to do with tracking what show is the current one,
and whether you've already started watching it, I wrote a few
functions to answer the couple date / time questions that I
needed. These are all pure functions, and all the types are inferred.

    *)
    val date_format = "%Y-%m%d"

    fun before_nine t =
        case read (timef "%H" t) of
            None => error <xml>Could not read Hour</xml>
          | Some h => h < 9
        
    fun recent_show t =
       let val seconds_day = 24*60*60
           val nt = (if before_nine t then (addSeconds t (-seconds_day)) else t)
           val wd = timef "%u" nt in
       case wd of
           "6" => addSeconds nt (-seconds_day)
         | "7" => addSeconds nt (-(2*seconds_day))
         | _ => nt
       end
    (*

The server that I have this application hosted on is in a different
timezone than the show is broadcasted in (EST), so we have to adjust
the current time so that we can tell if it is late enough in the day
to get the current days broadcast. Depending on what timezone your
computer is, this may need to be changed.

    *)
    fun est_now () =
        n <- now;
        return (addSeconds n (-(4*60*60)))

    (*

We track users by tokens - these are short random strings generated
with our random library. The mechanism for syncing devices is to visit
the url (with the token) on every device, so the tokens will need to be
typed in. For that reason, I didn't want to make the tokens very long,
which means that collisions are a real possibility. To deal with this,
I set the length to be 6 characters, plus the number of tokens, log_26
(since users are encoded with lower case letters, n users can be encoded
with log_26 characters, so we use this as a baseline, and add
several so that the collision probability is low).

In this, we see how SQL queries work. You can embed SQL (a subset of
SQL, defined in the manual), and this is translated into a query
datatype, and there are many functions in the standard library to run
those queries. We see here two: `oneRowE1`, which expects to get back
just one row, and will extract 1 value from it. `E` means that it
computes a single output expression.  Note that it will error if there
is no result, but since we are selecting the count, this should be
fine. `hasRows` is an even simpler function; it simply runs the query
and returns true iff there are rows.

Also note that we refer to the table by name as declared above, and we refer
to columns as record members of the table. To embed regular Ur/Web values within
SQL queries, we use `{[value]}`. These queries will not type check if you try
to select columns that don't exist, and of course does escaping etc.

    *)
    (* linking to cmath would be better, but since I only
       need an approximation, this is fine *)
    fun log26_approx n c : int =
        if c < 26 then n else
        log26_approx (n+1) (c / 26)
    
    
    (* Handlers for creating and persisting token *)
    fun new_token () : transaction string =
        count <- oneRowE1 (SELECT COUNT( * ) FROM u);
        token <- Random.lower_str (6 + (log26_approx 0 count));
        used <- hasRows (SELECT * FROM u WHERE u.Token = {[token]});
        if used then new_token () else return token

    (*

We write small functions to set and clear the tokens. We do this so that after
a user has visited the unique player url at least once on each device, they will
only have to remember the application url, not their unique url. `now` is a value
of type `transaction time`, which gives the current time, and `setCookie`/`clearCookie`
should be self explanatory.

    *)
    fun set_token token =
        t <- now;
        setCookie c {Value = token,
                     Expires = Some (addSeconds t (365*24*60*60)),
                     Secure = False}
        
    fun clear_token () =
        clearCookie c

    (*

The next thing is a bunch of html fragments. Ur/Web doesn't have a "templating" system,
but it is perfectly possible to create one by defining functions that take the values
to insert in. I've opted for a simpler option, and just defined common pieces. HTML
is written in normal XML format, within `<xml>` tags, and like the SQL tags, these are
typechecked - having attributes that shouldn't exist, nesting tags that don't belong,
or not closing tags all cause the code not to compile.

There are a couple rough edges - some tags are not defined (but you
can define new ones in FFI modules), and some attributes can't be used
because they are keywords (hence `typ` instead of `type`), but overall
it is a neat system, and works very well.

    *)
    fun heading () = 
        <xml>
            <meta name="viewport" content="width=device-width"/>
            <link rel="stylesheet" typ="text/css" href="http://dbpmail.net/css/default.css"/>
            <link rel="stylesheet" typ="text/css" href="http://lab.dbpmail.net/dn/main.css"/>
        </xml>
    
    fun about () =
        <xml>
          <p>
          This is a player for the news program
          <a href="http://democracynow.org">Democracy Now!</a>
          that remembers how much you have watched.
        </p>
        </xml>
        
    fun footer () =
        <xml>
          <p>Created by <a href="http://dbpmail.net">Daniel Patterson</a>.
            <br/>
            View the <a href="http://hub.darcs.net/dbp/dnplayer">Source</a>.</p>
        </xml>

    (*

We now get to the web handlers. These are all url/form entry points,
and do the bulk of the work.  The first one, `main`, which we rewrote
in `dn.urp` to be the root handler, is mostly HTML - the only catch
being that if you have a cookie set, we just redirect you to the
player.

`getCookie` returns an `option CookieType` where `CookieType` is the
type of the cookie (in our case, it is a string). `redirect` takes a
`url`, and urls can be created from handlers (ie, values of type
`transaction page`) with the `url` function. So we apply `player`
which is a handler we'll define later, to the token value (as a token
is the parameter that `player` expects), and grab a url for that.

One catch to this is that Ur/Web doesn't know that `player` isn't
going to cause side effects, which would mean that it shouldn't have a
url created for it (side effecting things should only be POSTed to),
which was why we had to declare `player` as `safeGet` in `dn.urp`

We also see a form that submits to `create_player`, which is another
handler that we will define. One thing to note is that `create_player`
is a `unit -> transaction page` function - and the action for the submit
is just `create_page`, not `create_page ()` - the action of submitting
passes that parameter.

    *)
    fun main () =
        mc <- getCookie c;
        case mc of
            Some cv => redirect (url (player cv))
          | None => 
            return <xml>
              <head>
                {heading ()}
              </head>
              <body>
                <h2><a href="http://democracynow.org">Democracy Now!</a> Player</h2>
                {about ()}
                <p>
                  You can listen to headlines on your way to work on your phone,
                  pick up the first segment during lunch on your computer at work, and
                  finish the show in the evening, without worrying what device you are
                  on or whether you have time to watch the whole thing.
                </p>
                <h3>How it works</h3>
                <ol>
                  <li>
                    <form>
                      To start, if you've not created a player on any device:
                      <submit action={create_player} value="Create Player"/>
                    </form>
                  </li>
                  <li>Otherwise, visit the url for the player you created (it should look like
                    something <code>http://.../player/hcegaoe</code>) on this device
                    to synchronize your devices. You only need to do this once per device, after that
                    just visit the home page and we'll load your player.
                  </li>
                </ol>
    
                <h3>Compatibility</h3>
                <p>This currently works with Chrome (on computers and Android) and iPhones/iPads.</p>  
                {footer ()}
              </body>
            </xml>

    (*

`create_player` is pretty straightforward, but it shows a different
part of Ur/Web's SQL support: dml supports INSERT, UPDATE, and DELETE,
in the normal ways, with the same embedding as SQL queries (that
`{[value]}` puts a normal Ur/Web value into SQL). We create a token,
create a "user", setting that they are on the current day's show and
at the beginning of it (offset 0.0), store the token, and then
redirect to the player.

    *)
    and create_player () =
        n <- est_now ();
        token <- new_token ();
        dml (INSERT INTO u (Token, Date, Offset)
             VALUES ({[token]}, {[timef date_format (recent_show n)]}, 0.0));
        set_token token;
        redirect (url (player token))

    (*

The next two functions encompass most of the player, which is the core
of the application. The way that it is structured is a little odd, but
with justification: Chrome on Android caches extremely aggressively, and
doesn't seem to pay attention to headers that say not to, which means that
if you visited the application, and then a few days later open up Chrome
again, it will seem like it is loading the page, but it is loading the
cached HTML, it is not getting it from the server. This is really bad for
us, because it means it will have both an old offset (in case you watched
some of the show from another device), but worse, on subsequent days it
will be trying to play the wrong day's show! You can manually reload the
page, but this is silly, so what we do is initially just load a blank page,
and then immediately make a remote call to actually load the page. So what
is cached is a little bit of HTML and some javascript that loads the page
for real.

We do all of this is functional reactive style: we declare a `source`,
which is a place where values will be put, and it will cause parts of
the page (that are `signal`ed) to update their values. Then we set an
onload handler for the body, which, first, makes an `rpc` call to a server
side function (which is just another function, like all of these handlers),
and then set the `source` that we defined to be the result of rendering
the player. `render` is a client-side function that just creates the
appropriate forms / html.

Finally, we will call a client-side function init, which will do some
setup and then call into the javascipt ffi to the ffi `init` function,
which will handle the HTML5 audio/video APIs (which Ur/Web doesn't
support, and are very browser specific anyway).

One incredibly special thing that is going on is the `SourceL.set os`
that is passed to javascript. If you remember from our `.urp` file, we
imported sourceL. It is a special reactive construct that allows you
to set up handlers that cause side effects (are transactions) when the
value inside the `SourceL` changes. So what is happening is we have
created one of these on the server, in `player_remote`, and sent it
back to the client. The client then curries the `set` function with
that source, producing a single argument function that just takes the
value to be updated. We hand this function to javascript, so that in
our FFI code, we can just set values into this, and it can reactively
cause stuff to happen in our server-side code.

The reactive component on the page is the `<dyn>` tag, which is a
special construct that allows side-effect free operations on
sources. `signal s` grabs the current value from the source `s`, and
in this case we just return this, but we could do various things to
it. The result of the block is what the value of the `<dyn>` tag
is. In this case, we have just made a place where we can stick HTML,
by calling `set s some_html`.


    *)
    and player token =
        s <- source <xml/>;
        return <xml>
          <head>
            {heading ()}
          </head>
          <body onload={v <- rpc (player_remote token);
                        set s (render token v.Player v.Show);
                        init token v.Player v.Source (SourceL.set v.Source) v.Video v.Audio}>
            <dyn signal={v <- signal s; return v}/>
          </body>
          </xml>
    (*

The remote component is where most of the logic of the player
resides. By now, you should be able to read most of what's going
in. Some points to highlight are the place where we create the
`SourceL` that we will pass back, and set its initial value to
offset. Also, `fresh` is a way of generating identifiers to use within
html. Our render function will use this identifier for the player, which
is necessary for the javascript FFI to know where it is. Finally, `bless`
is a function that will turn strings into urls, by checking against
the policy outlined in the `.urp` file for the application.

    *)
    and player_remote token =
        n <- est_now ();
        op <- oneOrNoRows1 (SELECT * FROM u WHERE (u.Token = {[token]}));
        case op of
            None =>
            clear_token ();
            redirect (url (main ()))
          | Some pi =>
            set_token token;
            let val show = recent_show n
                val fmtted_date = (timef date_format show) in
                (if fmtted_date <> pi.Date then
                    (* Need to switch to new day *)
                    dml (UPDATE u SET Date = {[fmtted_date]}, Offset = 0.0 WHERE Token = {[token]})
                else
                    return ());
                let val offset = (if fmtted_date = pi.Date then pi.Offset else 0.0)
                    val video_url = bless (strcat "http://dncdn.dvlabs.com/ipod/dn"
                                                  (strcat fmtted_date ".mp4"))
                    val audio_url = bless (strcat "http://traffic.libsyn.com/democracynow/dn"
                                                  (strcat fmtted_date "-1.mp3")) in
                os <- SourceL.create offset;
                player_id <- fresh;
                
                return {Player = player_id, Show = show, Offset = offset,
                        Source = os, Video = video_url, Audio = audio_url}
                end
            end


    (*

The next three functions are simple - the first just renders the actual
player. Note that we use the `player_id` we generated in
`player_remote`. Then we provide a way to forget the player (if you
want to unlink two devices, forget the player on one and create a new
one), and due to some imperfections with how we keep the time in sync
(mostly based on weirdness of different browsers implementations of
the HTML5 video/audio APIs), to seek backwards, or start the show
over, we need to tell the server explicitly, so we provide a handler to do
that.

    *)
    and render token player_id date =
         <xml><h2>
           <a href="http://democracynow.org">Democracy Now!</a> Player</h2>
           {about ()}
           <h3>{[timef "%A, %B %e, %Y" date]}</h3>
           <div id={player_id}></div>
           <br/><br/><br/>
           <form>
             <submit action={start_over token} value="Start Show Over"/>
           </form>
           <form>
             <submit action={forget} value="Forget This Device"/>
           </form>
           {footer ()}
         </xml>
    
    (* Drop the cookie, so that client will not auto-redirect to player *)
    and forget () =
        clear_token ();
        redirect (url (main ()))
    
    (* Because of browser quirks, this is the only way to get to an earlier time, synchronized *)
    and start_over token () =
        dml (UPDATE u SET Offset = 0.0 WHERE Token = {[token]});
        redirect (url (player token))

    (*

Now we get to the last web handlers. The first one is a client side
initializer. The main thing it sets up is a handler to `rpc` to the
server whenever the offset `SourceL` changes.  The call is to `update`
(which we'll define in a moment), and it optionally returns a new time
to set the client to.

This may sound a little odd, but the basic situation is that you play
part of the way through the show on one device, then pause, watch some
on another device, and now hit play on the first device. It will POST
a new time, but the server will tell it that it should actually be at
a later time, and so we use the javascript FFI function `set_offset`
to set the offset.

Finally we make it so that the client silently fails if the connection
fails (this is bad behavior, but simple), and call the javascript FFI
initialization function, which will set up the player and any HTML5 API
related stuff.

    *)
    and init token player_id os set_offset video_url audio_url =
        SourceL.onChange os (fn offset => newt <- rpc (update token offset);
                                          case newt of
                                              None => return ()
                                            | Some time => Dnjs.set_offset time);
        offset <- SourceL.get os;
        onConnectFail (return ());
        Dnjs.init player_id offset set_offset video_url audio_url
    
    (*

The last function is the simple handler that we called when the offset
`SourceL` changes. It updates the time if the time is greater than the
recorded offset (this is why we need the `start_over` handler), and
otherwise returns the recorded offset to be updated.

    *)
    and update token offset =
        op <- oneOrNoRows1 (SELECT * FROM u WHERE (u.Token = {[token]}));
        case op of
             None => return None
           | Some r => (if offset > r.Offset then 
                           dml (UPDATE u SET Offset = {[offset]}
                                WHERE Token = {[token]} AND {[offset]} > Offset);
                           return None
                       else return (Some r.Offset))

## sourceL.urs

    (*

This came from a supplemental standard library, and, as explained earlier,
allows you to create `source`-like containers that call side-effecting handlers
when their values change.
    
    *)
    (* Reactive sources that accept change listeners *)

    con t :: Type -> Type
    
    val create : a ::: Type -> a -> transaction (t a)
    
    val onChange : a ::: Type -> t a -> (a -> transaction {}) -> transaction {}
    
    val set : a ::: Type -> t a -> a -> transaction {}
    val get : a ::: Type -> t a -> transaction a
    val value : a ::: Type -> t a -> signal a

## sourceL.ur

    (*

The `sourceL`s are built on top of normal `source`s, and just call the `OnSet` function
when you call `set`.

    *)

    con t a = {Source : source a,
               OnSet : source (a -> transaction {})}
    
    fun create [a] (i : a) =
        s <- source i;
        f <- source (fn _ => return ());
    
        return {Source = s,
                OnSet = f}
    
    fun onChange [a] (t : t a) f =
        old <- get t.OnSet;
        set t.OnSet (fn x => (old x; f x))
    
    fun set [a] (t : t a) (v : a) =
        Basis.set t.Source v;
        f <- get t.OnSet;
        f v
    
    fun get [a] (t : t a) = Basis.get t.Source
    
    fun value [a] (t : t a) = signal t.Source

## dnjs.urs

    (*

This is the signature file for our javascript FFI. It declares what functions will be
exported to be accessible within Ur/Web, and what types they have.

    *)
    val init : id -> (* id for player container *)
               float -> (* offset value *)
               (float -> transaction unit) -> (* set function *)
               url -> (* video url *)
               url -> (* audio url *)
               transaction unit
    
    val set_offset : float -> transaction unit

## dn.js

    /*

Since this is a adventure in Ur/Web, not Javascript, and there are
plenty of places to learn about the quirks and features of HTML5 media
APIs (and I don't claim to be an expert), I'm just going to paste the
code in without detailed commentary. The only points that are worth
looking at are how we use `setter`, which you will remember is a
curried function that will be updating a `SourceL`, causing rpcs to
update the time. To call functions from the FFI, you use `execF`, and
to force a transaction to actually occur, you have to apply the
function (to anything), so we end up with double applications.

Other than that, all that is here is some browser detection (as different
browsers have different media behavior) and preferences about media type in
localstorage.

    */
    function init(player, offset, setter, video_url, audio_url) {
        // set up toggle functionality
        $("#"+player).after("<button id='toggle'>Switch to " +
                            (prefersVideo() ? "audio" : "video") + "</button>");
        $("#toggle").click(function () {
            window.localStorage["dn-prefers-video"] = !prefersVideo();
            location.reload();
        });
    
        // put player on the page
        if (canPlayVideo() && prefersVideo()) {
            $("#"+player).html("<video id='player' width='320' height='180' controls src='" +
                               video_url + "'></video>");
        } else {
            $("#"+player).html("<audio id='player' width='320' controls src='" +
                               audio_url + "'></audio>");
        }
    
        // seek / start the player, if applicable
        if (isDesktopChrome()) {
            $("#player").one("canplay", function () {
                var player = this;
                if (offset != 0) {
                    player.currentTime = offset;
                }
                player.play();
                window.setInterval(update_time(setter), 1000);
            });
        } else if (isiOS() || isAndroidChrome()) {
            // iOS doesn't let you seek till much later... and won't let you start automatically,
            // so calling play() is pointless
    	$("#player").one("canplaythrough",function () {
    	    $("#player").one("progress", function () {
    		if (offset != 0) {
                        $("#player")[0].currentTime = offset;
                    }
                    window.setInterval(update_time(setter), 1000);
    	    });
    	});   
        } else {
            $("#player").after("<h3>As of now, the player does not support your browser.</h3>");
        }
    }
    
    function set_offset(time) {
        var player = $("#player")[0];
        if (time > player.currentTime) {
            player.currentTime = time;
        }
        
    }
    
    // the function that grabs the time and updates it, if needed
    function update_time(setter) {
        return function () {
            var player = $("#player")[0];
            if (!player.paused) {
                // a transaction is a function from unit to value, hence the extra call
                execF(execF(setter, player.currentTime), null)
            }
        };
    }
    
    // browser detection / preference storage
    
    function canPlayVideo() {
        var v = document.createElement('video');
        return (v.canPlayType && v.canPlayType('video/mp4').replace(/no/, ''));
    }
    
    function prefersVideo() {
        return (!window.localStorage["dn-prefers-video"] || window.localStorage["dn-prefers-video"] == "true");
    }
    
    function isiOS() {
        var ua = navigator.userAgent.toLowerCase();
        return (ua.match(/(ipad|iphone|ipod)/) !== null);
    }
    
    function isDesktopChrome () {
        var ua = navigator.userAgent.toLowerCase();
        return (ua.match(/chrome/) !== null) && (ua.match(/mobile/) == null);
    }
    
    function isAndroidChrome () {
        var ua = navigator.userAgent.toLowerCase();
        return (ua.match(/chrome/) !== null) && (ua.match(/android/) !== null);
    }

## Makefile

To actually build our application, we have to first build our C library. Then we'll
build the app, using the sqlite backend. To get this running, we then need to do
`sqlite3 dn.db < dn.sql` (note you only need to do this once) and then start the
server with `./dn.exe`. You can then visit the application at `http://localhost:8080`.
This has been tested on current Debian Linux and Mac OSX.

    all: app
    
    librandom.a: librandom.o
    	ar rcs $@ $<
    
    librandom.o: random.c
    	gcc -I/usr/local/include/urweb -g -c -o $@ $<
    
    app: dn.ur dn.urs dn.urp librandom.a
    	urweb -dbms sqlite -db dn.db dn
    
    .PHONY: clean
    
    clean:
    	rm -f librandom.a librandom.o