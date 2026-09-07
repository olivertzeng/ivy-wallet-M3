"""Validate the 131 -> 132 SQL and preserved rows in an in-memory SQLite database.

Host-side check only; this does not replace Room's on-device migration validation.
"""
import json
import re
import sqlite3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
schema_dir = root / "shared/data/core/schemas/com.ivy.data.db.IvyRoomDatabase"
before = json.loads((schema_dir / "131.json").read_text())["database"]
after = json.loads((schema_dir / "132.json").read_text())["database"]
migration = (root / "shared/data/core/src/main/java/com/ivy/data/db/migration/Migration131to132_DualCurrencyCards.kt").read_text()
db = sqlite3.connect(":memory:")
for entity in before["entities"]:
    db.execute(entity["createSql"].replace("${TABLE_NAME}", entity["tableName"]))
db.execute("""INSERT INTO accounts(name,currency,color,orderNum,includeInBalance,creditLimit,isSynced,isDeleted,id)
              VALUES ('Visa','BDT',1,0,0,100000,0,0,'card')""")
db.execute("""INSERT INTO transactions(accountId,type,amount,isSynced,isDeleted,id)
              VALUES ('card','EXPENSE',20000,0,0,'expense')""")
for sql in re.findall(r'db.execSQL\("([^"]+)"\)', migration):
    db.execute(sql)
assert db.execute("SELECT name,creditLimit,creditCardGroupId,creditLimitShared,creditExchangeRate FROM accounts").fetchone() == (
    "Visa", 100000, None, 0, None
)
assert db.execute("SELECT accountId,amount FROM transactions").fetchone() == ("card", 20000)
columns = {row[1]: row for row in db.execute("PRAGMA table_info(accounts)")}
expected = next(entity for entity in after["entities"] if entity["tableName"] == "accounts")
assert set(columns) == {field["columnName"] for field in expected["fields"]}
for field in expected["fields"]:
    actual = columns[field["columnName"]]
    assert actual[2] == field["affinity"]
    assert bool(actual[3]) == field.get("notNull", False)
assert columns["creditLimitShared"][4] == "0"
print("PASS: schema 131 -> 132 matches generated account columns and preserves card and transaction rows")
