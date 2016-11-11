package com.ppiotrow.bitmaps

import com.ppiotrow.bitmaps.Implicits.RoaringBitmapImpl
import com.ppiotrow.bitmaps.util.BitmapTest
import org.roaringbitmap.RoaringBitmap.bitmapOf
import org.scalatest.WordSpecLike

class AlternativeTest extends BitmapTest with WordSpecLike {

  "BitmapExecutor" must {
    "support simple alternative A or B or C" in {
      check(
        expr =
          Or(
            Get("A"),
            Get("B"),
            Get("C")
          ),
        db = Map(
          "A" -> bitmapOf(1, 3),
          "B" -> bitmapOf(1, 5),
          "C" -> bitmapOf(7, 8)),
        expected = bitmapOf(1, 3, 5, 7, 8))
    }

    "calculate deep associative alternative ((((a OR b) OR c) OR d) OR e)" in {
      check(
        expr =
          Or(
            Or(
              Or(
                Or(
                  Get("a"), Get("b")),
                Get("c")),
              Get("d")),
            Get("e")),
        db = Map(
          "a" -> bitmapOf(1, 3, 5, 7, 9, 123, 777),
          "b" -> bitmapOf(1, 2, 5, 444, 123, 777, 11111),
          "c" -> bitmapOf(1, 5, 123, 8, 777),
          "d" -> bitmapOf(2, 3, 4, 5, 123, 777),
          "e" -> bitmapOf(1, 2, 5, 6, 7, 8, 777, 11111)),
        expected = bitmapOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 123, 444, 777, 11111))
    }

    "calculate alternatives of conjunctions (a AND b) OR (c AND d)" in {
      check(
        expr =
          Or(
            And(Get("a"), Get("b")),
            And(Get("c"), Get("d"))),
        db = Map(
          "a" -> bitmapOf(1, 3, 5, 7),
          "b" -> bitmapOf(1, 2, 5, 17),
          "c" -> bitmapOf(3, 5, 8, 777),
          "d" -> bitmapOf(1, 5, 123, 777)),
        expected = bitmapOf(1, 5, 777))
    }

    "return Full when Full in Alternative" in {
      check(
        expr = Or(Get("A"), Full),
        db = Map(
          "A" -> bitmapOf(1, 3, 7),
          "FULL" -> bitmapOf(1, 2, 3, 4, 5, 6, 7)),
        expected = bitmapOf(1, 2, 3, 4, 5, 6, 7))
    }

    "ignore Empty in Alternative" in {
      check(
        expr = Or(Empty, Get("A")),
        db = Map(
          "A" -> bitmapOf(1, 3, 7)),
        expected = bitmapOf(1, 3, 7))
    }
  }
}
