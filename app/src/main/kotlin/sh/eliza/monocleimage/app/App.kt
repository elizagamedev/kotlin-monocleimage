package sh.eliza.monocleimage.app

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import sh.eliza.monocleimage.MonocleImage

fun main(args: Array<String>) {
  val image = createMonocleImageFromPath(args[0])
  saveMonocleImageToPath(image, args[1])

  val yBytes = image.yRows.values.map { it.data.size }.sum()
  val uvBytes = image.uvRows.values.map { it.data.size }.sum()

  println("yRows.size = ${image.yRows.size}, uvRows.size = ${image.uvRows.size}")
  println("approximately ${yBytes + uvBytes} bytes (y = $yBytes, uv = $uvBytes)")
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
