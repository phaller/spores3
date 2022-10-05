# Spores3

[![Build Status](https://github.com/phaller/spores3/actions/workflows/build-test.yml/badge.svg)](https://github.com/phaller/spores3/actions)

## Introduction

Spores3 is a project that provides abstractions for closures (or lambda expressions or anonymous functions) whose environment is made explicit. The environment of a closure is defined by the variables captured by the closure. The goal is to make closures more flexible and safer by avoiding some of the issues of closures when used in the context of concurrent or distributed programming.

**Flexibility and safety.** Spores are more flexible than closures, and safer for concurrency and distribution. For example:
- The environment of a spore can be constrained using type classes. For example, the environment type of a spore can be enforced to be thread-safe (e.g., `Future[T]`) or immutable.
- Spores can be serialized simply and robustly using type-class-based serialization libraries, such as [uPickle](https://com-lihaoyi.github.io/upickle/). To increase safety, spores can be enforced at compile time to be serializable. For example, the compiler can check whether there is a uPickle `ReadWriter` for the spore's environment.
- Spores can be duplicated such that their environment is deeply copied, cloning possibly mutable objects. This enables safer concurrency, for example, by duplicating spores before spawning them as concurrent tasks.

Spores3 is a new take on the earlier
[Spores](https://scalacenter.github.io/spores/spores.html). Spores3
uses a new approach for type-class-based serialization, and feature a
simpler, more robust implementation.

## Add to your project

Add the following dependency to your `build.sbt`:

```
libraryDependencies += "com.phaller" %% "spores3" % "0.1.0"
```

## Overview

Creating a simple spore is similar to creating a regular anonymous
function:

```scala
val s = Spore((x: Int) => x + 2)
```

One of the main differences to anonymous functions is visible in the
type of the above spore:

```scala
Spore[Int, Int] { type Env = Nothing }
```

In contrast to regular function types, spore types have a type member
`Env` indicating the type of their environment. Since the above spore
doesn't have an environment (its body only accesses the parameter) the
environment type is `Nothing`.

Let's create a spore with an environment. Instead of simply using a
variable within the body of a spore which becomes part of the
environment, the environment of a spore needs to be passed explicitly
as an argument:

```scala
val str = "anonymous function"

val s = Spore(str) {  // `str` is the environment of the spore
  env => (x: Int) => x + env.length
}
```

If a spore has an environment, then the spore's body has an additional
parameter, called `env` above, which enables accessing the
environment.  In the above example, `env` has type
`String`. Consequently, the type of the spore is `Spore[Int, Int] {
type Env = String }`.

Note that the environment of a spore is always passed as a **single**
argument; environments with several values/objects require the use of
tuples, for example:

```scala
val str = "anonymous function"
val num = 5

val s = Spore((str, num)) {
  env => (x: Int) => x + env._1.length - env._2
}
```

The corresponding spore type is `Spore[Int, Int] { type Env = (String,
Int) }`. Since the environment is passed as the first parameter of the
body function, it is possible to use pattern matching, which avoids
the use of clunky accessors `_1`, `_2`, etc.:

```scala
val str = "anonymous function"
val num = 5

val s = Spore((str, num)) {
  case (s, n) => (x: Int) => x + s.length - n
}
```

## Pickling of spores

Spores provide a specialized form of closures which are safe and
efficient to serialize. The design of spores does not require the use
of a specific serialization/pickling library. Instead, spores can be
integrated with different serialization libraries. Initially, an
integration with [uPickle](https://com-lihaoyi.github.io/upickle/) is
provided.

Let's have a look at an example that shows how to pickle a spore using
uPickle. The shown code snippets assume the following imports:

```scala
    import com.phaller.spores.{Spore, SporeData, PackedSporeData}
    import com.phaller.spores.upickle.given
```

First, the definition of the spore:

```scala
    object MySpore extends Spore.Builder[Int, Int, Int](
      env => (x: Int) => env + x + 1
    )
```

Here, `MySpore` is actually not a concrete spore but a spore
**builder**. The reason is that the environment of the spore is left
unspecified. Note the three type arguments in `Spore.Builder[Int, Int,
Int]`.  The corresponding function type `Int => Int` would only have
two type arguments.  The type of a spore builder requires a third type
argument indicating the **type of the spore's environment**. The
builder type's first type argument specifies the environment type.

The body of the spore refers to the spore's environment using the
extra `env` parameter. By providing a concrete environment, an actual
spore can be created as follows:

```scala
    val x = 12
    val sp = MySpore(x)  // environment is integer value 12
```

Applying the spore yields the expected result:

```scala
    assert(sp(3) == 16)
```

Instead of serializing the instance on the heap that `sp` points to,
the idea is to instead serialize a `SporeData` object which contains
all the data and information that's necessary to re-create the
corresponding spore with its environment, possibly on a different
machine. For example, the `SporeData` object includes the
fully-qualified name of the spore builder defined above.

A `SporeData` object is created as follows:

```scala
    val data = SporeData(MySpore, Some(x)) // `x` is the environment, as before
```

Using the `given` instance in package `com.phaller.spores.upickle`, the
`SporeData` object can be pickled and unpickled:

```scala
    val pickledData = write(data)
    val unpickledData = read[PackedSporeData](pickledData)
```

(The `read` and `write` methods have been imported from
`upickle.default`.) Note that when unpickling `pickledData` the target
type `PackedSporeData` is specified. This way, **the type of the
environment does not need to be provided**. A less convenient
alternative would be to unpickle to type `SporeData[Int, Int] { type
Env = Int }`. This is not recommended, however, because the code
unpickling the spore is usually not aware of the environment type.

With a `PackedSporeData` object in our hands we can easily make a
spore with its environment properly initialized:

```scala
    val unpickledSpore = unpickledData.toSpore[Int, Int]
    assert(unpickledSpore(3) == 16)
```

## Spores and Capture Checking

The experimental [capture
checking](https://dotty.epfl.ch/docs/reference/experimental/cc.html)
extension of Scala's type system introduces capturing types which
enable tracking and checking **capabilities**. A capability is a
variable or parameter with a **capturing type** which includes a
capture set. The capture set of (the type of) a capability `c`
consists of those capabilities that `c` gets its authority from.

The above-linked reference documentation shows an example of a logger
that requires and retains a `FileSystem` capability `fs`, and thus has
capturing type `{fs} Logger`. (Here, `{fs}` is the capture set.)

Among others, capture checking introduces **pure functions** of type
`A -> B` which cannot capture any capabilities. However, a pure
function might still capture a variable that's not a capability. The
body of a spore is even more restricted, however: it cannot capture
**any variable**. That's why the capture checking of spores is
required even when using the capture checking extension.
