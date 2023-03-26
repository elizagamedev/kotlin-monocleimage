package sh.eliza.monocleimage

import java.util.BitSet

/** Keeps track of sending a serialized monocle image over the wire. */
class SerializedMonocleImageSender(
  val image: SerializedMonocleImage,
) {
  data class Payload(
    /** If true, payload is a confirmation request. */
    val isConfirmation: Boolean,
    /** Payload data. */
    val data: List<Byte>,
  )

  var isDone = false
    private set

  private val allRowIndicesBitset = image.allRowIndices.toBitSet(MonocleImage.HEIGHT * 2)

  private val toSendQueue = ArrayDeque<SerializedMonocleImage.Payload>()

  /** Return the next payload to be sent over the wire. */
  fun next(): Payload {
    check(!isDone) { "Nothing left to send." }
    if (toSendQueue.isEmpty()) {
      // Ask the peer to confirm what they have.
      return Payload(isConfirmation = true, allRowIndicesBitset)
    }
    // Send the next item in the queue.
    return Payload(isConfirmation = false, toSendQueue.removeFirst().data)
  }

  /** Process confirmation from peer. */
  fun onConfirmationResponse(data: ByteArray) {
    val confirmed = BitSet.valueOf(data).toSet()
    val difference = image.allRowIndices - confirmed
    if (difference.isEmpty()) {
      isDone = true
      return
    }
    val sending = mutableSetOf<Int>()
    for (index in difference) {
      if (index !in sending) {
        val payload = image.payloadByIndex[index]!!
        sending.addAll(payload.rowIndices)
        toSendQueue.addLast(payload)
      }
    }
  }
}
