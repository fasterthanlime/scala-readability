
object ForeachSample {
  def main(args: Array[String]) = {

    // sample start
    println("Hoy there!")

    (0 until 5).foreach { i =>
      println(i)
    }

    println("Hey there!")
    // sample end

  }
}

// vim: set ts=2 sw=2 et:
