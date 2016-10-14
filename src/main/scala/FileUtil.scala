import java.net.URL
import java.nio.file.{Files, Path, Paths}


object FileUtil extends FileUtil

trait FileUtil {
  def imageExists(path: Path): Boolean = {
    Files.exists(path)
  }

  def saveImage(path: Path, image: Array[Byte]): Unit = {
    val parentDir = path.getParent
    if (!Files.exists(parentDir))
      Files.createDirectories(parentDir)

    Files.write(path, image)
  }

  def extractImageUrls(baseImagePath: Path, images: Map[String, String]): List[(URL, Path)] = {
    images.values.map { imageUrl =>
      val url = new URL(imageUrl)
      val path = Paths.get(baseImagePath + url.getPath)
      url -> path
    }.toList
  }
}
