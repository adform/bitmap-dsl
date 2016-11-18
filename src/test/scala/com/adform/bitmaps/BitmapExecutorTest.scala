package com.adform.bitmaps

import com.adform.bitmaps.Implicits.RoaringBitmapImpl
import com.adform.bitmaps.util.BitmapTest
import org.roaringbitmap.RoaringBitmap.bitmapOf
import org.scalatest.WordSpecLike


class BitmapExecutorTest extends BitmapTest with WordSpecLike {

  "BitmapExecutor" must {

    "calculate some example (a AND (b OR c OR d)) AND e" in {
      check(
        expr =
          And(
            And(
              Get("a"),
              Or(
                Get("b"),
                Get("c"),
                Get("d"))),
            Get("e")),
        db = Map(
          "a" -> bitmapOf(1, 3, 6, 8, 11),
          "b" -> bitmapOf(1, 2, 6, 11),
          "c" -> bitmapOf(3, 6, 777),
          "d" -> bitmapOf(3, 6, 999),
          "e" -> bitmapOf(1, 3, 5, 7, 9)),
        expected = bitmapOf(1, 3))
    }

    "calculate complicated expression example" in {
      check(
        expr = And(
          Or(
            Get("a"),
            Get("b"),
            And(
              Get("c"),
              Get("p4"),
              Get("d4")),
            Get("d")),
          Not(
            Or(Get("c1"), Get("c2"), Get("c3"))),
          And(
            And(
              Get("m"),
              Get("i")),
            And(
              Or(Get("p"), Get("dk")),
              Not(
                Or(Get("z1"), Get("z2")))))
        ),
        db = Map(
          "a" -> bitmapOf(1, 101, 201, 301, 401),
          "b" -> bitmapOf(2, 102, 202, 302, 402),
          "c" -> bitmapOf(4, 104, 204, 304, 404),
          "d" -> bitmapOf(7, 107, 207, 307, 407),
          "p4" -> bitmapOf(4, 104, 204, 304),
          "d4" -> bitmapOf(104, 204, 304, 404),
          "c1" -> bitmapOf(101, 102, 104, 107),
          "c2" -> bitmapOf(201, 202, 204, 207),
          "c3" -> bitmapOf(301, 302, 304, 307),
          "m" -> bitmapOf(1, 2, 7, 401, 402, 407),
          "i" -> bitmapOf(2, 3, 7, 401),
          "p" -> bitmapOf(2, 3),
          "dk" -> bitmapOf(7),
          "z1" -> bitmapOf(3),
          "z2" -> bitmapOf(1)
        ),
        expected = bitmapOf(2, 7))
    }

    "not ignore the audience with only negation" in {
      check(
        expr = And(
          Or(
            Get("a"),
            Get("c")),
          Or(
            Get("d2"),
            And(
              Not(
                Or(
                  Get("c276"),
                  Get("c208"),
                  Get("c826")
                )))
          )),
        db = Map(
          "a" -> bitmapOf(1, 101, 201, 301, 401),
          "c" -> bitmapOf(2, 102, 202, 302, 402),
          "d2" -> bitmapOf(2, 102, 201, 202, 302, 402),
          "c276" -> bitmapOf(2, 302),
          "c208" -> bitmapOf(102, 402),
          "c826" -> bitmapOf(301, 401),
          "FULL" -> bitmapOf(1, 2, 101, 102, 201, 202, 301, 302, 401, 402)),
        expected = bitmapOf(2, 102, 201, 202, 302, 402, 1, 101))
    }
  }
}
