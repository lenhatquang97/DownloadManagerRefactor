package com.quangln2.downloadmanagerrefactor.data.model

import java.io.Serializable

class ChunkFile {
    var chunkNums: Int = 0
    var listsOfChunk: MutableList<FromTo> = mutableListOf()

    constructor(chunkNums: Int, listsOfChunk: MutableList<FromTo>) {
        this.chunkNums = chunkNums
        this.listsOfChunk = listsOfChunk
    }
}

class FromTo : Serializable {
    var from: Long = 0
    var to: Long = 0
    var curr: Long = 0

    constructor(from: Long, to: Long, curr: Long) {
        this.from = from
        this.to = to
        this.curr = curr
    }
}