package spores.upickle

import spores.{SporeData, PackedSporeData}

import upickle.default.*


given sporeDataReadWriter[T, R]: ReadWriter[SporeData[T, R] { type Env = Nothing }] =
  readwriter[ujson.Value].bimap[SporeData[T, R] { type Env = Nothing }](
    sporeData => ujson.Arr(sporeData.fqn, 0),
    json => new SporeData[T, R](json(0).str) {
      type Env = Nothing
      def envOpt = None
    }
  )

given packedSporeDataReadWriter: ReadWriter[PackedSporeData] =
  readwriter[ujson.Value].bimap[PackedSporeData](
    sporeData => {
      val hasEnvNum = if (sporeData.envOpt.nonEmpty) 1 else 0
      if (hasEnvNum == 1)
        ujson.Arr(sporeData.fqn, hasEnvNum, sporeData.envOpt.get)
      else
        ujson.Arr(sporeData.fqn, hasEnvNum)
    },
    json => {
      val fqn = json(0).str
      val hasEnvNum = json(1).num
      val envOpt = if (hasEnvNum == 1) Some(json(2).str) else None
      PackedSporeData(fqn, envOpt)
    }
  )

given sporeDataWithEnvReadWriter[N, T, R](using ReadWriter[N]): ReadWriter[SporeData[T, R] { type Env = N }] =
  readwriter[ujson.Value].bimap[SporeData[T, R] { type Env = N }](
    sporeData => {
      val hasEnvNum = if (sporeData.envOpt.nonEmpty) 1 else 0
      if (hasEnvNum == 1) {
        val pickledEnv = write(sporeData.envOpt.get)
        ujson.Arr(sporeData.fqn, hasEnvNum, pickledEnv)
      } else {
        ujson.Arr(sporeData.fqn, hasEnvNum)
      }
    },
    json => {
      val fqn = json(0).str
      val hasEnvNum = json(1).num
      val maybeEnv = if (hasEnvNum == 1) Some(read[N](json(2).str)) else None
      new SporeData[T, R](fqn) {
        type Env = N
        def envOpt = maybeEnv
      }
    }
  )
