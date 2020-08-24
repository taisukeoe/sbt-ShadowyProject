import scala.concurrent.Future

trait A1 {
  val a = org.apache.commons.lang3.ArrayUtils.EMPTY_BOOLEAN_ARRAY
}

object A1Main {
  def main(args: Array[String]): Unit = println(args.head)
}
