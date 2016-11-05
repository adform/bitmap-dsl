package com.ppiotrow.bitmaps

import com.ppiotrow.bitmaps.util.TestDB
import org.roaringbitmap.RoaringBitmap
import org.scalatest.{ MustMatchers, WordSpecLike }


class BitmapExecutorTest extends MustMatchers with WordSpecLike {

  import com.ppiotrow.bitmaps.Implicits.RoaringBitmapImpl

  "BitmapExecutor" must {
    "calculate alternative (a OR b)" in {
      val expression = Or(Get("a"), Get("b"))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "a" -> RoaringBitmap.bitmapOf(1, 3, 5, 111),
        "b" -> RoaringBitmap.bitmapOf(2, 4, 5)
      ))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(1, 2, 3, 4, 5, 111)
    }

    "calculate conjunction (a AND b)" in {
      val expression = And(Get("a"), Get("b"))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "a" -> RoaringBitmap.bitmapOf(1, 3, 5, 111),
        "b" -> RoaringBitmap.bitmapOf(1, 2, 4, 5)
      ))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(1, 5)
    }

    "calculate associative conjunction ((a AND b) AND c)" in {
      val expression = And(And(Get("a"), Get("b")), Get("c"))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "a" -> RoaringBitmap.bitmapOf(1, 3, 5, 111),
        "b" -> RoaringBitmap.bitmapOf(2, 4, 5),
        "c" -> RoaringBitmap.bitmapOf(5, 111, 777)
      ))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(5)
    }

    "calculate deep associative conjunction ((((a AND b) AND c) AND d) AND e)" in {
      val expression = And(And(And(And(Get("a"), Get("b")), Get("c")), Get("d")), Get("e"))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "a" -> RoaringBitmap.bitmapOf(1, 3, 5, 7, 9, 123, 777),
        "b" -> RoaringBitmap.bitmapOf(1, 2, 5, 444, 123, 777, 11111),
        "c" -> RoaringBitmap.bitmapOf(1, 5, 123, 8, 777),
        "d" -> RoaringBitmap.bitmapOf(2, 3, 4, 5, 123, 777),
        "e" -> RoaringBitmap.bitmapOf(1, 2, 3, 4, 5, 6, 7, 8, 777, 11111)
      ))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(5, 777)
    }

    "calculate deep associative alternative ((((a OR b) OR c) OR d) OR e)" in {
      val expression = Or(Or(Or(Or(Get("a"), Get("b")), Get("c")), Get("d")), Get("e"))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "a" -> RoaringBitmap.bitmapOf(1, 3, 5, 7, 9, 123, 777),
        "b" -> RoaringBitmap.bitmapOf(1, 2, 5, 444, 123, 777, 11111),
        "c" -> RoaringBitmap.bitmapOf(1, 5, 123, 8, 777),
        "d" -> RoaringBitmap.bitmapOf(2, 3, 4, 5, 123, 777),
        "e" -> RoaringBitmap.bitmapOf(1, 2, 5, 6, 7, 8, 777, 11111)
      ))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 123, 444, 777, 11111)
    }

    "calculate conjunction of alternatives (a OR b) AND (c OR d)" in {
      val expression = And(Or(Get("a"), Get("b")), Or(Get("c"), Get("d")))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "a" -> RoaringBitmap.bitmapOf(1, 3, 5, 7),
        "b" -> RoaringBitmap.bitmapOf(1, 2, 5, 17),
        "c" -> RoaringBitmap.bitmapOf(3, 8, 777),
        "d" -> RoaringBitmap.bitmapOf(1, 5, 123, 777)
      ))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(1, 3, 5)
    }

    "calculate alternatives of conjunctions (a AND b) OR (c AND d)" in {
      val expression = Or(And(Get("a"), Get("b")), And(Get("c"), Get("d")))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "a" -> RoaringBitmap.bitmapOf(1, 3, 5, 7),
        "b" -> RoaringBitmap.bitmapOf(1, 2, 5, 17),
        "c" -> RoaringBitmap.bitmapOf(3, 5, 8, 777),
        "d" -> RoaringBitmap.bitmapOf(1, 5, 123, 777)
      ))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(1, 5, 777)
    }

    "calculate some example (a AND (b OR c OR d)) AND e" in {
      val expression = And(And(Get("a"), Or(Get("b"), Get("c"), Get("d"))), Get("e"))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "a" -> RoaringBitmap.bitmapOf(1, 3, 6, 8, 11),
        "b" -> RoaringBitmap.bitmapOf(1, 2, 6, 11),
        "c" -> RoaringBitmap.bitmapOf(3, 6, 777),
        "d" -> RoaringBitmap.bitmapOf(3, 6, 999),
        "e" -> RoaringBitmap.bitmapOf(1, 3, 5, 7, 9)
      ))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(1, 3)
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
        "inv1" -> RoaringBitmap.bitmapOf(1, 101, 201, 301, 401),
        "inv2" -> RoaringBitmap.bitmapOf(2, 102, 202, 302, 402),
        "inv4" -> RoaringBitmap.bitmapOf(4, 104, 204, 304, 404),
        "inv7" -> RoaringBitmap.bitmapOf(7, 107, 207, 307, 407),
        "packg4" -> RoaringBitmap.bitmapOf(4, 104, 204, 304),
        "deal4" -> RoaringBitmap.bitmapOf(104, 204, 304, 404),
        "cat100" -> RoaringBitmap.bitmapOf(101, 102, 104, 107),
        "cat200" -> RoaringBitmap.bitmapOf(201, 202, 204, 207),
        "cat300" -> RoaringBitmap.bitmapOf(301, 302, 304, 307),
        "mobile" -> RoaringBitmap.bitmapOf(1, 2, 7, 401, 402, 407),
        "ios" -> RoaringBitmap.bitmapOf(2, 3, 7, 401),
        "pl" -> RoaringBitmap.bitmapOf(2, 3),
        "denmark" -> RoaringBitmap.bitmapOf(7),
        "zip1" -> RoaringBitmap.bitmapOf(3),
        "zip2" -> RoaringBitmap.bitmapOf(1)
      ))

      val result = new BitmapExecutor(db).execute(expression)
      val expected = RoaringBitmap.bitmapOf(2, 7)

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
        "inv|1" -> RoaringBitmap.bitmapOf(1, 101, 201, 301, 401),
        "inv|3" -> RoaringBitmap.bitmapOf(2, 102, 202, 302, 402),
        "device|2" -> RoaringBitmap.bitmapOf(2, 102, 201, 202, 302, 402),
        "country_id|276" -> RoaringBitmap.bitmapOf(2, 302),
        "country_id|208" -> RoaringBitmap.bitmapOf(102, 402),
        "country_id|826" -> RoaringBitmap.bitmapOf(301, 401),
        "FULL" -> RoaringBitmap.bitmapOf(1, 2, 101, 102, 201, 202, 301, 302, 401, 402)))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(2, 102, 201, 202, 302, 402, 1, 101)
    }

    "return empty result when Empty in Conjunction" in {

      val expression = And(Empty, Get("device|2"))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map())

      val result = new BitmapExecutor(db).execute(expression)

      val expected = new RoaringBitmap()
      result mustBe expected
    }

    "ignore Full in Conjunction" in {

      val expression = And(Full, Get("device|2"))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "device|2" -> RoaringBitmap.bitmapOf(1, 3, 7)))

      val result = new BitmapExecutor(db).execute(expression)

      val expected = RoaringBitmap.bitmapOf(1, 3, 7)
      result mustBe expected
    }

    "return Full when Full in Alternative" in {

      val expression = Or(Get("device|2"), Full)
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "device|2" -> RoaringBitmap.bitmapOf(1, 3, 7),
        "FULL" -> RoaringBitmap.bitmapOf(1, 2, 3, 4, 5, 6, 7)))
      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(1, 2, 3, 4, 5, 6, 7)
    }

    "ignore Empty in Alternative" in {

      val expression = Or(Empty, Get("device|2"))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "device|2" -> RoaringBitmap.bitmapOf(1, 3, 7)))

      val result = new BitmapExecutor(db).execute(expression)

      val expected = RoaringBitmap.bitmapOf(1, 3, 7)
      result mustBe expected
    }
  }
}
