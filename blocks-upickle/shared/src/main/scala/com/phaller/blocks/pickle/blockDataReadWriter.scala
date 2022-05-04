package com.phaller.blocks.pickle

import com.phaller.blocks.{BlockData, PackedBlockData}

import upickle.default._


given blockDataReadWriter[T, R]: ReadWriter[BlockData[T, R] { type Env = Nothing }] =
  readwriter[ujson.Value].bimap[BlockData[T, R] { type Env = Nothing }](
    blockData => ujson.Arr(blockData.fqn, 0),
    json => new BlockData[T, R](json(0).str) {
      type Env = Nothing
      def envOpt = None
    }
  )

given packedBlockDataReadWriter: ReadWriter[PackedBlockData] =
  readwriter[ujson.Value].bimap[PackedBlockData](
    blockData => {
      val hasEnvNum = if (blockData.envOpt.nonEmpty) 1 else 0
      if (hasEnvNum == 1)
        ujson.Arr(blockData.fqn, hasEnvNum, blockData.envOpt.get)
      else
        ujson.Arr(blockData.fqn, hasEnvNum)
    },
    json => {
      val fqn = json(0).str
      val hasEnvNum = json(1).num
      val envOpt = if (hasEnvNum == 1) Some(json(2).str) else None
      PackedBlockData(fqn, envOpt)
    }
  )

given blockDataWithEnvReadWriter[N, T, R](using ReadWriter[N]): ReadWriter[BlockData[T, R] { type Env = N }] =
  readwriter[ujson.Value].bimap[BlockData[T, R] { type Env = N }](
    blockData => {
      val hasEnvNum = if (blockData.envOpt.nonEmpty) 1 else 0
      if (hasEnvNum == 1) {
        val pickledEnv = write(blockData.envOpt.get)
        ujson.Arr(blockData.fqn, hasEnvNum, pickledEnv)
      } else {
        ujson.Arr(blockData.fqn, hasEnvNum)
      }
    },
    json => {
      val fqn = json(0).str
      val hasEnvNum = json(1).num
      val maybeEnv = if (hasEnvNum == 1) Some(read[N](json(2).str)) else None
      new BlockData[T, R](fqn) {
        type Env = N
        def envOpt = maybeEnv
      }
    }
  )
