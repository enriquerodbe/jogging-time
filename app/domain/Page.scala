package domain

case class Page[T](results: Seq[T], total: Int, count: Int, offset: Int)
