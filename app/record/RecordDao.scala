package record

import domain._
import filter.FilterOptions
import filter.database.ConditionBuilder
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider

@Singleton
class RecordDao @Inject() (
    val dbConfigProvider: DatabaseConfigProvider,
    recordConditionBuilder: ConditionBuilder[RecordField],
) extends RecordsTable {

  import profile.api._

  def create(record: Record): DBIO[Record] =
    recordsInsert += record

  def retrieve(userId: Option[Long], filter: FilterOptions[RecordField]): DBIO[Seq[Record]] =
    retrieveQuery(userId, filter)
      .drop(filter.offset)
      .take(filter.limit)
      .result

  private def retrieveQuery(userId: Option[Long], filter: FilterOptions[RecordField]) =
    records
      .filterOpt(userId)(_.userId === _)
      .filter(recordConditionBuilder(filter.condition))

  def count(userId: Option[Long], filter: FilterOptions[RecordField]): DBIO[Int] =
    retrieveQuery(userId, filter).length.result

  def update(record: Record): DBIO[Int] =
    records
      .filter(r => r.id === record.id && r.userId === record.userId)
      .map(r => (r.date, r.distance, r.duration, r.location))
      .update((record.date, record.distance, record.duration, record.location))

  def delete(id: Long): DBIO[Int] =
    records.filter(_.id === id).delete

  def delete(userId: Long, recordId: Long): DBIO[Int] =
    records.filter(r => r.id === recordId && r.userId === userId).delete

}
