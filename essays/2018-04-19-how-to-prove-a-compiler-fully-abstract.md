---
title: How to prove a compiler fully abstract
author: Daniel Patterson
---

A compiler that preserves and reflects equivalences is called a **fully
abstract** compiler. This is a powerful property for a compiler that is
different (but complimentary) to the more common notion of [compiler
correctness](/essays/2018-01-16-how-to-prove-a-compiler-correct.html). So what
does it mean, and how do we prove it?

> All the code for this post, along with instructions to get it running, is in
> the repository
> [https://github.com/dbp/howtoprovefullabstraction](https://github.com/dbp/howtoprovefullabstraction).
> If you have any trouble getting it going, please open an issue on that
> repository and I'll help figure it out with you.


Both **equivalence preservation** and **equivalence reflection** (what make a
compiler fully abstract) relate to how the compiler treats program equivalences,
which in this case I'm considering observational equivalence. Two programs `p1`
and `p2` are **observationally equivalent** if you cannot tell any difference
between the result of running them, including any side effects.

For example, if the only observable behavior about programs in your language
that you can make is see what output they print, this means that the two
programs print the same output, even if they are implemented in completely
different ways. Observational equivalence is extremely useful, especially for
compilers, which when optimizing may change how a particular program is
implemented but should not change the observable behavior. But it is also useful
for programmers, who commonly refactor code, which means they change how the
code is implemented (to make it easier to maintain, or extend, or better support
some future addition), without changing any functionality. _Refactoring is an
equivalence-preserving transformation._ We write observational equivalence on
programs formally as:

```
p1 ≈ p1
```

### Contextual equivalence

But we often also want to compile not just whole programs, but particular
modules, expressions, or in the general sense, **components**, and in that case,
we want an analogous notion of equivalence. Two components are **contextually
equivalent** if in all program contexts they produce the same observable
behavior. In other words, if you have two modules, but any way you combine those
modules with the rest of a program (so the rest is syntactically identical, but
the modules differ), the results are observationally equivalent, then those two modules are
contextually equivalent. We will write this, overloading the `≈` for both
observational and contextual equivalence, as:

```
e1 ≈ e1
```

As an example, if we consider a simple functional language and consider our
components to be individual expressions, it should be clear that these two
expressions are contextually equivalent:

```
λx. x * 2 ≈ λx. x + x
```

While they are implemented differently, no matter how they are used, the result
will always be the same (as the only thing we can do with these functions is
call them on an argument, and when we do, each will double its argument, even
though in a different way). It's important to note that contextual equivalence
always depends on what is observable within the language. For example, in
Javascript, you can reflect over the syntax of functions, and so the above two
functions, written as:

```javascript
function(x){ return x * 2; } ≈ function(x){ return x + x; }
```

Would not be contextually equivalent, because there exists a program context
that can distinguish them. What is that context? Well, if we imagine plugging in
the functions above into the "hole" written as `[·]` below, the result will be
different for the two functions! This is because the `toString()` method on
functions in Javascript returns the source code of the function. 

```javascript
([·]).toString()
```

From the perspective of optimizations, this is troublesome, as you can't be sure
that a transformation between the above programs was safe (assuming one was much
faster than the other), as there could be code that relied upon the particular
way that the source code had been written. There are more complicated things you
can do (like optimizing speculatively and falling back to unoptimized versions
when reflection was needed). In general though, languages with that kind of
reflection are both harder to write fast compilers for and harder to write
secure compilers for, and while it's not the topic of this post, it's always
important to know what you mean by contextual equivalence, which usually means:
_what can program contexts determine about components_.

### Part 1. Equivalence reflection

With that in mind, what does **equivalence reflection** and **equivalence
preservation** for a compiler mean? Let's start with **equivalence reflection**,
as that's the property that all your correct compilers already have. Equivalence
reflection means that if two components, when compiled, are equivalent, then the
source components must have been equivalent. We can write this more formally as
(where we write `s ↠ t` to mean a component `s` is compiled to `t`):


```
∀ s1 s2 t1 t2. s1 ↠ t1 ∧ s2 ↠ t2 ∧ t1 ≈ t2 ⇒ s1 ≈ s2
```

What are the consequences of this definition? And why do correct compilers have
this property? Well, the contrapositive is actually easier to understand: it
says that if the source components weren't equivalent then the target components
would have to be different, or more formally:

```
∀ s1 s2 t1 t2. s1 ↠ t1 ∧ s2 ↠ t2 ∧ s1 ≉ s2 ⇒ t1 ≉ t2
```


If this didn't hold, then the compiler could take different source components
and compile them to the same target component! Which means you could have
different source programs you wrote, which have observationally different
behavior, and your compiler would produce the same target program! Any correct
compiler has to preserve observational behavior, and it couldn't do that in this
case, as the target program only has one behavior, so it can't have both the
behavior of `s1` and `s2` (for pedants, not considering non-deterministic
targets). 

So equivalence reflection should be thought of as related to compiler
correctness. Note, however, that equivalence reflection is _not_ the same as
compiler correctness: as long as your compiler produced different target
programs for different source programs, all would be fine -- your compiler could
hash the source program and produce target programs that just printed the hash
to the screen, and it would be equivalence reflecting, since it would produce
different target programs not only for source programs that were observationally
different, but even syntactically different! That would be a pretty bad
compiler, and certainly not correct, but it would be equivalence reflecting.

### Part 2. Equivalence preservation

Equivalence preservation, on the other hand, is the hallmark of fully abstract
compilers, and it is a property that even most correct compilers do not have,
though it would certainly be great if they did. It says that if two source
components are equivalent, then the compiled versions must still be equivalent.
Or, more formally:


```
∀ s1 s2 t1 t2. s1 ↠ t1 ∧ s2 ↠ t2 ∧ s1 ≈ s2 ⇒ t1 ≈ t2
```

(See, I just reversed the implication. Neat trick! But now it means something
totally different). One place where this has been studied extensively is by
security researchers, because what it tells you is that observers in the target
can't make observations that aren't possible to distinguish in the source. Let's
make that a lot more concrete, where we will also see why it's not frequently
true, even of proven correct compilers. 

Say your language has some information hiding feature, like a private field, and
you have two source components that are identical except they have different
values stored in the private field. If the compiler does not preserve the fact
that it is private (because, for example, it translates the higher level object
structure into a C struct or just a pile of memory accessed by assembly), then other
target code could read the private values, and these two components will no
longer be equivalent. 

This also has implications for programmer refactoring and compiler
optimizations: I (or my compiler) might think that it is safe to replace one
version of the program with another, because I know that in my language these
are equivalent, but what I don't know is that the compiler reveals
distinguishing characteristics, and perhaps some target-level library that I'm
linking with relies upon details (that were supposed to be hidden) of how the
old code worked. If that's the case, I can have a working program, and make a
change that does not change the meaning of the component _in my language_, but
the whole program can no longer work.

Proving a compiler fully abstract, therefore, is all about proving equivalence
preservation. So how do we do it?

### How to prove equivalence preservation

Looking at what we have to prove, we see that given contextually equivalent
source components `s1` and `s2`, we need to show that `t1` and `t2` are
contextually equivalent. We can expand this to explicitly quantify over the
contexts that combine with the components to make whole programs:

```
∀ s1 s2 t1 t2. s1 ↠ t1 ∧ s2 ↠ t2 ∧ (∀Cs. Cs[s1] ≈ Cs[s2]) ⇒ (∀Ct. Ct[t1] ≈ Ct[t2])
```

Noting that as mentioned above, I am overloading `≈` to now mean whole-program
observational equivalence (so, running the program produces the same
observations).

First I'll outline how the proof will go in general, and then we'll consider an
actual example compiler and do the proof for the concrete example. 

We can see that in order to prove this, we need to consider an arbitrary target
context `Ct` and show that `Ct[t1]` and `Ct[t2]` are observationally equivalent.
We do this by showing that `Ct[t1]` is observationally equivalent to `Cs'[s1]`
-- that is, we produce a source context `Cs'` that we claim is equivalent to
`Ct`. We do this by way of a "back-translation", which will be a sort of
compiler in reverse. Assuming that we can produce such a `Cs'` and that
`Cs'[s1]` and `Ct[t1]` (and correspondingly `Cs'[s2]` and `Ct[t2]`) are indeed
observationally equivalent (noting that this relies upon a cross-language notion
of observations), we can prove that `Ct[t1]` and `Ct[t2]` are observationally
equivalent by instantiating our hypothesis `∀Cs. Cs[s1] ≈ Cs[s2]` with `Cs'`.
This tells us that `Cs'[s1] ≈ Cs'[s2]`, and by transitivity, `Ct[t1] ≈ Ct[t2]`.

It can be helpful to see it is a diagram, where the top line is given by the
hypothesis (once instantiated with the source context we come up with by way of
backtranslation) and coming up with the back-translation and showing that `Ct`
and `Cs'` are equivalent is the hard part of the proof.

```
Cs'[s1]  ≈  Cs'[s2]
  ≈           ≈
Ct[t1]   ?  Ct[t2]
```

### Concrete example of languages, compiler, & proof of full abstraction

Let's make this concrete with an example. This will be presented some in english
and some in the proof assistant Coq. This post isn't an introduction to Coq; for
that, see e.g., Bertot and Casteron's Coq'Art, Chlipala's CPDT, or Pierce et
al's Software Foundations.

Our source language is arithmetic expressions over integers with addition and
subtraction:

```
e ::= n
    | e + e
    | e - e
```

This is written down in Coq as:

```coq
Inductive Expr : Set := 
  | Num : Z -> Expr
  | Plus : Expr -> Expr -> Expr 
  | Minus : Expr -> Expr -> Expr.
```

Evaluation is standard (if you wanted to parse this, you would need to deal with
left/right associativity, and probably add parenthesis to disambiguate, but we
consider the point where you already have a tree structure, so it is
unambiguous). We can write the evaluation function as:

```coq
Fixpoint eval_Expr (e : Expr) : Z := 
  match e with
  | Num n => n                               
  | Plus e1 e2 => eval_Expr e1 + eval_Expr e2
  | Minus e1 e2 => eval_Expr e1 - eval_Expr e2
  end.
```

Our target language is a stack machine which uses a stack of integers to
evaluate the sequence of instructions. In addition to having instructions to add
and subtract, our stack machine has an extra instruction: `OpCount`. This
instruction returns how many operations remain on the stack machine, and it puts
that integer on the top of the stack. This is the simplest abstraction I could
think of that will provide an interesting case study for problems of full
abstraction, and is a stand-in for both reflection (as it allows the program to
inspect other parts of the program), and also somewhat of a proxy for execution
time (remaining). Our stack machine requires that the stack be empty at the end
of execution.

```coq
Inductive Op : Set :=
| Push : Z -> Op
| Add : Op
| Sub : Op
| OpCount : Op.
```
              
Let's see the compiler and the evaluation function (note that we reverse the
order when we pop values off the stack from when we put them on in the compiler).

```coq
Fixpoint compile_Expr (e : Expr) : list Op :=
  match e with
  | Num n => [Push n]
  | Plus e1 e2 => compile_Expr e1 ++ compile_Expr e2 ++ [Add]
  | Minus e1 e2 => compile_Expr e1 ++ compile_Expr e2 ++ [Sub]
  end.

Fixpoint eval_Op (s : list Z) (ops : list Op) : option Z :=
  match (ops, s) with
  | ([], [n]) => Some n
  | (Push z :: rest, _) => eval_Op (z :: s) rest 
  | (Add :: rest, n2 :: n1 :: ns) => eval_Op (n1 + n2 :: ns)%Z rest
  | (Sub :: rest, n2 :: n1 :: ns) => eval_Op (n1 - n2 :: ns)%Z rest
  | (OpCount :: rest, _) => eval_Op (Z.of_nat (length rest) :: s) rest
  | _ => None
  end.
```

We can prove a basic (_whole program_) compiler correctness result for this
(for more detail on this type of result, see [this
post](/essays/2018-01-16-how-to-prove-a-compiler-correct.html)), where first we
prove a general `eval_step` lemma and then use that to prove correctness (note: the
`hint` and `hint_rewrite` tactics are from an experimental
[literatecoq](https://github.com/dbp/literatecoq) library that adds support for
proof-local hinting, which some might think is a hack but I think makes the
proofs much more readable/maintainable).

```coq
Lemma eval_step : forall a : Expr, forall s : list Z, forall xs : list Op,
      eval_Op s (compile_Expr a ++ xs) = eval_Op (eval_Expr a :: s) xs.
Proof.
  hint_rewrite List.app_assoc_reverse.
  induction a; intros; iauto; simpl;
  hint_rewrite IHa2, IHa1;
  iauto'.
Qed.

Theorem compiler_correctness : forall a : Expr,
    eval_Op [] (compile_Expr a) = Some (eval_Expr a).
Proof.
  hint_rewrite eval_step.
  hint_simpl.
  induction a; iauto'.
Qed.
```

Now, before we can state properties about equivalences, we need to define what
we mean by equivalence for our source and target languages. Both produce no side
effects, so the only observation is the end result. Thus, observational
equivalence is pretty straightforward; it follows from evaluation:

```coq
Definition equiv_Expr (e1 e2 : Expr) : Prop := eval_Expr e1 = eval_Expr e2.
Definition equiv_Op (p1 p2 : list Op) : Prop := eval_Op [] p1 = eval_Op [] p2.
```

But, we want to talk not just about whole programs, but about partial programs
that can get linked with other parts to create whole programs. In order to do
that, we create a new type of "evaluation context" for our `Expr`, that has a
hole (typically written on paper as `[·]`). This is a program that is missing an
expression, which must be filled into the hole. Given how simple our language
is, any expression can be filled in to the hole and that will produce a valid
program. We only want to have _one_ hole per partial program, so in the cases
for `+` and `-`, one branch must be a normal `Expr` (so it contains no hole),
and the other can contain one hole. Our `link_Expr` function takes a context and an
expression and fills in the hole.

```coq
Inductive ExprCtxt : Set := 
| Hole : ExprCtxt
| Plus1 : ExprCtxt -> Expr -> ExprCtxt
| Plus2 : Expr -> ExprCtxt -> ExprCtxt 
| Minus1 : ExprCtxt -> Expr -> ExprCtxt
| Minus2 : Expr -> ExprCtxt -> ExprCtxt.

Fixpoint link_Expr (c : ExprCtxt) (e : Expr) : Expr :=
  match c with
  | Hole => e
  | Plus1 c' e' => Plus (link_Expr c' e) e'
  | Plus2 e' c' => Plus e' (link_Expr c' e)
  | Minus1 c' e' => Minus (link_Expr c' e) e'
  | Minus2 e' c' => Minus e' (link_Expr c' e)
  end.
```

For our stack machine, partial programs are much easier, since a program is just
a list of `Op`, which means that any program can be extended by adding new `Op`s
on either end (or inserting in the middle). 

With `ExprCtxt`, we can now define "contextual equivalence" for our source language: 

```coq
Definition ctxtequiv_Expr (e1 e2 : Expr) : Prop :=
  forall c : ExprCtxt, eval_Expr (link_Expr c e1) = eval_Expr (link_Expr c e2).
```

We can do the same with our target, simplifying slightly and saying that we will
allow adding arbitrary `Op`s before and after, but not in the middle, of an
existing sequence of `Op`s.

```coq
Definition ctxtequiv_Op (p1 p2 : list Op) : Prop :=
  forall c1 c2 : list Op, eval_Op [] (c1 ++ p1 ++ c2) = eval_Op [] (c1 ++ p2 ++ c2).
```

To prove our compiler fully abstract, remember we need to prove that it
preserves and reflects equivalences. Since we already proved that it is correct,
proving that it reflects equivalences should be relatively straightforward, so
lets start there. The lemma we want is:

```coq
Lemma equivalence_reflection :
  forall e1 e2 : Expr,
  forall p1 p2 : list Op,
  forall comp1 : compile_Expr e1 = p1,
  forall comp2 : compile_Expr e2 = p2,
  forall eqtarget : ctxtequiv_Op p1 p2,
    ctxtequiv_Expr e1 e2.
Proof.
  unfold ctxtequiv_Expr, ctxtequiv_Op in *.
  intros.
  induction c; simpl; try solve [hint_rewrite IHc; iauto];
    (* NOTE(dbp 2018-04-16): Only the base case, for Hole, remains *)
    [idtac].
  (* NOTE(dbp 2018-04-16): In the hole case, specialize the target ctxt equiv hypothesis to empty *)
  specialize (eqtarget [] []); simpl in eqtarget; repeat rewrite app_nil_r in eqtarget.

  (* NOTE(dbp 2018-04-16): At this point, we know e1 -> p1, e2 -> p2, & p1 ≈ p2,
  and want e1 ≈ e2, which follows from compiler correctness *)
  rewrite <- comp1 in eqtarget. rewrite <- comp2 in eqtarget.
  repeat rewrite compiler_correctness in eqtarget.
  inversion eqtarget.
  reflexivity.
Qed.
```

This lemma is a little more involved, but not by much; we proceed by induction
on the structure of the evaluation contexts, and in all but the case for `Hole`,
the induction hypothesis gives us exactly what we need. In the base case, we
need to appeal to the `compiler_correctness` lemma we proved earlier, but
otherwise it follows easily.

So what about equivalence preservation? We can state the lemma quite easily:

```coq
Lemma equivalence_preservation :
  forall e1 e2 : Expr,
  forall p1 p2 : list Op,
  forall comp1 : compile_Expr e1 = p1,
  forall comp2 : compile_Expr e2 = p2,
  forall eqsource : ctxtequiv_Expr e1 e2,
    ctxtequiv_Op p1 p2.
Proof.
Abort.
```

But proving it is another matter. In fact, it's not provable, because it's not
true. We can come up with a counter-example, using that `OpCount` instruction we
(surreptitiously) added to our target language. These two expressions are
contextually equivalent in our source language (should be obvious, but putting a proof):

```coq
Example src_equiv : ctxtequiv_Expr (Plus (Num 1) (Num 1)) (Num 2).
Proof.
  unfold ctxtequiv_Expr.
  induction c; simpl; try rewrite IHc; iauto.
Qed.
```

But they are not contextually equivalent in the target; in particular, if we put
the `OpCount` instruction before and then the `Add` instruction afterwards, the
result will be the value plus the number of instructions it took to compute it:

```coq
Example target_not_equiv :
  eval_Op [] (OpCount :: compile_Expr (Plus (Num 1) (Num 1)) ++ [Add]) <>
  eval_Op [] (OpCount :: compile_Expr (Num 2) ++ [Add]).
Proof.
  simpl.
  congruence.
Qed.
```

The former evaluating to `6`, while the latter evaluates to `4`. This means that
there is no way we are going to be able to prove equivalence preservation (as we
have a counter-example!). 

So what do we do? Well, this scenario is not uncommon, and it's the reason why
many, even correct, compilers are not fully abstract. It's also related to why
many of these compilers may still have security problems! The solution is to
somehow protect the compiled code from having the equivalences disrupted. If
this were a real machine, we might want to have some flag on instructions that
meant that they should not be counted, and `OpCount` would just not return
anything if it saw any of those (or would count them as 0). Alternately, we
might give our target language a type system that is able to rule out linking
with code that uses the `OpCount` instruction, or perhaps restricts how it can
be used.

Because this is a blog-post sized example, and I wanted to keep the proofs as
short as possible, and the unstructured and untyped nature of our target (which,
indeed, is much less structured than our source language; the fact that the
source is so well-structured is why our whole-program correctness result was so
easy!) will mean the proofs get relatively complex (or require us to add various
auxiliary definitions), so the solution I'm going to take is somewhat extreme.
Rather than, say, restricting how `OpCount` is used, or even ruling out linking
with `OpCount`, we're going to highly restrict what we can link with. This is
very artificial, and done entirely so that the proofs can fit into a few lines.
In this case, rather than a list, we are going to allow one `Op` before and one
`Op` after our compiled program, neither of which can be `OpCount`, and further,
we still want the resulting program to be well-formed (i.e., no errors, only one
number on stack at end), so either there should be nothing before and after, or
there is a `Push n` before and either `Add` or `Sub` after. (You should be able
to verify that no other combination of `Op` before or after will fulfill our
requirement).

We can define these possible linking contexts and a helper to combine them with
programs as the following:

```coq
Inductive OpCtxt : Set :=
| PushAdd : Z -> OpCtxt
| PushSub : Z -> OpCtxt
| Empty : OpCtxt.

Definition link_Op  (c : OpCtxt) (p : list Op) : list Op :=
  match c with
  | PushAdd n => Push n :: p ++ [Add]
  | PushSub n => Push n :: p ++ [Sub]
  | Empty => p
  end.
```

Using that, we can redefine contextual equivalence for our target language, only
permitting these contexts:

```coq
Definition ctxtequiv_Op (p1 p2 : list Op) : Prop :=
  forall c : OpCtxt, eval_Op [] (link_Op c p1) = eval_Op [] (link_Op c p2).

```

The only change to our proof of equivalence reflection is on one line, to
change our specialization of the target contexts, now to the `Empty` context:

```coq
specialize (eqtarget Empty) (* Empty rather than [] [] *)
```

With that change, we now believe that our compiler, when linked against these
restricted contexts, is indeed fully abstract. So let's prove it. If you recall
from earlier in this post, proving equivalence preservation means proving
that the bottom line implies the top, in the following diagram:

```
Cs'[s1]  ≈  Cs'[s2]
  ≈           ≈
Ct[t1]   ?  Ct[t2]
```

In order to do that, we rely upon a backtranslation to get from `Ct` to `Cs'`,
where `Ct` is a target context, in this tiny example our restricted `OpCtxt`. We
can write that backtranslation as:

```coq
Definition backtranslate (c : OpCtxt) : ExprCtxt :=
  match c with
  | PushAdd n => Plus2 (Num n) Hole
  | PushSub n => Minus2 (Num n) Hole
  | Empty => Hole
  end.
```

The second part of the proof is showing that the vertical equivalences in the
diagram hold --- that is, that if `s1` is compiled to `t1` and `Ct` is
backtranslated to `Cs'` then `Ct[t1]` is equivalent to `Cs'[s1]`. We can state
and prove that as the following lemma, which follows from straightforward case
analysis on the structure of our target context and backtranslation (using our
`eval_step` lemmas):

```coq
Lemma back_translation_equiv :
  forall c : OpCtxt,
  forall p : list Op,
  forall e : Expr,
  forall c' : ExprCtxt, 
    compile_Expr e = p ->
    backtranslate c = c' ->
    eval_Op [] (link_Op c p) = Some (eval_Expr (link_Expr c' e)).
Proof.
  hint_rewrite eval_step, eval_step'.
  intros.
  match goal with
  | [ c : OpCtxt |- _] => destruct c
  end; 
    match goal with
    | [ H : backtranslate _ = _ |- _] => invert H
    end; simpl; iauto. 
Qed.
```

Once we have that lemma, we can prove equivalence preservation directly. We do
this by doing case analysis on the target context we are given, backtranslating
it and then using the lemma we just proved to get the equivalence that we need.

```coq
Lemma equivalence_preservation :
  forall e1 e2 : Expr,
  forall p1 p2 : list Op,
  forall comp1 : compile_Expr e1 = p1,
  forall comp2 : compile_Expr e2 = p2,
  forall eqsource : ctxtequiv_Expr e1 e2,
    ctxtequiv_Op p1 p2.
Proof.
  unfold ctxtequiv_Expr, ctxtequiv_Op in *.
  intros.

  remember (backtranslate c) as c'.
  destruct c; iauto;

  erewrite back_translation_equiv with (e := e1) (c' := c'); iauto;
  erewrite back_translation_equiv with (e := e2) (c' := c'); iauto;
  specialize (eqsource c'); simpl in *; congruence.
Qed.
```

This was obviously a very tiny language and a very restrictive linker that only
allowed very restrictive contexts, but the general shape of the proof is the
same as that used in more realistic languages published in research conferences
today!

So next time you see a result about a correct (or even hoped to be correct)
compiler, ask if it is fully abstract! And if it's not, are the violations of
equivalences something that could be exploited? Or something that would
invalidate optimizations?

Some recent conference publications include Devriese et al, [Fully-abstract
compilation by approximate
back-translation](https://lirias.kuleuven.be/bitstream/123456789/570054/2/logrel-for-facomp-authorversion.pdf)
published in POPL'16 and New at al, [Fully Abstract Compilation via Universal
Embedding](http://www.ccs.neu.edu/home/amal/papers/fabcc.pdf), published in
ICFP'16.


> As stated at the top of the post, all the code in this post is available at
> [https://github.com/dbp/howtoprovefullabstraction](https://github.com/dbp/howtoprovefullabstraction). 
> If you have any trouble getting it going, please open an issue on that
> repository and I'll help figure it out with you.
