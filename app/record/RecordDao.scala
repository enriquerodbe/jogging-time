package record

import domain.Record
import filter.{FilterDao, FilterOptions}
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider

private[record] class RecordDao @Inject()(
    val dbConfigProvider: DatabaseConfigProvider)
  extends RecordsTable with FilterDao {

  import profile.api._

  def create(record: Record): DBIO[Record] = recordsInsert += record

  def retrieve(userId: Long, filter: FilterOptions): DBIO[Seq[Record]] = {
    records
      .filter(_.userId === userId)
      .filter(buildCondition(filter.condition, _))
      .drop(filter.offset)
      .take(filter.limit)
      .result
  }

  def count(userId: Long, filter: FilterOptions): DBIO[Int] = {
    records
      .filter(_.userId === userId)
      .filter(buildCondition(filter.condition, _))
      .length
      .result
  }

  def retrieve(filter: FilterOptions): DBIO[Seq[Record]] = {
    records
      .filter(buildCondition(filter.condition, _))
      .drop(filter.offset)
      .take(filter.limit)
      .result
  }

  def count(filter: FilterOptions): DBIO[Int] = {
    records
      .filter(buildCondition(filter.condition, _))
      .length
      .result
  }

  def update(record: Record): DBIO[Int] = {
    records
      .filter(r => r.id === record.id && r.userId === record.userId)
      .map(r => (r.date, r.distance, r.duration, r.location))
      .update((record.date, record.distance, record.duration, record.location))
  }

  def delete(id: Long): DBIO[Int] = records.filter(_.id === id).delete

  def delete(userId: Long, recordId: Long): DBIO[Int] = {
    records.filter(r => r.id === recordId && r.userId === userId).delete
  }
}
