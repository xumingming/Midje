(ns midje.checkers.t-simple
  (:use midje.sweet
        [midje.checkers.defining :only [checker?]]
        midje.test-util))

(facts "about truthy"
  #'truthy => checker?
  truthy => checker?
  true => truthy
  1 => truthy
  (truthy false) => false
  (truthy nil) => false)

(facts "about falsey"
  #'falsey => checker?
  falsey => checker?
  false => falsey
  nil => falsey
  (falsey true) => false
  (falsey 1) => false)

(fact "truthy and falsey have capitalized versions"
  false => FALSEY
  true => TRUTHY)

(facts "about anything"
  #'anything => checker?
  anything => checker?
  true => anything
  false => anything
  even? => anything
  "irrelevant is a synonym"
  1 => irrelevant)

(facts "about exactly"
  #'exactly => checker?
  exactly => checker?
  (exactly odd?) => checker? 
  true => (exactly true)
  ( (exactly 2) 2) => truthy
  ( (exactly 1) 2) => falsey
  even? => (exactly even?))

(facts "roughly"
  (fact "is a checker that produces checkers"
    #'roughly => checker?
    roughly => checker?
    (roughly 3) => checker?
    (roughly 3 1) => checker?)

  (fact "allows an explicit range"
    0.99 =not=> (roughly 2.0 1.0)
    3.01 =not=> (roughly 2.0 1.0)

    0.00 => (roughly 1.0 1.0)
    2.00 => (roughly 1.0 1.0))

  (fact "provides an implicit range if needed"
    998.999 =not=> (roughly 1000)
    999.001 => (roughly 1000)
    1000.990 => (roughly 1000)
    1001.001 =not=> (roughly 1000))

  (fact "works with negative numbers"
    -1 => (roughly -1)
    -1.00001 => (roughly -1)
    -0.99999 => (roughly -1)
  
    -1 => (roughly -1 0.1)
    -0.90001 => (roughly -1 0.1)
    -1.00001 => (roughly -1 0.1))

  (fact "no longer has an old bug to do with collections"
    [-0.16227766016837952 6.16227766016838]
    => (just (roughly -0.1622) (roughly 6.1622)))

  (fact "non-numbers produces a falsey result"
    ((roughly 12) nil) => false
    ((roughly 12) "ba") => false))

(defn throw-exception
  ([] (throw (NullPointerException.)))
  ([message] (throw (Error. message)))
)

(facts "about throws"
  (throws NullPointerException) => checker?
  (throws NullPointerException "hi") => checker?
  
  (throw-exception) => (throws NullPointerException)
  (throw-exception "hi") => (throws Error "hi")
  (throw-exception "hi") => (throws Error #"h."))

(after-silently 
 (fact 
   (throw-exception "throws Error") => (throws NullPointerException)
   (throw-exception "throws Error") => (throws Error "bye"))
 (fact 
   @reported => (two-of checker-fails)))

(fact "throws accepts many varieties of arglists"
  (throw-exception "msg") => (throws Error)
  (throw-exception "msg") => (throws "msg")
  (throw-exception "msg") => (throws #(= "msg" (.getMessage %)))

  (throw-exception "msg") => (throws Error "msg" )
  (throw-exception "msg") => (throws #(= "msg" (.getMessage %)) Error)
  (throw-exception "msg") => (throws Error #(= "msg" (.getMessage %)) )
  (throw-exception "msg") => (throws #(= "msg" (.getMessage %)) "msg")
  (throw-exception "msg") => (throws "msg" #(= "msg" (.getMessage %)))
  (throw-exception "msg") => (throws "msg" Error )

  (throw-exception "msg") => (throws Error "msg" #(= "msg" (.getMessage %)))
  (throw-exception "msg") => (throws #(= "msg" (.getMessage %)) Error "msg")
  (throw-exception "msg") => (throws Error #(= "msg" (.getMessage %)) "msg")
  (throw-exception "msg") => (throws #(= "msg" (.getMessage %)) "msg" Error)
  (throw-exception "msg") => (throws "msg" #(= "msg" (.getMessage %)) Error)
  (throw-exception "msg") => (throws "msg" Error #(= "msg" (.getMessage %))) )

(fact "throws works with checkers that use Midje's extended notion of false"
  (throw-exception "msg") => (throws #( (contains "m") (.getMessage %)))
  (throw-exception "msg") =deny=> (throws #( (contains "message") (.getMessage %)))
  ;; Following would be a user error, but the results should be helpful.
  (throw-exception "msg") =deny=> (contains "msg"))

(fact "`throws` can even accept multiple predicates"
  (throw-exception "msg") => (throws #(= "msg" (.getMessage %)) #(= "msg" (.getMessage %)) #(= "msg" (.getMessage %)))
  (throw-exception "msg") => (throws "msg" #(= "msg" (.getMessage %)) #(= "msg" (.getMessage %)) #(= "msg" (.getMessage %)))
  (throw-exception "msg") => (throws Error #(= "msg" (.getMessage %)) #(= "msg" (.getMessage %)) #(= "msg" (.getMessage %)))
  (throw-exception "msg") => (throws "msg" Error #(= "msg" (.getMessage %)) #(= "msg" (.getMessage %)) #(= "msg" (.getMessage %))))

(fact "`throws` can accept multiple messages - imagine regexs for large error mesages"
  (throw-exception "msg") => (throws #"^m" #"g$")
  (throw-exception "msg") => (throws Error #"^m" #"g$")
  ;; Note that both regexps should match.
  (throw-exception "msg") =deny=> (throws Error #"m" #"h"))

(fact "`throws` matches any exception that is an instance of expected"
  (throw (NullPointerException.)) => (throws Exception))

(after-silently
 (fact "`throws` fails when not given an exception"
   1 => (throws Exception))
 (fact
   @reported => (just checker-fails)))

;; Unexpected exceptions
(after-silently
 (facts
   (throw-exception "throws Error") => anything
   (throw-exception "throws Error") => falsey
   (throw-exception "throws Error") => truthy)
 (fact
   @reported => (three-of checker-fails)))
