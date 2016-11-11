package com.ppiotrow.bitmaps

import com.ppiotrow.bitmaps.Implicits.RoaringBitmapImpl
import com.ppiotrow.bitmaps.util.BitmapTest
import org.roaringbitmap.RoaringBitmap.bitmapOf
import org.scalatest.WordSpecLike

class NegationTest extends BitmapTest with WordSpecLike {

  "BitmapExecutor" must {
    "support simple negation ¬A" in {
      check(
        expr = Not(Get("A")),
        db = Map(
          "A" -> bitmapOf(1, 3),
          "FULL" -> bitmapOf(1, 2, 3, 4)),
        expected = bitmapOf(2, 4))
    }

    "support double negation ¬(¬A) ignoring FULL" in {
      check(
        expr = Not(Not(Get("A"))),
        db = Map(
          "A" -> bitmapOf(1, 3)),
        expected = bitmapOf(1, 3))
    }

    "support alternative withing negation" in {
      check(
        expr = Not(
          Or(
            Get("A"),
            Get("B")
          )),
        db = Map(
          "A" -> bitmapOf(1, 3),
          "B" -> bitmapOf(1, 5),
          "FULL" -> bitmapOf(1, 2, 3, 4, 5)),
        expected = bitmapOf(2, 4))
    }

    "support conjunction withing negation" in {
      check(
        expr = Not(
          And(
            Get("A"),
            Get("B")
          )),
        db = Map(
          "A" -> bitmapOf(1, 3),
          "B" -> bitmapOf(1, 5),
          "FULL" -> bitmapOf(1, 2, 3, 4, 5)),
        expected = bitmapOf(2, 3, 4, 5))
    }

    "support negation of Empty bitmap" in {
      check(
        expr = Not(Empty),
        db = Map("FULL" -> bitmapOf(1, 2, 3)),
        expected = bitmapOf(1, 2, 3))
    }

    "support negation of Full bitmap" in {
      check(
        expr = Not(Full),
        db = Map(),
        expected = bitmapOf())
    }
  }
}
