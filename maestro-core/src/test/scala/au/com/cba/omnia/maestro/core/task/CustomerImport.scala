package au.com.cba.omnia.maestro.core.task

import scalikejdbc._

object CustomerImport {

  Class.forName("org.hsqldb.jdbcDriver")

  val data = List("1|Fred|001|D|M|259", "2|Betty|005|D|M|205", "3|Bart|002|F|M|225")

  def tableSetup(connectionString: String, username: String, password: String, table: String = "customer_import"): Unit = {
    val tableName = SQLSyntax.createUnsafely(table)
    ConnectionPool.singleton(connectionString, username, password)
    implicit val session = AutoSession

    sql"""
      create table ${tableName} (
        id integer,
        name varchar(20),
        accr varchar(20),
        cat varchar(20),
        sub_cat varchar(20),
        balance integer
      )
    """.execute.apply()

    data.map(line => line.split('|')).foreach(
      row => sql"""insert into ${tableName}(id, name, accr, cat, sub_cat, balance)
        values (${row(0)}, ${row(1)}, ${row(2)}, ${row(3)}, ${row(4)}, ${row(5)})""".update().apply())
  }
}
