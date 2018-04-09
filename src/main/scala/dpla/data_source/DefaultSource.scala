package dpla.data_source

import com.micronautics.aws.S3
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.sources._
import org.apache.spark.sql.types._

import scala.util.Try

class DefaultSource extends RelationProvider with DataSourceRegister {

  override def shortName(): String = "dpla"

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]):
  BaseRelation = {

    // Get required args for all OAI harvests.
    val dataset = parameters.get("dataset")

    dataset match { //room for expansion to more types in the master dataset later
      case Some("jsonl") => new DplaJsonlRelation(parameters)(sqlContext)
      case _ => new DplaJsonlRelation(parameters)(sqlContext)
    }
  }
}

class DplaJsonlRelation(parameters: Map[String, String])(@transient val sqlContext: SQLContext)
  extends BaseRelation with TableScan with Serializable {

  private val data = sqlContext.read.json(getFiles: _*)

  override def schema: StructType = data.schema

  override def buildScan(): RDD[Row] = data.rdd

  def getFiles: Seq[String] = {
    val s3 = new S3()
    val allJsonFiles = s3.listObjectsByPrefix("dpla-master-dataset", "", ".json").sorted

    // providers: Unique provider prefixes.  Use the list given in the execution arguments, if present.
    val providers: Set[String] = parameters.get("providers") match {
      case Some(listString) => listString.split(",").toSet
      case None => allJsonFiles.map(_.split("/", 2)(0)).toSet
    }

    // Find the most recent JSON files, looking at the last timestamp per provider
    val tsPat: scala.util.matching.Regex = """^.*jsonl/(\d{8}_\d{6}).*""".r

    val recents = providers.flatMap(p => Try({
        val pFiles = allJsonFiles.filter(_.startsWith(p + "/")).sorted
        val tstamps = pFiles.map(f => {
          val tsPat(ts: String) = f; ts
        }).toSet // unique timestamp per provider
        val mostRecentTs = parameters.get("maxTimestamp") match {
          case Some(x) =>
            val older = tstamps.toList.filter(ts => ts < x)
            if (older.isEmpty) "NOTFOUND" else older.max
          case None => tstamps.toList.max
        }
        pFiles.
          filter(_.matches(s".*$mostRecentTs.*")).
          map(f => s"s3a://dpla-master-dataset/$f")
      }).getOrElse(List.empty))

    recents.toSeq
  }

}


