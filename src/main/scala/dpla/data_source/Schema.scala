package dpla.data_source

import org.apache.spark.sql.types._

object Schema {

  val edmAgentType = StructType(Seq(
    StructField("uri", StringType, nullable = true),
    StructField("name", StringType, nullable = true),
    StructField("providedLabel", StringType, nullable = true),
    StructField("note", StringType, nullable = true),
    StructField("scheme", StringType, nullable = true),
    StructField("exactMatch", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("closeMatch", ArrayType(StringType, containsNull = false), nullable = false)
  ))

  val edmTimeSpanType = StructType(Seq(
    StructField("originalSourceDate", StringType, nullable = true),
    StructField("prefLabel", StringType, nullable = true),
    StructField("begin", StringType, nullable = true),
    StructField("end", StringType, nullable = true)
  ))

  val skosConceptType = StructType(Seq(
    StructField("concept", StringType, nullable = true),
    StructField("providedLabel", StringType, nullable = true),
    StructField("note", StringType, nullable = true),
    StructField("scheme", StringType, nullable = true),
    StructField("exactMatch", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("closeMatch", ArrayType(StringType, containsNull = false), nullable = false)
  ))

  val dplaPlaceType = StructType(Seq(
    StructField("name", StringType, nullable = true),
    StructField("city", StringType, nullable = true),
    StructField("county", StringType, nullable = true),
    StructField("region", StringType, nullable = true),
    StructField("state", StringType, nullable = true),
    StructField("country", StringType, nullable = true),
    StructField("coordinates", StringType, nullable = true)
  ))

  val literalOrUriType = StructType(Seq(
    StructField("value", StringType, nullable = false),
    StructField("isUri", BooleanType, nullable = false)
  ))

  val collectionType = StructType(Seq(
    StructField("title", StringType, nullable = true),
    StructField("description", StringType, nullable = true)
  ))

  val edmWebResourceType = StructType(Seq(
    StructField("uri", StringType, nullable = false),
    StructField("fileFormat", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("dcRights", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("edmRights", StringType, nullable = true)
  ))

  val sourceResourceType = StructType(Seq(
    StructField("alternateTitle", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("collection", ArrayType(collectionType), nullable = false),
    StructField("contributor", ArrayType(edmAgentType), nullable = false),
    StructField("creator", ArrayType(edmAgentType), nullable = false),
    StructField("date", ArrayType(edmTimeSpanType), nullable = false),
    StructField("description", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("extent", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("format", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("genre", ArrayType(skosConceptType, containsNull = false), nullable = false),
    StructField("identifier", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("language", ArrayType(skosConceptType, containsNull = false), nullable = false),
    StructField("place", ArrayType(dplaPlaceType, containsNull = false), nullable = false),
    StructField("publisher", ArrayType(edmAgentType, containsNull = false), nullable = false),
    StructField("relation", ArrayType(literalOrUriType, containsNull = false), nullable = false),
    StructField("replacedBy", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("replaces", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("rights", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("rightsHolder", ArrayType(edmAgentType, containsNull = false), nullable = false),
    StructField("subject", ArrayType(skosConceptType, containsNull = false), nullable = false),
    StructField("temporal", ArrayType(edmTimeSpanType, containsNull = false), nullable = false),
    StructField("title", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("type", ArrayType(StringType, containsNull = false), nullable = false)))

  val schema: StructType = StructType(Seq(
    StructField("dplaUri", StringType, nullable = false),
    StructField("SourceResource", sourceResourceType, nullable = false),
    StructField("dataProvider", edmAgentType, nullable = false),
    StructField("originalRecord", StringType, nullable = false),
    StructField("hasView", ArrayType(edmWebResourceType, containsNull = false), nullable = false),
    StructField("intermediateProvider", edmAgentType, nullable = true),
    StructField("isShownAt", edmWebResourceType, nullable = false),
    StructField("object", edmWebResourceType, nullable = true),
    StructField("preview", edmWebResourceType, nullable = true),
    StructField("provider", edmAgentType, nullable = false),
    StructField("edmRights", StringType, nullable = true),
    StructField("sidecar", StringType, nullable = true)
  ))


}
