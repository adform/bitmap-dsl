import com.adform.bitmaps._
import org.roaringbitmap.RoaringBitmap

//example storage implementation
case class ExampleDB(map: Map[String, RoaringBitmap]) extends BitmapDB[RoaringBitmap] {
  override def getBitmap(key: String) = map.getOrElse(key, new RoaringBitmap())
  override def full = getBitmap("FULL")
}

object Example extends App {
  val students = ExampleDB(Map(
    "study|MIT" -> RoaringBitmap.bitmapOf(1, 2, 7, 9),
    "born|USA"  -> RoaringBitmap.bitmapOf(1, 2),
    "born|PL"   -> RoaringBitmap.bitmapOf(3, 9)))
  val expr =
    And(
      Get("study|MIT"),
      Or(
        Get("born|USA"),
        Get("born|PL")
      ))

  import com.adform.bitmaps.Implicits.RoaringBitmapImpl
  val result = new BitmapExecutor(students).execute(expr)
  print(s"MIT Students born in USA or PL: $result") // {1,2,9}
}
