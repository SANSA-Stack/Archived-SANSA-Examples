package net.sansa_stack.examples.spark.ml.clustering

import scala.collection.mutable

import net.sansa_stack.ml.spark.clustering._
import net.sansa_stack.ml.spark.clustering.algorithms.SilviaClustering
import net.sansa_stack.rdf.spark.io._
import net.sansa_stack.rdf.spark.model._
import org.apache.jena.riot.Lang
import org.apache.log4j.{ Level, Logger }
import org.apache.spark.sql.SparkSession

object SilviaClusteringExample {

  def main(args: Array[String]) {
    parser.parse(args, Config()) match {
      case Some(config) =>
        run(config.in, config.out)
      case None =>
        println(parser.usage)
    }
  }

  def run(input: String, output: String): Unit = {

    val spark = SparkSession.builder
      .appName(s"SilviaClustering example ( $input )")
      .master("local[*]")
      .config("spark.hadoop.validateOutputSpecs", "false")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    println("============================================")
    println("| Silvia Clustering example                      |")
    println("============================================")

    Logger.getRootLogger.setLevel(Level.WARN)

    val lang = Lang.NTRIPLES
    val triples = spark.rdf(lang)(input)

    val silvia = triples.cluster(ClusteringAlgorithm.SilviaClustering).asInstanceOf[SilviaClustering].run()

    silvia.collect.foreach(println)

    spark.stop

  }

  case class Config(in: String = "", out: String = "")

  val parser = new scopt.OptionParser[Config]("SilviaClustering") {

    head("SilviaClustering: an example SilviaClustering app.")

    opt[String]('i', "input").required().valueName("<path>").
      action((x, c) => c.copy(in = x)).
      text("path to file contains the input files")

    opt[String]('o', "output").optional().valueName("<directory>").
      action((x, c) => c.copy(out = x)).
      text("the output directory")

    help("help").text("prints this usage text")
  }
}
