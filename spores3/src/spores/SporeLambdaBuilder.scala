package spores

import upickle.default.*

import spores.Reflection


/** Internal API. Used by the spores.jvm.Spore lambda factories. */
@Reflection.EnableReflectiveInstantiation
private[spores] trait SporeLambdaBuilder[+T] extends SporeLambdaBuilder0[ReadWriter, T]
