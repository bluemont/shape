# Shape

**Note**: I'm currently drafting and iterating on this README before starting
on implementation. THERE IS NO CODE YET! :)

Shape is a Clojure library to parse a data structure in relation to an
arbitrarily complex shape specification.

It borrows ideas from parsing tools (e.g. customizable grammar rules that can
refer to other rules), type systems (e.g. the 'maybe' type), data validators
(e.g. numerical validations), and regular expressions (the '?', '+', and '*'
operators).

## Examples

```clj
(s/parse {:a 1} {:x :s/integer :y :s/integer})
```

Returns `nil` because there is no match.

```clj
(s/parse {:x 3 :y 4} {:x :s/integer :y :s/integer})
```

Returns `true` because there is a match.

```clj
(s/parse {:x 3 :y 4} {:x '(:s/bind x :s/integer) :y :s/integer})
```

Returns `((:x 4))` because there is a match with one binding (similar to a
'capture' in a regular expression.)

## Goals

Shape is designed:

  1. to be as data-centric as possible

    * to expect Clojure data structures as inputs (e.g. lists, maps, vectors) --
      not just strings
    * to use [edn][edn] data to represent the parsing grammar rules

  2. to compile grammar rules at run-time

    * to allow rules to change at run-time, thus providing dynamic grammar

  3. to parse at run-time

    * using exhaustive parsing

Due to these goals, Shape may be well-suited for applications that:

  * process diverse external data
  * have a large, dynamic, open set of parse rules
  * differentially process and route data according to its shape

### Exhaustive Parsing

Exhaustive parsing was chosen because, in my understanding, it plays better for
my use case:

  * I use Shape with a large set of changing rules
  * I don't want to have to specify which rules take precedence
  * I don't want Shape to concern itself with resolving parse ambiguities --
    other systems with additional context are better suited for that task

In contrast, non-exhaustive parsers must use some kind of ordering to determine
which result(s) to return first.

  * sometimes the order is explicit in the grammar
    * e.g. greedy / reluctant operators in regular expressions
    * e.g. alternation in context-free grammars
  * sometimes parsers allow rules to 'trump' other rules
    * e.g. rules may be applied in the order they are defined
    * e.g. longer (more [specific][CSS1]) rules may be preferred
    * e.g. rule precedence may be declared
  * sometimes the match order is unspecified
    * e.g. due to a consequence of internal implementation details
    * e.g. random by design

[CSS1]: https://developer.mozilla.org/en-US/docs/Web/CSS/Specificity

## Syntax Walkthrough

The shape syntax is explained in this section.

The examples below use `s` as an alias for `shape.core`.

A quote (`'`) is used as a prefix in some of the examples that follow. This
signals to the Clojure reader to not evaluate the form. This causes the form to
get treated only as data structure. Otherwise, if evaluated, "Non-empty Lists
are considered calls to either special forms, macros, or functions. A call has
the form (operator operands*)." as explained on Clojure's [Evaluation][eval]
page.

[eval]: http://clojure.org/evaluation

### Exact Values

#### 42

  * matches 42
  * does not bind anything

#### `:debt`

  * matches the value `:debt`
  * does not bind anything

### Types

#### `:s/integer`

  * matches an integer
  * does not bind anything

#### `:s/string`

  * matches a string
  * does not bind anything

#### `:s/keyword`

  * matches a keyword
  * does not bind anything

#### `:s/symbol`

  * matches a symbol
  * does not bind anything
  * note: symbols are less common than keywords, in practice

#### `:s/any`

  * matches any value
  * does not bind anything

#### `'(:s/maybe 42)`

  * matches 42 or nil
  * does not bind anything

### Binding (i.e. Capturing)

#### `'s`

  * invalid syntax
  * if you want to bind (i.e. capture) a match, see the next example

#### `'(:s/bind s :s/string)`

  * matches a string
  * binds to `s`

#### `'(:s/bind x (:s/maybe :s/integer))`

  * matches an integer or nil
  * binds to `x`

### Lists

#### `()`

  * matches an empty list
  * does not bind anything

#### `'(:s/any)`

  * matches a list of one element
  * the element can match any value
  * does not bind anything

#### `'(:model)`

  * matches a list of one element
  * the element must equal `:model`
  * does not bind anything

### Lists with Binding

#### `'(x)`

  * invalid syntax
  * if you want to bind a list of one element, see the next example

#### `'((:s/bind x))`

  * matches a list of one element
  * the element can match any value
  * binds element to `x`
  * same as `'((:s/bind x :s/any))`

#### `'(x y)`

  * invalid syntax
  * if you want to bind a list of two elements, see the next example

#### `'((:s/bind x) (:s/bind y))`

  * matches a list of two elements
  * binds the first element to `x`
  * binds the second element to `y`
  * same as `'((:s/bind x :s/any) (:s/bind y :s/any)`

### Maps

#### `{}`

  * matches an empty map
  * does not bind anything

#### `{:a :b}`

  * only matches the `{:a :b}` map
  * does not bind anything

#### `{:a :s/string}`

  * matches a map
  * requires the `:a` key
  * the `:a` key must have a string value
  * no additional keys are allowed

#### `{:s/string :s/integer}`

  * matches a map with one key/value pair
  * the key must be a string
  * the value must be an integer

### `{:a :s/string '(:s/optional-key :b) :s/integer}`

  * the `:a` key is required
  * if the `:a` key is present, it must have a string value
  * the `:b` key is optional
  * if the `:b` key is present, it must have an integer value

### Variable Length Lists

#### `'((:s/? :s/keyword))`

  * matches a list of 0 or 1 elements
  * does not bind anything

#### `'((:s/* :s/any))`

  * matches a list of 0+ elements
  * does not bind anything

#### `'((:s/+ :s/))`

  * matches a list of 1+ elements
  * does not bind anything

### Variable Length Lists with Binding

#### `'((:s/* (:s/bind x :s/any)))`

  * matches a list of 0+ elements
  * binding examples:
    * `'()` -> no bindings
    * `'(7)` -> `'((x 7))`
    * `'(7 11)` -> `'((x 7) (x 11))`
  * note that `:s/*` is outermost

#### `'(:s/bind x ((:s/* :s/any)))`

  * matches a list of 0+ elements
  * binding examples:
    * `'()` -> `'((x ()))`
    * `'(7)` -> `'((x (7)))`
    * `'(7 11)` -> `'((x (7 11)))`
  * note that `:s/bind` is outermost

### Variable Length Vectors

These are analagous to variable length lists.

#### `'[(:s/? :s/integer)]`

  * matches a vector of 0 or 1 integers
  * does not bind anything

#### `'[(:s/* :s/string)]`

  * matches a vector of 0+ strings
  * does not bind anything

#### `'[(:s/+ :s/keyword)]`

  * matches a vector of 1+ keywords
  * does not bind anything

### Mixed Variable Length Collections

#### `'[:start (:s/+ :s/keyword)]`

  * matches a vector of 2+ elements
  * the first element must be `:start`
  * the second (and subsequent elements, if present) must be keywords
  * does not bind anything

#### `'[:start (:s/bind x (:s/+ :s/integer)]`

  * matches a vector of 2+ elements
  * the first element must be `:start`
  * the second (and subsequent elements, if present) must be integers
  * binds elements 2 and afterwards to `x`. for example:
    * `[:start 1]` -> `'((x 1))`
    * `[:start 1 2]` -> `'((x 1) (x 2)`

### Collections, Sequences, Sequential Data

#### `'(:s/coll?)`

  * matches any [collection][collection], whethered ordered (lists, vectors) or
    unordered (maps, sets)
  * does not bind anything

[collection]: http://clojure.org/data_structures#Data%20Structures-Collections

#### `'(:s/seq?)`

  * matches any [sequence](http://clojure.org/sequences)
  * more particularly, it matches data that implements ISeq (e.g. lists)
  * does not bind anything

#### `'(:s/sequential?)`

  * matches any sequential data (a collection implementing `Sequential`) such as
    a list or vector
  * does not bind anything

#### `'(:s/sequential? (:s/+ :s/integer))`

  * matches any sequential data with 0 or 1 integer
  * does not bind anything

#### `(:s/bind xs (:s/sequential? (:s/+ :s/integer)))`

  * matches any sequential data with 0 or 1 integer
  * binds the entire collection to `xs`
  * note: bind is on the outside

## License

Copyright 2015 Bluemont Labs LLC

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.

## Related Work

Shape is similar in some ways to other tools.

### Instaparse

[Instaparse][Instaparse] parses strings using context-free grammars and returns
Clojure data structures.

### Prismatic Schema

Prismatic's [Schema][Schema] is "a rich language for describing data shapes."
Schema was created to make it easier to declare and validate data in Clojure
programs; in particular, to make API data handling easier and less error-prone.

> At Prismatic, one of the issues we were running into with a large
> Clojure(Script) codebase was the ability to document the kind of data
> functions took and what they returned. Aside from documentation, when the
> contract of a function is broken at runtime Clojure often might not fail at
> all, and it's up to the developer to track a nil halfway through the codebase
> to find the root cause. For these reasons, we built the Schema library for
> declaring and validating data
> shapes. -- [Schema for Clojure(Script) Data Shape Declaration and Validation, 2013-09-04][S2]

> One reason we built Schema was to make sure our backend API servers send and
> receive properly formed data when communicating with our iOS and web clients.
> -- [Schema 0.2.0: back with Clojure(Script) data coercion, 2014-01-09][S1]

[S1]:
http://blog.getprismatic.com/schema-for-clojurescript-data-shape-declaration-and-validation/

[S2]: http://blog.getprismatic.com/schema-0-2-0-back-with-clojurescript-data-coercion/

Using Schema made their programs more readable (because Schema is declarative)
and debuggable (because the shapes are validated with useful error
messages). Cosmetically and practically, Schema provides many of the same
advantages as a static type system, except that validation does not happen at
compile time. It avoids a problem with many type systems where

Schema uses a declarative schema system. Typically, schemas are evaluated when an
application is first loaded and don't change over the life of the program. This
makes sense given the main goals and use cases of the project.


Schema build on each other by referencing other schemas using [Vars][Vars],
which makes persistence (e.g. to [edn]) less practical.

Schema is flexible. It works well to validate external data. That said, I don't
think Schema shines for conditional data processing with a large, diverse rule
set. I designed Shape for processing data streams of diverse shapes that get
routed based on how they are parsed against hundreds of rules.

[Instaparse]: https://github.com/Engelberg/instaparse
[Schema]: https://github.com/Prismatic/schema
[Vars]: http://clojure.org/vars
[edn]: https://github.com/edn-format/edn
