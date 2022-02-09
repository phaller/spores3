package com.phaller.blocks.pickle

import com.phaller.blocks.BlockData

import upickle.default._


given blockDataReadWriter: ReadWriter[BlockData[Nothing]] =
  readwriter[ujson.Value].bimap[BlockData[Nothing]](
    blockData => ujson.Arr(blockData.fqn, 0),
    json => BlockData(json(0).str, None)
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
      BlockData(fqn, envOpt)
    }
  )
