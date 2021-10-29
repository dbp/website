# A Hacker's Replacement for GMail

_Note: Since writing this I've replaced Exim with Postfix and Courier with Dovecot. This is outlined in the Addendum, but the main text is unchanged. Please read the whole guide before starting, as you can skip some of the steps and go straight to the final system._

## Motivation

I reluctantly switched to GMail about six months ago, after using many
so-called "replacements for GMail" (the last of which was
Fastmail). All of them were missing one or more features that I
require of email:

1. Access to the same email on multiple machines (but, these can all be
machines I control).
2. Access to important email on my phone (Android). Sophisticated access not important - just a high-tech pager.
3. Ability to organize messages by threads.
4. Ability to categorize messages by tags (folders are _not_ sufficient).
5. Good search functionality.

But, while GMail has all of these things, there were nagging reasons
why I still wanted an alternative: handing an advertising company most
of my personal and professional correspondance seems like a bad idea,
having no (meaningful) way to either sign or encrypt email is
unfortunate, and while it isn't a true deal-breaker, having
lightweight programmatic access to my email is a really nice thing
(you can get a really rough approximation of this with the RSS feeds
GMail provides). Furthermore, I'd be happy if I only get important
email on my phone (ie, I want a whitelist on the phone - unexpected
email is not something that I need to respond to all the time, and this
allows me to elevate the notification for these messages, as they truly
are important).

Over the past several months, I gradually put together a mail system
that provides all the required features, as well as the three bonuses
(encryption, easy programmatic access, and phone whitelisting). I'm
describing it as a "Hacker's Replacement for GMail" as opposed to just
a "Replacement for GMail" because it involves a good deal of
familiarity with Unix (or at least, to set up and debug the whole
system it did. Perhaps following along is easier). But, the end result
is powerful enough that for me, it is worth it. I finally switched
over to using it primarily recently, confirming that all works as
expected. I wanted to share the instructions in case they prove useful
to someone else setting up a similar system.

This is somewhere between an outline and a HOWTO. I've organized it
roughly in order of how I set things up, but some of the parts are
more sketches than detailed instructions - supplement it with normal
documentation. Most are based on notes from things
as I did them, only a few parts were reconstructed. In general, I try
to highlight the parts that were difficult / undocumented, and gloss
over stuff that should be easy (and/or point to detailed
docs). Without further ado:

## Overall Design

* Debian GNU/Linux as mail server operating system (both Linux and Mac as clients, though Windows should be doable)
* Exim4 as the mail server
* Courier-IMAP for mobile usage
* Spamassassin (with Pyzor) for spam
* [notmuch](http://notmuchmail.org/) to manage the email database+tags+search
* [afew](https://github.com/teythoon/afew) for managing notmuch tagging/email moving
* Emacs client for notmuch on all computers
* [K9-Mail](https://play.google.com/store/apps/details?id=com.fsck.k9) on android (my phone)

Mail is received by the mail server and put in a Archive
subdirectory which is _not_ configured for push in K9-Mail. The mail
is processed and tagged by afew, and any messages with the tag
"important" are moved into the Important subdirectory. This directory
is set up for push in K9-Mail, so I get all important email right away. No
further tagging can be done through the mobile device, but that wasn't
a requirement. read/unread status will be synced two-way to notmuch,
which _is_ important.

## Step By Step Instructions

1. The first and most important part is having a server. I've been
really happy with VPSes I have from [Digital
Ocean](https://www.digitalocean.com/?refcode=93aab578a407) (warning: that's
a referral link. [Here's one without.](https://www.digitalocean.com)) - they
provide big-enough VPSes for email and a simple website for $5/month.
There are also many other providers. The important thing is to get a server, if
you don't already have one.

2. The next thing you'll need is a domain name. You can use a subdomain of
one you already have, but the simplest thing is to just get a new one. This is
$10-15/year. Once you have it, you want to set a few records (these are set in the
"Zone File", and should be easy to set up through the online control panel of
whatever registrar you used):

~~~~~
A mydomain.com. IP.ADDR.OF.SERVER (mydomain.com. might be written @)
MX 10 mydomain.com.
~~~~~

This sets the domain to point to your server, and sets the mail record to point
to that domain name. You will also need to set up a PTR record, or reverse DNS.
If you got the server through Digital Ocean, you can set up the DNS records
through them, and they allow you to set the PTR record for each server easily.
Whereever you set it up, it should point at mydomain.com. (Note trailing period.
Otherwise it will resolve to mydomain.com.mydomain.com - not what you want!).

3. Now set up the mail server itself. I use Debian, but it shouldn't
be terribly different with other distributions (but you should follow their
instructions, not the ones I link to here, because I'm sure there are specifics
that are dependent on how Debian sets things up). Since Debian uses
Exim4 by default, I used that, and set up Courier as an IMAP server. I
followed these instructions:
[blog.edseek.com/~jasonb/articles/exim4_courier/](http://blog.edseek.com/~jasonb/articles/exim4_courier/)
(sections 2, 3, and 4). The only important thing I had to change was to
force the hostname, by finding the line it
`/etc/exim4/exim4.conf.template` that looks like:

~~~~~
.ifdef MAIN_HARDCODE_PRIMARY_HOSTNAME
~~~~~

And adding above it, `MAIN_HARDCODE_PRIMARY_HOSTNAME = mydomain.com`
(no trailing period). This is so that the header that the mail server
displays matches the domain. If this isn't the case, some mail servers
won't deliver messages. At this point, you can test the mail server by
sending yourself emails, using the `swaks` tool, or running it through
an online testing tool like [MX Toolbox](http://mxtoolbox.com/)

The last important thing is to set up spam filtering. When using a big email
provider that spends a lot of effort filtering spam (and has huge data sets to do it),
it's easy to forget how much spam is actually sent. But, fortunately open source
software is also capable of eliminating it. To set Spamassassin up, I generally
followed the documentation on [the debian
wiki](http://wiki.debian.org/Exim#Spam_scanning). I changed the last
part of the configuration so that instead of changing the subject for
spam messages to have "`***SPAM***`", it adds the following header:

~~~~~
add_header = X-Spam-Flag: YES
~~~~~

This is the header that the default spam filter from `afew` will look for and
tag messages as spam with. Once messages are tagged as spam, they
won't show up in searches, won't ever end up in your inbox, etc. On
the other hand, they aren't ever deleted, so if something does end up
there, you can always find it (you just have to use notmuch search
with the `--exclude=false` parameter).

That sets up basic Spamassassin, which works quite well. To make it
work even better, we'll install
[Pyzor](http://wiki.apache.org/spamassassin/UsingPyzor), which is a
service for collaborative spam filtering (sort of an open source
system that gets you similar behavior to what GMail can do by having
access to so many people's email). It works by constructing a digest
of the message and hashing it, and then sending that hash to a server
to see if anyone has marked it as spam.

Install pyzor with `aptitude install pyzor`, then run `pyzor discover`
(as root), and at least on my system, I needed to run `chmod a+r
/etc/mail/spamassassin/servers` (as root) in order to have it work
(the following test command would report permission denied on that
file if I didn't). Now restart spamassassin (`/etc/init.d/spamassassin
restart`) and test that it's working, by running:

~~~~
echo "test" | spamassassin -D pyzor 2>&1 | less
~~~~

This should print (among other things):

~~~~~
Jun 29 16:31:53.026 [24982] dbg: pyzor: network tests on, attempting Pyzor
Jun 29 16:31:54.640 [24982] dbg: pyzor: pyzor is available: /usr/bin/pyzor
Jun 29 16:31:54.641 [24982] dbg: pyzor: opening pipe: /usr/bin/pyzor --homedir ...
Jun 29 16:31:54.674 [24982] dbg: pyzor: [25043] finished: exit 1
Jun 29 16:31:54.674 [24982] dbg: pyzor: check failed: no response
~~~~~

According to [the
documentation](http://wiki.apache.org/spamassassin/UsingPyzor), this
is expected, because "test" is not a valid message.

4. Now we want to set up our delivery. Create a `.forward` file in the home directory of
the account on the server that is going to recieve mail. It should contain

~~~~~
# Exim filter

save Maildir/.Archive/
~~~~~

What this does is put all mail that is recieved into the Archive
subdirectory (the dots are convention of the version of the Maildir
format that Courier-IMAP uses).

5. Next, we want to set up notmuch. You can install it and the python
bindings (needed by afew) with:

~~~~~
aptitude install notmuch python-notmuch
~~~~~

6. Run `notmuch setup` and put in your name, email, and make sure that
the directory to your email archive is "/home/YOURUSER/Maildir". Run
`notmuch new` to have it create the directories and, if you tested the
mail server by sending yourself messages, import those initial
messages.

7. Install afew from
[github.com/teythoon/afew](https://github.com/teythoon/afew). You can
start with the default configuration, and then add filters that will
add the tag 'important', as well as any other automatic tagging you
want to have.  I commented out the ClassifyingFilter because it wasn't
working - and I wasn't convinced I wanted it, so didn't bother to
figure out how te get it to work.

Some simple filters look like:

~~~~~
[Filter.0]
message = messages from someone
query = from:someone.important@email.com
tags = +important
[Filter.1]
message = messages I don't care about
query = subject:Deal
tags = -unread +deals
~~~~~

For the `[MailMover]` section, you want the configuration to look like:

~~~~~
[MailMover]
folders = Archive Important
max_age = 15

# rules
Archive = 'tag:important AND NOT tag:spam':.Important
Important = 'NOT tag:important':.Archive 'tag:spam':.Archive
~~~~~

This says to take anything in Archive with the important tag and put
it in important (but never spam). Note that the folders we are moving
to are prefixed with a dot, but the names of the folders aren't. Now
we need to set everything up to run automatically.

8. We are going to use inotify, and specifically the tool `incron`, to
watch for changes in our .Archive inbox and add files to the database,
tag them, and move those that should be moved to .Important. On Debian,
you can obtain `incron` with:

~~~~~
aptitude install incron
~~~~~

Now edit your incrontab (similar to crontab) with `incrontab -e` and
put an entry like:

~~~~~
/home/MYUSER/Maildir/.Archive/new IN_MOVED_TO,IN_NO_LOOP /home/MYUSER/bin/my-notmuch-new.sh
~~~~~

This says that we want to watch for `IN_MOVED_TO` events, we don't
want to listen while the script is running (if something goes wrong
with your importing script, you could cause an infinite spawning of
processes, which will take down the server). If a message is delivered
while the script is running, it might not get picked up until the next
run, but for me that was fine (you may want to eliminate the
`IN_NO_LOOP` option and see if it actually causes loops. In previous
configurations, I crashed my server twice through process spawning
loops, and didn't want to do it again while debugging). When
`IN_MOVED_TO` occurs, we call a script we've written. You can
obviously put this anywhere, just make it executable:

~~~~~
#!/bin/bash
/usr/local/bin/notmuch new >> /dev/null 2>&1
/usr/local/bin/afew -nt >> /dev/null 2>&1
/usr/local/bin/afew -m >> /dev/null 2>&1
~~~~~

It is intentionally being very quiet because output from cron jobs
will trigger emails...  and thus if there were a mistake, we could be
in infinite loop land again. This means you should make sure the commands
are working (ie, there aren't mistakes in your config files), because you
won't see any debug output from them when they are run through this script.

8. Now let's set up the mobile client. I'm not sure of a good way to
do this on iOS (aside from just manually checking the Important
folder), but perhaps a motivated person could figure it out. Since I
have an Android phone, it wasn't an issue.  On Android, install K9-Mail, and
set up your account with the incoming / outgoing mail server to be
just 'mydomain.com'. Click on the account, and it will show just Inbox
(not helpful). Hit the menu button, then click folders, and check
"display all folders". Now hit the menu again and click folders and
hit "refresh folders".

Provided at least one message has been put into Important and Archive,
those should both show up now. Open the folder 'Important' and use the
settings to enable push for it. Also add it to the Unified
Inbox. Similarly, disable push on the Inbox (this latter doesn't
really matter, because we never deliver messages to the inbox). If you
have trouble finding these settings (which I did for a while), note
that the settings that are available are contingent upon the screen
you are on. The folders settings only exist when you are looking at
the list of folders (not the unified inbox / list of accounts, and not
the contents of a folder).

9. Finally, the desktop client. I'm using the emacs client, because I
spend most of my time inside emacs, but there are several other
clients - one for vim, one called 'bower' that is curses based (that
I've used before, but is less featureful than the emacs one), and a
few others. `alot`, a python client, won't work, because it assumes
that the notmuch database is local (which is a really stupid
assumption). The rest just assume that `notmuch` is in the path. This
means that you can follow the instructions here:
[notmuchmail.org/remoteusage](http://notmuchmail.org/remoteusage/) to
have the desktop use the mail database on the server. To test, run
`notmuch count` on your local machine, and it should return the same
thing (the total number of messages) as it does on the mail server.

Once this is working, install notmuch locally, so that you get the
emacs bindings (or, just download the source and put the contents of
the emacs folder somewhere and include it in your .emacs). You should
now be able to run `M-x notmuch` in emacs and get to your
inbox. Setting up mail sending is a little trickier - most of the
documentation I found didn't work!

The first thing to do, in case your ISP is like mine and blocks port 25, is to
change the default listening port for the server. Open up `/etc/default/exim4`
and set `SMTPLISTENEROPTIONS` equal to `-oX 25:587 -oP /var/run/exim4/exim.pid`.
This will have it listen on both 25 and 587.

Next, set up emacs to use your mail server to send mail, and to load
notmuch. This incantation in your `.emacs` should do the trick:

~~~~~
;; If you opted to just stick the elisp files somewhere, add that path here:
;; (add-to-list 'load-path "~/path/folder/with/emacs-notmuch")
(require 'notmuch)
(setq smtpmail-starttls-credentials '(("mydomain.com" 587 nil nil))
      smtpmail-auth-credentials (expand-file-name "~/.authinfo")
      smtpmail-default-smtp-server "mydomain.com"
      smtpmail-smtp-server "mydomain.com"
      smtpmail-smtp-service 587)
(require 'smtpmail)
(setq message-send-mail-function 'smtpmail-send-it)
(require 'starttls)
~~~~~

Now eval your .emacs (or restart emacs), and you are almost ready to send mail.

You just need to put a line like this into `~/.authinfo`:

~~~~~
machine mydomain.com login MYUSERNAME password MYPASSWORD port 587
~~~~~

With appropriate permissions (`chmod 600 ~/.authinfo`).

Now you can test this by typing `C-x m` or `M-x notmuch` and then from
there, hit the 'm' key - both of these open the composition
window. Type a message and who it is to, and then type `C-c C-c` to send it.
It should take a second and then say it was sent at the bottom of the window.

This should work as-is on Linux. Another machine I sometimes use is a
mac, and things are a little more complicated. The main problem is
that to send mail, we need starttls. You can install `gnutls` through
Homebrew, Fink, or Macports, but the next problem is that if you are
using Emacs installed from emacsformacosx.com (and thus it is a
graphical application), it is not started from a shell, which means it
doesn't have the same path, and thus doesn't know how to find
gnutls. To fix this problem (which is more general), you can install a
tiny Emacs package called `exec-path-from-shell` (this requires Emacs
24, which you should use - then `M-x package-install`) that
interrogates a shell about what the path should be. Then, we just have
to tell it to use `gnutls` and all should work. We can do this all in
a platform specific way (so it won't run on other platforms):

~~~~~
(when (memq window-system '(mac ns))
  (exec-path-from-shell-initialize)
  (setq starttls-use-gnutls t)
  (setq starttls-gnutls-program "gnutls-cli")
  (setq starttls-extra-arguments nil)
  )
~~~~~

10. Address lookup. It's really nice to have an address book based on messages in your mailbox. An easy way to do this is to install addrlookup: get the source from `http://github.com/spaetz/vala-notmuch/raw/static-sources/src/addrlookup.c`, build with

~~~~~
cc -o addrlookup addrlookup.c `pkg-config --cflags --libs gobject-2.0` -lnotmuch
~~~~~

and move the resulting binary into your path (all of this on your server), and then create a similar wrapper as for notmuch:

~~~~~
~/bin/addrlookup:

#!/bin/bash
printf -v ARGS "%q " "$@"
exec ssh your_server addrlookup ${ARGS}
~~~~~

And then add this to your `.emacs`:

~~~~~
(require 'notmuch-address)
(setq notmuch-address-command "/path/to/addrlookup")
(notmuch-address-message-insinuate)
~~~~~

Now if you hit "TAB" after you start typing in an address, it will
prompt you with completions (use up/down arrow to move between, hit
enter to select).

## Conclusion

Congratulations! You now have a mail system that is more
powerful than GMail and completely controlled by you. And there is
a lot more you can do. For example, to enable encryption (to start, just
signing emails), install `gnupg`, create a key and associate
it with your email address, and add the following line to your .emacs and all
messages will be signed by default (it adds a line in the message that
when you send it causes emacs to sign the email. Note that this line
must be the first line, so add your message _below_ it):

~~~~~
(add-hook 'message-setup-hook 'mml-secure-message-sign-pgpmime)
~~~~~

An unfortunate current limitation is that the keys are checked by the
notmuch commandline, so you need to install public keys on the
server. This is fine, except that the emacs client installs them
locally when you click on an unknown key (hit $ when viewing a message to see
the signatures). So, at least for now, you have to manually add keys to
the server with `gpg --recv-key KEYID` before they will show up as verified
on the client (signing/encrypting still works, because that is done locally).
Hopefully this will be fixed soon.


**Added July 9th, 2013:**

## Addendum


Among the large amount of feedback I received on this post, many
people recommended that I use Postfix and Dovecot over Exim and
Courier. Postfix chosen because of security (Exim has a less than
stellar history), and dovecot because it is simpler and faster than
Courier (and more importantly, combined with Postfix
frequently). Security is really important to me (as I want this system
to be easy to mantain), so I decided to switch it. Since I'm not doing
anything particularly complicated with the mail server / IMAP, the
conversion was relatively straightforward. For people reading this,
I'd suggest just doing this from the start (and substitute for the
parts setting up Exim / Courier), but if you've already followed the
instructions (as I have), here is what you should do to change. Note
that I have gotten much of this information from guides at
[syslog.tv](https://syslog.tv/), modified as needed.

1. Install postfix and dovecot with (accept the replacement policy):


~~~
sudo apt-get install dovecot-imapd postfix sasl2-bin libsasl2-2 libsasl2-modules procmail
~~~

2. Add this to end of `/etc/postfix/main.cf`, to tell Postfix to use Maildir, sasl,

~~~
    home_mailbox = Maildir/

    smtpd_sasl_type = dovecot
    smtpd_sasl_path = private/auth
    smtpd_sasl_auth_enable = yes
    smtpd_sasl_security_options = noanonymous
    smtpd_sasl_local_domain = $myhostname
    broken_sasl_auth_clients = yes

    smtpd_sender_restrictions = permit_sasl_authenticated,
    permit_mynetworks,

    smtpd_recipient_restrictions = permit_mynetworks,
    permit_sasl_authenticated,
    reject_unauth_destination,
    reject_unknown_sender_domain,
~~~

Add this to the end of /etc/postfix/master.cf:

~~~~~
spamassassin unix - n n - - pipe
  user=spamd argv=/usr/bin/spamc -f -e
  /usr/sbin/sendmail -oi -f ${sender} ${recipient}
~~~~~

**NOTE**: It's been pointed out to me that you may not have a `spamd`
  user on your system, this won't work. So check that, and add the
  user if it's missing.

And this at the beginning, right after the line `smpt inet n ...`

~~~~~
   -o content_filter=spamassassin
~~~~~

And uncomment the line starting with 'submission' and put the following after it:

~~~~
  -o syslog_name=postfix/submission
  -o smtpd_tls_security_level=encrypt
  -o smtpd_sasl_auth_enable=yes
  -o smtpd_sasl_type=dovecot
  -o smtpd_sasl_path=private/auth
  -o smtpd_client_restrictions=permit_sasl_authenticated,reject
  -o smtpd_recipient_restrictions=reject_non_fqdn_recipient,
     reject_unknown_recipient_domain,permit_sasl_authenticated,reject
~~~~


3.
Change myhostname to be fully qualified if it isn't. Confirm all is well with http://mxtoolbox.com

4. Set up dovecot. Edit /etc/dovecot/conf.d/10-mail.conf and change mail_location to:

~~~
maildir:~/Maildir/
~~~


Edit /etc/dovecot/conf.d/10-master.conf and inside `service auth`
comment the block that is there, and uncomment the one that is:

~~~
  unix_listener /var/spool/postfix/private/auth {
    mode = 0666
  }
~~~

Edit /etc/dovecot/conf.d/10-auth.conf and uncomment the line at the top:

~~~
disable_plaintext_auth = yes
~~~

Now to test that this is working, use `swaks` from a remote host, and run a command like:


~~~
swaks -a -tls -q HELO -s mydomain.com -au myuser -ap "mypassword" -p 587
~~~

And you should get a good response.


5. Filing mail with procmail.

Delete `~/.forward` - we'll be using procmail to put the mail in the
Archive directory.

Put this in /etc/procmailrc

~~~
DROPPRIVS=YES
ORGMAIL=$HOME/Maildir/
MAILDIR=$ORGMAIL
DEFAULT=$ORGMAIL
~~~

Make ~/.procmailrc be:

~~~
:0 c
.Archive/

:0
| /usr/local/bin/my-notmuch-new.sh
~~~

This says to copy the message to the archive and then run
my-notmuch-new.sh (which is a shell script that used to be called by
incron). Technically it pipes the message to the script, but the
script ignores standard in, so it is equivalent to just calling the
script. Now fix the ownership:

~~~
chmod 600 .procmailrc
~~~


Remove incron, which we aren't using anymore.

~~~
sudo aptitude remove incron
~~~

7. Fix up spamassassin.

Get the top of /etc/spamassassin/local.cf to look like:

~~~~
rewrite_header Subject
# just add good headers
add_header spam Flag _YESNOCAPS_
add_header all Status _YESNO_, score=_SCORE_ required=_REQD_ tests=_TESTS_ autolearn=_AUTOLEARN_ version=_VERSION_
~~~~

This adds the proper headers so that `afew` recognizes and tags as spam accordingly. And that should be it!

8. I'm not sure of a way to tell K9Mail that the certificate on the
IMAP server has changed, so I just deleted the account and recreated
it.

Note: if you find any mistakes in this, or parts that needed
additional steps, [let me know](mailto:dbp@dbpmail.net) and I'll
correct/add to this.
