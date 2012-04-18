
object ForeachSample {
  def main(args: Array[String]) = {
    println("Hoy there!")

    displayFiveNumbers

    println("Hey there!")


  }

  def displayFiveNumbers() {
    (0 until 5).foreach { i =>
      println(i)
    }
  }
}

// vim: set ts=2 sw=2 et:
