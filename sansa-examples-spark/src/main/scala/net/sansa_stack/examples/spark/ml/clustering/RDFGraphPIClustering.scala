package net.sansa_stack.examples.spark.ml.clustering

import scala.collection.mutable

import net.sansa_stack.ml.spark.clustering._
import net.sansa_stack.ml.spark.clustering.algorithms.RDFGraphPowerIterationClustering
import net.sansa_stack.rdf.spark.io._
import net.sansa_stack.rdf.spark.model._
import org.apache.jena.riot.{ Lang, RDFDataMgr }
import org.apache.log4j.{ Level, Logger }
import org.apache.spark.sql.SparkSession

object RDFGraphPIClustering {

  def main(args: Array[String]) {
    parser.parse(args, Config()) match {
      case Some(config) =>
        run(config.in, config.out, config.k, config.maxIterations)
      case None =>
        println(parser.usage)
    }
  }

  def run(input: String, output: String, k: Int, maxIterations: Int): Unit = {

    val spark = SparkSession.builder
      .appName(s"Power Iteration Clustering example ( $input )")
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()
    System.setProperty("spark.akka.frameSize", "2000")

    println("============================================")
    println("| Power Iteration Clustering   example     |")
    println("============================================")

    val lang = Lang.NTRIPLES
    val triples = spark.rdf(lang)(input)

    val cluster = triples.cluster(ClusteringAlgorithm.RDFGraphPowerIterationClustering).asInstanceOf[RDFGraphPowerIterationClustering]
      .setK(k).setMaxIterations(maxIterations).run()

    cluster.collect.foreach(println)

    spark.stop

  }

  case class Config(in: String = "", out: String = "", k: Int = 2, maxIterations: Int = 5)

  val defaultParams = Config()

  val parser = new scopt.OptionParser[Config]("RDFGraphPIClustering") {

    head("PowerIterationClusteringExample: an example PIC app using concentric circles.")

    opt[String]('i', "input").required().valueName("<path>")
      .text(s"path (local/hdfs) to file that contains the input files (in N-Triple format)")
      .action((x, c) => c.copy(in = x))

    opt[String]('o', "out").required().valueName("<directory>").
      action((x, c) => c.copy(out = x)).
      text("the output directory")

    opt[Int]('k', "k")
      .text(s"number of circles (/clusters), default: ${defaultParams.k}")
      .action((x, c) => c.copy(k = x))

    opt[Int]("maxIterations")
      .text(s"number of iterations, default: ${defaultParams.maxIterations}")
      .action((x, c) => c.copy(maxIterations = x))

    help("help").text("prints this usage text")
  }
}
