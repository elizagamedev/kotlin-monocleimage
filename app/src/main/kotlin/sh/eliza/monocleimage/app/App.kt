package sh.eliza.monocleimage.app

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import sh.eliza.monocleimage.MonocleImage
import sh.eliza.monocleimage.SerializedMonocleImage
import sh.eliza.monocleimage.SerializedMonocleImageReceiver
import sh.eliza.monocleimage.SerializedMonocleImageSender

fun main(args: Array<String>) {
  val image = createMonocleImageFromPath(args[0])
  println("yRows.size = ${image.yRows.size}, uvRows.size = ${image.uvRows.size}")

  // Serialize and simulate sending over the wire.
  val serializedImage = SerializedMonocleImage.createFromMonocleImage(image, mtu = 512)
  val serializedImageSize = serializedImage.payloads.map { it.data.size }.sum()
  println(
    "serialized image is $serializedImageSize bytes over ${serializedImage.payloads.size} payloads"
  )

  val sender = SerializedMonocleImageSender(serializedImage)
  val receiver = SerializedMonocleImageReceiver()

  while (!sender.isDone) {
    val payload = sender.next()
    if (payload.isConfirmation) {
      sender.onConfirmationResponse(receiver.onRequestConfirmation(payload.data.toByteArray()))
    } else {
      receiver.push(payload.data)
    }
  }

  check(receiver.isDone) { "Receiver should be done by now!" }
  val receivedImage = receiver.toMonocleImage()

  saveMonocleImageToPath(receivedImage, args[1])
}

private fun saveMonocleImageToPath(monocleImage: MonocleImage, path: String) {
  val image = BufferedImage(MonocleImage.WIDTH, MonocleImage.HEIGHT, BufferedImage.TYPE_INT_RGB)
  monocleImage.toRgb { row, col, rgb -> image.setRGB(col, row, rgb) }
  ImageIO.write(image, "png", File(path))
}

private fun createMonocleImageFromPath(path: String): MonocleImage {
  val image = ImageIO.read(File(path))
  return MonocleImage.createFromRgb { row, col -> image.getRGB(col, row) }
}
