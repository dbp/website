#lang racket

(require lang/htdp-intermediate-lambda)
(require deinprogramm/signature/signature)

(define (HashMapOf K V)
  (let* ([key? (lambda (k) (not (false? (apply-signature K k))))]
         [val? (lambda (v) (not (false? (apply-signature V v))))]
         [hmof? (lambda (h) (and (hash? h)
                                 (andmap key? (hash-keys h))
                                 (andmap val? (hash-values h))))])
    (signature (predicate hmof?))))

(provide hash? hash-empty?
         hash-equal? hash
         hash-set hash-set*
         hash-ref hash-has-key?
         hash-update hash-remove
         hash-clear hash-map
         hash-keys hash-values
         hash->list hash-keys-subset?
         hash-count hash-empty?
         gensym
         HashMapOf)
