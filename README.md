# Blocks

[![Build Status](https://github.com/phaller/blocks/actions/workflows/build-test.yml/badge.svg)](https://github.com/phaller/blocks/actions)

## Introduction

Blocks provide abstractions for closures (or lambda expressions or anonymous functions) whose environment is made explicit. The environment of a closure is defined by the variables captured by the closure. The goal is to make closures more flexible and safer by avoiding some of the issues of closures when used in the context of concurrent or distributed programming.

**Flexibility.** Blocks are more flexible than closures. For example:
- blocks can be enforced to not capture any variables (using type checking)
- blocks can be serialized robustly using any serialization library, including type-class-based serialization (for example, using [uPickle](https://com-lihaoyi.github.io/upickle/))
- blocks can be duplicated such that their environment is deeply cloned

**Safety.** Blocks are safer for concurrency and distribution. For example,
- blocks can be enforced to only capture variables that are thread-safe (e.g., Future[T]) or immutable (using type classes)
- blocks can be enforced at compile time to be serializable, not restricted to Java or Kryo serialization (for example, the compiler can check whether there is a uPickle ReadWriter)
- blocks can capture their environment by deep-copy, cloning possibly mutable objects in the environment before spawning a concurrent task

[Spores](https://scalacenter.github.io/spores/spores.html) provided some of the same properties. Blocks can be seen as a new take on spores that builds on several new features of Scala 3, in particular, context functions and opaque types. By leveraging these features, blocks have a simpler, more robust implementation (with only about 10 lines of macro code). In addition, blocks use a new approach for type-class-based serialization.


## Pickling of blocks

Blocks provide a specialized form of closures which are safe and
efficient to serialize. The design of blocks does not require the use
of a specific serialization/pickling library. Instead, blocks can be
integrated with different serialization libraries. Initially, an
integration with [uPickle](https://com-lihaoyi.github.io/upickle/) is
provided.

Let's have a look at an example that shows how to pickle a block using
uPickle. The shown code snippets assume the following imports:

```scala
    import com.phaller.blocks.{Block, BlockData}
    import com.phaller.blocks.pickle.given
```

First, the definition of the block:

```scala
    object MyBlock extends Block.Builder[Int, Int, Int](
      (x: Int) => Block.env + x + 1
    )
```

Here, `MyBlock` is actually not a concrete block but a block
**builder**. The reason is that the environment of the block is left
unspecified. Note the three type arguments in `Block.Builder[Int, Int, Int]`.
The corresponding function type `Int => Int` would only have two type
arguments.  The type of a block builder requires a third type argument
indicating the **type of the block's environment**. The builder type's
first type argument specifies the environment type.

The body of the block refers to the block's environment using
`Block.env`. By providing a concrete environment, an actual block can
be created as follows:

```scala
    val x = 12
    val block = MyBlock(x)  // environment is integer value 12
```

Applying the block yields the expected result:

```scala
    assert(block(3) == 16)
```

Instead of serializing the instance on the heap that `block` points to,
the idea is to instead serialize a `BlockData` object which contains all
the data and information that's necessary to re-create the corresponding
block with its environment, possibly on a different machine. For
example, the `BlockData` object includes the fully-qualified name of the
block builder defined above.

A `BlockData` object is created as follows:

```scala
    val data = BlockData(MyBlock, Some(x)) // `x` is the environment, as before
```

Using the `given` instance in package `com.phaller.blocks.pickle`, the
`BlockData` object can be pickled and unpickled as usual:

```scala
    val pickledData = write(data)
    val unpickledData = read[BlockData[Int]](pickledData)
    assert(unpickledData == data)
```

(The `read` and `write` methods have been imported from
`upickle.default`.) Note that when unpickling `pickledData` the type
of the environment needs to be provided (type `Int` in
`BlockData[Int]`).

With a (possibly unpickled) `BlockData` object in our hands we can
easily make a block with its environment properly initialized:

```scala
    val unpickledBlock = unpickledData.toBlock[Int, Int]
    assert(unpickledBlock(3) == 16)
```
