package play.extras.iteratees

import play.api.libs.json._
import org.specs2.mutable.Specification
import play.api.libs.iteratee.Enumerator
import concurrent.Await
import concurrent.duration.Duration

object JsonSpec extends Specification {
  "json iteratee" should {
    "parse an empty object" in test(Json.obj())
    "parse a string" in test(Json.obj("string" -> "value"))
    "parse escaped values in a string" in test(JsString("""a\"\n\r"""))
    "parse a number" in test(Json.obj("number" -> 10))
    "parse true" in test(Json.obj("boolean" -> true))
    "parse false" in test(Json.obj("boolean" -> false))
    "parse null" in test(Json.obj("obj" -> JsNull))
    "parse an empty array" in test(Json.obj("array" -> Json.arr()))
    "parse an array with stuff in it" in test(Json.obj("array" -> Json.arr("foo", "bar")))
    "parse a complex object" in test(Json.obj(
      "string" -> "value",
      "number" -> 10,
      "boolean" -> true,
      "null" -> JsNull,
      "array" -> Json.arr(Json.obj("foo" -> "bar"), 20),
      "obj" -> Json.obj("one" -> 1, "two" -> 2, "nested" -> Json.obj("spam" -> "eggs"))
    ))
    "parse large object" in test({
      val repeatedItem = Json.obj(
        "deviceId" -> "122F08E1-F147-469E-9BF9-6074EAC814AC",
        "timestamp" -> "119802237.153125",
        "x" -> "0.027740478515625",
        "y" -> "-0.0445556640625",
        "z" -> "-1.002105712890625"
      )
      Json.arr(Array.fill(100)(repeatedItem))
    }, "large")
  }

  def test(json: JsValue, name: String = "anon") = {
    val start = System.currentTimeMillis
    Await.result(Enumerator(Json.stringify(json).toCharArray) |>>> JsonIteratees.jsValue, Duration.Inf) must_== json
    val end = System.currentTimeMillis
    println(s"$name: ${end - start}")
  }
}
