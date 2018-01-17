---
title: How to prove a complier correct
author: Daniel Patterson
---

At POPL'18 (Principles of Programming Languages) last week, I ended up in a
conversation with someone who was talking about a very interesting DSL (domain
specific language) they are working on. In particular, we were talking about
software engineering, and the work that she was doing to test it and increase
her confidence that the implementation was correct! The topic of how exactly one
goes about proving a compiler correct came up, and I realized that I couldn't
think of a high-level (but _concrete_) overview of what that might look like.
Also, like many compilers, hers is implemented in Haskell, so it seemed like a
good opportunity to try out the really cool work presented at the colocated
conference CPP'18 (Certified Programs and Proofs) titled ["Total Haskell is
Reasonable Coq" by Spector-Zabusky, Breitner, Rizkallah, and
Weirich](https://arxiv.org/A's/1711.09286). They have a tool (`hs-to-coq`) that
extracts Coq definitions from (certain) terminating Haskell programs (of which
at least small compilers hopefully qualify).

The intention of this post is twofold:

1. Show how to take a compiler (albeit a tiny one) that was built with no
   intention of verifying it and after the fact prove it correct. Part of the
   ability to do this in such a seamless way is the wonderful `hs-to-coq` tool
   mentioned above, though there is no reason in principle you couldn't carry
   out this translation manually (in practice maintenance becomes an issue,
   hence realistic verified compilers relying on writing their implementations
   within theorem provers like Coq and then _extracting_ executable versions
   automatically).
2. Give a more concrete sense of what exactly you need to show when you are
   proving a compiler correct. By necessity, this is a very simplified scenario
   without a lot of the subtleties that appear in real verification efforts
   (e.g., undefined behavior, multiple compiler passes, linking with code after
   compilation, etc). On the other hand, even this simplified scenario could
   cover many cases of DSLs, and understanding the subtleties that come up
   should be much easier once you understand the basic case!
   
   
> All the code for this post, along with instructions to get it running, is in
> the repository
> [https://github.com/dbp/howtoproveacompiler](https://github.com/dbp/howtoproveacompiler). If
> you have any trouble getting it going, open an issue on that repository.
   
### DSL & Compiler

To make this simple, my source language is arithmetic expressions with adding
and multiplication. I represent this as an explicit data structure in Haskell:

``` haskell
data Arith = Num Int
           | Plus Arith Arith
           | Times Arith Arith
```

And a program is an `Arith`. For example, the source expression "1 + (2 * 4)" is
represented as `Plus 1 (Times 2 4)`. The target of this is a sequence of
instructions for a stack machine. The idea of the stack machine is that there is
a stack of values that can be used by instructions. The target language
expressions are:

``` haskell
data StackOp = SNum Int
             | SPlus
             | STimes
             deriving Show
```

And a program is a `[StackOp]`. For example, the previous example "1 + (2 * 4)"
could be represented as `[SNum 1, SNum 2, SNum 4, STimes, SPlus]`. The idea is
that a number evaluates to pushing it onto the stack and plus/times evaluate by
popping two numbers off the stack and pushing the sum/product respectively back
on. But we can make this concrete by writing an `eval` function that takes an
initial stack (which will probably be empty), a program, and either produces an
integer (the top of the stack after all the instructions run) or an error
(which, for debugging sake, is the state of the stack and rest of the program
when it got stuck).

``` haskell
eval :: [Int] -> [StackOp] -> Either ([Int], [StackOp]) Int
eval (n:_) []               = Right n
eval ns (SNum n:xs)         = eval (n:ns) xs
eval (n2:n1:ns) (SPlus:xs)  = eval (n1+n2:ns) xs
eval (n2:n1:ns) (STimes:xs) = eval (n1*n2:ns) xs
eval vals instrs            = Left (vals, instrs)
```

Now that we have our source and target language, and know how the target works,
we can implement our compiler. Part of why this is a good small example is that
the compiler is very simple! 

``` haskell
compile :: Arith -> [StackOp]
compile (Num n) = SNum n
compile (Plus a1 a2) = compile a1 ++ compile a2 ++ SPlus
compile (Times a1 a2) = compile a1 ++ compile a2 ++ STimes
```

The cases for plus/times are the cases that are slightly non-obvious, because
they can contain further recursive expressions, but if you think about what the
`eval` function is doing, once the stack machine _finishes_ evaluating
everything that `a1` compiled to, the number that the left branch evaluated to
should be on the top of the stack. Then once it finishes evaluating what `a2`
compiles to the number that the right branch evaluated to should be on the top
of the stack. This means that evaluating `SPlus` or `STimes` will put the
sum/product on the top of the stack, as expected. That's a pretty informal
argument about correctness, but we'll have a chance to get a _lot_ more formal
later.

## Formalizing

Now that we have a Haskell compiler, we want to prove it correct! So what do we
do? First, we want to convert this to Coq using the
[hs-to-coq](https://github.com/antalsz/hs-to-coq) tool. If you follow the
instructions there and then run the tool on the file containing the above code
you will get a Coq (.v) source file. You can then evaluate it using a Coq
interactive mode (I use Proof General within Emacs using Company Coq).

Side note: this example is so tiny we haven't run into something that _will_ be
really common: imagine instead of the compile function above, we instead wanted
to take `[Arith]`. This would still work, and would result in the list of
results stored on the stack (so probably you would want to change `eval` to
print everything that was on the stack at the end, not just the top). If you
wrote this `compile`:

``` haskell
compile :: [Arith] -> [StackOp]
compile [] = []
compile (Num n:xs) = SNum n : compile xs
compile (Plus a1 a2:xs) = compile [a1] ++ compile [a2] ++ SPlus : compile xs
compile (Times a1 a2:xs) = compile [a1] ++ compile [a2] ++ STimes : compile xs
```

You would get an error! It says that the compile function is not terminating!

This is good introduction into a (major) difference between Haskell and Coq:
in Haskell, any term can run forever. For a programming language, this is an
inconvenience, as you can end up with code that is perhaps difficult to debug if
you didn't want it to (it's also useful if you happen to be writing a server
that is supposed to run forever!). For a language intended to be used to _prove_
things, this feature would be a non-starter, as it would make the logic unsound.
The issue is that in Coq, (at a high level), a type is a theorem and the term
that inhabits the type is a proof of that theorem. But in Haskell, you can write:

``` haskell
anything :: a
anything = anything
```

i.e., for any type, you can provide a term with that type --- that is, the term
that simply never returns. If that were possible in Coq, you could prove any
theorem, and the entire logic would be useless (or unsound, which technically
means you can prove logical falsehood, but since falsehood allows you to prove
anything, it's the same thing).

Returning to this (only slightly contrived) program, it isn't actually that our
program runs forever (and if you do want to prove things about programs that do,
you'll need to do much more work!), just that Coq can't tell that it doesn't. In
general, it's not possible to tell this for sufficiently powerful languages
(this is what the Halting problem says for Turing machines, and thus holds for
anything with similar expressivity). What Coq relies on is that some argument is
inductively defined (which we have: both lists and `Arith` expressions) and that
all recursive calls are to _structurally smaller_ parts of the arguments. If
that holds, we are guaranteed to terminate, as inductive types cannot be
infinite (note: unlike Haskell, Coq is _not_ lazy, which is another difference,
but we'll ignore that). If we look at our recursive call, we called `compile`
with `[a1]`. While `a1` is structurally smaller, we put that inside a list and
used that instead, which thus violates what Coq was expecting.

There are various ways around this (like adding another argument whose purpose
is to track termination, or adding more sophisticated measurements), but there
is another option: adding a helper function `compile'` that does what our
original `compile` did: compiles a single `Arith`. The intuition that leads
to trying this is that in this new `compile` we are decreasing on both the length of
the list and the structure of the `Arith`, but we are trying to do both at the
same time. By separating things out, we eliminate the issue:

``` haskell
compile :: [Arith] -> [StackOp]
compile []     = []
compile (x:xs) = compile' x ++ compile xs

compile' :: Arith -> [StackOp]
compile' (Num n)       = [SNum n]
compile' (Plus a1 a2)  = compile' a1 ++ compile' a2 ++ [SPlus]
compile' (Times a1 a2) = compile' a1 ++ compile' a2 ++ [STimes]
```

## Proving things

We now have a Coq version of our compiler, complete with our evaluation
function. So we should be able to write down a theorem that we would like to
prove. What should the theorem say? Well, there are various things you could
prove, but the most basic theorem in compiler correctness says essentially that
running the source program and the target program "do the same thing". This is
often stated as "semantics preservation" and is often formally proven by way of
a backwards simulation: whatever the target program does, the source program
also should do. In languages with ambiguity (nondeterminism, undefined
behavior), this becomes much more complicated, but in our setting, we would
state it as:

```
Theorem. For all source arith expressions A, if eval [] (compile A) produces
  integer N then evaluating A should produce the same number N.
```

The issue that's immediately apparent is that we don't actually have a way of
directly evaluating the source expression. So we should add this function to our
Haskell source. In a non-trivial DSL, this may be a significant part of the
formalization process, but it is also incredibly important, because this is the
part where you are actually specifying exactly what the source DSL means
(otherwise, the only "meaning" it has is whatever the compiler happens to do).
We can write this function as:

``` haskell
eval' :: Arith -> Int
eval' (Num n) = n
eval' (Plus a1 a2) = (eval' a1) + (eval' a2)
eval' (Times a1 a2) = (eval' a1) * (eval' a2)
```

And we can re-run `hs-to-coq` to get it added to our Coq development. We can now
formally state the theorem we want to prove as:

```
Theorem compiler_correctness : forall a : Arith, 
  eval nil (compile a) = Data.Either.Right (eval' a).
```

I'm going to sketch out how this proof went. Proving stuff is complex, but this
maybe gives a sense of some of the thinking that goes into it. To go further,
you probably want to take a course if you can find one, or follow a book like:

- Bertot and Casteron's [Coq'Art: Interactive Theorem Proving and Program Development](https://archive.org/details/springer_10.1007-978-3-662-07964-5).
- Adam Chlipala's [Certified Programming with Dependent Types](http://adam.chlipala.net/cpdt/)
- Pierce et. al.'s [Software Foundations](https://softwarefoundations.cis.upenn.edu/)

If you were to prove this on paper, you would proceed by induction on the
structure of the arithmetic expression, so let's start that way. The base case
goes away trivially and we can expand the case for plus using:

```
induction a; iauto; simpl.
```

We see:
```
IHa1 : eval nil (compile a1) = Either.Right (eval' a1)
IHa2 : eval nil (compile a2) = Either.Right (eval' a2)
============================
eval nil (compile a1 ++ compile a2 ++ SPlus :: nil) = Either.Right (eval' a1 + eval' a2)%Z
```

Which, if we look at it for a little while, we realize two things:

1. Our induction hypotheses really aren't going to work, intuitively because of
   the `Either` --- our program won't produce `Right` results for the subtrees,
   so there probably won't be a way to rely on that fact.
2. On the other hand, what does look like a Lemma we should be able to prove has
   to do with evaluating a partial program. Rather than trying to induct on the
   entire statement, we instead try to prove that `eval`ing a `compile`d term
   will result in the `eval'`d term on the top of the stack. This is an instance
   of a more general pattern -- that often the toplevel statement that you want
   has too much specificity, and you need to instead prove something that is
   more general and then use it for the specific case. So here's the Lemma we
   want to prove:
   
```
Lemma eval_step : forall a : Arith, forall xs : list StackOp, 
        eval nil (compile a ++ xs) = eval (eval' a :: nil) xs.
```

This is more general, and again we start by inducting on `a`, expanding and
eliminating the base case:

```
induction a; intros; simpl; iauto.
```

We now end up with _better_ inductive hypotheses:

```
  IHa1 : forall xs : list StackOp, eval nil (compile a1 ++ xs) = eval (eval' a1 :: nil) xs
  IHa2 : forall xs : list StackOp, eval nil (compile a2 ++ xs) = eval (eval' a2 :: nil) xs
  ============================
  eval nil ((compile a1 ++ compile a2 ++ SPlus :: nil) ++ xs) =
  eval ((eval' a1 + eval' a2)%Z :: nil) xs
```

We need to reshuffle the list associativity and then we can rewrite using the first hypotheses:

```
rewrite List.app_assoc_reverse. rewrite IHa1.
```

But now there is a problem (this is _common_, hence going over it!). We want to
use our second hypothesis. Once we do that, we can reduce based on the
definition of `eval` and we'll be done (with this case, but multiplication is
the same). The issue is that `IHa2` needs the stack to be empty, and the stack
we have is `eval' a1 :: nil`, so it can't be used:

```
  IHa1 : forall xs : list StackOp, eval nil (compile a1 ++ xs) = eval (eval' a1 :: nil) xs
  IHa2 : forall xs : list StackOp, eval nil (compile a2 ++ xs) = eval (eval' a2 :: nil) xs
  ============================
  eval (eval' a1 :: nil) ((compile a2 ++ SPlus :: nil) ++ xs) =
  eval ((eval' a1 + eval' a2)%Z :: nil) xs
```

The solution is to go back to what our Lemma statement said and generalize it to
arbitrary stacks, so that the inductive hypotheses are correspondingly stronger:

```
Lemma eval_step : forall a : Arith, forall s : list Num.Int, forall xs : list StackOp, 
        eval s (compile a ++ xs) = eval (eval' a :: s) xs.
```

Now if we start the proof in the same way:

```
induction a; intros; simpl; iauto.
```

We run into an odd problem. We have a silly obligation:

```
  match s with
  | nil => eval (i :: s) xs
  | (_ :: nil)%list => eval (i :: s) xs
  | (_ :: _ :: _)%list => eval (i :: s) xs
  end = eval (i :: s) xs
```

Which will go away once we break apart the list `s` and simplify (if you look
carefully, it has the same thing in all three branches of the match). There are
a couple approaches to this:

1. We could just do it manually: `destruct s; simpl; eauto; destruct s; simpl;
   eauto`. But it shows up multiple times in the proof, and that's a mess and
   someone reading the proof script may be confused what is going on.
2. We could write a tactic for the same thing:
    
    ```
    try match goal with
         |[l : list _ |- _ ] => solve [destruct l; simpl; eauto; destruct l; simpl; eauto]
        end.
    ```
    
    This has the advantage that it doesn't depend on the name, you can call it
    whenever (it won't do anything if it isn't able to discharge the goal), but
    where to call it is still somewhat messy (as it'll be in the middle of the
    proofs). We could hint using this tactic (using `Hint Extern`) to have it
    handled automatically, but I generally dislike adding global hints for
    tactics (unless there is a very good reason!), as it can slow things down
    and make understanding why proofs worked more difficult.
3. We can also write lemmas for these. There are actually two that come up, and
   both are solved trivially:
   
    ```
    Lemma list_pointless_split : forall A B:Type, forall l : list A, forall x : B,
        match l with | nil => x | (_ :: _)%list => x end = x.
    Proof.
      destruct l; eauto.
    Qed.
    Lemma list_pointless_split' : forall A B:Type, forall l : list A, forall x : B,
            match l with | nil => x | (_ :: nil)%list => x | (_ :: _ :: _)%list => x end = x.
    Proof.
      destruct l; intros; eauto. destruct l; eauto.
    Qed.
    ```
    In this style, we can then hint using these lemmas locally to where they are needed.

Now we know the proof should follow from list associativity, this pointless list
splitting, and the inductive hypotheses. We can write this down formally (this
relies on the `literatecoq` library, which is just a few tactics at this point) as:

```
Lemma eval_step : forall a : Arith, forall s : list Num.Int, forall xs : list StackOp, 
        eval s (compile a ++ xs) = eval (eval' a :: s) xs.
Proof.
  hint_rewrite List.app_assoc_reverse.
  hint_rewrite list_pointless_split, list_pointless_split'.
  
  induction a; intros; simpl; iauto;
    hint_rewrite IHa1, IHa2; iauto'.
Qed.
```

Which says that we know that we will need the associativity lemma and these list
splitting lemmas somewhere. Then we proceed by induction, handle the base case,
and then use the inductive hypotheses to handle the rest. 

We can then go back to our main theorem, and proceed in a similar style. We
prove by induction, relying on the `eval_step` lemma, and in various places
needing to simplify (for the observant reader, `iauto` and `iauto'` only differ
in that `iauto'` does a deeper proof search).

```
Theorem compiler_correctness : forall a : Arith, 
  eval nil (compile a) = Data.Either.Right (eval' a).
Proof.
  hint_rewrite eval_step.
  hint_simpl.
  induction a; iauto'.
Qed.
```

We now have a proof that the compiler that we wrote in Haskell is correct,
insofar as it preserves the meaning expressed in the source-level `eval'`
function to the meaning in the `eval` function in the target. This isn't, of
course, the only theorem you could prove. Another one that would be interesting
would be that no compiled program ever got stuck (i.e., never produces a `Left`
error).


> As stated at the top of the post, all the code in this post is available at
> [https://github.com/dbp/howtoproveacompiler](https://github.com/dbp/howtoproveacompiler).
> Hopefully next time someone talks about proving a compiler correct, you have a
> better sense of what that means, at least at a high level!
