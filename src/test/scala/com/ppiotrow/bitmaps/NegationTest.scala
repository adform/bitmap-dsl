package com.ppiotrow.bitmaps

import com.ppiotrow.bitmaps.Implicits.RoaringBitmapImpl
import com.ppiotrow.bitmaps.util.TestDB
import org.roaringbitmap.RoaringBitmap
import org.scalatest.{MustMatchers, WordSpecLike}

class NegationTest extends MustMatchers with WordSpecLike {

  "BitmapExecutor" must {
    "support simple negation ¬A" in {
      val expression = Not(Get("A"))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "A" -> RoaringBitmap.bitmapOf(1, 3),
        "FULL" -> RoaringBitmap.bitmapOf(1, 2, 3, 4)
      ))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(2, 4)
    }

    "support double negation ¬(¬A) ignoring FULL" in {
      val expression = Not(Not(Get("A")))
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "A" -> RoaringBitmap.bitmapOf(1, 3)))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(1, 3)
    }

    "support alternative withing negation" in {
      val expression =
        Not(
          Or(
            Get("A"),
            Get("B")
          )
        )
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "A" -> RoaringBitmap.bitmapOf(1, 3),
        "B" -> RoaringBitmap.bitmapOf(1, 5),
        "FULL" -> RoaringBitmap.bitmapOf(1, 2, 3, 4, 5)))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(2, 4)
    }

    "support conjunction withing negation" in {
      val expression =
        Not(
          And(
            Get("A"),
            Get("B")
          )
        )
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "A" -> RoaringBitmap.bitmapOf(1, 3),
        "B" -> RoaringBitmap.bitmapOf(1, 5),
        "FULL" -> RoaringBitmap.bitmapOf(1, 2, 3, 4, 5)))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(2, 3, 4, 5)
    }

    "support negation of Empty bitmap" in {
      val expression = Not(Empty)
      val db: BitmapDB[RoaringBitmap] = TestDB(Map(
        "FULL" -> RoaringBitmap.bitmapOf(1, 2, 3)))

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf(1, 2, 3)
    }

    "support negation of Full bitmap" in {
      val expression = Not(Full)
      val db: BitmapDB[RoaringBitmap] = TestDB(Map())

      val result = new BitmapExecutor(db).execute(expression)

      result mustBe RoaringBitmap.bitmapOf()
    }
  }
}
