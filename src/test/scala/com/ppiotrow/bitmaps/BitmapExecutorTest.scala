package com.ppiotrow.bitmaps

import com.ppiotrow.bitmaps.util.TestDB
import org.roaringbitmap.RoaringBitmap
import org.roaringbitmap.RoaringBitmap.bitmapOf
import org.scalatest.{MustMatchers, WordSpecLike}


class BitmapExecutorTest extends MustMatchers with WordSpecLike {

  import com.ppiotrow.bitmaps.Implicits.RoaringBitmapImpl

  "BitmapExecutor" must {

    "calculate some example (a AND (b OR c OR d)) AND e" in {
      val expression = And(And(Get("a"), Or(Get("b"), Get("c"), Get("d"))), Get("e"))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "a" -> bitmapOf(1, 3, 6, 8, 11),
        "b" -> bitmapOf(1, 2, 6, 11),
        "c" -> bitmapOf(3, 6, 777),
        "d" -> bitmapOf(3, 6, 999),
        "e" -> bitmapOf(1, 3, 5, 7, 9)
      ))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe bitmapOf(1, 3)
    }

    "calculate full example " +
      "(inv1 OR inv2 OR (inv4 AND packg4 AND deal4) OR inv7) AND " +
      "¬(cat100 OR cat200 OR cat300) AND " +
      "((mobile AND ios) AND ( (pl OR dennmark) AND ¬(zip1 OR zip2) ))" in {

      val inventories = Or(Get("inv1"), Get("inv2"), And(Get("inv4"), Get("packg4"), Get("deal4")), Get("inv7"))
      val categories = Not(Or(Get("cat100"), Get("cat200"), Get("cat300")))
      val audience = {
        val device = And(Get("mobile"), Get("ios"))
        val location = And(Or(Get("pl"), Get("denmark")), Not(Or(Get("zip1"), Get("zip2"))))
        And(device, location)
      }
      val expression = And(inventories, categories, audience)
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "inv1" -> bitmapOf(1, 101, 201, 301, 401),
        "inv2" -> bitmapOf(2, 102, 202, 302, 402),
        "inv4" -> bitmapOf(4, 104, 204, 304, 404),
        "inv7" -> bitmapOf(7, 107, 207, 307, 407),
        "packg4" -> bitmapOf(4, 104, 204, 304),
        "deal4" -> bitmapOf(104, 204, 304, 404),
        "cat100" -> bitmapOf(101, 102, 104, 107),
        "cat200" -> bitmapOf(201, 202, 204, 207),
        "cat300" -> bitmapOf(301, 302, 304, 307),
        "mobile" -> bitmapOf(1, 2, 7, 401, 402, 407),
        "ios" -> bitmapOf(2, 3, 7, 401),
        "pl" -> bitmapOf(2, 3),
        "denmark" -> bitmapOf(7),
        "zip1" -> bitmapOf(3),
        "zip2" -> bitmapOf(1)
      ))

      val result = new BitmapExecutor(db).execute(expression)
      val expected = bitmapOf(2, 7)

      result mustBe expected
    }

    "not ignore the audience with only negation" in {
      val audience1 = Get("device|2")
      val audience2 =
        And(
          Not(
            Or(
              Get("country_id|276"),
              Get("country_id|208"),
              Get("country_id|826")
            )))
      val expression =
        And(
          Or(
            Get("inv|1"),
            Get("inv|3")),
          Or(
            audience1,
            audience2
          ))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "inv|1" -> bitmapOf(1, 101, 201, 301, 401),
        "inv|3" -> bitmapOf(2, 102, 202, 302, 402),
        "device|2" -> bitmapOf(2, 102, 201, 202, 302, 402),
        "country_id|276" -> bitmapOf(2, 302),
        "country_id|208" -> bitmapOf(102, 402),
        "country_id|826" -> bitmapOf(301, 401),
        "FULL" -> bitmapOf(1, 2, 101, 102, 201, 202, 301, 302, 401, 402)))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe bitmapOf(2, 102, 201, 202, 302, 402, 1, 101)
    }
  }
}
