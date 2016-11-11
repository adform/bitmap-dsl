package com.ppiotrow.bitmaps

import com.ppiotrow.bitmaps.Implicits.RoaringBitmapImpl
import com.ppiotrow.bitmaps.util.BitmapTest
import org.roaringbitmap.RoaringBitmap.bitmapOf
import org.scalatest.WordSpecLike

class ConjunctionTest extends BitmapTest with WordSpecLike {
  "BitmapExecutor" must {
    "calculate conjunction A and B" in {
      check(
        expr = And(Get("A"), Get("B")),
        db = Map(
          "A" -> bitmapOf(1, 3, 5, 111),
          "B" -> bitmapOf(1, 2, 4, 5)),
        expected = bitmapOf(1, 5))
    }

    "calculate associative conjunction ((A and B) and C)" in {
      check(
        expr =
          And(
            And(
              Get("A"), Get("B")),
            Get("C")),
        db = Map(
          "A" -> bitmapOf(1, 3, 5, 111),
          "B" -> bitmapOf(2, 4, 5),
          "C" -> bitmapOf(5, 111, 777)),
        expected = bitmapOf(5))
    }

    "calculate deep associative conjunction ((((A and B) and C) and D) and E)" in {
      check(
        expr =
          And(
            And(
              And(
                And(
                  Get("A"), Get("B")),
                Get("C")),
              Get("D")),
            Get("E")),
        db = Map(
          "A" -> bitmapOf(1, 3, 5, 7, 9, 123, 777),
          "B" -> bitmapOf(1, 2, 5, 444, 123, 777, 11111),
          "C" -> bitmapOf(1, 5, 123, 8, 777),
          "D" -> bitmapOf(2, 3, 4, 5, 123, 777),
          "E" -> bitmapOf(1, 2, 3, 4, 5, 6, 7, 8, 777, 11111)),
        expected = bitmapOf(5, 777))
    }

    "calculate conjunction of alternatives (A or B) and (C or D)" in {
      check(
        expr =
          And(
            Or(
              Get("A"),
              Get("B")),
            Or(
              Get("C"),
              Get("D"))),
        db = Map(
          "A" -> bitmapOf(1, 3, 5, 7),
          "B" -> bitmapOf(1, 2, 5, 17),
          "C" -> bitmapOf(3, 8, 777),
          "D" -> bitmapOf(1, 5, 123, 777)),
        expected = bitmapOf(1, 3, 5))
    }

    "ignore Full in Conjunction" in {
      check(
        expr = And(Full, Get("A")),
        db = Map(
          "A" -> bitmapOf(1, 3, 7)),
        expected = bitmapOf(1, 3, 7))
    }

    "return empty result when Empty in Conjunction" in {
      check(
        expr = And(Empty, Get("A")),
        db = Map(),
        expected = bitmapOf())
    }
  }

}
