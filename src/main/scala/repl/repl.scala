package repl

object repl extends App {
  val liczby = List(1, 2, 3, 4)
  val parzyste = liczby.filter(_ % 2 == 0)

  parzyste.foreach(println)
}
