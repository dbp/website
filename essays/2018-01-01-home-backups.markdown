---
title: (Cheap) home backups
author: Daniel Patterson
date: January 1st, 2017
---

Backing things up is important. Some stuff, like code that lives in
repositories, naturally ends up many places, so it perhaps is less important to
explicitly back up. Other things, like photos, or personal documents, generally
don't have a natural redundant home, so they need some backup story, and relying
on various online services is both risky (what if they go out of business,
"pivot", etc), potentially time-consuming to keep track of (services for photos
may not allow videos, or at least not full resolution ones, etc), limited in
various ways (max file sizes, storage allotments, etc), not to mention concerns
about privacy. The requirements for the system I outline here are:

1. (Home) scalable -- i.e., any reasonable amount of data that I could generate
   personally I should be able to dump in one place, and be confident that it
   won't go away. What makes up the bulk is photos, some music, and some audio
   and video files. For me, this is currently about 1TB (+-0.5TB).
2. Cheap. I'm willing to pay about $100-200/year total (including hardware). 
3. Simple. There has to be _one_ place where I can dump files, it has to be
   simple enough to recover from complete failure of a piece of hardware even if
   I haven't touched it in a long time (because if it is working, I won't have
   had to tweak it in months / years). Ideally it should be usable without
   commandline familiarity, so it can serve my whole home.
4. Safe. Anything that's not in my physical control should be encrypted.
5. Reasonably reliable. Redundancy across hardware, geographic locations, etc.
   This is obviously balanced with other concerns (in particular, 2 and 3)!

I've tried various solutions, but what I've ended up with seems to be working
pretty well. It's a combination of some cheap hardware, inexpensive cloud
storage, and decent backup software.

### Why not an off-the-shelf NAS?

In the past, I tried one (it was a Buffalo model). I wasn't impressed by the
software (which was hard to upgrade, install other stuff on it, maintain, etc),
the power consumption (this was several years ago, but _idle_ the two-drive
system used over 30watts idle, which is the same power that my similarly aged
quad core workstation uses idle!). Also, a critical element of this system for
me is that there is an off-site component, so getting that software on it is
critical, and I'd rather have a well-supported linux computer to deal with.
Obviously this depends in the particular NAS you get, but the system below is
perfect _for me_. Experimenting with the below was much cheaper than dropping
hundreds more dollars on a new NAS that may not have worked any better than the
old one.

### Hardware

- $70 - [Raspberry Pi 3](http://amzn.to/2zggJoh) (That's an affiliate link. Here's [one that isn't](https://www.amazon.com/CanaKit-Raspberry-Complete-Starter-Kit/dp/B0778CZ97B/)). This consumes very little power (a little over 1W without the disks,
  probably around 10W with them spinning, more like 3W when they are idling),
  takes up very little space, but seems plenty fast enough to act as a file
  server. That price includes a case, heat-sink, SD card, power adaptor, etc. If
  you had any of these things, you can probably get a cheaper kit (the single
  board itself is around $35). Note that you _really_ want a heat-sink on the
  processor. I ran without it for a while (forgot to install it) and it would
  overheat and hard lock. It's a tradeoff that they put a much faster processor
  in these than in prior generations -- I think it's worth it (it's an amazingly
  capably computer for the size/price).

- $75 - Three [external USB SATA hard drive enclosures](http://amzn.to/2BluHHj).
  You might be able to find these cheaper -- the ones I got were metal, which
  seemed good in terms of heat dissipation, and have been running for a little
  over a year straight without a problem (note: this is actually one more than
  I'm using at any given time, to make it easier to rotate in new drives; BTRFS,
  which I'm using, allows you to just remove a drive and add a new one, but the
  preferred method is to have both attached, and issue a `replace` command).

- $170 - Two [2TB WD Red SATA drives](http://amzn.to/2C1lc3E). These are
  actually recent upgrades -- the server was been running on older 1TB Green
  drives (four and five years old respectively), but one of them started
  reporting failures (I would speculate the older of the two, but I didn't
  check) so I replaced both. The cheaper blue drives probably would have been
  fine (the Greens that the Blues have replaced certainly have lasted well
  enough, running nearly 24/7 for years), but the "intended to run 24/7" Red
  ones were still not that expensive so I thought I might as well spring for them.
  
### Cloud

- [Backblaze B2](https://www.backblaze.com/b2/cloud-storage.html). This seems to
  be the cheapest storage that scales down to storing nothing. At my usage
  (0.5-2TB) it costs about $3-10/month, which is a good amount, and given that
  it is one of three copies (the other two being on the two hard drives I have
  attached to the Pi) I'm not worried about the missing reliability vs for
  example Amazon S3 (B2 gives 8 9s of durability vs S3 at 11 9s, but to get that
  S3 charges you 3-4x as much).
  
### Software

- The Raspberry Pi is running Raspbian (Debian distributed for the Raspberry
  Pi). The external hard drives are a RAID1 with BTRFS. If I were doing it from
  scratch, I would investigate ZFS, but I've been migrating this same data over
  different drives and home servers since ZFS was essentially totally
  experimental on linux, and on linux, for RAID1, BTRFS seems totally stable.
  The point is, you should use an advanced file system in RAID1 (on ZFS you
  could go higher, but I prefer simplicity and the power consumption of having
  just two drives) that can detect&correct errors, lets you swap in new drives
  and migrate out old ones, migrate to larger drives, etc. This is essentially
  the feature-set that both ZFS and BTRFS have, but the former is considered to
  be more stable and the latter has been in linux for longer.
- For backups, I'm using [Duplicacy](https://github.com/gilbertchen/duplicacy),
  which is annoyingly similarly named
  to a much older backup tool called [Duplicity](http://duplicity.nongnu.org/). It's also
  annoyingly _not_ free software, but for personal use, the command-line version
  (which is the only version that I would be using) _is_ free. I actually settled
  on this after trying and failing to use (actually open-source) competitors: 

    First, I tried the aforementioned [Duplicity](http://duplicity.nongnu.org/)
    (using its friendly frontend [duply](http://duply.net/)). I actually was
    able to make some full backups (the full size of the archive was around
    600GB), but then it started erroring out because it would out-of-memory when
    trying to unpack the file lists. The backup format of Duplicity is not super
    efficient, but it is very simple (which was appealing -- just tar files and
    various indexes with lists of files). Unfortunately, some operations need
    memory that seems to scale with the size of the currently backed up archive,
    which is a non-starter for my little server with 1GB of ram (and in general
    _shouldn't_ be acceptable for backup software, but...)
  
    I next tried a newer option, [restic](https://github.com/restic/restic).
    This also had the same problem, though it wasn't even able to make a backup
    (though that was probably a good thing, as I wasted less time!). They are
    aware of it (see,
    e.g., [this issue](https://github.com/restic/restic/issues/450), so maybe at
    some point it'll be an option, but that issue is almost two years old so ho
    hum...).
  
    So finally I went with the bizarrely sort-of-but-not-really open-source
    option, Duplicacy. I found other people talking about running it on a
    Raspberry Pi, and it seemed like the primary place where memory consumption
    could become a problem was the number of threads used to upload, which
    thankfully is an argument. I settled on 16 and it seems to work fine (i.e.,
    `duplicacy backup -stats -threads 16`) -- the memory consumption seems to
    hover below 60%, which leaves a very healthy buffer for anything else that's
    going on (or periodic little jumps), and regardless, more threads don't seem
    to get it to work faster. The documentation on how to use the command-line
    version is a little sparse, but once I figured out that to configure it to
    connect automatically to my B2 account I needed a file
    `.duplicacy/preferences` that looked like (see `keys` section; the rest will
    probably be written out for you if you run `duplicacy` first; alternatively,
    just put this file in place and everything will be set up):
  
    ```
    [
      {
          "name": "default",
          "id": "SOME-ID",
          "storage": "b2://BUCKET_NAME",
          "encrypted": true,
          "no_backup": false,
          "no_restore": false,
          "no_save_password": false,
          "keys": {
              "b2_id": "ACCOUNT_ID",
              "b2_key": "ACCOUNT_KEY",
              "password": "ENCRYPTION_PASSWORD"
          }
      }
    ]
    ```

    Everything else was pretty much smooth sailing (though, as per usual, the
    initial backup is quite slow. The Raspberry Pi 3 processor is certainly much
    faster than previous Raspberry Pis, and fast enough for this purpose, but it
    definitely still has to work hard! And my residential cable upstream is not
    all that impressive. A couple days later though, the initial backup completed).
    
    Periodic backups run with the same command, and intermediate ones can be
    pruned away as well (I use `duplicacy prune -keep 30:180 -keep 7:30 -keep
    1:1`, run after my daily backup, to keep monthly backups beyond 6 months,
    weekly beyond 1 month, and daily below that. I have a cron job that runs the
    backup daily, so the last is not strictly necessary, but if I do manual
    backups it'll clean them up over time). To restore from total loss of the
    Pi, you just need to put the config file above into `.duplicacy/preferences`
    relative to the current directory and you can run `duplicacy restore`. You
    can also grab individual files (which I tested on a different machine; I
    haven't tested restoring a full backup) by creating the above mentioned file
    and then running `duplicacy list -files -r N` (where N is the snapshot you
    want to get the file from; run `duplicacy list` to find which one you want)
    and then to get a file `duplicacy cat -r N path/to/file > where/to/put/it`.

- I'm still working out how to detect errors in the hard drives. I can see them
  manually by running `sudo btrfs device stats /mntpoint`. When this shows that
  a drive is failing (i.e., lots of errors), add a new drive to the spare
  enclosure, format it, and then run `sudo btrfs replace start -f N /dev/sdX
  /mntpoint` where N is the number of the device that is failing (when you run
  `sudo btrfs fi show /mntpoint`) and `/dev/sdX` is the new drive. To check for
  and correct errors in the file system (not the underlying drive), run `sudo
  btrfs scrub start /mntpoint`. This will run in the background; if you care you
  can check the status with `sudo btrfs scrub status /mntpoint`.

- If you want to expand the capacity of the disks, replace the drives as if they
  failed (see previous bullet) and then run `sudo btrfs fi resize N:max
  /mntpoint` for each `N` (run `sudo btrfs fi show` to see what your dev ids
  are). When you replace them, they stay at the same capacity -- this resize
  expands the filesystem to the full device.
- For tech people (i.e., who are comfortable with `scp`), this is enough -- just
  get files onto the server, into the right directory, and it'll be all set. For
  less tech-savvy people, you can install samba on the raspberry pi and then set
  up a share like the following (put this at the bottom of
  `/etc/samba/smb.conf`):
  
    ```
    [sharename]
    comment = Descriptive name
    path = /mntpoint
    browseable = yes
    writeable = yes
    read only = no
    only guest = no
    create mask = 0777
    directory mask = 0777
    public = yes
    guest ok = no
    ```
    
    Then set `pi`s password with `sudo smbpasswd -i pi`. Now restart the service
    with `sudo /etc/init.d/sambda restart` and then from a mac (and probably
    windows) you can connect to the pi with the "Connect to Server"
    interface, connect as the `pi` user with the password you set, and see the
    share. Note that to be able to make changes, the `/mntpoint` (and what's in
    it) needs to be writeable by the `pi` user. You can also use a different
    user, set up samba differently, etc.

### Summary

The system described above runs 24/7 in my home. It cost $325 in hardware
(which, if you want to skip the extra USB enclosure to start and use WD Blue
drives rather than Red ones you can cut $65 -- i.e., $260 total), $1/month in
electricity (I haven't measured this carefully, but that's what 10W costs where I
live) and currently costs about $3/month in cloud storage, though that will go
up over time, so to be more fair let's say $5/month. Assuming no hard drive
replacements for three years (which is the warrantee on the hard drives I have,
so a decent estimate), the total cost over that time is $325 + $54 + $170 =
$549, or around $180 per year, which is squarely in the range that I wanted.


