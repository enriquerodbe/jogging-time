package fixture

import fixture.Fixture.initialRecords
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import record.RecordsTable

class RecordsFixture @Inject()(
    val dbConfigProvider: DatabaseConfigProvider) extends RecordsTable {

  def insertRecords(): Unit = {
    val _ = db.run(recordsInsert ++= initialRecords)
  }
}
