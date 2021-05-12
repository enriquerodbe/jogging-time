package record

import domain.{Distance, Speed}
import filter.parser.Parser.Instances.{doubleParser, intParser}

object Parser {

  object Instances {

    implicit val distanceParser: filter.parser.Parser[Distance] =
      s => Distance(intParser.parse(s))

    implicit val speedParser: filter.parser.Parser[Speed] =
      s => Speed(doubleParser.parse(s))

  }

}
