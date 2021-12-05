package com.phaller.blocks.pickle

import com.phaller.blocks.BlockData

import upickle.default._


given blockDataReadWriter[E](using envReadWriter: ReadWriter[E]): ReadWriter[BlockData[E]] =
  readwriter[ujson.Value].bimap[BlockData[E]](
    blockData => {
      val pickledEnv = write(blockData.env)
      ujson.Arr(blockData.fqn, pickledEnv)
    },
    json => {
      val fqn = json(0).str
      val env = read[E](json(1).str)
      BlockData(fqn, env)
    }
  )
