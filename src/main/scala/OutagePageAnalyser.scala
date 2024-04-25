import org.jsoup.Jsoup

import java.io.File
import java.nio.file.Files
import scala.collection.immutable
import scala.jdk.CollectionConverters.ListHasAsScala

object OutagePageAnalyser {

  val outagePagesRepo =
    sys.env.getOrElse("OUTAGE_PAGES_REPO", throw new RuntimeException("OUTAGE_PAGES_REPO not set"))

  def main(args: Array[String]): Unit = {
    val productionPageDir = new File(s"/$outagePagesRepo/production")
    if (!productionPageDir.isDirectory) throw new RuntimeException("Not a valid directory")

    val outagePageDirs = productionPageDir.listFiles().toList.filter(_.isDirectory)
    println(s"There are ${outagePageDirs.length} outage page directories in Production")

    val indexFiles: List[File] =
      outagePageDirs.flatMap(_.listFiles().toList.filter(_.getName.contains("index.html")))

    println(s"There are ${indexFiles.length} index.html pages in Production")

    val parsed: Seq[PageAssetsAndSvg] = indexFiles.map(parseFile)
    val stylesheetsAndFilenames: Seq[(String, String)] = parsed.flatMap(perFile => perFile.stylesheets.map((_, perFile.filename)))
    val groupedByStylesheet: Map[String, Int] = stylesheetsAndFilenames.groupBy(_._1).map(allThis => (allThis._1, allThis._2.map(_._2).length))
    groupedByStylesheet.foreach { case (k, v) => println(s"|$k|$v|") }
  }

  private def parseFile(file: File): PageAssetsAndSvg = {
    val fileName = file.getPath.stripPrefix(outagePagesRepo)
    val contents = Files.readString(file.toPath)
    val parsedContents = Jsoup.parse(contents)

    val stylesheets = parsedContents.select("link[rel=\"stylesheet\"]").eachAttr("href").asScala.toList
    val images = parsedContents.select("img[src$=.png]").eachAttr("src").asScala.toList
    val svg = parsedContents.select("svg").asScala.headOption.map(_.toString)

    PageAssetsAndSvg(fileName, stylesheets = stylesheets, images, svg)
  }

  private case class PageAssetsAndSvg(filename: String, stylesheets: List[String], images: List[String], svg: Option[String])
}
