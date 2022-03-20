package com.phaller.blocks.pickle

import com.phaller.blocks.{BlockData, SerBlockData}

import upickle.default._


given blockDataReadWriter: ReadWriter[BlockData[Nothing]] =
  readwriter[ujson.Value].bimap[BlockData[Nothing]](
    blockData => ujson.Arr(blockData.fqn, 0),
    json => new BlockData(json(0).str, None)
  )

given serBlockDataReadWriter: ReadWriter[SerBlockData] =
  readwriter[ujson.Value].bimap[SerBlockData](
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
      SerBlockData(fqn, envOpt)
    }
  )

given blockDataWithEnvReadWriter[E](using ReadWriter[E]): ReadWriter[BlockData[E]] =
  readwriter[ujson.Value].bimap[BlockData[E]](
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
      val envOpt = if (hasEnvNum == 1) Some(read[E](json(2).str)) else None
      new BlockData(fqn, envOpt)
    }
  )
