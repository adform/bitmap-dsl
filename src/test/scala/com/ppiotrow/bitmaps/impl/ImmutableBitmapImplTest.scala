package com.ppiotrow.bitmaps.impl

import com.ppiotrow.bitmaps.Implicits.ImmutableRoaringBitmapImpl
import com.ppiotrow.bitmaps.util.TestDB
import com.ppiotrow.bitmaps.{BitmapDB, BitmapExecutor, Get}
import org.roaringbitmap.buffer.ImmutableRoaringBitmap
import org.roaringbitmap.buffer.ImmutableRoaringBitmap.bitmapOf
import org.scalatest.{MustMatchers, WordSpecLike}

class ImmutableBitmapImplTest extends MustMatchers with WordSpecLike {
  "ImmutableBitmapImpl" must {
    "calculate batchAND" in {
      ImmutableRoaringBitmapImpl.batchAnd(and) mustBe bitmapOf(8, 9)
    }
    "calculate batchOR" in {
      ImmutableRoaringBitmapImpl.batchOr(or) mustBe bitmapOf(1, 2, 3, 4, 5, 7, 8, 9, 10)
    }
    "not change input bitmaps in negation" in {
      val b1 = bitmapOf(1, 3, 4, 6, 8)
      val b2 = bitmapOf(1, 5, 8, 10)

      val result = ImmutableRoaringBitmapImpl.andNot(b1, b2)

      result mustBe bitmapOf(3, 4, 6)
      b1 mustBe bitmapOf(1, 3, 4, 6, 8)
      b2 mustBe bitmapOf(1, 5, 8, 10)
    }
  }
  "BitmapExecutor" must {
    "compile with evidence for ImmutableRoaringBitmap" in {
      val testDB: BitmapDB[ImmutableRoaringBitmap] = TestDB(Map(
        "A" -> bitmapOf(1, 3, 4, 6, 8)
      ))

      new BitmapExecutor(testDB).execute(Get("A")) mustBe bitmapOf(1, 3, 4, 6, 8)
    }
  }
  val and = ImplTestData.and.map(bits => bitmapOf(bits: _*))

  val or = ImplTestData.or.map(bits => bitmapOf(bits: _*))
}
