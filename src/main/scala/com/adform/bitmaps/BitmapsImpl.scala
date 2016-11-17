package com.adform.bitmaps

import org.roaringbitmap.buffer.{BufferFastAggregation, ImmutableRoaringBitmap, MutableRoaringBitmap}
import org.roaringbitmap.{FastAggregation, RoaringBitmap}

import scala.annotation.tailrec
import scala.collection.JavaConverters.asJavaIteratorConverter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BitmapsImpl[T] {
  def batchAnd(bitmaps: List[T]): T
  def batchOr(bitmaps: List[T]): T
  def andNot(left: T, right: T): T
  def empty: T
}

object Implicits {

  implicit object RoaringBitmapImpl extends BitmapsImpl[RoaringBitmap] {
    override def batchAnd(bitmaps: List[RoaringBitmap]) =
      FastAggregation.and(bitmaps.iterator.asJava)
    override def batchOr(bitmaps: List[RoaringBitmap]) =
      FastAggregation.or(bitmaps.iterator.asJava)
    override def empty = new RoaringBitmap()
    override def andNot(left: RoaringBitmap, right: RoaringBitmap) = {
      val copy = left.clone()
      copy.andNot(right)
      copy
    }
  }

  implicit object ConcurrentRoaringBitmapImpl extends BitmapsImpl[Future[RoaringBitmap]] {
    val GROUP_SIZE = 10
    override def batchAnd(bitmaps: List[RoaringBitmapF]) =
      groupedReduce(bitmaps, RoaringBitmapImpl.batchAnd)
    override def batchOr(bitmaps: List[RoaringBitmapF]) =
      groupedReduce(bitmaps, RoaringBitmapImpl.batchOr)
    override def andNot(left: RoaringBitmapF, right: RoaringBitmapF) = for {
      l <- left
      r <- right
    } yield RoaringBitmapImpl.andNot(l, r)
    override def empty = Future.successful(RoaringBitmapImpl.empty)

    @tailrec
    private def groupedReduce(l: List[RoaringBitmapF], reducer: Reducer): RoaringBitmapF = l match {
      case Nil => empty
      case h :: Nil => h
      case _ => groupedReduce(l.grouped(GROUP_SIZE)
        .map(group => Future.sequence(group).map(reducer))
        .toList, reducer)
    }

    type Reducer = List[RoaringBitmap] => RoaringBitmap
    type RoaringBitmapF = Future[RoaringBitmap]
  }

  implicit object ImmutableRoaringBitmapImpl extends BitmapsImpl[ImmutableRoaringBitmap] {
    override def batchAnd(bitmaps: List[ImmutableRoaringBitmap]) =
      BufferFastAggregation.and(bitmaps.iterator.asJava)
    override def batchOr(bitmaps: List[ImmutableRoaringBitmap]) =
      BufferFastAggregation.or(bitmaps.iterator.asJava)
    override def empty = new MutableRoaringBitmap().toImmutableRoaringBitmap
    override def andNot(left: ImmutableRoaringBitmap, right: ImmutableRoaringBitmap) = {
      val copy = left.toMutableRoaringBitmap
      copy.andNot(right)
      copy
    }
  }
  implicit object MutableRoaringBitmapImpl extends BitmapsImpl[MutableRoaringBitmap] {
    override def batchAnd(bitmaps: List[MutableRoaringBitmap]) =
      BufferFastAggregation.and(bitmaps.iterator.asJava)
    override def batchOr(bitmaps: List[MutableRoaringBitmap]) =
      BufferFastAggregation.or(bitmaps.iterator.asJava)
    override def empty = new MutableRoaringBitmap()
    override def andNot(left: MutableRoaringBitmap, right: MutableRoaringBitmap) = {
      val copy = left.clone()
      copy.andNot(right)
      copy
    }
  }
}
