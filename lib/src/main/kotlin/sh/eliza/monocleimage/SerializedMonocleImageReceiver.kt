package sh.eliza.monocleimage

import java.util.BitSet

/** Keeps track of receiving a serialized monocle image over the wire. */
class SerializedMonocleImageReceiver {
  var isDone = false
    private set

  private val receivedRows = mutableMapOf<Int, MonocleImage.Row>()
  private val receivedRowIndicesBitSet = BitSet(MonocleImage.HEIGHT * 2)

  /** Process the next data packet received from the peer. */
  fun push(payload: List<Byte>) {
    // Parse all rows out of the packet.
    val numberOfRows = payload[0].toInt() and 0xFF
    var o = SerializedMonocleImage.METADATA_SIZE
    for (i in 0 until numberOfRows) {
      val header =
        (payload[o + 0].toInt() and 0xFF) or
          ((payload[o + 1].toInt() and 0xFF) shl 8) or
          ((payload[o + 2].toInt() and 0xFF) shl 16) or
          ((payload[o + 3].toInt() and 0xFF) shl 24)
      o += SerializedMonocleImage.ROW_HEADER_SIZE

      val isCompressed = (header and 0x80000000.toInt()) != 0
      val index = (header shr 18) and 0x7FF
      val offset = (header shr 9) and 0x1FF
      val size = (header shr 0) and 0x1FF
      val data = payload.subList(o, o + size)
      o += size

      println("isCompressed = $isCompressed, index = $index, offset = $offset, size = $size")

      receivedRows[index] = MonocleImage.Row(offset, data, isCompressed)
      receivedRowIndicesBitSet.set(index)
    }
    require(o == payload.size) { "Payload contains extra data." }
  }

  /** Process confirmation request from peer, and generate a response. */
  fun onRequestConfirmation(data: ByteArray): ByteArray {
    val confirmedBitSet = BitSet.valueOf(data)
    if (confirmedBitSet == receivedRowIndicesBitSet) {
      isDone = true
      return data.clone() // TODO: remove defensive copy?
    }
    return receivedRowIndicesBitSet.toByteArray()
  }

  // TODO: throw exception if not done?
  fun toMonocleImage() =
    MonocleImage(
      yRows =
        receivedRows
          .entries
          .mapNotNull { (index, row) ->
            if (index < MonocleImage.HEIGHT * 2) {
              Pair(index, row)
            } else {
              null
            }
          }
          .toMap(),
      uvRows =
        receivedRows
          .entries
          .mapNotNull { (index, row) ->
            if (index >= MonocleImage.HEIGHT * 2) {
              Pair(index - MonocleImage.HEIGHT * 2, row)
            } else {
              null
            }
          }
          .toMap(),
    )
}
