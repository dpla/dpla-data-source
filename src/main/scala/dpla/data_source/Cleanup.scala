package dpla.data_source

import org.apache.spark.sql.Row
import org.json4s.JsonAST._
import org.json4s.jackson.JsonMethods



object Cleanup {

  val sampleRecord =
    """
      |{
      |  "@context": "http://dp.la/api/items/context",
      |  "dataProvider": [
      |    "Cornell University"
      |  ],
      |  "admin": {
      |    "object_status": 1
      |  },
      |  "object": "https://books.google.com/books/content?id=BtFYAAAAYAAJ&printsec=frontcover&img=1&zoom=5",
      |  "aggregatedCHO": "#sourceResource",
      |  "provider": {
      |    "@id": "http://dp.la/api/contributor/hathitrust",
      |    "name": "HathiTrust"
      |  },
      |  "ingestDate": "2018-04-11T23:28:27.814466Z",
      |  "@type": "ore:Aggregation",
      |  "ingestionSequence": null,
      |  "isShownAt": "http://catalog.hathitrust.org/Record/009884092",
      |  "sourceResource": {
      |    "publisher": [
      |      "Washington, D.C. : United States General Accounting Office"
      |    ],
      |    "description": [
      |      "Available via Internet from: http://www.gao.gov",
      |      "GAO-03-450.",
      |      "May 2003.",
      |      "Cover title.",
      |      "Includes bibliographical references."
      |    ],
      |    "language": [
      |      {
      |        "iso639_3": "eng",
      |        "name": "English"
      |      }
      |    ],
      |    "title": [
      |      "Human Capital : opportunities to improve executive agencies' hiring processes /"
      |    ],
      |    "@id": "http://dp.la/api/items/9f9ca5a5d6531f74598c654ecc8379b6#sourceResource",
      |    "format": [
      |      "Electronic resource",
      |      "Language material"
      |    ],
      |    "rights": "Public domain. Learn more at http://www.hathitrust.org/access_use",
      |    "creator": [
      |      "United States. General Accounting Office"
      |    ],
      |    "extent": [
      |      "ii, 51 p. : 28 cm."
      |    ],
      |    "spatial": [
      |      {
      |        "country": "United States",
      |        "name": "United States"
      |      }
      |    ],
      |    "date": "2003",
      |    "specType": [
      |      "Book",
      |      "Government Document"
      |    ],
      |    "identifier": [
      |      "sdr-coo4830391",
      |      "(OCoLC)52825301",
      |      "LC call number: JF1601 .U58 2003",
      |      "Hathi: 009884092"
      |    ],
      |    "type": [
      |      "text"
      |    ],
      |    "subject": [
      |      {
      |        "name": "Administrative agencies--United States--Personnel management"
      |      },
      |      {
      |        "name": "Human capital--United States"
      |      },
      |      {
      |        "name": "Civil service--United States--Recruiting"
      |      },
      |      {
      |        "name": "United States--Officials and employees--Recruiting"
      |      }
      |    ]
      |  },
      |  "ingestType": "item",
      |  "_id": "hathitrust--009884092",
      |  "@id": "http://dp.la/api/items/9f9ca5a5d6531f74598c654ecc8379b6",
      |  "id": "9f9ca5a5d6531f74598c654ecc8379b6"
      |}
    """.stripMargin

  def cleanup(data: String): String = {
    val json = JsonMethods.parse(data)
    val sr = json \ "sourceResource"
    JsonMethods.compact(
      JObject(
        JField("dataProvider", singleValue(json, "dataProvider")),
        JField("id", singleValue(json, "id")),
        JField("intermediateProvider", singleValue(json, "id")),
        JField("isShownAt", singleValue(json, "isShownAt")),
        JField("object", singleValue(json, "object")),
        JField("sourceResource", JObject(
          JField("alternativeTitle", multiValue(sr, "alternativeTitle")),
          JField("collection", collection(json)),
          JField("contributor", multiValue(sr, "contributor")),
          JField("creator", multiValue(sr, "creator")),
          JField("date", date(json)), // custom validation
          JField("description", multiValue(sr, "description")),
          JField("extent", multiValue(sr, "extent")),
          JField("format", multiValue(sr, "format")),
          JField("genre", multiValue(sr, "genre")),
          JField("identifier", multiValue(sr, "identifier")),
          // JField("language", multiValue(sr, "language")), // custom validation
          JField("publisher", multiValue(sr, "publisher")),
          JField("relation", multiValue(sr, "relation")),
          JField("rights", multiValue(sr, "rights")), // strict
          JField("subject", subject(json)),
          // JField("temporal", multiValue(json \ "sourceResource", "temporal")), // custom validation
          JField("title", multiValue(sr, "title")), // strict
          JField("type", multiValue(sr, "type")) // strict
          // replacedBy not in map v3.1 i3 jsonl export
          // replaces not in map v3.1 i3 jsonl export
          // rightsHolder not in map v3.1 i3 jsonl export
        ))
      )
    )
  }

  /**
    * Enforces single value field, takes head element from Array
    * @param json
    * @param field
    * @return
    */
  def singleValue(json: JValue, field: String): JValue = {
    val value = json \ field
    value match {
      case JString(_) => value
      case JArray(jArray) => jArray.head
    }
  }

  /**
    * Enforces multi value field. wraps single value in arry
    * @param json
    * @param field
    * @return
    */
  def multiValue(json: JValue, field: String): JValue = {
    val value = json \ field

    value match {
      case JArray(_) => value
      case _ => JArray(List(value))
    }
  }

  def collection(json: JValue): JArray = {
    val collection = json \ "sourceResource" \ "collection"

    val cleanColl = collection match {
      case JNothing => JNothing
      case JArray(_) => collection
      case _: JObject => JArray(List(collection))
    }

    JArray(cleanColl.children.flatMap {
      case JNothing => None
      case jString: JString => Some(JObject(JField("title", jString)))
      case jObject: JObject => Some(jObject)
      case _ => None //delete or cleanup
    })
  }

  def date(json: JValue): JArray = {
    val date = json \ "sourceResource" \ "date"
    val cleanedDate = date match {
      case JArray(_) => date
      case _: JValue => JArray(List(date))
    }

    JArray(cleanedDate.children.flatMap( {
      case jObject: JObject => Some(jObject)
      case jString: JString => Some(JObject(JField("displayDate", jString)))
      case _ => None
    }))
  }

  def subject(json: JValue): JArray = {
    val subject: JValue = json \ "sourceResource" \ "subject"

    val cleanedSubject = subject match {
      case JArray(_) => subject
      case _: JValue => JArray(List(subject))
    }

    JArray(cleanedSubject.children.flatMap {
      case jObject: JObject => Some(jObject)
      case jString: JString => Some(JObject(JField("name", jString)))
      case _ => None //delete or cleanup
    })
  }

  def main(args: Array[String]): Unit = {
    val cleanStr = Cleanup.cleanup(Cleanup.sampleRecord)
    println(JsonMethods.pretty(JsonMethods.parse(cleanStr)))
  }

}