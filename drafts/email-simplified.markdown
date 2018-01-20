---
title: "Email for Hackers: Simplified"
author: Daniel Patterson
---

Four and a half years ago I wrote a very popular guide titled [A Hacker's
Replacement for GMail](/essays/2013-06-29-hackers-replacement-for-gmail.html)
about my system for email based on `notmuch`, `emacs`, my own mail server, etc
(it's still the only thing I've written that's gotten any amount of traffic). I
ran that system for several years, but eventually one thing killed it: **spam**.
Perhaps I never got the various services set up (not just learning from prior
messages, but talking to services that told me about IP addresses that were
deemed to be spammers, etc). I still would get at least several spam messages
per day. I even tried using paid anti-spam services (so all mail filters through
them, they forward on to your mail server, and your mail server only accepts
messages from their servers). Unlike what I was led to believe, I never seemed
to have trouble with deliverability (maybe I got lucky and the IP address my
server was assigned had never spammed, but with DKIM, SPF, etc, everything
worked!).

I went back to GMail for a little while, still behind my own domain (note to the
reader: if you take nothing else from this, seriously consider registering a
domain and paying Google to host your email. The domain is ~$10-15/year, the
email is ~$4/month, and the transition to switch addresses is certainly painful,
but once you've done it Google doesn't own your identity anymore. You can, in
the future, without anyone noticing, switch to another company, or self-host.
It's worth it!), but I missed writing email in emacs. 

So, I started trying to figure out a better system. As usual, I started with requirements:

1. Be able to read, write, search email in Emacs.
2. Push notifications on the computer.
3. Be able to read, write, search email on iPhone (push notifications too).
4. Have a single repository where email is stored, to make backup simpler.
5. Keep mail organized into an Inbox and an everything else (Archive).

Points 4&5 led me to decide that, for my purposes, Maildir/IMAP is a perfectly
fine authoritative source for my email. In my previous system, I was always a
little worried about having to rely on the notmuch database, as it was an
undocumented format (that changed with new versions) with a single client
program. Realistically, aside from tags that can be automatically applied
(`sent`, `unread`, mailing list ones), the only tag that I care about in
`inbox`, and it seemed like I should be able to synchronize that to match where
messages are in the Maildir folders.




https://www.fastmail.com/?STKI=17129600
