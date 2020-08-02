package util.filter

import slick.lifted.Rep

trait FilterTable {

  def getColumn[T](field: Field[T]): Rep[T]
}
