
def count(n: Int = 4): Unit = n match {
  case -1 => {}
  case _ => count(n - 1); println(n)
}
count()

// vim: set ts=4 sw=4 et:
