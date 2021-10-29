# Declarative ajax - imagining Heist-Async

I've recently started working with Snap, the Haskell web framework, (http://snapframework.com), and one reason (among many) for my reason to switch from Ocsigen, a web framework written in OCaml (which I've written posts about before) was the desire to more flexibly handle ajax based websites. While it seems good in some ways, I eventually decided that Ocsigen's emphasis on declaring services as having certain types (ie, a fragment of a page, a whole page, a redirect, etc) is in some ways at odds with the way the web works.

After starting to work in Haskell again, and with the Snap team authored templating system Heist, I immediately began looking for ways to work with ajax content more flexibly than I had been doing before. Inspired by the work of Facebook on Primer (provided to the world at https://gist.github.com/376039 ), which is their base-line system for dynamic content - basically, event listeners waiting for onclick events on links that have a special attribute that says it should perform an ajax request, and event listeners for onsubmit events on forms that have a special attribute that indicates the forms should be serialized and submitted asynchronously. But even more interesting than that (to me) was the other half of their system (not, I believe, public, and regardless, written in PHP), which is that the server side response decides what client side div's it should replace.

At first that sounds a little dirty - it basically entails mixing (conceptually) server code and client code. But then it allows a different sort of methodology - that even with client side modifications, it is the server that ultimately has all control - including what to replace on the client. This is a fascinating idea, because clientside code is notoriously limited be being written in javascript (or with javascript libraries), and thinking about having to maintain clientside and serverside code seems to be a much dirtier solution than having the server, in short, control the client.

Taking this idea, and bringing it into the world of Heist, which is (in my opinion) a fantastic templating system (more info at http://snapframework.com/docs/tutorials/heist ), ended up being quite straightforward, as Heist lends itself to the idea of extending the syntax of html, much like the facebook primer system did.

At first I thought that there should be haskell code that would specify things like "replaceDivsWithSplices ..." where div's would be identified and corresponding splices (things that can be inserted into heist templates) would replace them, and then "replaceDivsWithTemplates", etc, but the whole solution seemed a little off.

And then I realized that the entire idea could be summed up with a single tag: "div-async". The idea would be, this would be a special div that could foreseeably be replaced by an asychronous response. A template would have many divs that were marked this way, which in a non-async response would do nothing special, but when an async response came back, all div-async's would replace corresponding tags on the page.

The only things that remained were the two tags to start the async requests, which I named "a-async" and "form-async", and a little javascript to make the moving parts work together. And so, heist-async was born. (for the impatient, the code exists at https://github.com/dbp/heist-async , and while I am using this code currently and it seems to work, it could change significantly as things are worked out)

The basics of how this works should be obvious, but I can illustrate a basic example. On a page you have an announcements box. You want the user to be able to click a button and have the announcements box reload without reloading the whole page (new announcements may have occurred). So you have a page template that looks like this:

    <html><body><h1>Some page</h1>
      <apply template="announcements></apply>
      <a-async href="/recent_announcements">Reload</a-async>
    </body></html>

And an announcements template that looks like this:

    <div-async name="announcements">
      <announcements>
        <text/>
      </announcements>
    </div-async>

Now to glue this together, all you need to do is serve the original page (with the proper splice set so that the <announcements> tag actually works), and, at the /recent_announcements url, you just serve the announcements template. Since it is the exact same template, it obviously has the same identifier for the div-async (which is just the attribute "name"), and will therefore replace the current anouncements box with the recently loaded one.

Now that is pretty cool - what it means is that you can have one set of templating code, and the only change you need to do is separate any parts you want to be able to load asynchronously into separate templates, and make sure there is a div-async wrapper around it. (NOTE: since I didn't mention it before, it might be helpful to now - div-async is just a regular div, so you can set all the regular things, like id, class, etc. Also feel free to take existing div's and just add -async and set a name).

At this point, I was pretty happy with this, and thought it was working pretty well, but of course the real world is much more complicated, and not everything is so simple - sometimes a single asynchronous request should mean a lot of different things on a page should change. In this case, it is possible that the simple template inheritance will not work, but with the addition of a template that is just for the response, that includes all the templates that should be updated, it seems to work pretty well. An example of one of these could be:

    <apply template="announcements"></apply>
    <apply template="title"></apply>

In this case, there is still no duplication of formatting code, all that exists now is an explicit list of all the parts of the page that should be replaced by a given request.

Other common things; to hide an element, sending back:

    <div-async name="something" style="display:none"></div-async>

Should work. You could also put some empty placeholder div's like that on a page, and later replace them with ones with actual content.

What I noticed about this is that it makes dynamic page changes very explicit in the templates, which I think is a very good thing - and certainly makes it easier to reason about page changes.

Getting to this point, I started using this to implement a bunch of parts of a new site I'm working on, and I was happily impressed by how it all seemed to be working. Using this, it seems like ajax can be thought of as just an aspect of the templating system - describe what should be replaced, and it will be, without ever having to worry about the clientside code (which is 12k of lightweight libraries and 60 significant lines of code of custom javascript. The 60 lines should easily be able to be translated to depend on common javascript libraries like jQuery, I just didn't want to make that a requirement).

I'm interested in feedback on the library, and ways that it can be improved. It is still very early software (a week ago, it did not exist), but it is something that I've found very powerful, and I'm kind of interested in where it can be taken / what people think about it.
