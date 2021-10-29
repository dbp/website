# Math/Science integrated with Scheme

I had an idea today, of an interactive homework assignment for a Chemistry class. It was a prompt, and you could type in queries and it would give responses. The basics would be:

    # (questions)
    => To Do: (1,2,3,4,5,6,7)
       Complete: ()
    # (question-1)
    => 1. How many grams of Na are needed to make 28 grams of NaCl?
    # (periodic-table 'Na)
    => Sodium - Atomic Number 11 - Weight 22.98976928
    # (periodic-table 'Cl)
    => Chlorine - Atomic Number 17 - Weight 35.453
    # (* 22.98976928 (/ 28 (+ 22.98976928 35.453)))
    => 11.01
    # (answer-1 11.01)
    => Correct! Great job. 1/7 Questions completed.
    # (questions)
    => To Do: (2,3,4,5,6,7)
       Complete: (1)

Now if you don't know Scheme syntax, the line with the numeric calculation might be a little confusing, but once you realize that it is just pure prefix notation (the operator always comes first, every set of parenthesis wrap an operation) it should start making sense. I'm pretty sure I could explain Scheme to anyone who is taking high-school science in an hour, but the three sentence explanation is: Every expression is wrapped inside parenthesis. The first word inside the parenthesis is a function, the rest (there don't have to be any) are arguments to the function, which can be be other expressions or basic items like numbers or strings. Arithmetic follows this pattern, which may seem a little unnatural at first, but this consistency means that you now know almost all there is to know about Scheme.

But what I've described here isn't actually much better than the web question and answer system that I saw today, that gave me this idea. It's basically just an interactive text-based version of the same thing. What I started thinking of is having the capability to add things like this:

    # (assignment-equations)
    Arrhenius equation - k = Ae^(-Ea/RT)
      provided versions:
      (arrhenius-k frequency-factor activation-energy temperature)
      (arrhenius-a reaction-coefficient activation-energy temperature)
    ...
    # (arrhenius-k 1.01e11 13500 273)
    2.6375e8

Which would provide both references and ways to do some of the more boring rote work quickly. Descriptions of the equations could also exist, making it even more of an interactive learning project. But what would be even better would be to allow students to define new functions (or redefine old ones) on the fly. Let's say there are a bunch of different calculations that require the same involved steps. I saw today I student working through two laborious calculations, which differed only in that the value for the activation energy. What would be amazing is if a student could do something like:

    # (define (my-arrh-eq act-energy)
        (arrhenius-k (arrhenius-a 2.75e-2 act-energy 293) act-energy 333))
    => Defined new function my-arrh-eq!
    # (my-arrh-eq 14500)
    => 1.01

I don't remember if that was the answer or even the value for the activation energy (it probably isn't), but that was the general solution. Now the problem was that a rate coefficient (2.75e-2) was given for 20degrees celsius and and the problem asked what was the rate coefficient for 60degrees celsius (same reaction). The problem was posed with two different activation energies, and identical and reasonably involved calculations resulted - using the given 20degree setup to solve for the frequency factor and then plug that into the same equation this time using 60degrees.

But what was interesting about this problem was the technique of solving one equation and using a part of that in the other - not actually doing out the arithmetic. It would be amazing if a student could build things like the function above, which clearly demonstrate an understanding of the technique, but also reveal a capacity to organize their thoughts and string together the pieces into higher level abstractions - a critical part of the type of thinking that underlies computer programming, and something that is going to become more and more important as time goes on.

I think there is amazing potential to systems like this - where programming is built into the fabric of math and science work, because it will both teach students to program (which is a very helpful thing), but it will also focus their attention and mental efforts on understanding how to string together concepts and actually solve problems, not just how to do calculations. I think it could also have a motivating effect because when you start writing programs like this, you feel like you are somehow getting out of doing boring work (which you are), and that you must be cheating somehow (and that feels good!). Little do you know that you are actually learning the material better than the person who did the calculations out by hand, because you focused on what was really important and had to figure out the general solution.

Now some of this is already happening - probably mostly using TI-BASIC on graphing calculators, but the system is reasonably unnatural (and no one is teaching students how to use it) and removed from basic work that I don't think it is very widespread. I think a system that students would interact with that would allow them to build functions and use existing ones in the course of doing work would be a really amazing thing, both for their understanding of the subject itself and also to learn computer programming (or, more generally, "algorithmic thinking").
