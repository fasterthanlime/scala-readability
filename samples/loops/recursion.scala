
object WhileSample {
  def main(args: Array[String]) = {

    // sample start
    def count(n: Int = 4): Unit = n match {
      case -1 => {}
      case _ => count(n - 1); println(n)
    }
    count()
    // sample start

  }
}

// vim: set ts=2 sw=2 et:
