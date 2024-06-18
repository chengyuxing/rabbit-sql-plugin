/*[me]*/
select ${  fields  }, '${fields}', '${  db}'
from ${db}.pg_aggregate
where
   -- #choose
   -- #when :num <> blank
    id = :num
   -- #break
   -- #when :id = blank
   or id = 14
   -- #break
   -- #default
   or id = 10
-- #break
-- #end
;