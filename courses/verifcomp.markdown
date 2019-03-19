---
title: "Verified Compilers and Multi-Language Software"
---

-------       -------
Course        CS4910: Verified Compilers and Multi-Language Software
Semester      Fall 2019
Meeting       Monday/Thursday, 11:45AM-1:25PM
Instructor    Daniel Patterson ([dbp@dbpmail.net](mailto:dbp@dbpmail.net))
Office        WVH 308
--------      ------


### **Why this course?**

> “Can you trust your compiler?” 
> &mdash; Xavier Leroy, developer of the Compcert verified C compiler

Software is written in a variety of languages, and in most cases, in order to
run, it must be compiled down to some lower-level target. **But what if that
compiler isn't correct?** Compilers are some of the trickier pieces of software
we build, and yet bugs in them are insidious: bugs in compilers mean that
one of our most basic debugging strategies, reading source code, may not be
helpful, since what actually ran may not correspond to that source code! 

In the presence of buggy compilers, we must worry about what the compiler does
and how our programs are translated to the target, possibly looking at compiler
output or stepping through with low-level debuggers. Much better if the compiler
were, essentially, invisible --- if the source code fully specified what would
happen. But a compiler can only truly be invisible if we can be sure that it
does exactly what we expect: if we are sure it is correct. To be absolutely
sure, we must prove it correct, and build a _verified_ compiler.

Colliding with the field of verified compilation is the question of how
different languages should be able to interact --- that interaction, or linking,
happens after compilation, in the lower-level target of compilation. But when
using a variety of source languages, we would like to think in terms of those
source languages, not worry about how our various languages compile, and so
again, we are left wanting an invisible compiler, a verified compiler.

These are our high-level motivations. The course itself, while motivated with
these problems, will be very grounded: we will be **building languages,
compilers, and proving them correct**.

### **How will the course be structured?**
**Part 1.** We want to produce verified compilers, and so we will use one of the
state-of-the-art tools used for this: the [Coq proof
assistant](https://coq.inria.fr/). This is a general purpose tool --- in
addition to being used to build a verified [optimizing C
compiler](http://compcert.inria.fr/), in work towards [certified operating
systems](http://flint.cs.yale.edu/certikos/) and
[other](http://verasco.imag.fr/wiki/Main_Page)
[verification](https://github.com/mit-plv/fiat-crypto)
[projects](https://jasmin-lang.github.io/). It has also been used in various
efforts of formalizing mathematics (most notably, the [Four Color
Theorem](https://en.wikipedia.org/wiki/Four_color_theorem)). For our purpose, Coq allows
us to write functional programs, like compilers, and prove properties about
them, like correctness. The
first section of the course will be dedicated to learning Coq and becoming
familiar with the process of _mechanized_ proof --- that is, proofs that are
checked by a machine. The text that we will use for this section of the course
is ["Programs and Proofs" by Ilya Sergey](https://ilyasergey.net/pnp).

**Part 2.** This part of the course will involve designing, in groups, different
source languages. These will all likely be simple functional languages, and we
will do plenty of design review to ensure that the language design you pick will
not cause too much difficulty later on.

**Part 3.** The final part will involve building and proving correct compilers
from your language to a common low-level target language. All the languages will
compile to the same target language, and so once we have compilers, and
concurrent with the verification effort, you will experiment building small
programs that use a mixture of different student languages.

### **A note on collaboration**
This will be a highly collaborative course. For the first section, the actual
assignments will be done individually, to ensure that you get sufficient
practice with theorem-proving, as proof assistants like Coq are not something
you can learn without using. Even so, we _expect and encourage people to work
together throughout the course_, beyond just the teams you are working in,
provided that the actual work you submit is your own. Once the first section
ends, the course will truly become a large collaboration. All of our compilers
will be in a shared repository, and while you will be assessed on and
be responsible for understanding your own compiler, all are welcome to help any
of your classmates. We will also do group reviews or reviews of one group by
another.


### **Requirements**
Intended for advanced undergraduates, you would be well prepared by either
having taken CS4400 (programming languages) or CS4410 (compilers). However, if
you are interested and haven't taken either, **please reach out to the
instructor**. I'm happy to talk to you about your background, or give you
additional information about the type of stuff we'll be working on to see if it
seems like it'll be a good fit. While not required, familiarity with typed
functional languages (e.g., Scala, Haskell, or OCaml) would be helpful, since
Coq is related to that class of language, as would any exposure to formal proof
in mathematics.


### **Other question? Something not clear?**

Please reach out to the instructor: Daniel Patterson ([dbp@dbpmail.net](mailto:dbp@dbpmail.net))

<br/><br/>
